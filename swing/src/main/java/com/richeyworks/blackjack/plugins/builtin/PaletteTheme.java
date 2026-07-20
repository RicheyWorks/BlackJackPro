package com.richeyworks.blackjack.plugins.builtin;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.plugin.TableTheme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 * A complete table look driven entirely by a colour palette.
 *
 * <p>{@code ClassicTheme} and {@code NeonTheme} each hand-render every card and
 * chip, which is why the game shipped with exactly two themes: adding a third
 * meant writing a third renderer. Nearly all of that code was identical, and
 * the differences were colours. This draws one good card and one good chip and
 * takes the palette as data, so a new theme is a dozen lines rather than a
 * hundred and thirty.
 *
 * <p>The renderer hands each paint call a scratch {@link Graphics2D} it
 * disposes afterwards, so nothing here needs to restore font, colour, or
 * stroke.
 *
 * @param backStyle how the card back is decorated, which is what stops a set of
 *                  recoloured themes looking like the same theme five times
 */
public class PaletteTheme implements TableTheme {

    /** Card-back decoration. The cheapest way to give a palette its own identity. */
    public enum BackStyle {
        /** Diagonal cross-hatch. Traditional. */
        HATCH,
        /** Concentric rounded rectangles. Calm, modern. */
        RINGS,
        /** Vertical pinstripes. Formal. */
        STRIPES,
        /** Radiating chevrons from the centre. Art-deco. */
        CHEVRON,
        /** A single bordered panel with a monogram. Minimal. */
        PLAIN
    }

    private final String    id;
    private final String    displayName;
    private final Color     feltTop;
    private final Color     feltBottom;
    private final Color     accent;
    private final Color     cardFace;
    private final Color     cardInk;
    private final Color     cardRed;
    private final Color     cardEdge;
    private final Color     backFill;
    private final Color     backLine;
    private final BackStyle backStyle;

    public PaletteTheme(String id, String displayName,
                        Color feltTop, Color feltBottom, Color accent,
                        Color cardFace, Color cardInk, Color cardRed, Color cardEdge,
                        Color backFill, Color backLine, BackStyle backStyle) {
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

    @Override public String id()          { return id; }
    @Override public String displayName() { return displayName; }
    @Override public Color  feltTop()     { return feltTop; }
    @Override public Color  feltBottom()  { return feltBottom; }
    @Override public Color  accent()      { return accent; }

    @Override public Color highlight() {
        // Derived from the accent so every palette gets a glow that belongs to
        // it, rather than one shared yellow that clashes with half of them.
        return new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70);
    }

    /* ----------------------------------------------------------------------- */
    /* Cards                                                                   */
    /* ----------------------------------------------------------------------- */

    @Override
    public void paintCardFace(Graphics2D g, int x, int y, Card card) {
        Shape r = new RoundRectangle2D.Float(x, y, cardWidth(), cardHeight(), 13, 13);
        g.setColor(cardFace);
        g.fill(r);
        g.setColor(cardEdge);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(r);

        Color ink = card.suit().isRed() ? cardRed : cardInk;
        g.setColor(ink);

        Font corner = g.getFont().deriveFont(Font.BOLD, 18f);
        Font pip    = g.getFont().deriveFont(Font.PLAIN, 15f);

        g.setFont(corner);
        g.drawString(card.rank().label(), x + 8, y + 22);
        g.setFont(pip);
        g.drawString(card.suit().glyph(), x + 8, y + 39);

        // Corner repeated upside-down, as a real card is.
        AffineTransform old = g.getTransform();
        g.translate(x + cardWidth(), y + cardHeight());
        g.rotate(Math.PI);
        g.setFont(corner);
        g.drawString(card.rank().label(), 8, 22);
        g.setFont(pip);
        g.drawString(card.suit().glyph(), 8, 39);
        g.setTransform(old);

        // Court cards show their letter; everything else shows a large pip, so
        // the hand is readable at a glance without needing card art.
        Font big = g.getFont().deriveFont(Font.BOLD, 46f);
        g.setFont(big);
        String mid = isCourt(card.rank()) ? card.rank().label() : card.suit().glyph();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(mid, x + (cardWidth() - fm.stringWidth(mid)) / 2,
                y + cardHeight() / 2 + fm.getAscent() / 2 - 6);
    }

