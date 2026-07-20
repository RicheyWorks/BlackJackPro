package com.richeyworks.blackjack.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Discovers plugins from two sources:
 * <ol>
 *   <li>The application classpath (via {@link ServiceLoader}).</li>
 *   <li>Any JAR file in the user's {@code plugins/} directory.</li>
 * </ol>
 *
 * Loaded plugins are accessible via {@link #all()}; convenience methods
 * surface their themes, AI policies, and side bets in flat lists.
 */
public final class PluginRegistry {

    private final List<BlackJackPlugin> plugins = new ArrayList<>();
    /** Names of plugins that failed to load, for display in the plugin manager. */
    private final List<String> failures = new ArrayList<>();
    /** Loader for external JARs; held so its file handles can be released on exit. */
    private URLClassLoader externalLoader;
    /** Verified copies of the external JARs, deleted on shutdown. */
    private final List<Path> staging = new ArrayList<>();

    /**
     * Load from classpath (Service Loader) plus an optional external directory.
     *
     * <p>A plugin is third-party code: a broken one must not take the game down
     * with it. Every provider is instantiated and initialised in isolation, and
     * any {@link Throwable} it raises is recorded in {@link #failures()} and the
     * plugin skipped. This includes {@code ServiceConfigurationError}, which is
     * an {@link Error} rather than an exception and is what a JAR with a stale
     * {@code META-INF/services} entry throws — previously that propagated out of
     * here, out of the launcher, and the game never drew a window.
     */
    public void loadAll(Path externalPluginsDir) {
        loadFromServiceLoader();
        if (externalPluginsDir != null && Files.isDirectory(externalPluginsDir)) {
            loadFromDirectory(externalPluginsDir);
        }
        for (BlackJackPlugin p : new ArrayList<>(plugins)) {
            try {
                p.onLoad();
            } catch (Throwable t) {
                plugins.remove(p);
                fail(describe(p), t);
            }
        }
    }

    private void loadFromServiceLoader() {
        collect(ServiceLoader.load(BlackJackPlugin.class), null);
    }

    /**
     * Drain a {@link ServiceLoader} one provider at a time so a single bad entry
     * doesn't abort the rest. {@code Iterator.next()} is what throws
     * {@code ServiceConfigurationError}, so each call needs its own guard;
     * {@code hasNext()} can throw too, and if it does there is nothing left to
     * read and we stop.
     *
     * @param onlyFrom if non-null, keep only providers loaded by this loader —
     *                 a child {@code ServiceLoader} also re-discovers everything
     *                 on the parent classpath, and registering built-ins twice
     *                 would show them twice in the plugin manager.
     */
    private void collect(ServiceLoader<BlackJackPlugin> loader, ClassLoader onlyFrom) {
        Iterator<BlackJackPlugin> it = loader.iterator();
        while (true) {
            BlackJackPlugin p;
            try {
                if (!it.hasNext()) return;
                p = it.next();
            } catch (ServiceConfigurationError e) {
                fail("a plugin provider", e);
                continue;               // skip the bad entry, keep reading
            } catch (Throwable t) {
                fail("plugin discovery", t);
                return;
            }
            if (onlyFrom == null || p.getClass().getClassLoader() == onlyFrom) plugins.add(p);
        }
    }

    private void fail(String what, Throwable t) {
        String msg = what + ": " + t.getClass().getSimpleName()
                + (t.getMessage() == null ? "" : " — " + t.getMessage());
        failures.add(msg);
        System.err.println("Plugin skipped (" + msg + ")");
    }

    private static String describe(BlackJackPlugin p) {
        try { return p.manifest().name(); }
        catch (Throwable ignored) { return p.getClass().getName(); }
    }

    /** Human-readable reasons any plugin was skipped this session. Never null. */
    public List<String> failures() { return Collections.unmodifiableList(failures); }

    /**
     * Load plugins from an external directory, but only JARs whose SHA-256
     * appears in the {@code trusted.sha256} allow-list in that same directory.
     *
     * <p>This is secure-by-default: with no allow-list file (or no matching
     * entry) nothing external is loaded, so a JAR merely dropped into the
     * folder cannot execute — the user must explicitly record its checksum
     * first. Combined with anchoring the directory under a per-user data path
     * (rather than the process working directory), this removes the silent
     * arbitrary-code-execution path the loader previously had.
     *
     * <p>Allow-list format: one {@code <jar-file-name>=<hex-sha256>} per line;
     * blank lines and lines starting with {@code #} are ignored.
     *
     * <p>Each approved JAR is copied to a private temporary file and both the
     * hash check and the class loading are performed against that copy. Hashing
     * the original and then handing its path to a class loader that reopens it
     * leaves a window in which the file can be swapped between the two reads —
     * verifying and loading the same bytes closes it.
     */
    private void loadFromDirectory(Path dir) {
        Map<String, String> allow = readAllowList(dir.resolve("trusted.sha256"));
        if (allow.isEmpty()) return;   // no trusted plugins declared
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
            List<URL> urls = new ArrayList<>();
            for (Path jar : stream) {
                String name     = jar.getFileName().toString();
                String expected = allow.get(name);
                if (expected == null) {
                    System.err.println("Plugin skipped (not in allow-list): " + name);
                    continue;
                }
                Path staged = stage(jar);
                if (staged == null) {
                    fail(name, new IOException("could not stage plugin for verification"));
                    continue;
                }
                String actual = sha256(staged);
                if (actual == null || !actual.equalsIgnoreCase(expected)) {
                    System.err.println("Plugin skipped (checksum mismatch): " + name);
                    deleteQuietly(staged);
                    continue;
                }
                staging.add(staged);
                urls.add(staged.toUri().toURL());
            }
            if (urls.isEmpty()) return;
            externalLoader = new URLClassLoader(
                    urls.toArray(new URL[0]),
                    BlackJackPlugin.class.getClassLoader());
            collect(ServiceLoader.load(BlackJackPlugin.class, externalLoader), externalLoader);
        } catch (IOException e) {
            System.err.println("Plugin discovery failed: " + e.getMessage());
        }
    }

    /** Read the {@code name=hex-sha256} allow-list; empty map if absent/unreadable. */
    private Map<String, String> readAllowList(Path file) {
        Map<String, String> map = new HashMap<>();
        if (!Files.isRegularFile(file)) return map;
        try {
            for (String line : Files.readAllLines(file)) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#")) continue;
                int eq = s.indexOf('=');
                if (eq <= 0) continue;
                map.put(s.substring(0, eq).trim(), s.substring(eq + 1).trim());
            }
        } catch (IOException e) {
            System.err.println("Could not read plugin allow-list: " + e.getMessage());
        }
        return map;
    }

    /**
     * Copy a candidate JAR to a private temp file so it can be hashed and loaded
     * as one immutable set of bytes. Returns {@code null} if the copy fails.
     */
    private Path stage(Path jar) {
        Path tmp = null;
        try {
            tmp = Files.createTempFile("bjp-plugin-", ".jar");
            Files.copy(jar, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            return tmp;
        } catch (IOException e) {
            if (tmp != null) deleteQuietly(tmp);
            return null;
        }
    }

    private static void deleteQuietly(Path p) {
        try { Files.deleteIfExists(p); } catch (IOException ignored) { }
    }

    /** Lowercase hex SHA-256 of a file, or {@code null} on any error. */
    private String sha256(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            StringBuilder sb = new StringBuilder(64);
            for (byte b : md.digest()) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Unload every plugin and release the external JARs.
     *
     * <p>Closing the loader matters on Windows, where an open {@code URLClassLoader}
     * keeps a lock on each JAR — leaving it open means the user cannot update or
     * delete a plugin while the game is running.
     */
    public void shutdown() {
        for (BlackJackPlugin p : plugins) {
            try { p.onUnload(); } catch (Throwable ignored) { }
        }
        plugins.clear();
        if (externalLoader != null) {
            try { externalLoader.close(); } catch (IOException ignored) { }
            externalLoader = null;
        }
        // Only removable once the loader has released its handles.
        for (Path p : staging) deleteQuietly(p);
        staging.clear();
    }

    public List<BlackJackPlugin> all() { return Collections.unmodifiableList(plugins); }

    public List<TableTheme> themes() {
        List<TableTheme> out = new ArrayList<>();
        for (BlackJackPlugin p : plugins) out.addAll(p.themes());
        return out;
    }

    public List<AiPlugin> aiStrategies() {
        List<AiPlugin> out = new ArrayList<>();
        for (BlackJackPlugin p : plugins) out.addAll(p.aiStrategies());
        return out;
    }

    public List<SideBet> sideBets() {
        List<SideBet> out = new ArrayList<>();
        for (BlackJackPlugin p : plugins) out.addAll(p.sideBets());
        return out;
    }
}
