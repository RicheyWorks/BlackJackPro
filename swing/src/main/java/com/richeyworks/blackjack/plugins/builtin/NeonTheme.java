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
import java.awt.geom.RoundRectangle2D;

/**
 * Cyberpunk/synthwave theme — purple felt, neon-magenta card edges,
 * cyan center pips. Ships in the box as a built-in example of how a theme
 * plugin can completely change the table's look without touching engine code.
 */
public final class NeonTheme implements TableTheme {

    private static final Color FELT_TOP    = new Color(0x2C003E);
    private static final Color FELT_BOTTOM = new Color(0x000814);
    private static final Color NEON_MAG    = new Color(0xFF2CB6);
    private static final Color NEON_CYAN   = new Color(0x18FFFF);
    private static final Color CARD_BG     = new Color(0x101820);

    @Override public String displayName() { return "Neon"; }
    @Override public Color  feltTop()     { return FELT_TOP; }
    @Override public Color  feltBottom()  { return FELT_BOTTOM; }
    @Override public Color  accent()      { return NEON_CYAN; }
    @Override public Color  highlight()   { return new Color(255, 44, 182, 60); }

    @Override
    public void paintCardFace(Graphics2D g, int x, int y, Card card) {
        Shape r = new RoundRectangle2D.Float(x, y, cardWidth(), cardHeight(), 12, 12);
        g.setColor(CARD_BG);
        g.fill(r);
        g.setStroke(new BasicStroke(2f));
        g.setColor(NEON_MAG);
        g.draw(r);

        Color suitColor = card.suit().isRed() ? NEON_MAG : NEON_CYAN;
        g.setColor(suitColor);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 18f));
        g.drawString(card.rank().label(), x + 8, y + 22);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 16f));
        g.drawString(card.suit().glyph(), x + 8, y + 40);

        // big center glyph
        g.setFont(g.getFont().deriveFont(Font.BOLD, 48f));
        String mid = (card.rank() == Rank.JACK || card.rank() == Rank.QUEEN
                   || card.rank() == Rank.KING) ? card.rank().label() : card.suit().glyph();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(mid, x + (cardWidth() - fm.stringWidth(mid)) / 2,
                y + cardHeight() / 2 + fm.getAscent() / 2 - 6);
    }

    @Override
    public void paintCardBack(Graphics2D g, int x, int y) {
        Shape r = new RoundRectangle2D.Float(x, y, cardWidth(), cardHeight(), 12, 12);
        g.setColor(new Color(0x14002A));
        g.fill(r);
        g.setStroke(new BasicStroke(2f));
        g.setColor(NEON_CYAN);
        g.draw(r);

        g.setColor(NEON_MAG);
        g.setStroke(new BasicStroke(1.2f));
        for (int i = -cardHeight(); i < cardWidth() + cardHeight(); i += 10) {
            g.drawLine(x + i, y, x + i - cardHeight(), y + cardHeight());
        }
        g.setColor(NEON_CYAN);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
        FontMetrics fm = g.getFontMetrics();
        String s = "BJ";
        g.drawString(s, x + (cardWidth() - fm.stringWidth(s)) / 2,
                y + cardHeight() / 2 + fm.getAscent() / 2 - 2);
    }

    @Override
    public void paintChip(Graphics2D g, int cx, int cy, int radius, int value) {
        Color face;
        switch (value) {
            case 1   -> face = new Color(0xECECEC);
            case 5   -> face = new Color(0xFF2CB6);
            case 25  -> face = new Color(0x18FFFF);
            case 100 -> face = new Color(0xFFB400);
            case 500 -> face = new Color(0x9B59B6);
            default  -> face = NEON_MAG;
        }
        g.setColor(face.darker());
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setColor(face);
        g.fillOval(cx - radius + 4, cy - radius + 4, radius * 2 - 8, radius * 2 - 8);
        g.setStroke(new BasicStroke(2f));
        g.setColor(Color.WHITE);
        g.drawOval(cx - radius + 2, cy - radius + 2, radius * 2 - 4, radius * 2 - 4);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
        FontMetrics fm = g.getFontMetrics();
        String s = String.valueOf(value);
        g.setColor(Color.BLACK);
        g.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getAscent() / 2 - 2);
    }
}
