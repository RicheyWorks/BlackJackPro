package com.richeyworks.blackjack.sidebet;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.engine.Suit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwentyOnePlusThreeOverflowTest {

    @Test
    void suitedTripsDoesNotWrapNegativeOnLargeStake() {
        TwentyOnePlusThree bet = new TwentyOnePlusThree();
        // bet * 101 would overflow int above ~21.2M
        int stake = 30_000_000;
        int r = bet.settle(
                List.of(new Card(Rank.SEVEN, Suit.SPADES), new Card(Rank.SEVEN, Suit.SPADES)),
                new Card(Rank.SEVEN, Suit.SPADES),
                stake);
        assertTrue(r > 0, "payout must stay non-negative, was " + r);
        assertEquals(Integer.MAX_VALUE, r, "saturates at int max rather than wrapping");
    }

    @Test
    void sideBetManagerRefusesOverflowingPending() {
        SideBetManager m = new SideBetManager(new TwentyOnePlusThree());
        int added = m.add(SideBetManager.MAX_PENDING, Integer.MAX_VALUE);
        assertEquals(SideBetManager.MAX_PENDING, added);
        assertEquals(0, m.add(1, Integer.MAX_VALUE), "must refuse further stake past cap");
    }
}
