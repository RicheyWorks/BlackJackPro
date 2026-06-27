package com.richeyworks.blackjack.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;

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

    /** Brand felt color used across the libGDX UI. */
    public static final Color FELT  = new Color(0x143620ff);
    public static final Color ACCENT = new Color(0xc9a227ff);
    public static final Color TEXT   = new Color(0xf8e9a1ff);

    private final Platform platform;

    public BlackJackGame(Platform platform) {
        this.platform = platform;
    }

    public Platform platform() { return platform; }

    @Override
    public void create() {
        setScreen(new TableScreen(this));
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

        /** Storage location for save files. Implementation chooses
         *  ({@code Gdx.files.local(...)} on desktop, internal app storage on
         *  Android, etc). */
        default String saveDir() { return "."; }
    }
}
