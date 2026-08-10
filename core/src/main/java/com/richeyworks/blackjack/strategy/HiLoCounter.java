package com.richeyworks.blackjack.strategy;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.engine.Rank;

/**
 * Maintains a Hi-Lo running count from the cards it has been shown.
 *
 * <p>Lives in {@code core} rather than the Swing plugin package because
 * counting cards is arithmetic, not user interface — it was only ever in
 * {@code swing/} by accident of history, which is why the mobile build had no
 * counter despite the code being entirely platform-neutral.
 *
 * <p>Informational: the running and true counts are displayed, and the
 * shouldHit/chooseBet helpers exist for AI plugins that want them. Nothing here
 * changes how the engine deals.
 */
public final class HiLoCounter {

    private int runningCount;
    private int cardsSeen;

    public String displayName() { return "Hi-Lo Counter"; }

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

    /**
     * True count = running / decks remaining.
     *
     * <p>Accepts a fractional deck count. Callers used to pass
     * {@code remaining / 52} as an {@code int}, which maps 53–103 cards all to
     * "1 deck" and inflates the true count by up to ~2× mid-shoe.
     */
    public double trueCount(double decksRemaining) {
        if (decksRemaining <= 0) return runningCount;
        return runningCount / decksRemaining;
    }

    /** Decks still in the shoe, never below a single card's fraction. */
    public static double decksRemaining(int cardsRemaining) {
        return Math.max(cardsRemaining / 52.0, 1.0 / 52.0);
    }

    public boolean shouldHit(Engine engine, Hand hand) {
        // standard threshold 17, biased by count
        double tc = trueCount(decksRemaining(engine.shoe().remaining()));
        int threshold;
        if (tc >= 3)   threshold = 18;
        else if (tc <= -2) threshold = 14;
        else            threshold = 17;
        return hand.value() < threshold;
    }

    public int chooseBet(Engine engine, int bankroll) {
        if (bankroll <= 0) return 0;
        double tc = trueCount(decksRemaining(engine.shoe().remaining()));
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
