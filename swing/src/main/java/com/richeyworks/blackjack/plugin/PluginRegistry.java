package com.richeyworks.blackjack.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private void loadFromDirectory(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
            List<URL> urls = new ArrayList<>();
            for (Path jar : stream) urls.add(jar.toUri().toURL());
            if (urls.isEmpty()) return;
            URLClassLoader cl = new URLClassLoader(
                    urls.toArray(new URL[0]),
                    BlackJackPlugin.class.getClassLoader());
            for (BlackJackPlugin p : ServiceLoader.load(BlackJackPlugin.class, cl)) {
                plugins.add(p);
            }
        } catch (IOException e) {
            System.err.println("Plugin discovery failed: " + e.getMessage());
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
