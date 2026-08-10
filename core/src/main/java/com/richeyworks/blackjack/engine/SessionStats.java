package com.richeyworks.blackjack.engine;

/** Lifetime statistics for the current play session (or saved across launches). */
public final class SessionStats {
    /**
     * Rounds dealt (one per {@code deal()}). Not the same as resolved player
     * hands — a three-way split can add three wins against a single {@code hands}
     * tick. Prefer {@link #resolvedHands()} for rates.
     */
    public int hands;
    public int wins;
    public int losses;
    public int pushes;
    public int blackjacks;
    public int busts;
    public int doubles;
    public int splits;
    public int surrenders;
    public int peakBankroll = 0;
    public int totalWagered;
    public int totalReturned;

    /**
     * Player-hand win rate in {@code [0, 1]}.
     *
     * <p>Uses {@code wins / (wins + losses + pushes)} rather than {@code wins / hands}.
     * {@link #hands} counts deals; wins/losses/pushes count each split hand. A
     * three-way split of winners used to report 300% "win rate".
     */
    public double winRate() {
        int decided = resolvedHands();
        return decided == 0 ? 0.0 : (double) wins / decided;
    }

    /** Hands that received an outcome (wins + losses + pushes). */
    public int resolvedHands() {
        long sum = (long) wins + losses + pushes;
        return sum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }

    /** Net chip movement (returned minus wagered) without int wrap. */
    public int net() {
        long n = (long) totalReturned - totalWagered;
        if (n > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (n < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) n;
    }


    public void reset() {
        hands = wins = losses = pushes = blackjacks = busts
              = doubles = splits = surrenders
              = totalWagered = totalReturned = 0;
        peakBankroll = 0;
    }
}
