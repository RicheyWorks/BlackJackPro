package com.richeyworks.blackjack.engine;

/** Tunable rules / payouts. Mutable so plugins or settings can override defaults. */
public final class BlackjackRules {

    public int     decks               = 6;
    public double  penetration         = 0.75;   // % of shoe dealt before reshuffle
    public boolean dealerHitsSoft17    = false;
    public boolean lateSurrender       = true;
    public boolean offerInsurance      = true;
    public int     maxSplits           = 3;       // up to 4 hands total
    public boolean doubleAfterSplit    = true;
    public boolean splitAcesOneCard    = true;

    /** Natural blackjack payout numerator/denominator. 3:2 default. */
    public int blackjackPayoutNum = 3;
    public int blackjackPayoutDen = 2;

    /** Insurance payout. Always 2:1 in standard rules. */
    public int insurancePayoutNum = 2;
    public int insurancePayoutDen = 1;

    /*
     * Rounding policy: the game pays in whole dollars, so any ratio that lands
     * on a fraction has to round somewhere. Every rounding here goes the
     * player's way — money owed to the player rounds up, money taken from the
     * player rounds down.
     *
     * This follows the house: casinos vary, but where a half-dollar cannot be
     * paid, rounding up is the normal outcome. It also avoids a systematic edge
     * the player never agreed to. Flooring the 3:2 payout shorted every
     * odd-dollar bet by exactly $0.50 — and with 1/5/25/100/500 chips that is
     * the common case, not an edge case: a single $5 chip, a single $25 chip,
     * or any odd number of $5 chips all landed on it.
     */

    /**
     * Blackjack winnings for a bet, excluding the returned stake.
     * Rounds up: a $25 natural pays 38, not 37 (true value 37.50).
     */
    public int blackjackPayout(int bet) {
        if (bet < 0) return 0;
        return payUp(bet, blackjackPayoutNum, blackjackPayoutDen);
    }

    /** Insurance winnings, excluding the returned premium. 2:1 is always exact. */
    public int insurancePayout(int insuranceBet) {
        if (insuranceBet < 0) return 0;
        return payUp(insuranceBet, insurancePayoutNum, insurancePayoutDen);
    }

    /**
     * The insurance premium for a main bet — half the stake, rounded <em>down</em>.
     * This is money the player hands over, so the rounding favours them here by
     * charging less. Centralised because the engine both offers and collects it.
     */
    public int insurancePremium(int bet) {
        if (bet <= 0) return 0;
        return bet / 2;
    }

    /**
     * What late surrender returns — half the stake, rounded <em>up</em>.
     * A $25 surrender returns 13, not 12 (true value 12.50): this is money
     * coming back to the player, so it rounds their way like the payouts.
     */
    public int surrenderRefund(int bet) {
        if (bet <= 0) return 0;
        return payUp(bet, 1, 2);
    }


    /**
     * {@code ceil(amount * num / den)} computed in {@code long} so a large bet
     * cannot overflow the multiplication before the division brings it back
     * into range. Saturates at {@link Integer#MAX_VALUE} so a cast never wraps
     * to a negative winnings figure (which used to make {@code credit} skip the
     * payout entirely on a natural above ~1.43e9).
     */
    private static int payUp(int amount, int num, int den) {
        if (amount <= 0 || num < 0) return 0;
        if (den <= 0) {
            throw new IllegalStateException("payout denominator must be positive, was " + den);
        }
        long raw = ((long) amount * num + den - 1) / den;
        if (raw > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) raw;
    }

}
