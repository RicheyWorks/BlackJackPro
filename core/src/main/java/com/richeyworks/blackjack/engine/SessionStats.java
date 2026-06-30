package com.richeyworks.blackjack.engine;

/** Lifetime statistics for the current play session (or saved across launches). */
public final class SessionStats {
    public int hands;
    public int wins;
    public int losses;
    public int pushes;
    public int blackjacks;
    public int busts;
    public int doubles;
    public int splits;
    public int surrenders;
    public int peakBankroll = 1000;
    public int totalWagered;
    public int totalReturned;

    public double winRate() { return hands == 0 ? 0.0 : (double) wins / hands; }

    /** Net chip movement (returned minus wagered). */
    public int net() { return totalReturned - totalWagered; }

    public void reset() {
        hands = wins = losses = pushes = blackjacks = busts
              = doubles = splits = surrenders
              = totalWagered = totalReturned = 0;
        peakBankroll = 1000;
    }
}
