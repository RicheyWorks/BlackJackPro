package com.richeyworks.blackjack.gdx;

import com.badlogic.gdx.Game;
import com.richeyworks.blackjack.table.Palettes;
import com.richeyworks.blackjack.table.TablePalette;

/**
 * libGDX entry point. Hands control to {@link TableScreen} immediately.
 *
 * The same instance is used by every platform launcher
 * ({@code DesktopLauncher} for LWJGL3 and {@code AndroidLauncher} for the APK).
 * Anything that needs platform-specific access (file paths, audio bridge,
 * Steam SDK) is passed in via the {@link Platform} interface so the core
 * stays platform-agnostic.
 */
public final class BlackJackGame extends Game {

    /**
     * The active theme, shared by every screen.
     *
     * <p>This used to be three hardcoded Colors, which is why the mobile build
     * had one green felt and no theme picker while the desktop had seven looks.
     * The palette now comes from {@code core}, so both builds render the same
     * themes from the same numbers.
     */
    private GdxPalette palette = new GdxPalette(Palettes.classic());

    private final Platform platform;
    private GameSession    session;
    private TableScreen    table;
    private final GdxSfx   sfx = new GdxSfx();

    public BlackJackGame(Platform platform) {
        this.platform = platform;
    }

    public Platform platform() { return platform; }

    /** Persistent player state. Available from {@link #create()} onwards. */
    public GameSession session() { return session; }

    /** Sound effects, shared by every screen. */
    public GdxSfx sfx() { return sfx; }

    /** Colours for the active theme. Never null. */
    public GdxPalette palette() { return palette; }

    /** Switch theme and remember it. Persists immediately, like the rule toggles. */
    public void setPalette(TablePalette p) {
        this.palette = new GdxPalette(p);
        if (session != null) {
            session.settings().themeId = p.id();
            session.persist();
        }
    }

    @Override
    public void create() {
        session = new GameSession(platform);
        // Restore the saved theme before any screen draws a frame. themeId is
        // the same settings key the desktop build uses, so a shared data
        // directory keeps one choice across both.
        palette = new GdxPalette(Palettes.byId(session.settings().themeId));
        sfx.setMuted(!session.settings().sfxEnabled);
        sfx.setVolume(session.settings().sfxVolume);
        table   = new TableScreen(this);
        setScreen(table);
    }

    /**
     * Android calls this on every task switch, incoming call, and screen-off,
     * and it is the last callback guaranteed to run before the process may be
     * killed — {@code dispose()} frequently never arrives. Saving here is what
     * makes progress survive on mobile.
     */
    @Override
    public void pause() {
        super.pause();          // forwards to the active Screen
        if (session != null) session.persist();
    }

    /**
     * Desktop close and orderly Android teardown both land here.
     *
     * <p>{@code Game.dispose()} only calls {@code hide()} on the current screen —
     * it never disposes any of them — so every screen's GL resources have to be
     * released explicitly or they leak.
     */
    @Override
    public void dispose() {
        if (session != null) session.persist();
        super.dispose();            // hides the active screen
        if (table != null) table.dispose();   // and disposes the menu it owns
        sfx.dispose();
    }

    /**
     * Capability surface implemented per-platform. Lets the game ask "where do
     * I save data?" without depending on java.nio.file (which is fine on Android
     * but undefined on web targets).
     */
    public interface Platform {
        /** Vibrate if hardware supports it. No-op on desktop. */
        default void hapticTick() {}

        /** Display a transient toast/notification. */
        default void toast(String msg) { System.out.println("[toast] " + msg); }

        /**
         * Absolute directory for saves, settings, and achievements.
         *
         * <p>Returning null or blank defers to {@code AppPaths} host detection,
         * which is right for desktop and wrong for Android — there
         * {@code os.name} is {@code Linux} and {@code user.home} is not
         * writable, so Android must return {@code getFilesDir()}.
         */
        default String saveDir() { return null; }
    }
}
