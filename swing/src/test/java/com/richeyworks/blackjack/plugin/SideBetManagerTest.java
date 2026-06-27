package com.richeyworks.blackjack.plugin;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.engine.Suit;
import com.richeyworks.blackjack.plugins.builtin.TwentyOnePlusThree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SideBetManagerTest {

    private static Card c(Rank r, Suit s) { return new Card(r, s); }
    private static SideBetManager withPlugin() { return new SideBetManager(new TwentyOnePlusThree()); }

    @Test void unavailableWhenNoPlugin() {
        SideBetManager m = new SideBetManager(null);
        assertFalse(m.available());
        assertEquals(0, m.add(50, 1000), "no plugin => nothing staked");
        assertEquals(0, m.resolve(
                List.of(c(Rank.SEVEN, Suit.SPADES), c(Rank.SEVEN, Suit.SPADES)),
                c(Rank.SEVEN, Suit.SPADES)));
    }

    @Test void addRespectsBankroll() {
        SideBetManager m = withPlugin();
        assertEquals(25, m.add(25, 100));
        assertEquals(25, m.pending());
        assertEquals(0, m.add(200, 75), "cannot stake more than the bankroll");
        assertEquals(25, m.pending());
    }

    @Test void clearRefundsPending() {
        SideBetManager m = withPlugin();
        m.add(40, 1000);
        assertEquals(40, m.clear());
        assertEquals(0, m.pending());
    }

    @Test void resolveWinPaysAndResets() {
        SideBetManager m = withPlugin();
        m.add(10, 1000);
        int payout = m.resolve(
                List.of(c(Rank.SEVEN, Suit.SPADES), c(Rank.SEVEN, Suit.SPADES)),
                c(Rank.SEVEN, Suit.SPADES));          // suited trips => 100:1
        assertEquals(10 + 1000, payout);
        assertEquals("Suited Trips", m.lastOutcome());
        assertEquals(0, m.pending(), "stake is consumed on resolve");
    }

    @Test void resolveLossReturnsZero() {
        SideBetManager m = withPlugin();
        m.add(10, 1000);
        int payout = m.resolve(
                List.of(c(Rank.TWO, Suit.HEARTS), c(Rank.EIGHT, Suit.SPADES)),
                c(Rank.JACK, Suit.CLUBS));            // nothing
        assertEquals(0, payout);
        assertEquals(0, m.pending());
    }

    @Test void resolveWithNoStakeIsNoOp() {
        SideBetManager m = withPlugin();
        assertEquals(0, m.resolve(
                List.of(c(Rank.ACE, Suit.SPADES), c(Rank.KING, Suit.SPADES)),
                c(Rank.QUEEN, Suit.SPADES)));
    }
}
