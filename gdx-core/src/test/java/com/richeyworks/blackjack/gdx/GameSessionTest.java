package com.richeyworks.blackjack.gdx;

import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.persist.AppPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The libGDX build previously constructed {@code new Engine(1000, ...)} at
 * screen creation and never touched {@code :core}'s persistence classes, so a
 * player's bankroll, lifetime stats, and achievements were discarded whenever
 * the process ended — routine on Android rather than exceptional.
 *
 * <p>These tests pin the save/restore cycle down without needing a GL context,
 * a device, or an Android runtime.
 */
class GameSessionTest {

    /** Platform stub that reports a caller-chosen save directory. */
    private static BlackJackGame.Platform platform(Path dir) {
        return new BlackJackGame.Platform() {
            @Override public String saveDir() { return dir == null ? null : dir.toString(); }
        };
    }

    @Test void firstRunStartsAtTheDefaultBankroll(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        assertEquals(GameSession.STARTING_BANKROLL, s.engine().bankroll());
        assertEquals(0, s.engine().stats().hands);
        assertEquals(Phase.BETTING, s.engine().phase());
    }

    @Test void bankrollSurvivesARestart(@TempDir Path dir) {
        GameSession first = new GameSession(platform(dir));
        first.engine().setBankroll(2750);
        first.persist();

        GameSession second = new GameSession(platform(dir));
        assertEquals(2750, second.engine().bankroll(),
                "the whole point: progress outlives the process");
    }

    @Test void lifetimeStatsSurviveARestart(@TempDir Path dir) {
        GameSession first = new GameSession(platform(dir));
        first.engine().stats().hands      = 42;
        first.engine().stats().wins       = 19;
        first.engine().stats().blackjacks = 3;
        first.persist();

        GameSession second = new GameSession(platform(dir));
        assertEquals(42, second.engine().stats().hands);
        assertEquals(19, second.engine().stats().wins);
        assertEquals(3,  second.engine().stats().blackjacks);
    }

    @Test void achievementProgressSurvivesARestart(@TempDir Path dir) {
        GameSession first = new GameSession(platform(dir));
        first.achievements().setProgress("first_hand", 1);
        first.persist();

        GameSession second = new GameSession(platform(dir));
        assertTrue(second.achievements().get("first_hand").unlocked(),
                "an unlocked achievement must not be forgotten on restart");
    }

    @Test void stakedChipsAreReturnedNotDestroyed(@TempDir Path dir) {
        GameSession first = new GameSession(platform(dir));
        first.engine().addBet(200);
        assertEquals(800, first.engine().bankroll(), "chips moved onto the felt");

        // Interrupted mid-bet: Android pauses the app and may kill it. Only the
        // bankroll is persisted, so an unreturned pendingBet is money destroyed.
        first.persist();

        GameSession second = new GameSession(platform(dir));
        assertEquals(GameSession.STARTING_BANKROLL, second.engine().bankroll(),
                "being backgrounded mid-bet must not cost the player chips");
    }

    /**
     * 21+3 lives outside the engine (in TableScreen's SideBetManager). The stake
     * is debited from the bankroll at placement; if a pause saves without
     * refunding it first, the chips vanish — the new process has pending=0.
     * TableScreen.persistSafely() is what closes that hole; this documents the
     * money shape the screen has to reverse.
     */
    @Test void aSideBetStakeAlreadyDebitedMustBeRefundedBeforeSave(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        // Simulate: side bet took $25 off the bankroll, main bet still open.
        s.engine().setBankroll(s.engine().bankroll() - 25);
        // What persist() alone would write (without the screen refunding first):
        s.persist();
        assertEquals(GameSession.STARTING_BANKROLL - 25,
                new GameSession(platform(dir)).engine().bankroll(),
                "without a refund the reduced bankroll is what survives — the screen must undo this");
    }

