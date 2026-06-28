package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicStrategyTest {

    private static Card c(Rank r) { return new Card(r, Suit.SPADES); }
    private static Hand hand(Rank... ranks) {
        Hand h = new Hand();
        for (Rank r : ranks) h.add(c(r));
        return h;
    }

    @Test void hardSeventeenAlwaysStand() {
        for (Rank up : Rank.values()) {
            assertEquals(BasicStrategy.Action.S,
                    BasicStrategy.recommend(hand(Rank.TEN, Rank.SEVEN), c(up)),
                    "hard 17 vs " + up + " should stand");
        }
    }

    @Test void hardEleven_doubleVsAnything() {
        for (Rank up : Rank.values()) {
            assertEquals(BasicStrategy.Action.D,
                    BasicStrategy.recommend(hand(Rank.SIX, Rank.FIVE), c(up)),
                    "hard 11 vs " + up + " should double");
        }
    }

    @Test void twelveStandsAgainst4to6_hitsOtherwise() {
        // Hard 12 = 9+3 to avoid pair detection
        assertEquals(BasicStrategy.Action.H, BasicStrategy.recommend(hand(Rank.NINE, Rank.THREE), c(Rank.TWO)));
        assertEquals(BasicStrategy.Action.H, BasicStrategy.recommend(hand(Rank.NINE, Rank.THREE), c(Rank.THREE)));
        assertEquals(BasicStrategy.Action.S, BasicStrategy.recommend(hand(Rank.NINE, Rank.THREE), c(Rank.FOUR)));
        assertEquals(BasicStrategy.Action.S, BasicStrategy.recommend(hand(Rank.NINE, Rank.THREE), c(Rank.FIVE)));
        assertEquals(BasicStrategy.Action.S, BasicStrategy.recommend(hand(Rank.NINE, Rank.THREE), c(Rank.SIX)));
        assertEquals(BasicStrategy.Action.H, BasicStrategy.recommend(hand(Rank.NINE, Rank.THREE), c(Rank.SEVEN)));
    }

    @Test void acesAlwaysSplit() {
        for (Rank up : Rank.values()) {
            assertEquals(BasicStrategy.Action.P,
                    BasicStrategy.recommend(hand(Rank.ACE, Rank.ACE), c(up)),
                    "A,A vs " + up + " should split");
        }
    }

    @Test void tensNeverSplit() {
        for (Rank up : Rank.values()) {
            assertEquals(BasicStrategy.Action.S,
                    BasicStrategy.recommend(hand(Rank.TEN, Rank.TEN), c(up)),
                    "10,10 vs " + up + " should stand");
        }
    }

    @Test void eightsAlwaysSplit() {
        for (Rank up : Rank.values()) {
            assertEquals(BasicStrategy.Action.P,
                    BasicStrategy.recommend(hand(Rank.EIGHT, Rank.EIGHT), c(up)),
                    "8,8 vs " + up + " should split");
        }
    }

    @Test void soft18BehaviorAcrossUpcards() {
        // A,7 = soft 18.  D vs 3-6, S vs 2/7/8, H vs 9/10/A.
        assertEquals(BasicStrategy.Action.S, BasicStrategy.recommend(hand(Rank.ACE, Rank.SEVEN), c(Rank.TWO)));
        assertEquals(BasicStrategy.Action.D, BasicStrategy.recommend(hand(Rank.ACE, Rank.SEVEN), c(Rank.FIVE)));
        assertEquals(BasicStrategy.Action.S, BasicStrategy.recommend(hand(Rank.ACE, Rank.SEVEN), c(Rank.EIGHT)));
        assertEquals(BasicStrategy.Action.H, BasicStrategy.recommend(hand(Rank.ACE, Rank.SEVEN), c(Rank.NINE)));
        assertEquals(BasicStrategy.Action.H, BasicStrategy.recommend(hand(Rank.ACE, Rank.SEVEN), c(Rank.ACE)));
    }

    @Test void soft17DoublesVs3to6_hitsVs2() {
        assertEquals(BasicStrategy.Action.H, BasicStrategy.recommend(hand(Rank.ACE, Rank.SIX), c(Rank.TWO)), "soft 17 vs 2 should hit");
        assertEquals(BasicStrategy.Action.D, BasicStrategy.recommend(hand(Rank.ACE, Rank.SIX), c(Rank.THREE)));
        assertEquals(BasicStrategy.Action.D, BasicStrategy.recommend(hand(Rank.ACE, Rank.SIX), c(Rank.SIX)));
        assertEquals(BasicStrategy.Action.H, BasicStrategy.recommend(hand(Rank.ACE, Rank.SIX), c(Rank.SEVEN)));
    }
}
