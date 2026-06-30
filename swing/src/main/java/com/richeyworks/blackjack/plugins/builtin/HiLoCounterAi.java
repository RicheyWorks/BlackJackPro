package com.richeyworks.blackjack.plugins.builtin;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.plugin.AiPlugin;

/**
 * Demonstration AI that maintains a Hi-Lo running count from cards it has
 * observed and uses the true count to deviate from basic strategy hit/stand
 * thresholds. It also bets bigger when the count is positive.
 *
 * Counting is per-instance and informational only — the engine has its own
 * built-in counter on the shoe, but this plug-in keeps its own to demonstrate
 * how a plugin would track game state.
 */
public final class HiLoCounterAi implements AiPlugin {

    private int runningCount;
    private int cardsSeen;

    @Override public String displayName() { return "Hi-Lo Counter"; }

    /** Update the running count whenever the AI observes a card on the table. */
    public void observe(Card c) {
        cardsSeen++;
        Rank r = c.rank();
        if (r == Rank.TWO || r == Rank.THREE || r == Rank.FOUR
         || r == Rank.FIVE || r == Rank.SIX)        runningCount++;
        else if (r == Rank.TEN || r == Rank.JACK
              || r == Rank.QUEEN || r == Rank.KING
              || r == Rank.ACE)                     runningCount--;
    }

    public double trueCount(int decksRemaining) {
        return decksRemaining <= 0 ? runningCount : (double) runningCount / decksRemaining;
    }

    @Override
    public boolean shouldHit(Engine engine, Hand hand) {
        // standard threshold 17, biased by count
        double tc = trueCount(Math.max(1, engine.shoe().remaining() / 52));
        int threshold;
        if (tc >= 3)   threshold = 18;
        else if (tc <= -2) threshold = 14;
        else            threshold = 17;
        return hand.value() < threshold;
    }

    @Override
    public int chooseBet(Engine engine, int bankroll) {
        if (bankroll <= 0) return 0;
        double tc = trueCount(Math.max(1, engine.shoe().remaining() / 52));
        int units;
        if      (tc >= 4) units = 8;
        else if (tc >= 3) units = 4;
        else if (tc >= 2) units = 2;
        else              units = 1;
        return Math.min(bankroll, units * 5);
    }

    public int  runningCount() { return runningCount; }
    public int  cardsSeen()    { return cardsSeen; }
    public void resetCount()   { runningCount = 0; cardsSeen = 0; }
}
