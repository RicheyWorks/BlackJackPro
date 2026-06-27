package com.richeyworks.blackjack.plugin;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Hand;

import java.util.List;

/**
 * Optional side wagers (21+3, Perfect Pairs, Lucky Ladies, ...). Evaluated at
 * the end of each round against the player's initial two cards and the dealer
 * up-card. The engine deducts the bet amount and applies the returned payout.
 */
public interface SideBet {

    /** Display name shown next to the bet slot. */
    String displayName();

    /** True if this side bet can be placed in the current round. */
    default boolean isAvailable() { return true; }

    /**
     * Settle the side bet.
     *
     * @param playerCards the player's first two cards
     * @param dealerUp    the dealer's up-card
     * @param bet         the amount wagered
     * @return total returned to the player (0 = lost, bet = push, &gt;bet = win incl. stake)
     */
    int settle(List<Card> playerCards, Card dealerUp, int bet);

    /** Optional resolution-time description ("Suited Trips", "Mixed", etc.). */
    default String lastOutcome() { return ""; }

    /** Optional payouts table, for the help UI. Each entry is "Outcome -> N:1". */
    default List<String> payoutTable() { return List.of(); }
}