    @Test void aHandAlreadyInPlayIsLeftAlone(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        s.engine().addBet(100);
        s.engine().deal();
        if (s.engine().phase() == Phase.BETTING) return;   // settled instantly

        int before = s.engine().bankroll();
        s.persist();
        assertEquals(before, s.engine().bankroll(),
                "clearBet is phase-guarded; a live hand is forfeited, not refunded");
    }

    @Test void persistIsRepeatable(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        s.engine().setBankroll(1234);
        // pause() fires on every task switch, so this runs constantly.
        for (int i = 0; i < 5; i++) assertDoesNotThrow(() -> s.persist());

        assertEquals(1234, new GameSession(platform(dir)).engine().bankroll());
    }

    @Test void filesLandInTheDirectoryThePlatformChose(@TempDir Path dir) throws Exception {
        GameSession s = new GameSession(platform(dir));
        s.persist();

        assertEquals(dir, s.dataDir());
        assertTrue(Files.exists(dir.resolve("save.txt")),            "save written");
        assertTrue(Files.exists(dir.resolve("settings.properties")), "settings written");
        assertTrue(Files.exists(dir.resolve("achievements.txt")),    "achievements written");
    }

    @Test void aBlankSaveDirFallsBackToHostDetection() {
        // Desktop returns null so the gdx preview shares the Swing build's save
        // location instead of writing into the process working directory.
        assertEquals(AppPaths.dataDir(), new GameSession(platform(null)).dataDir());
        assertEquals(AppPaths.dataDir(), AppPaths.dataDir("   "));
    }

    @Test void anAbsoluteOverrideIsUsedVerbatim(@TempDir Path dir) {
        // Android hands us getFilesDir(), which is already app-scoped -- appending
        // another "BlackJackPro" folder underneath it would just be noise.
        assertEquals(dir, AppPaths.dataDir(dir.toString()));
    }

    @Test void houseRulesFromSettingsReachTheEngine(@TempDir Path dir) {
        GameSession first = new GameSession(platform(dir));
        first.settings().dealerHitsSoft17 = true;
        first.settings().lateSurrender    = false;
        first.persist();

        GameSession second = new GameSession(platform(dir));
        assertTrue(second.engine().rules().dealerHitsSoft17,
                "a saved rule must actually be applied to the engine");
        assertFalse(second.engine().rules().lateSurrender);
    }

    @Test void resetClearsProgressAndPersistsImmediately(@TempDir Path dir) {
        GameSession first = new GameSession(platform(dir));
        first.engine().setBankroll(9000);
        first.engine().stats().hands = 77;
        first.persist();

        first.reset();
        assertEquals(GameSession.STARTING_BANKROLL, first.engine().bankroll());
        assertEquals(0, first.engine().stats().hands);

        GameSession second = new GameSession(platform(dir));
        assertEquals(GameSession.STARTING_BANKROLL, second.engine().bankroll(),
                "reset is durable without waiting for a later pause()");
        assertEquals(0, second.engine().stats().hands);
    }

    @Test void aMissingDirectoryIsCreatedOnFirstSave(@TempDir Path dir) {
        // A fresh install has no data directory yet.
        Path nested = dir.resolve("does").resolve("not").resolve("exist");
        GameSession s = new GameSession(platform(nested));
        s.engine().setBankroll(500);
        assertDoesNotThrow(() -> s.persist());

        assertEquals(500, new GameSession(platform(nested)).engine().bankroll());
    }

    @Test void aCorruptSaveDoesNotPreventStartup(@TempDir Path dir) throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("save.txt"), "bankroll=not-a-number\n???\nwins=oops\n");
        Files.writeString(dir.resolve("achievements.txt"), "garbage\n");

        // A hand-edited or truncated file must degrade to defaults, not crash the
        // app on launch -- there is no console on a phone to read the trace from.
        GameSession s = assertDoesNotThrow(() -> new GameSession(platform(dir)));
        assertTrue(s.engine().bankroll() >= 0);
    }
}
