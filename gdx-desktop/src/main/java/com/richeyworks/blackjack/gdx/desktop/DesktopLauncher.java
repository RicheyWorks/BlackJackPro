package com.richeyworks.blackjack.gdx.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.richeyworks.blackjack.gdx.BlackJackGame;

/**
 * LWJGL3 desktop launcher for the libGDX game. Lets us iterate on the
 * cross-platform UI on the desktop before pushing to Android.
 *
 * Run with: gradlew :gdx-desktop:run
 */
public final class DesktopLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("BlackJack Pro");
        cfg.setWindowedMode(1280, 720);
        cfg.useVsync(true);
        cfg.setForegroundFPS(60);
        // Returning null defers to AppPaths host detection, which resolves the
        // same per-user directory the Swing build uses -- so the desktop preview
        // shares one save rather than scattering save.txt into whatever
        // directory the process happened to start in.
        new Lwjgl3Application(new BlackJackGame(new BlackJackGame.Platform() {
            @Override public String saveDir() { return null; }
        }), cfg);
    }
}
