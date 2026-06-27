package com.richeyworks.blackjack.plugins.builtin;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.plugin.SideBet;

import java.util.List;

/**
 * 21+3 side bet. Evaluates the player's first two cards together with the
 * dealer's up-card as a three-card poker hand.
 *
 * Standard pay table (varies by casino — this is the most common):
 *   - Suited trips:   100:1
 *   - Straight flush:  40:1
 *   - Three of a kind: 30:1
 *   - Straight:        10:1
 *   - Flush:            5:1
 *   - otherwise lose
 */
public final class TwentyOnePlusThree implements SideBet {

    private String last = "";

    @Override public String displayName() { return "21+3"; }

    @Override public String lastOutcome() { return last; }

    @Override public List<String> payoutTable() {
        return List.of(
                "Suited Trips  -> 100:1",
                "Straight Flush -> 40:1",
                "Three of Kind -> 30:1",
                "Straight      -> 10:1",
                "Flush         ->  5:1"
        );
    }

    @Override
    public int settle(List<Card> playerCards, Card dealerUp, int bet) {
        if (playerCards == null || playerCards.size() < 2 || dealerUp == null) {
            last = "no eval"; return 0;
        }
        Card a = playerCards.get(0), b = playerCards.get(1), c = dealerUp;

        boolean suited     = a.suit() == b.suit() && b.suit() == c.suit();
        boolean threeKind  = a.rank() == b.rank() && b.rank() == c.rank();
        boolean straight   = isStraight(a, b, c);

        int multiplier;
        if (threeKind && suited)       { last = "Suited Trips";    multiplier = 100; }
        else if (straight && suited)   { last = "Straight Flush";  multiplier =  40; }
        else if (threeKind)            { last = "Three of a Kind"; multiplier =  30; }
        else if (straight)             { last = "Straight";        multiplier =  10; }
        else if (suited)               { last = "Flush";           multiplier =   5; }
        else                           { last = "no win";          return 0; }
        return bet + bet * multiplier;
    }

    private static boolean isStraight(Card a, Card b, Card c) {
        int[] r = { rankIndex(a.rank()), rankIndex(b.rank()), rankIndex(c.rank()) };
        java.util.Arrays.sort(r);
        // straight either consecutive, or the wheel (A,2,3)
        return (r[2] - r[1] == 1 && r[1] - r[0] == 1)
            || (r[0] == 0 && r[1] == 1 && r[2] == 12);   // 2,3,A
    }

    private static int rankIndex(Rank r) {
        switch (r) {
            case TWO:   return 0;
            case THREE: return 1;
            case FOUR:  return 2;
            case FIVE:  return 3;
            case SIX:   return 4;
            case SEVEN: return 5;
            case EIGHT: return 6;
            case NINE:  return 7;
            case TEN:   return 8;
            case JACK:  return 9;
            case QUEEN: return 10;
            case KING:  return 11;
            case ACE:   return 12;
            default: throw new IllegalStateException();
        }
    }
}
