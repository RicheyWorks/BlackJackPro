package com.richeyworks.blackjack.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /** Load from classpath (Service Loader) plus an optional external directory. */
    public void loadAll(Path externalPluginsDir) {
        loadFromServiceLoader();
        if (externalPluginsDir != null && Files.isDirectory(externalPluginsDir)) {
            loadFromDirectory(externalPluginsDir);
        }
        for (BlackJackPlugin p : plugins) p.onLoad();
    }

    private void loadFromServiceLoader() {
        for (BlackJackPlugin p : ServiceLoader.load(BlackJackPlugin.class)) {
            plugins.add(p);
        }
    }

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
                String actual = sha256(jar);
                if (actual == null || !actual.equalsIgnoreCase(expected)) {
                    System.err.println("Plugin skipped (checksum mismatch): " + name);
                    continue;
                }
                urls.add(jar.toUri().toURL());
            }
            if (urls.isEmpty()) return;
            URLClassLoader cl = new URLClassLoader(
                    urls.toArray(new URL[0]),
                    BlackJackPlugin.class.getClassLoader());
            for (BlackJackPlugin p : ServiceLoader.load(BlackJackPlugin.class, cl)) {
                // A child ServiceLoader also re-discovers providers on the parent
                // classpath; keep only those actually loaded from the external JARs
                // so built-in plugins aren't registered (and shown) twice.
                if (p.getClass().getClassLoader() == cl) plugins.add(p);
            }
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

    public void shutdown() {
        for (BlackJackPlugin p : plugins) {
            try { p.onUnload(); } catch (RuntimeException ignored) { }
        }
        plugins.clear();
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
