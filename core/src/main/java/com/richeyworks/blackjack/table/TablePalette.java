package com.richeyworks.blackjack.table;

/**
 * The colours and card-back style that define one table look, as plain data.
 *
 * <h2>Why the colours are ints</h2>
 * Themes started life in the Swing module built on {@code java.awt.Color}, which
 * meant the libGDX and Android builds could not use them at all — Android has no
 * AWT, so the whole package would fail to load. Keeping a palette as integer
 * {@code 0xRRGGBB} values lets it live in {@code core}, which is deliberately
 * AWT-free, and lets each front end turn them into whatever colour type it
 * actually draws with. {@link com.richeyworks.blackjack.engine.Suit} already set
 * this precedent for the same reason.
 *
 * <p>A palette carries no rendering code. Swing draws it with {@code Graphics2D}
 * and libGDX with a {@code ShapeRenderer}, so the two look like the same theme
 * without sharing a line of drawing logic — and neither can quietly drift from
 * the other, because the colours have exactly one definition.
 */
public final class TablePalette {

    /**
     * Card-back decoration. Varying this alongside the colours is what stops a
     * set of recoloured palettes reading as the same theme several times over.
     */
    public enum BackStyle {
        /** Diagonal cross-hatch. Traditional. */
        HATCH,
        /** Concentric rounded rectangles. Calm, modern. */
        RINGS,
        /** Vertical pinstripes. Formal. */
        STRIPES,
        /** Radiating chevrons from the centre. Art-deco. */
        CHEVRON,
        /** A single bordered panel. Minimal. */
        PLAIN
    }

    private final String    id;
    private final String    displayName;
    private final int       feltTop;
    private final int       feltBottom;
    private final int       accent;
    private final int       cardFace;
    private final int       cardInk;
    private final int       cardRed;
    private final int       cardEdge;
    private final int       backFill;
    private final int       backLine;
    private final BackStyle backStyle;

    public TablePalette(String id, String displayName,
                        int feltTop, int feltBottom, int accent,
                        int cardFace, int cardInk, int cardRed, int cardEdge,
                        int backFill, int backLine, BackStyle backStyle) {
        this.id          = id;
        this.displayName = displayName;
        this.feltTop     = feltTop;
        this.feltBottom  = feltBottom;
        this.accent      = accent;
        this.cardFace    = cardFace;
        this.cardInk     = cardInk;
        this.cardRed     = cardRed;
        this.cardEdge    = cardEdge;
        this.backFill    = backFill;
        this.backLine    = backLine;
        this.backStyle   = backStyle;
    }

    /** Stable identifier, saved in settings. Never shown to the player. */
    public String id() { return id; }

    /** Human-facing name. Safe to change without resetting anyone's preference. */
    public String displayName() { return displayName; }

    public int feltTop()    { return feltTop; }
    public int feltBottom() { return feltBottom; }
    public int accent()     { return accent; }

    /** Card background. */
    public int cardFace() { return cardFace; }
    /** Ink for the black suits. */
    public int cardInk()  { return cardInk; }
    /** Ink for the red suits — not always red; Ink draws all four in black. */
    public int cardRed()  { return cardRed; }
    /** Card outline. */
    public int cardEdge() { return cardEdge; }

    public int backFill() { return backFill; }
    public int backLine() { return backLine; }

    public BackStyle backStyle() { return backStyle; }

    /** Red channel of {@code rgb}, 0-255. */
    public static int red(int rgb)   { return (rgb >> 16) & 0xFF; }
    /** Green channel of {@code rgb}, 0-255. */
    public static int green(int rgb) { return (rgb >> 8) & 0xFF; }
    /** Blue channel of {@code rgb}, 0-255. */
    public static int blue(int rgb)  { return rgb & 0xFF; }

    /**
     * Perceived brightness of {@code rgb}, 0-255. Used to choose readable text
     * over an arbitrary background rather than guessing black or white.
     */
    public static int luma(int rgb) {
        return (int) (0.299 * red(rgb) + 0.587 * green(rgb) + 0.114 * blue(rgb));
    }

    /** {@code 0x000000} or {@code 0xFFFFFF}, whichever stays legible on {@code rgb}. */
    public static int contrastOn(int rgb) {
        return luma(rgb) > 140 ? 0x000000 : 0xFFFFFF;
    }

    @Override public String toString() { return displayName + " (" + id + ")"; }
}
