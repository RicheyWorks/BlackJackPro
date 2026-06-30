package com.richeyworks.blackjack.plugins.builtin;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.engine.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HiLoCounterAiTest {

    private static Card c(Rank r) { return new Card(r, Suit.SPADES); }

    @Test void lowCardsIncrementCount() {
        HiLoCounterAi ai = new HiLoCounterAi();
        for (Rank r : new Rank[]{Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX}) ai.observe(c(r));
        assertEquals(5, ai.runningCount());
        assertEquals(5, ai.cardsSeen());
    }

    @Test void highCardsDecrementCount() {
        HiLoCounterAi ai = new HiLoCounterAi();
        for (Rank r : new Rank[]{Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE}) ai.observe(c(r));
        assertEquals(-5, ai.runningCount());
    }

    @Test void neutralCardsDoNotChangeCount() {
        HiLoCounterAi ai = new HiLoCounterAi();
        for (Rank r : new Rank[]{Rank.SEVEN, Rank.EIGHT, Rank.NINE}) ai.observe(c(r));
        assertEquals(0, ai.runningCount());
        assertEquals(3, ai.cardsSeen());
    }

    @Test void trueCountDividesByDecksRemaining() {
        HiLoCounterAi ai = new HiLoCounterAi();
        for (int i = 0; i < 6; i++) ai.observe(c(Rank.FIVE));   // running +6
        assertEquals(6, ai.runningCount());
        assertEquals(3.0, ai.trueCount(2), 1e-9);               // 6 / 2 decks
        assertEquals(6.0, ai.trueCount(0), 1e-9);               // guard: decks<=0 -> running
    }

    @Test void resetClearsCount() {
        HiLoCounterAi ai = new HiLoCounterAi();
        ai.observe(c(Rank.FIVE));
        ai.observe(c(Rank.KING));
        ai.resetCount();
        assertEquals(0, ai.runningCount());
        assertEquals(0, ai.cardsSeen());
    }
}
