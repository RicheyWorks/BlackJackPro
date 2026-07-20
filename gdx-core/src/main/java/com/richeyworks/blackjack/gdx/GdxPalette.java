package com.richeyworks.blackjack.gdx;

import com.badlogic.gdx.graphics.Color;
import com.richeyworks.blackjack.table.TablePalette;

/**
 * A {@link TablePalette} turned into libGDX {@link Color}s, once.
 *
 * <p>The palette stores plain {@code 0xRRGGBB} ints so it can live in
 * {@code core} without dragging AWT onto Android. Converting on every frame
 * would allocate a handful of Colors per card per draw, so each theme is
 * converted once when it is selected and the objects reused.
 */
public final class GdxPalette {

    private final TablePalette source;

    public final Color feltTop;
    public final Color feltBottom;
    public final Color accent;
    public final Color cardFace;
    public final Color cardInk;
    public final Color cardRed;
    public final Color cardEdge;
    public final Color backFill;
    public final Color backLine;

    /** Readable text over the felt — derived, so every palette gets a legible HUD. */
    public final Color text;
    /** Active-hand glow, translucent so the cards stay visible beneath it. */
    public final Color highlight;
    /** Button face and its label, both derived from the felt and accent. */
    public final Color buttonFace;
    public final Color buttonText;
    /** Speech-bubble fill and ink. */
    public final Color bubbleFill;
    public final Color bubbleInk;
    public final Color bubbleName;

    public GdxPalette(TablePalette p) {
        this.source     = p;
        this.feltTop    = rgb(p.feltTop());
        this.feltBottom = rgb(p.feltBottom());
        this.accent     = rgb(p.accent());
        this.cardFace   = rgb(p.cardFace());
        this.cardInk    = rgb(p.cardInk());
        this.cardRed    = rgb(p.cardRed());
        this.cardEdge   = rgb(p.cardEdge());
        this.backFill   = rgb(p.backFill());
        this.backLine   = rgb(p.backLine());

        // The felt is the background for everything else, so the HUD colour is
        // derived from it rather than fixed: a light theme like Desert needs
        // dark text where Midnight needs light, and hardcoding one broke the
        // other.
        this.text       = TablePalette.luma(p.feltBottom()) > 140
                            ? rgb(0x1A1A1A) : rgb(0xF2F2F2);
        this.highlight  = new Color(accent.r, accent.g, accent.b, 0.28f);
        this.buttonFace = new Color(accent.r * 0.35f, accent.g * 0.35f, accent.b * 0.35f, 1f);
        this.buttonText = rgb(TablePalette.contrastOn(darken(p.accent(), 0.35f)));
        this.bubbleFill = rgb(p.cardFace());
        this.bubbleInk  = rgb(p.cardInk());
        this.bubbleName = rgb(p.cardEdge());
    }

    public TablePalette source()  { return source; }
    public String       id()      { return source.id(); }
    public String       name()    { return source.displayName(); }
    public TablePalette.BackStyle backStyle() { return source.backStyle(); }

    /** Chip face colours, constant across themes — see {@code Palettes}. */
    public static Color chip(int value) {
        return switch (value) {
            case 1   -> rgb(0xECECEC);
            case 5   -> rgb(0xC0392B);
            case 25  -> rgb(0x2E7D32);
            case 100 -> rgb(0x1B1B1B);
            case 500 -> rgb(0x6A1B9A);
            default  -> rgb(0xF1C40F);
        };
    }

    /** Black or white over a chip face, whichever stays legible. */
    public static Color chipInk(int value) {
        return switch (value) {
            case 100, 500, 5 -> Color.WHITE;
            default          -> Color.BLACK;
        };
    }

    private static Color rgb(int v) {
        return new Color(TablePalette.red(v)   / 255f,
                         TablePalette.green(v) / 255f,
                         TablePalette.blue(v)  / 255f, 1f);
    }

    private static int darken(int rgb, float factor) {
        int r = (int) (TablePalette.red(rgb)   * factor);
        int g = (int) (TablePalette.green(rgb) * factor);
        int b = (int) (TablePalette.blue(rgb)  * factor);
        return (r << 16) | (g << 8) | b;
    }
}
