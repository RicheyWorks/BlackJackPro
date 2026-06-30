package com.richeyworks.blackjack.engine;

/**
 * Ranks 2..A. {@link #value()} returns the blackjack value; aces start at
 * 11 and may be downgraded to 1 by {@link Hand#value()} as needed.
 */
public enum Rank {
    TWO  ("2",  2),
    THREE("3",  3),
    FOUR ("4",  4),
    FIVE ("5",  5),
    SIX  ("6",  6),
    SEVEN("7",  7),
    EIGHT("8",  8),
    NINE ("9",  9),
    TEN  ("10", 10),
    JACK ("J",  10),
    QUEEN("Q",  10),
    KING ("K",  10),
    ACE  ("A",  11);

    private final String label;
    private final int    value;

    Rank(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String label() { return label; }
    public int    value() { return value; }

    /** Parse the conventional one/two-character rank label. */
    public static Rank fromLabel(String s) {
        for (Rank r : values()) if (r.label.equalsIgnoreCase(s)) return r;
        throw new IllegalArgumentException("Unknown rank: " + s);
    }
}
