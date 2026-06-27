package com.richeyworks.blackjack.engine;

import java.awt.Color;

/** The four standard suits, with rendering metadata kept off the hot path. */
public enum Suit {
    SPADES  ("♠", Color.BLACK),
    HEARTS  ("♥", new Color(0xC0392B)),
    DIAMONDS("♦", new Color(0xC0392B)),
    CLUBS   ("♣", Color.BLACK);

    private final String glyph;
    private final Color color;

    Suit(String glyph, Color color) {
        this.glyph = glyph;
        this.color = color;
    }

    public String glyph() { return glyph; }
    public Color  color() { return color; }
}
