package com.richeyworks.blackjack.gdx;

import com.richeyworks.blackjack.engine.Phase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * House-rule changes and session reset, driven from the mobile settings screen.
 *
 * <p>The logic lives on {@link GameSession} rather than in {@code MenuScreen}
 * precisely so it can be tested without a GL context, a device, or an Android
 * runtime — the screen only draws and dispatches.
 */
class GameSessionRulesTest {

    private static BlackJackGame.Platform platform(Path dir) {
        return new BlackJackGame.Platform() {
            @Override public String saveDir() { return dir.toString(); }
        };
    }

    /** Deal until a hand is actually in play, or give up. */
    private static boolean startAHand(GameSession s) {
        for (int i = 0; i < 50; i++) {
            if (s.engine().phase() != Phase.BETTING) return true;
            s.engine().addBet(10);
            s.engine().deal();
        }
        return s.engine().phase() != Phase.BETTING;
    }

    @Test void aRuleChangeReachesBothTheEngineAndTheSettings(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        assertTrue(s.setRule(GameSession.Rule.DEALER_HITS_SOFT_17, true));

        // Writing only one of the two is how the desktop menu and settings
        // dialog drifted apart (SW-13) -- the engine changed, the file didn't,
        // and the next launch silently reverted it.
        assertTrue(s.engine().rules().dealerHitsSoft17, "engine updated");
        assertTrue(s.settings().dealerHitsSoft17,       "settings updated");
    }

    @Test void aRuleChangeSurvivesARestart(@TempDir Path dir) {
        GameSession first = new GameSession(platform(dir));
        first.setRule(GameSession.Rule.LATE_SURRENDER, false);

        // setRule persists on the spot rather than waiting for a pause().
        GameSession second = new GameSession(platform(dir));
        assertFalse(second.engine().rules().lateSurrender);
        assertFalse(second.settings().lateSurrender);
    }

    @Test void everyRuleRoundTrips(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        for (GameSession.Rule rule : GameSession.Rule.values()) {
            boolean before = s.rule(rule);
            assertTrue(s.setRule(rule, !before), rule + " should be settable while betting");
            assertEquals(!before, s.rule(rule), rule + " did not flip");
            assertEquals(!before, new GameSession(platform(dir)).rule(rule), rule + " did not persist");
        }
    }

    @Test void rulesAreLockedWhileAHandIsInPlay(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        assertTrue(startAHand(s), "needed a hand in progress");

        assertFalse(s.canChangeRules());
        boolean before = s.rule(GameSession.Rule.DEALER_HITS_SOFT_17);

        // Changing how the dealer draws mid-hand would move the odds after the
        // player has already committed chips.
        assertFalse(s.setRule(GameSession.Rule.DEALER_HITS_SOFT_17, !before),
                "rule change must be refused mid-hand");
        assertEquals(before, s.rule(GameSession.Rule.DEALER_HITS_SOFT_17), "nothing changed");
    }

    @Test void rulesUnlockAgainBetweenRounds(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        assertTrue(s.canChangeRules(), "betting phase is open");
        assertTrue(startAHand(s));
        assertFalse(s.canChangeRules());

        // Play the hand out; the engine settles synchronously back to BETTING.
        int guard = 0;
        while (s.engine().phase() != Phase.BETTING && guard++ < 40) {
            if (s.engine().phase() == Phase.INSURANCE) s.engine().takeInsurance(false);
            else if (s.engine().canStand())            s.engine().stand();
            else if (s.engine().canHit())              s.engine().hit();
            else break;
        }
        assertEquals(Phase.BETTING, s.engine().phase());
        assertTrue(s.canChangeRules(), "open again once the hand is done");
        assertTrue(s.setRule(GameSession.Rule.OFFER_INSURANCE, false));
    }

    @Test void rulesReadFromTheEngineNotTheSettingsFile(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        // The engine is the authority during play; a settings value that never
        // made it into the engine must not be reported as active.
        s.settings().dealerHitsSoft17 = true;
        assertFalse(s.rule(GameSession.Rule.DEALER_HITS_SOFT_17),
                "an unapplied setting is not a live rule");
    }

    @Test void resetClearsTheBoardAndTheStats(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        s.engine().setBankroll(4321);
        s.engine().stats().hands = 12;
        s.engine().stats().wins  = 5;
        s.engine().addBet(50);

        s.reset();

        assertEquals(GameSession.STARTING_BANKROLL, s.engine().bankroll());
        assertEquals(0, s.engine().pendingBet(), "chips come off the felt");
        assertEquals(0, s.engine().stats().hands);
        assertEquals(0, s.engine().stats().wins);
        assertEquals(Phase.BETTING, s.engine().phase());
    }

    @Test void resetAbandonsALiveHand(@TempDir Path dir) {
        // The regression: New Session mid-round used to leave PLAYER phase with
        // cards still dealt while bankroll snapped to $1000.
        GameSession s = new GameSession(platform(dir));
        s.engine().addBet(100);
        s.engine().deal();
        if (s.engine().phase() == Phase.INSURANCE) s.engine().takeInsurance(false);
        if (s.engine().phase() == Phase.BETTING) {
            // Instant settle (naturals) — still a valid abandon target next deal.
            s.engine().addBet(50);
            s.engine().deal();
            if (s.engine().phase() == Phase.INSURANCE) s.engine().takeInsurance(false);
        }
        if (s.engine().phase() != Phase.PLAYER && s.engine().phase() != Phase.INSURANCE) {
            return; // unlucky seeds; abandonRound is covered in EngineTest
        }

        s.reset();

        assertEquals(Phase.BETTING, s.engine().phase());
        assertTrue(s.engine().hands().get(0).isEmpty());
        assertTrue(s.engine().dealer().isEmpty());
        assertEquals(0, s.engine().pendingBet());
        assertEquals(GameSession.STARTING_BANKROLL, s.engine().bankroll());
        assertEquals(GameSession.STARTING_BANKROLL, s.engine().stats().peakBankroll);
        assertFalse(s.engine().canHit());
        s.engine().addBet(25);
        assertTrue(s.engine().canDeal(), "player must be able to deal after reset");
    }

    @Test void resetIsDurableWithoutWaitingForAPause(@TempDir Path dir) {
        GameSession first = new GameSession(platform(dir));
        first.engine().setBankroll(9999);
        first.persist();
        first.reset();

        assertEquals(GameSession.STARTING_BANKROLL,
                new GameSession(platform(dir)).engine().bankroll(),
                "a reset the player asked for must not be lost if the app is killed");
    }

    @Test void resetKeepsTheChosenHouseRules(@TempDir Path dir) {
        GameSession s = new GameSession(platform(dir));
        s.setRule(GameSession.Rule.DEALER_HITS_SOFT_17, true);
        s.reset();

        // Resetting the bankroll is not a reason to throw away preferences.
        assertTrue(s.rule(GameSession.Rule.DEALER_HITS_SOFT_17));
        assertTrue(new GameSession(platform(dir)).rule(GameSession.Rule.DEALER_HITS_SOFT_17));
    }

    @Test void everyRuleHasADisplayLabel() {
        for (GameSession.Rule rule : GameSession.Rule.values()) {
            assertNotNull(rule.label());
            assertFalse(rule.label().isBlank(), rule + " needs a label for the settings screen");
        }
    }
}
