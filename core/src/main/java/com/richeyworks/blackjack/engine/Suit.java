package com.richeyworks.blackjack.engine;

/**
 * The four standard suits, with platform-neutral rendering metadata.
 * Kept free of {@code java.awt} so the core module can run on Android (via
 * gdx-core), which has no AWT. UI layers turn {@link #rgb()} into their own
 * colour type (e.g. {@code new java.awt.Color(suit.rgb())} on desktop).
 */
public enum Suit {
    SPADES  ("♠", 0x000000, false),
    HEARTS  ("♥", 0xC0392B, true),
    DIAMONDS("♦", 0xC0392B, true),
    CLUBS   ("♣", 0x000000, false);

    private final String  glyph;
    private final int     rgb;
    private final boolean red;

    Suit(String glyph, int rgb, boolean red) {
        this.glyph = glyph;
        this.rgb   = rgb;
        this.red   = red;
    }

    public String  glyph() { return glyph; }

    /** 0xRRGGBB colour for the suit, platform-neutral (no java.awt dependency). */
    public int     rgb()   { return rgb; }

    /** True for hearts and diamonds. */
    public boolean isRed() { return red; }
}
