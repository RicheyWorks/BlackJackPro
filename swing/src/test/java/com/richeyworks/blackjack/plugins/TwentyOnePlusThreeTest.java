package com.richeyworks.blackjack.plugins;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.engine.Suit;
import com.richeyworks.blackjack.plugins.builtin.TwentyOnePlusThree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TwentyOnePlusThreeTest {

    private TwentyOnePlusThree bet;

    @BeforeEach void setUp() { bet = new TwentyOnePlusThree(); }

    private static Card c(Rank r, Suit s) { return new Card(r, s); }

    @Test void suitedTrips_pays100to1() {
        int r = bet.settle(
                List.of(c(Rank.SEVEN, Suit.SPADES), c(Rank.SEVEN, Suit.SPADES)),
                c(Rank.SEVEN, Suit.SPADES),
                10);
        assertEquals(10 + 1000, r, "suited trips should pay 100:1 + stake");
        assertEquals("Suited Trips", bet.lastOutcome());
    }

    @Test void straightFlush_pays40to1() {
        int r = bet.settle(
                List.of(c(Rank.FIVE, Suit.HEARTS), c(Rank.SIX, Suit.HEARTS)),
                c(Rank.SEVEN, Suit.HEARTS),
                10);
        assertEquals(10 + 400, r);
        assertEquals("Straight Flush", bet.lastOutcome());
    }

    @Test void threeOfAKind_pays30to1() {
        int r = bet.settle(
                List.of(c(Rank.KING, Suit.SPADES), c(Rank.KING, Suit.HEARTS)),
                c(Rank.KING, Suit.CLUBS),
                10);
        assertEquals(10 + 300, r);
        assertEquals("Three of a Kind", bet.lastOutcome());
    }

    @Test void straight_pays10to1() {
        int r = bet.settle(
                List.of(c(Rank.FIVE, Suit.SPADES), c(Rank.SIX, Suit.HEARTS)),
                c(Rank.SEVEN, Suit.CLUBS),
                10);
        assertEquals(10 + 100, r);
        assertEquals("Straight", bet.lastOutcome());
    }

    @Test void flush_pays5to1() {
        int r = bet.settle(
                List.of(c(Rank.TWO, Suit.HEARTS), c(Rank.EIGHT, Suit.HEARTS)),
                c(Rank.JACK, Suit.HEARTS),
                10);
        assertEquals(10 + 50, r);
        assertEquals("Flush", bet.lastOutcome());
    }

    @Test void losingHand_returnsZero() {
        int r = bet.settle(
                List.of(c(Rank.TWO, Suit.HEARTS), c(Rank.EIGHT, Suit.SPADES)),
                c(Rank.JACK, Suit.CLUBS),
                10);
        assertEquals(0, r);
    }

    @Test void wheelStraightDetected() {
        // A,2,3 is a valid straight in 21+3 (the wheel)
        int r = bet.settle(
                List.of(c(Rank.ACE, Suit.SPADES), c(Rank.TWO, Suit.HEARTS)),
                c(Rank.THREE, Suit.CLUBS),
                10);
        assertEquals(10 + 100, r);
    }
}
