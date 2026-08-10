package com.richeyworks.blackjack.strategy;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.engine.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HiLoCounterTest {

    private static Card c(Rank r) { return new Card(r, Suit.SPADES); }

    @Test void lowCardsIncrementCount() {
        HiLoCounter ai = new HiLoCounter();
        for (Rank r : new Rank[]{Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX}) ai.observe(c(r));
        assertEquals(5, ai.runningCount());
        assertEquals(5, ai.cardsSeen());
    }

    @Test void highCardsDecrementCount() {
        HiLoCounter ai = new HiLoCounter();
        for (Rank r : new Rank[]{Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE}) ai.observe(c(r));
        assertEquals(-5, ai.runningCount());
    }

    @Test void neutralCardsDoNotChangeCount() {
        HiLoCounter ai = new HiLoCounter();
        for (Rank r : new Rank[]{Rank.SEVEN, Rank.EIGHT, Rank.NINE}) ai.observe(c(r));
        assertEquals(0, ai.runningCount());
        assertEquals(3, ai.cardsSeen());
    }

    @Test void trueCountDividesByDecksRemaining() {
        HiLoCounter ai = new HiLoCounter();
        for (int i = 0; i < 6; i++) ai.observe(c(Rank.FIVE));   // running +6
        assertEquals(6, ai.runningCount());
        assertEquals(3.0, ai.trueCount(2), 1e-9);               // 6 / 2 decks
        assertEquals(6.0, ai.trueCount(0), 1e-9);               // guard: decks<=0 -> running
    }

    @Test void trueCountUsesFractionalDecksFromCardCount() {
        HiLoCounter ai = new HiLoCounter();
        for (int i = 0; i < 20; i++) ai.observe(c(Rank.FIVE));  // +20
        // 80 cards left is ~1.54 decks. Integer division used to report TC=20.
        double decks = HiLoCounter.decksRemaining(80);
        assertEquals(80 / 52.0, decks, 1e-9);
        assertEquals(20.0 / (80 / 52.0), ai.trueCount(decks), 1e-9);
    }

    @Test void resetClearsCount() {
        HiLoCounter ai = new HiLoCounter();
        ai.observe(c(Rank.FIVE));
        ai.observe(c(Rank.KING));
        ai.resetCount();
        assertEquals(0, ai.runningCount());
        assertEquals(0, ai.cardsSeen());
    }
}
