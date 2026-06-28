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

    /** Compute blackjack winnings (excluding stake return) for a given bet.
     *  Standard floor of bet * num / den: a 3:2 natural pays the largest whole
     *  chip amount not exceeding the true payout (bet 5 -> 7, 25 -> 37, 100 -> 150).
     *  The previous formula floored the bet to the denominator first, which
     *  underpaid every odd bet by up to one chip and paid 0 on a bet of 1. */
    public int blackjackPayout(int bet) {
        return bet * blackjackPayoutNum / blackjackPayoutDen;
    }

    public int insurancePayout(int insuranceBet) {
        return insuranceBet * insurancePayoutNum / insurancePayoutDen;
    }
}
