package com.richeyworks.blackjack.persist;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves a fixed, per-user, writable data directory for BlackJack Pro
 * (saves, settings, achievements, trusted plugins).
 *
 * <p>Historically these files were read from and written to a
 * working-directory-relative {@code resources/} folder, which made the trust
 * boundary "whoever controls the process working directory" — a problem for the
 * external plugin loader in particular (see {@code PluginRegistry}). Anchoring
 * writable state to a per-user location instead removes that ambiguity and keeps
 * state out of a (potentially shared or read-only) install tree.
 *
 * <ul>
 *   <li>Windows: {@code %LOCALAPPDATA%\BlackJackPro}</li>
 *   <li>macOS:   {@code ~/Library/Application Support/BlackJackPro}</li>
 *   <li>Linux:   {@code $XDG_DATA_HOME/BlackJackPro} or {@code ~/.local/share/BlackJackPro}</li>
 * </ul>
 *
 * <p><b>Android:</b> the detection above does not apply — Android reports
 * {@code os.name=Linux} with a {@code user.home} that is not writable, so the
 * Linux branch would resolve somewhere the app cannot write. Android callers
 * must pass their own directory to {@link #dataDir(String)} (the activity's
 * {@code getFilesDir()}), which is already app-private and needs no permission.
 */
public final class AppPaths {

    private static final String APP_FOLDER = "BlackJackPro";

    private AppPaths() {}

    /**
     * Data directory for a platform that knows where its own storage lives.
     *
     * @param baseOverride an absolute directory to use as-is; when null or blank
     *                     this falls back to {@link #dataDir()} host detection.
     *                     Passed straight through rather than having
     *                     {@code BlackJackPro} appended — a platform that hands
     *                     us a directory has already scoped it to this app.
     */
    public static Path dataDir(String baseOverride) {
        if (baseOverride == null || baseOverride.isBlank()) return dataDir();
        return Paths.get(baseOverride);
    }

    /** The per-user data directory. Not created here — callers create on write. */
    public static Path dataDir() {
        String os   = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", ".");
        Path base;
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            base = (localAppData != null && !localAppData.isBlank())
                    ? Paths.get(localAppData)
                    : Paths.get(home, "AppData", "Local");
        } else if (os.contains("mac") || os.contains("darwin")) {
            base = Paths.get(home, "Library", "Application Support");
        } else {
            String xdg = System.getenv("XDG_DATA_HOME");
            base = (xdg != null && !xdg.isBlank())
                    ? Paths.get(xdg)
                    : Paths.get(home, ".local", "share");
        }
        return base.resolve(APP_FOLDER);
    }
}
