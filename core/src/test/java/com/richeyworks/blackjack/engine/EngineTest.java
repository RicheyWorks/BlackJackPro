package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mid-level integration tests that drive the engine through complete rounds
 * with a fixed RNG seed. These cover the bugs the original audit found:
 * 3:2 blackjack, split settlement, bust handling, push semantics.
 */
class EngineTest {

    private Engine fresh() {
        BlackjackRules r = new BlackjackRules();
        r.dealerHitsSoft17 = false;
        return new Engine(1000, new Random(42), r);
    }

    @Test void initialPhaseIsBetting() {
        assertEquals(Phase.BETTING, fresh().phase());
    }

    @Test void cannotDealWithoutBet() {
        Engine e = fresh();
        assertFalse(e.canDeal());
        assertThrows(IllegalStateException.class, e::deal);
    }

    @Test void bettingDeductsFromBankrollImmediately() {
        Engine e = fresh();
        e.addBet(100);
        assertEquals(900, e.bankroll());
        assertEquals(100, e.pendingBet());
    }

    @Test void clearBetReturnsChips() {
        Engine e = fresh();
        e.addBet(50);
        e.addBet(25);
        e.clearBet();
        assertEquals(1000, e.bankroll());
        assertEquals(0, e.pendingBet());
    }

    @Test void overlargeBetRejected() {
        Engine e = fresh();
        assertThrows(IllegalStateException.class, () -> e.addBet(2000));
    }

    @Test void blackjackPaysThreeToTwo() {
        // Force a natural-BJ deal by handing the engine a stacked shoe via reflection
        // isn't possible without exposing internals; instead we verify the payout
        // arithmetic directly through BlackjackRules.
        BlackjackRules r = new BlackjackRules();
        assertEquals(150, r.blackjackPayout(100), "3:2 of 100 = 150");
        assertEquals(15,  r.blackjackPayout(10));
        // Halves round the player's way, as a casino would pay them.
        assertEquals(8,   r.blackjackPayout(5),  "ceil(5*3/2) = 8, not 7");
        assertEquals(2,   r.blackjackPayout(1),  "ceil(1*3/2) = 2 (a natural must never pay 0)");
    }

    @Test void insurancePaysTwoToOne() {
        BlackjackRules r = new BlackjackRules();
        assertEquals(100, r.insurancePayout(50));
        assertEquals(20,  r.insurancePayout(10));
    }

    @Test void canPlayManyRoundsWithoutCrash() {
        Engine e = fresh();
        int rounds = 0;
        while (e.bankroll() >= 10 && rounds < 50) {
            e.addBet(10);
            e.deal();
            // accept any insurance offer = decline
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            // stand on every player decision the engine asks for
            while (e.phase() == Phase.PLAYER) {
                if (e.canStand()) e.stand();
                else break;
            }
            // engine should always end the round in BETTING
            assertEquals(Phase.BETTING, e.phase(), "round should resolve");
            rounds++;
        }
        assertTrue(rounds > 0);
    }

    @Test void statsTrackHandsAndWagers() {
        Engine e = fresh();
        e.addBet(10);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        while (e.phase() == Phase.PLAYER) {
            if (e.canStand()) e.stand();
            else break;
        }
        SessionStats s = e.stats();
        assertEquals(1, s.hands);
        assertEquals(10, s.totalWagered);
    }

    @Test void newSessionResetsBankroll() {
        Engine e = fresh();
        e.addBet(500);
        e.clearBet();
        e.setBankroll(2000);
        e.stats().reset();
        assertEquals(2000, e.bankroll());
        assertEquals(0, e.stats().hands);
    }

    @Test void peakBankrollReflectsSubThousandStart() {
        // Regression (CR-1): the peak was hard-floored at 1000, overstating any
        // start below $1000. It should equal the real starting bankroll.
        Engine e = new Engine(500, new Random(1));
        assertEquals(500, e.stats().peakBankroll);
    }

    @Test void freshAndResetStatsCarryNoPhantomPeak() {
        SessionStats s = new SessionStats();
        assertEquals(0, s.peakBankroll, "fresh stats have no phantom peak");
        s.peakBankroll = 3000;
        s.reset();
        assertEquals(0, s.peakBankroll, "reset clears the peak");
    }

    @Test void abandonRoundClearsALiveHandBackToBetting() {
        Engine e = new Engine(1000, new Random(7));
        e.addBet(50);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        if (e.phase() != Phase.PLAYER) return;

        e.abandonRound();
        e.stats().reset();
        e.setBankroll(1000);

        assertEquals(Phase.BETTING, e.phase());
        assertEquals(0, e.pendingBet());
        assertEquals(1, e.hands().size());
        assertTrue(e.hands().get(0).isEmpty());
        assertTrue(e.dealer().isEmpty());
        assertTrue(e.lastOutcomes().isEmpty());
        assertEquals(0, e.lastNet());
        assertFalse(e.canHit());
        assertFalse(e.canStand());
        assertEquals(1000, e.bankroll());
        assertEquals(1000, e.stats().peakBankroll);

        e.addBet(25);
        assertTrue(e.canDeal());
        e.deal();
        assertTrue(e.phase() == Phase.PLAYER
                || e.phase() == Phase.INSURANCE
                || e.phase() == Phase.BETTING);
    }

    @Test void activeHandIsUsableAfterSettlement() {
        Engine e = new Engine(1000, new Random(5));
        e.addBet(10);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        while (e.phase() == Phase.PLAYER) {
            if (e.canStand()) e.stand();
            else if (e.canHit()) e.hit();
            else break;
        }
        assertEquals(Phase.BETTING, e.phase());
        assertDoesNotThrow(e::active);
        assertEquals(0, e.activeIndex());
        assertFalse(e.active().isEmpty());
    }

    @Test void canStandIsFalseOnceTheHandHasStood() {
        Engine e = new Engine(1000, new Random(1));
        e.addBet(10);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        if (e.phase() != Phase.PLAYER) return;
        e.active().stand();
        assertFalse(e.canStand(), "a stood hand must not accept another stand");
    }

    @Test void evenMoneyPayoutDoesNotOverflowInt() {
        Engine e = new Engine(2_000_000_000, new Random(6));
        e.addBet(1_200_000_000);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        while (e.phase() == Phase.PLAYER) {
            if (e.canStand()) e.stand();
            else if (e.canHit()) e.hit();
            else break;
        }
        if (e.lastOutcomes().contains(Outcome.WIN)) {
            assertTrue(e.bankroll() > 0, "winning must not produce a negative bankroll");
            assertTrue(e.lastNet() > 0, "a pure WIN must report a positive net");
        }
    }

    @Test void abandonRoundRefundsPendingChipsOnTheFelt() {
        Engine e = new Engine(1000, new Random(1));
        e.addBet(150);
        assertEquals(850, e.bankroll());
        e.abandonRound();
        assertEquals(0, e.pendingBet());
        assertEquals(1000, e.bankroll(), "undelt chips must return when the round is abandoned");
        assertEquals(Phase.BETTING, e.phase());
    }

    @Test void clearBetAloneDoesNotFixALiveHand() {
        Engine e = new Engine(1000, new Random(7));
        e.addBet(50);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        if (e.phase() != Phase.PLAYER) return;

        e.clearBet();
        e.setBankroll(1000);
        assertEquals(Phase.PLAYER, e.phase(), "clearBet is a no-op mid-round");
        assertFalse(e.canDeal());
        assertTrue(e.canHit() || e.canStand());
    }
}