    private static boolean isCourt(Rank r) {
        return r == Rank.JACK || r == Rank.QUEEN || r == Rank.KING;
    }

    @Override
    public void paintCardBack(Graphics2D g, int x, int y) {
        int w = cardWidth(), h = cardHeight();
        Shape r = new RoundRectangle2D.Float(x, y, w, h, 13, 13);
        g.setColor(backFill);
        g.fill(r);

        // clip() intersects rather than replaces, so decoration cannot escape
        // the card outline or Swing's damage region.
        Shape clip = g.getClip();
        g.clip(r);
        g.setColor(backLine);
        g.setStroke(new BasicStroke(1.1f));
        switch (backStyle) {
            case HATCH -> {
                for (int i = -h; i < w + h; i += 8) {
                    g.drawLine(x + i, y, x + i - h, y + h);
                    g.drawLine(x + i, y + h, x + i - h, y);
                }
            }
            case RINGS -> {
                for (int i = 6; i < Math.max(w, h) / 2; i += 9) {
                    g.drawRoundRect(x + i, y + i, w - i * 2, h - i * 2, 10, 10);
                }
            }
            case STRIPES -> {
                for (int i = 7; i < w; i += 7) g.drawLine(x + i, y + 4, x + i, y + h - 4);
            }
            case CHEVRON -> {
                int cx = x + w / 2;
                for (int i = 0; i < h + w; i += 11) {
                    g.drawLine(cx, y + i - w / 2, x, y + i);
                    g.drawLine(cx, y + i - w / 2, x + w, y + i);
                }
            }
            case PLAIN -> g.drawRoundRect(x + 7, y + 7, w - 14, h - 14, 9, 9);
        }
        g.setClip(clip);

        g.setColor(accent);
        g.setStroke(new BasicStroke(1.6f));
        g.draw(r);

        g.setColor(accent);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 13f));
        FontMetrics fm = g.getFontMetrics();
        String s = "BJ";
        g.drawString(s, x + (w - fm.stringWidth(s)) / 2, y + h / 2 + fm.getAscent() / 2 - 2);
    }

    /* ----------------------------------------------------------------------- */
    /* Chips                                                                   */
    /* ----------------------------------------------------------------------- */

    @Override
    public void paintChip(Graphics2D g, int cx, int cy, int radius, int value) {
        if (radius < 5) return;                      // too small to draw meaningfully
        Color face = chipColour(value);

        g.setColor(face.darker());
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setColor(face);
        g.fillOval(cx - radius + 4, cy - radius + 4, radius * 2 - 8, radius * 2 - 8);

        // Edge spots, the detail that reads as "casino chip" rather than "disc".
        g.setColor(contrastOn(face));
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * i / 3.0;
            int sx = (int) (cx + Math.cos(a) * (radius - 2));
            int sy = (int) (cy + Math.sin(a) * (radius - 2));
            g.drawLine(sx, sy, (int) (cx + Math.cos(a) * (radius - 5)),
                               (int) (cy + Math.sin(a) * (radius - 5)));
        }
        g.drawOval(cx - radius + 3, cy - radius + 3, radius * 2 - 6, radius * 2 - 6);

        g.setColor(contrastOn(face));
        g.setFont(g.getFont().deriveFont(Font.BOLD, 13f));
        FontMetrics fm = g.getFontMetrics();
        String s = String.valueOf(value);
        g.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getAscent() / 2 - 2);
    }

    /**
     * Chip colours are the standard casino denominations and deliberately do
     * <em>not</em> vary by theme: a player learns that green means 25 once, and
     * changing it per theme would make the chips harder to read for decoration.
     */
    protected Color chipColour(int value) {
        return switch (value) {
            case 1   -> new Color(0xECECEC);
            case 5   -> new Color(0xC0392B);
            case 25  -> new Color(0x2E7D32);
            case 100 -> new Color(0x1B1B1B);
            case 500 -> new Color(0x6A1B9A);
            default  -> new Color(0xF1C40F);
        };
    }

    /** Black or white, whichever stays legible on {@code bg}. */
    private static Color contrastOn(Color bg) {
        double luma = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        return luma > 0.55 ? Color.BLACK : Color.WHITE;
    }
}
