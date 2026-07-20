package com.richeyworks.blackjack.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A plugin is third-party code loaded before the window exists, so anything it
 * throws used to surface as "the game doesn't start" with nothing on screen.
 * These tests pin down that a broken plugin is skipped and recorded rather than
 * fatal, and that the allow-list still refuses everything it should.
 */
class PluginRegistryResilienceTest {

    private static final String SERVICE =
            "META-INF/services/com.richeyworks.blackjack.plugin.BlackJackPlugin";

    /** Write a JAR whose service file names {@code providerClass}. */
    private static Path jar(Path dir, String name, String providerClass) throws IOException {
        Path jar = dir.resolve(name);
        try (OutputStream os = Files.newOutputStream(jar);
             JarOutputStream jos = new JarOutputStream(os)) {
            jos.putNextEntry(new JarEntry(SERVICE));
            jos.write(providerClass.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        return jar;
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(Files.readAllBytes(file));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static void allow(Path dir, Path jar) throws Exception {
        Files.writeString(dir.resolve("trusted.sha256"),
                jar.getFileName() + "=" + sha256(jar) + "\n");
    }

    /**
     * How many plugins the classpath alone contributes. Anything above this in a
     * test came from the external directory.
     */
    private static int builtinCount() {
        PluginRegistry baseline = new PluginRegistry();
        baseline.loadAll(null);
        int n = baseline.all().size();
        baseline.shutdown();
        return n;
    }

    /** Built-in plugins always load from the classpath, so count only extras. */
    private static int externalCount(PluginRegistry r, int builtins) {
        return r.all().size() - builtins;
    }

    @Test void aBrokenServiceEntryIsSkippedNotFatal(@TempDir Path dir) throws Exception {
        Path j = jar(dir, "broken.jar", "com.nope.MissingPlugin");
        allow(dir, j);

        PluginRegistry r = new PluginRegistry();
        int builtins = builtinCount();

        // The failure mode being guarded against is ServiceConfigurationError --
        // an Error, so a `catch (Exception)` would not have held it.
        assertDoesNotThrow(() -> r.loadAll(dir));
        assertEquals(0, externalCount(r, builtins), "the bad provider is not registered");
        assertFalse(r.failures().isEmpty(), "and the user is told why");
        r.shutdown();
    }

    @Test void aJarNotOnTheAllowListIsIgnored(@TempDir Path dir) throws Exception {
        Path good = jar(dir, "good.jar", "com.nope.MissingPlugin");
        allow(dir, good);
        jar(dir, "unlisted.jar", "com.nope.AlsoMissing");   // deliberately not listed

        PluginRegistry r = new PluginRegistry();
        int builtins = builtinCount();
        assertDoesNotThrow(() -> r.loadAll(dir));
        assertEquals(0, externalCount(r, builtins));
        r.shutdown();
    }

    @Test void aTamperedJarIsRejected(@TempDir Path dir) throws Exception {
        Path j = jar(dir, "swapped.jar", "com.nope.MissingPlugin");
        allow(dir, j);
        // Same name, different bytes: the recorded checksum no longer matches.
        jar(dir, "swapped.jar", "com.nope.SomethingElseEntirely");

        PluginRegistry r = new PluginRegistry();
        int builtins = builtinCount();
        r.loadAll(dir);
        assertEquals(0, externalCount(r, builtins), "checksum mismatch must not load");
        r.shutdown();
    }

    @Test void noAllowListLoadsNothingExternal(@TempDir Path dir) throws Exception {
        jar(dir, "dropped.jar", "com.nope.MissingPlugin");   // no trusted.sha256

        PluginRegistry r = new PluginRegistry();
        int builtins = builtinCount();
        r.loadAll(dir);
        assertEquals(0, externalCount(r, builtins), "secure by default");
        assertTrue(r.failures().isEmpty(), "silently ignoring is not a failure to report");
        r.shutdown();
    }

    @Test void builtinPluginsStillLoadAlongsideABrokenExternalOne(@TempDir Path dir) throws Exception {
        int builtins = builtinCount();
        assumeBuiltinsExist(builtins);

        Path j = jar(dir, "broken.jar", "com.nope.MissingPlugin");
        allow(dir, j);

        PluginRegistry r = new PluginRegistry();
        r.loadAll(dir);
        assertEquals(builtins, r.all().size(),
                "one bad external JAR must not cost us the built-in pack");
        r.shutdown();
    }

    @Test void shutdownIsSafeWithoutAnyExternalDirectory() {
        PluginRegistry r = new PluginRegistry();
        r.loadAll(null);
        assertDoesNotThrow(r::shutdown);
        assertDoesNotThrow(r::shutdown, "idempotent");
        assertTrue(r.all().isEmpty(), "shutdown clears the registry");
    }

    @Test void failuresListIsNeverNullAndIsUnmodifiable() {
        PluginRegistry r = new PluginRegistry();
        r.loadAll(null);
        assertNotNull(r.failures());
        assertThrows(UnsupportedOperationException.class, () -> r.failures().add("x"));
    }

    private static void assumeBuiltinsExist(int builtins) {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                builtins > 0, "no built-in plugins on the test classpath");
    }
}
