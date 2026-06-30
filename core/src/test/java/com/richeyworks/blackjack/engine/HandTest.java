package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandTest {

    private static Card c(Rank r, Suit s) { return new Card(r, s); }

    @Test void emptyHandHasZeroValue() {
        Hand h = new Hand();
        assertEquals(0, h.value());
        assertFalse(h.isBust());
        assertFalse(h.isSoft());
    }

    @Test void hardSumWithoutAces() {
        Hand h = new Hand();
        h.add(c(Rank.SEVEN, Suit.SPADES));
        h.add(c(Rank.NINE,  Suit.HEARTS));
        assertEquals(16, h.value());
        assertFalse(h.isSoft());
    }

    @Test void softAceCountsAs11() {
        Hand h = new Hand();
        h.add(c(Rank.ACE,   Suit.SPADES));
        h.add(c(Rank.SIX,   Suit.HEARTS));
        assertEquals(17, h.value());
        assertTrue(h.isSoft());
    }

    @Test void aceDemotesWhenBustWouldOccur() {
        Hand h = new Hand();
        h.add(c(Rank.ACE,   Suit.SPADES));
        h.add(c(Rank.SIX,   Suit.HEARTS));
        h.add(c(Rank.TEN,   Suit.CLUBS));
        assertEquals(17, h.value(), "ace must demote from 11 to 1");
        assertFalse(h.isSoft(), "no soft ace remaining");
        assertFalse(h.isBust());
    }

    @Test void doubleAceStartsAsSoft12() {
        Hand h = new Hand();
        h.add(c(Rank.ACE, Suit.SPADES));
        h.add(c(Rank.ACE, Suit.HEARTS));
        assertEquals(12, h.value());
        assertTrue(h.isSoft());
        assertTrue(h.isPair());
    }

    @Test void naturalBlackjackDetected() {
        Hand h = new Hand();
        h.add(c(Rank.ACE,   Suit.SPADES));
        h.add(c(Rank.KING,  Suit.HEARTS));
        assertEquals(21, h.value());
        assertTrue(h.isBlackjack());
    }

    @Test void split21IsNotBlackjack() {
        Hand h = new Hand();
        h.add(c(Rank.ACE,   Suit.SPADES));
        h.add(c(Rank.KING,  Suit.HEARTS));
        h.markFromSplit();
        assertTrue(h.value() == 21);
        assertFalse(h.isBlackjack(), "21 after split must not pay as natural BJ");
    }

    @Test void pairDetectionUsesValueNotRank() {
        Hand h = new Hand();
        h.add(c(Rank.JACK,  Suit.SPADES));
        h.add(c(Rank.KING,  Suit.HEARTS));
        assertTrue(h.isPair(), "10/J/Q/K are all 10-value and should be splittable as a pair");
    }

    @Test void bustOver21() {
        Hand h = new Hand();
        h.add(c(Rank.TEN,   Suit.SPADES));
        h.add(c(Rank.NINE,  Suit.HEARTS));
        h.add(c(Rank.FIVE,  Suit.CLUBS));
        assertEquals(24, h.value());
        assertTrue(h.isBust());
    }
}
