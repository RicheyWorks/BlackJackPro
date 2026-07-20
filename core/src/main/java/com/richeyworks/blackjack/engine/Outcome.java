package com.richeyworks.blackjack.engine;

/**
 * How a single hand finished, as recorded by the engine's settlement.
 *
 * <p>This is the authoritative result: {@link Engine#settle()} assigns one of
 * these to every hand at the same moment it moves the money, so a front end
 * that renders {@link Engine#lastOutcomes()} can never disagree with what the
 * player was actually paid. Front ends must not re-derive the result by
 * comparing hand values — that duplicate logic is what this type exists to
 * remove.
 */
public enum Outcome {

    /** Natural 21 on the first two cards; paid at the blackjack rate. */
    BLACKJACK("Blackjack!"),

    /** Beat the dealer (or the dealer busted); paid even money. */
    WIN("Win"),

    /** Tied the dealer; the stake was returned. */
    PUSH("Push"),

    /** Lost on total, with the hand still under 21. */
    LOSS("Loss"),

    /** Went over 21. */
    BUST("Bust"),

    /** Late surrender; half the stake was returned. */
    SURRENDER("Surrendered");

    private final String label;

    Outcome(String label) { this.label = label; }

    /** Short human-readable label suitable for a status bar. */
    public String label() { return label; }

    /** True for the two outcomes that paid more than the stake back. */
    public boolean isWin() { return this == WIN || this == BLACKJACK; }
}
