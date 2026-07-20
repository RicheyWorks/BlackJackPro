package com.richeyworks.blackjack.gdx;

import com.richeyworks.blackjack.achievement.AchievementService;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.persist.AppPaths;
import com.richeyworks.blackjack.persist.SaveManager;
import com.richeyworks.blackjack.settings.GameSettings;

import java.nio.file.Path;
import java.security.SecureRandom;

/**
 * Owns the player's persistent state for the libGDX build: the {@link Engine},
 * the bankroll/stats save, settings, and achievements.
 *
 * <p>The mobile port previously constructed {@code new Engine(1000, ...)} at
 * screen creation and never touched the persistence classes in {@code :core} at
 * all, so a player's bankroll, lifetime stats, and achievements were discarded
 * every time the process ended — which on Android is routine rather than
 * exceptional. This class is the missing link between the two.
 *
 * <p><b>Where the files go</b> is decided by the platform, not by host
 * detection: Android reports {@code os.name=Linux} with an unwritable
 * {@code user.home}, so {@link AppPaths#dataDir()} would resolve somewhere the
 * app cannot write. {@link BlackJackGame.Platform#saveDir()} supplies the right
 * directory per target and this passes it through.
 *
 * <p><b>Saving is idempotent and cheap</b> — three small text files — because on
 * Android it has to run from {@code pause()}, which fires on every task switch,
 * incoming call, and screen-off, and is the last callback guaranteed before the
 * process can be killed.
 */
public final class GameSession {

    /** Bankroll handed to a player with no save file. */
    public static final int STARTING_BANKROLL = 1000;

    private final Engine             engine;
    private final SaveManager        save;
    private final GameSettings       settings;
    private final AchievementService achievements;
    private final Path               dataDir;

    public GameSession(BlackJackGame.Platform platform) {
        this.dataDir      = AppPaths.dataDir(platform == null ? null : platform.saveDir());
        this.settings     = new GameSettings(dataDir.resolve("settings.properties"));
        this.save         = new SaveManager(dataDir.resolve("save.txt"));
        this.achievements = new AchievementService(dataDir.resolve("achievements.txt"));

        this.engine = new Engine(STARTING_BANKROLL, new SecureRandom());
        engine.rules().dealerHitsSoft17 = settings.dealerHitsSoft17;
        engine.rules().lateSurrender    = settings.lateSurrender;
        engine.rules().offerInsurance   = settings.offerInsurance;

        // Restores bankroll and lifetime stats; a missing file leaves the
        // defaults above untouched, so a first run just starts at $1000.
        save.load(engine);
    }

    public Engine             engine()       { return engine; }
    public GameSettings       settings()     { return settings; }
    public AchievementService achievements() { return achievements; }
    public Path               dataDir()      { return dataDir; }

    /**
     * Write everything to disk. Safe to call as often as the platform likes.
     *
     * <p>Chips the player has pushed onto the felt live in {@code pendingBet},
     * which is not part of the save — only the bankroll is. They are returned
     * first so that being interrupted mid-bet doesn't destroy them, the same
     * defect fixed on the desktop side (SW-2). {@code clearBet()} is
     * phase-guarded, so a hand already in play is left alone and forfeited as it
     * would be at a real table.
     */
    public void persist() {
        engine.clearBet();
        save.save(engine);
        settings.save();
        achievements.save();
    }

    /** Reset to a fresh session and persist immediately. */
    public void reset() {
        engine.clearBet();
        engine.setBankroll(STARTING_BANKROLL);
        engine.stats().reset();
        engine.shoe().reshuffle();
        persist();
    }
}
