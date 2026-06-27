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
        Shoe s = new Shoe(1, new Random(0), 0.5); // 50% penetration
        int total = s.remaining();
        assertFalse(s.needsShuffle());
        for (int i = 0; i < total / 2; i++) s.deal();
        assertTrue(s.needsShuffle(), "should need shuffle after 50% dealt");
    }
}
