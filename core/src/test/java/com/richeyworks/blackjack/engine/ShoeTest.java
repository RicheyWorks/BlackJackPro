package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ShoeTest {

    @Test void containsExactly52TimesDecks() {
        Shoe s = new Shoe(6, new Random(42));
        assertEquals(6 * 52, s.remaining());
    }

    @Test void everyRankAppearsCorrectCount() {
        Shoe s = new Shoe(6, new Random(7));
        Map<Rank, Integer> counts = new HashMap<>();
        while (s.remaining() > 0) {
            Card c = s.deal();
            counts.merge(c.rank(), 1, Integer::sum);
        }
        // 6 decks * 4 suits = 24 of each rank
        for (Rank r : Rank.values()) {
            assertEquals(24, counts.get(r), "expected 24 of " + r);
        }
    }

    @Test void shoeShufflesDifferently() {
        Shoe a = new Shoe(1, new Random(1));
        Shoe b = new Shoe(1, new Random(2));
        // We don't need a deep diff — just that the top card differs frequently.
        // For determinism we use fixed seeds.
        assertNotEquals(a.deal(), b.deal(),
                "different seeds should produce different top cards");
    }

    @Test void needsShuffleTripsPastCut() {
        Shoe s = new Shoe(6, new Random(0), 0.5); // 50% of 312 = cut at 156
        int total = s.remaining();
        assertFalse(s.needsShuffle());
        for (int i = 0; i < total / 2; i++) s.deal();
        assertTrue(s.needsShuffle(), "should need shuffle after 50% dealt");
    }

    @Test void needsShuffleWhenTooFewCardsRemainForASafeRound() {
        // Penetration alone can leave ~8 cards on a 1-deck shoe — not enough
        // for multi-split play. The floor must force a reshuffle first.
        Shoe s = new Shoe(1, new Random(0), 0.99);
        while (!s.needsShuffle()) s.deal();
        assertTrue(s.remaining() <= Shoe.MIN_CARDS_FOR_ROUND,
                "remaining=" + s.remaining() + " should be at the safety floor");
        assertTrue(s.needsShuffle());
    }

    /**
     * Regression: multi-split near the cut used to empty the shoe mid-hand,
     * triggering a silent reshuffle inside {@link Shoe#deal()} and mixing two
     * shoes into one running count.
     */
    @Test void aggressivePlayNearCutNeverReshufflesMidHand() {
        BlackjackRules r = new BlackjackRules();
        r.decks = 1;
        r.penetration = 0.85;
        r.maxSplits = 3;
        r.splitAcesOneCard = false;

        int midHandReshuffles = 0;
        for (long seed = 0; seed < 5000; seed++) {
            Engine e = new Engine(100_000, new Random(seed), r);
            int guard = 0;
            while (e.shoe().remaining() > Shoe.MIN_CARDS_FOR_ROUND + 5 && guard++ < 80) {
                if (e.bankroll() < 50) e.setBankroll(100_000);
                e.addBet(5);
                e.deal();
                if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
                int g = 0;
                while (e.phase() == Phase.PLAYER && g++ < 50) {
                    int rem = e.shoe().remaining();
                    if (e.canSplit()) e.split();
                    else if (e.canHit() && e.active().value() < 17) e.hit();
                    else if (e.canStand()) e.stand();
                    else if (e.canHit()) e.hit();
                    else break;
                    if (e.shoe().remaining() > rem) midHandReshuffles++;
                }
            }
            // One more aggressive round right at the floor.
            if (e.bankroll() < 50) e.setBankroll(100_000);
            if (!e.canBet(5)) continue;
            e.addBet(5);
            e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            int g = 0;
            while (e.phase() == Phase.PLAYER && g++ < 50) {
                int rem = e.shoe().remaining();
                if (e.canSplit()) e.split();
                else if (e.canHit()) e.hit();
                else if (e.canStand()) e.stand();
                else break;
                if (e.shoe().remaining() > rem) midHandReshuffles++;
            }
        }
        assertEquals(0, midHandReshuffles,
                "a round must never auto-reshuffle the shoe mid-hand");
    }
}
