package com.richeyworks.blackjack.engine;

/**
 * The four standard suits, with platform-neutral rendering metadata.
 * Kept free of java.awt so the core module can run on Android (via gdx-core).
 */
public enum Suit {
    SPADES  ("\u2660", 0x000000, false),
    HEARTS  ("\u2665", 0xC0392B, true),
    DIAMONDS("\u2666", 0xC0392B, true),
    CLUBS   ("\u2663", 0x000000, false);

    private final String  glyph;
    private final int     rgb;
    private final boolean red;

    Suit(String glyph, int rgb, boolean red) {
        this.glyph = glyph;
        this.rgb   = rgb;
        this.red   = red;
    }

    public String  glyph() { return glyph; }
    public int     rgb()   { return rgb; }
    public boolean isRed() { return red; }
}
