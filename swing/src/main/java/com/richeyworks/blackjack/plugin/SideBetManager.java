package com.richeyworks.blackjack.plugin;

import com.richeyworks.blackjack.engine.Card;

import java.util.List;

/**
 * Owns the player's pending side-bet stake for one round and resolves it
 * against the opening cards. Deliberately free of any Swing dependency so the
 * money flow can be unit-tested directly. The UI moves chips in and out of the
 * bankroll using the amounts this class reports.
 */
public final class SideBetManager {

    private final SideBet bet;          // null when no side-bet plugin is loaded
    private int    pending;
    private int    lastPayout;
    private String lastOutcome = "";

    public SideBetManager(SideBet bet) { this.bet = bet; }

    public boolean available()   { return bet != null; }
    public int     pending()     { return pending; }
    public String  name()        { return bet == null ? "" : bet.displayName(); }
    public int     lastPayout()  { return lastPayout; }
    public String  lastOutcome() { return lastOutcome; }

    /**
     * Add to the pending side bet if it is affordable.
     * @return the amount actually added (0 if rejected); the caller deducts this
     *         from the bankroll.
     */
    public int add(int amount, int bankroll) {
        if (bet == null || amount <= 0 || amount > bankroll) return 0;
        pending += amount;
        return amount;
    }

    /** Cancel the pending bet. @return the amount to refund to the bankroll. */
    public int clear() {
        int refund = pending;
        pending = 0;
        return refund;
    }

    /**
     * Resolve the pending bet against the opening cards and reset it.
     * @return total returned to the player (0 = lost; &gt; stake = win incl. stake);
     *         the caller adds this to the bankroll. Returns 0 if nothing was staked.
     */
    public int resolve(List<Card> playerCards, Card dealerUp) {
        if (bet == null || pending <= 0) { lastPayout = 0; lastOutcome = ""; return 0; }
        int stake = pending;
        pending = 0;
        int returned = bet.settle(playerCards, dealerUp, stake);
        lastPayout  = returned;
        lastOutcome = bet.lastOutcome();
        return returned;
    }
}
