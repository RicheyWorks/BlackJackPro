package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@code can*()} queries must answer in every phase, not just the one they
 * are about.
 *
 * <p>These are what a UI calls to decide which buttons to enable, and it calls
 * them after <em>every</em> action — including the one that ends a round. But
 * {@code advanceHand()} leaves {@code activeHand} one past the end of the hands
 * list when the final hand finishes, so once a round settles {@code active()}
 * is out of bounds. Any query that dereferenced it before checking the phase
 * threw {@code IndexOutOfBoundsException} instead of returning false.
 *
 * <p>78% of completed rounds ended that way. The desktop UI asked
 * {@code canHit()} fourth in its button sequence, so every later update — the
 * remaining buttons and the table repaint — was silently skipped. On the Swing
 * EDT an uncaught exception aborts the handler without killing the app, so
 * nothing crashed and nothing failed: the table just stopped redrawing. It
 * survived a full module review and 249 tests, and turned up in the first two
 * hands of someone actually playing.
 */
class ActionQueriesTest {

    /** Every query a front end asks to decide what the player may do. */
    private static void assertAllAnswerable(Engine e, String situation) {
        record Query(String name, BooleanSupplier call) {}
        Query[] queries = {
            new Query("canDeal",      e::canDeal),
            new Query("canHit",       e::canHit),
            new Query("canStand",     e::canStand),
            new Query("canDouble",    e::canDouble),
            new Query("canSplit",     e::canSplit),
            new Query("canSurrender", e::canSurrender),
            new Query("canInsure",    e::canInsure),
        };
        for (Query q : queries) {
            assertDoesNotThrow(q.call()::getAsBoolean,
                    q.name() + "() threw in phase " + e.phase() + " (" + situation + ")");
        }
    }

    @Test void everyQueryAnswersAfterAStand() {
        Engine e = new Engine(1000, new Random(2));
        e.addBet(25);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        if (e.phase() != Phase.PLAYER) return;
        e.stand();
        assertEquals(Phase.BETTING, e.phase());
        assertAllAnswerable(e, "after standing");
    }

    @Test void everyQueryAnswersAfterABust() {
        for (long seed = 0; seed < 500; seed++) {
            Engine e = new Engine(1000, new Random(seed));
            e.addBet(25);
            e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER) continue;
            while (e.phase() == Phase.PLAYER && e.canHit()) e.hit();
            if (e.phase() != Phase.BETTING || !e.hands().get(0).isBust()) continue;
            assertAllAnswerable(e, "after busting");
            return;
        }
        fail("no bust found");
    }

    @Test void everyQueryAnswersAfterASurrender() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = new Engine(1000, new Random(seed));
            e.rules().lateSurrender = true;
            e.addBet(25);
            e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER || !e.canSurrender()) continue;
            e.surrender();
            assertAllAnswerable(e, "after surrendering");
            return;
        }
        fail("no surrenderable deal found");
    }

    @Test void everyQueryAnswersAfterSplitHandsAllFinish() {
        // The multi-hand path is where activeHand ends up furthest out of bounds.
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = new Engine(1000, new Random(seed));
            e.addBet(25);
            e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER || !e.canSplit()) continue;
            e.split();
            int guard = 0;
            while (e.phase() == Phase.PLAYER && guard++ < 40) {
                if (e.canStand()) e.stand(); else break;
            }
            if (e.phase() != Phase.BETTING) continue;
            assertTrue(e.hands().size() >= 2);
            assertAllAnswerable(e, "after all split hands finished");
            return;
        }
        fail("no splittable deal found");
    }

    @Test void everyQueryAnswersBeforeAnyCardsAreDealt() {
        assertAllAnswerable(new Engine(1000, new Random(1)), "fresh engine, nothing dealt");
    }

    @Test void everyQueryAnswersDuringTheInsurancePrompt() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = new Engine(1000, new Random(seed));
            e.addBet(25);
            e.deal();
            if (e.phase() != Phase.INSURANCE) continue;
            assertAllAnswerable(e, "insurance offered");
            return;
        }
        fail("no insurance offer found");
    }

    @Test void queriesReportNoActionsOnceTheRoundIsOver() {
        // Answering is not enough -- the answer has to be "no", or a UI would
        // leave the action buttons live between rounds.
        Engine e = new Engine(1000, new Random(4));
        e.addBet(25);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        if (e.phase() == Phase.PLAYER) e.stand();
        assertEquals(Phase.BETTING, e.phase());

        assertFalse(e.canHit());
        assertFalse(e.canStand());
        assertFalse(e.canDouble());
        assertFalse(e.canSplit());
        assertFalse(e.canSurrender());
        assertFalse(e.canInsure());
        assertFalse(e.canDeal(), "no bet is pending yet");
    }

    @Test void aFullSessionNeverThrowsFromAQuery() {
        // The shape a real front end drives: ask everything after every action,
        // for a few hundred rounds.
        Engine e = new Engine(100_000, new Random(20260720L));
        e.rules().lateSurrender = true;
        for (int round = 0; round < 300 && e.bankroll() >= 25; round++) {
            e.addBet(25);
            assertAllAnswerable(e, "bet placed, round " + round);
            e.deal();
            assertAllAnswerable(e, "dealt, round " + round);

            int guard = 0;
            while (e.phase() != Phase.BETTING && guard++ < 40) {
                if (e.phase() == Phase.INSURANCE)      e.takeInsurance(false);
                else if (round % 9 == 0 && e.canSurrender()) e.surrender();
                else if (round % 5 == 0 && e.canSplit())     e.split();
                else if (round % 3 == 0 && e.canDouble())    e.doubleDown();
                else if (e.canHit() && e.active().value() < 17) e.hit();
                else if (e.canStand())                       e.stand();
                else break;
                assertAllAnswerable(e, "mid-round, round " + round);
            }
            assertAllAnswerable(e, "settled, round " + round);
        }
        assertTrue(e.stats().hands > 100, "expected a decent sample, got " + e.stats().hands);
    }
}
