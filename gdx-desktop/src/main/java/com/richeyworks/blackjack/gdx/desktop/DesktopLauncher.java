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
        new Lwjgl3Application(new BlackJackGame(new BlackJackGame.Platform() {
            @Override public String saveDir() { return "."; }
        }), cfg);
    }
}
