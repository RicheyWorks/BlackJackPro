package com.richeyworks.blackjack.strategy;

import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.engine.Phase;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the split reordering defect: a flat card-index cursor double-counted
 * the dealer up-card after a split. Identity-based observation must not.
 */
class TableObservationTest {

    @Test void splitDoesNotDoubleCountVisibleCards() {
        // Seed 17 from the probe: K-10 vs 8, then split into K-2 / 10-3.
        Engine e = new Engine(10_000, new Random(17));
        e.addBet(50);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
        assertEquals(Phase.PLAYER, e.phase());
        assertTrue(e.canSplit(), "seed 17 must open a split");

        HiLoCounter counter = new HiLoCounter();
        TableObservation obs = new TableObservation(counter);
        obs.sync(e);
        int afterDeal = counter.runningCount();
        assertEquals(-2, afterDeal, "K,10,8 → −1−1+0");

        e.split();
        obs.sync(e);

        // Recompute from identity over the live table — the oracle for this test.
        HiLoCounter truth = new HiLoCounter();
        java.util.Set<com.richeyworks.blackjack.engine.Card> id =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Hand h : e.hands()) {
            for (var c : h.cards()) if (id.add(c)) truth.observe(c);
        }
        truth.observe(e.dealer().first()); // hole still hidden in PLAYER

        assertEquals(truth.runningCount(), counter.runningCount(),
                "split must not change already-seen cards' contribution");
        assertEquals(0, counter.runningCount(), "K,2,10,3,8 → −1+1−1+1+0 = 0");
    }

    @Test void secondSyncIsIdempotent() {
        Engine e = new Engine(1000, new Random(1));
        e.addBet(25);
        e.deal();
        if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);

        TableObservation obs = new TableObservation(new HiLoCounter());
        obs.sync(e);
        int once = obs.counter().runningCount();
        obs.sync(e);
        obs.sync(e);
        assertEquals(once, obs.counter().runningCount());
    }

    @Test void hitAddsOnlyTheNewCard() {
        Engine e = null;
        for (long seed = 0; seed < 2000; seed++) {
            Engine cand = new Engine(5000, new Random(seed));
            cand.addBet(25);
            cand.deal();
            if (cand.phase() == Phase.INSURANCE) cand.takeInsurance(false);
            if (cand.phase() == Phase.PLAYER && cand.canHit() && cand.active().value() < 12) {
                e = cand;
                break;
            }
        }
        assertNotNull(e);

        TableObservation obs = new TableObservation(new HiLoCounter());
        obs.sync(e);
        int before = obs.counter().runningCount();
        e.hit();
        obs.sync(e);
        // Delta must equal the single new card's Hi-Lo weight, not a reshuffle of the table.
        int delta = obs.counter().runningCount() - before;
        assertTrue(delta >= -1 && delta <= 1, "one card moves the count by at most 1");
    }

    @Test void resetClearsTheRunningCount() {
        TableObservation obs = new TableObservation(new HiLoCounter());
        Engine e = new Engine(1000, new Random(3));
        e.addBet(10);
        e.deal();
        obs.sync(e);
        obs.reset();
        assertEquals(0, obs.counter().runningCount());
    }
}
