package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the per-hand result the engine records during settlement.
 *
 * <p>Front ends render {@link Engine#lastOutcomes()} instead of re-deriving the
 * result from hand values, so these tests are what guarantee the label a player
 * sees matches the money they were actually paid. The old UI carried its own
 * copy of the settlement comparison; the point of this API is that there is now
 * exactly one implementation, and it is the one that moves the chips.
 */
class OutcomeTest {

    private static final int START = 1000, BET = 100;

    private static Engine engine(long seed) {
        BlackjackRules r = new BlackjackRules();
        r.dealerHitsSoft17 = false;
        return new Engine(START, new Random(seed), r);
    }

    /** Play a round to completion with a simple stand-on-17 policy. */
    private static Engine playOut(long seed, boolean takeInsurance) {
        Engine e = engine(seed);
        e.addBet(BET);
        e.deal();
        int guard = 0;
        while (e.phase() != Phase.BETTING && guard++ < 40) {
            if (e.phase() == Phase.INSURANCE) {
                e.takeInsurance(takeInsurance && e.canInsure());
            } else if (e.phase() == Phase.PLAYER) {
                if (e.canHit() && e.active().value() < 17) e.hit();
                else if (e.canStand()) e.stand();
                else if (e.canHit()) e.hit();
                else break;
            } else {
                break;
            }
        }
        return e;
    }

    @Test void noOutcomesBeforeTheFirstRound() {
        assertTrue(engine(1).lastOutcomes().isEmpty());
        assertEquals(0, engine(1).lastNet());
    }

    @Test void oneOutcomePerHandAfterEveryRound() {
        for (long seed = 0; seed < 500; seed++) {
            Engine e = playOut(seed, false);
            assertEquals(Phase.BETTING, e.phase(), "round should have settled, seed " + seed);
            assertEquals(e.hands().size(), e.lastOutcomes().size(),
                    "one outcome per hand, seed " + seed);
        }
    }

    @Test void outcomesAreImmutableToCallers() {
        Engine e = playOut(7, false);
        assertThrows(UnsupportedOperationException.class,
                () -> e.lastOutcomes().add(Outcome.WIN));
    }

    @Test void netMatchesTheActualBankrollChange() {
        for (long seed = 0; seed < 500; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            int before = e.bankroll() + e.pendingBet();   // money owned at the deal
            e.deal();
            int guard = 0;
            while (e.phase() != Phase.BETTING && guard++ < 40) {
                if (e.phase() == Phase.INSURANCE)   e.takeInsurance(false);
                else if (e.phase() == Phase.PLAYER) {
                    if (e.canHit() && e.active().value() < 17) e.hit();
                    else if (e.canStand()) e.stand();
                    else break;
                } else break;
            }
            assertEquals(e.bankroll() - before, e.lastNet(),
                    "lastNet must equal the real bankroll delta, seed " + seed);
        }
    }

    @Test void netAccountsForDoublingDown() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() != Phase.PLAYER || !e.canDouble()) continue;
            e.doubleDown();
            if (e.phase() != Phase.BETTING) continue;
            // A doubled hand risks 2 x BET, so the net can only be -2*BET, 0, or +2*BET.
            assertEquals(1, e.lastOutcomes().size());
            int net = e.lastNet();
            assertTrue(net == -2 * BET || net == 0 || net == 2 * BET,
                    "doubled round net was " + net + " at seed " + seed);
            assertEquals(e.bankroll() - START, net);
            return;
        }
        fail("no doubleable deal found");
    }

    @Test void bustIsRecordedAsBust() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER) continue;
            while (e.phase() == Phase.PLAYER && e.canHit()) e.hit();
            if (!e.hands().get(0).isBust()) continue;
            assertEquals(List.of(Outcome.BUST), e.lastOutcomes());
            assertEquals(-BET, e.lastNet(), "a bust forfeits exactly the stake");
            return;
        }
        fail("no bustable deal found");
    }

    @Test void surrenderIsRecordedAsSurrenderAndCostsHalf() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.rules().lateSurrender = true;
            e.addBet(BET);
            e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER || !e.canSurrender()) continue;
            e.surrender();
            assertEquals(List.of(Outcome.SURRENDER), e.lastOutcomes());
            assertEquals(-BET / 2, e.lastNet(), "late surrender forfeits half the stake");
            return;
        }
        fail("no surrenderable deal found");
    }

    @Test void naturalBlackjackIsRecordedAsBlackjack() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() != Phase.BETTING) continue;          // settled during the deal
            Hand p = e.hands().get(0);
            if (!p.isBlackjack() || e.dealer().isBlackjack()) continue;
            assertEquals(List.of(Outcome.BLACKJACK), e.lastOutcomes());
            assertEquals(BET * 3 / 2, e.lastNet(), "3:2 on the stake");
            return;
        }
        fail("no player natural found");
    }

    @Test void pushReturnsTheStakeAndNetsZero() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = playOut(seed, false);
            if (!e.lastOutcomes().equals(List.of(Outcome.PUSH))) continue;
            assertEquals(0, e.lastNet(), "a push is money-neutral");
            assertEquals(START, e.bankroll());
            return;
        }
        fail("no push found");
    }

    @Test void everyOutcomeAgreesWithTheStatsCounters() {
        for (long seed = 0; seed < 800; seed++) {
            Engine e = playOut(seed, false);
            List<Outcome> os = e.lastOutcomes();
            SessionStats s  = e.stats();
            long wins   = os.stream().filter(Outcome::isWin).count();
            long pushes = os.stream().filter(o -> o == Outcome.PUSH).count();
            long losses = os.stream().filter(o -> o == Outcome.LOSS
                                              || o == Outcome.BUST
                                              || o == Outcome.SURRENDER).count();
            assertEquals(s.wins,   wins,   "wins, seed " + seed);
            assertEquals(s.pushes, pushes, "pushes, seed " + seed);
            assertEquals(s.losses, losses, "losses, seed " + seed);
        }
    }

    @Test void winningOutcomesAlwaysLeaveThePlayerAhead() {
        for (long seed = 0; seed < 800; seed++) {
            Engine e = playOut(seed, false);
            List<Outcome> os = e.lastOutcomes();
            if (os.size() != 1) continue;               // single hand: net is unambiguous
            Outcome o = os.get(0);
            if (o.isWin())            assertTrue(e.lastNet() > 0, o + " should pay, seed " + seed);
            else if (o == Outcome.PUSH) assertEquals(0, e.lastNet(), "push, seed " + seed);
            else                      assertTrue(e.lastNet() < 0, o + " should cost, seed " + seed);
        }
    }

    @Test void aNewDealClearsThePreviousResult() {
        Engine e = playOut(3, false);
        assertFalse(e.lastOutcomes().isEmpty());
        e.addBet(BET);
        assertFalse(e.lastOutcomes().isEmpty(), "still visible while betting");
        e.deal();
        if (e.phase() != Phase.BETTING) {              // unless it settled immediately
            assertTrue(e.lastOutcomes().isEmpty(), "cleared for the new round");
            assertEquals(0, e.lastNet());
        }
    }

    @Test void splitProducesAnOutcomePerResultingHand() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER || !e.canSplit()) continue;
            e.split();
            int guard = 0;
            while (e.phase() == Phase.PLAYER && guard++ < 40) {
                if (e.canHit() && e.active().value() < 17) e.hit();
                else if (e.canStand()) e.stand();
                else break;
            }
            if (e.phase() != Phase.BETTING) continue;
            assertTrue(e.hands().size() >= 2, "split made at least two hands");
            assertEquals(e.hands().size(), e.lastOutcomes().size());
            assertEquals(e.bankroll() - START, e.lastNet());
            // A split hand's two-card 21 is not a natural and must never be
            // reported as one -- that would pay 3:2 and fire the fanfare.
            for (int i = 0; i < e.hands().size(); i++) {
                if (e.hands().get(i).fromSplit()) {
                    assertNotEquals(Outcome.BLACKJACK, e.lastOutcomes().get(i),
                            "split hand reported as a natural, seed " + seed);
                }
            }
            return;
        }
        fail("no splittable deal found");
    }

    @Test void insuranceIsIncludedInTheNet() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() != Phase.INSURANCE || !e.canInsure()) continue;
            e.takeInsurance(true);
            if (e.phase() != Phase.BETTING) continue;    // dealer had no blackjack
            // Insurance premium is half the stake and pays 2:1, so a dealer
            // natural leaves the player exactly even overall.
            assertEquals(e.bankroll() - START, e.lastNet());
            assertEquals(0, e.lastNet(), "insurance covers the loss on a dealer natural");
            return;
        }
        fail("no insured dealer blackjack found");
    }
}
