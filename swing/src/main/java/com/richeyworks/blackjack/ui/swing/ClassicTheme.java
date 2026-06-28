package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.plugin.TableTheme;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 * The built-in default theme. Pure Graphics2D — no image files. Lives in the
 * UI package because it's the shipping default, not a swappable plugin asset.
 */
public final class ClassicTheme implements TableTheme {

    @Override public String displayName() { return "Classic Felt"; }
    @Override public Color  feltTop()     { return new Color(0x14513B); }
    @Override public Color  feltBottom()  { return new Color(0x062418); }

    @Override
    public void paintCardFace(Graphics2D g, int x, int y, Card card) {
        Shape r = new RoundRectangle2D.Float(x, y, cardWidth(), cardHeight(), 14, 14);
        g.setColor(Color.WHITE);
        g.fill(r);
        g.setColor(new Color(0x2B2B2B));
        g.draw(r);

        g.setColor(new Color(card.suit().rgb()));
        Font corner = g.getFont().deriveFont(Font.BOLD, 18f);
        g.setFont(corner);
        g.drawString(card.rank().label(), x + 8, y + 22);
        Font glyph = g.getFont().deriveFont(Font.PLAIN, 16f);
        g.setFont(glyph);
        g.drawString(card.suit().glyph(), x + 8, y + 40);

        AffineTransform old = g.getTransform();
        g.translate(x + cardWidth(), y + cardHeight());
        g.rotate(Math.PI);
        g.setFont(corner);
        g.drawString(card.rank().label(), 8, 22);
        g.setFont(glyph);
        g.drawString(card.suit().glyph(), 8, 40);
        g.setTransform(old);

        Font big = g.getFont().deriveFont(Font.BOLD, 48f);
        g.setFont(big);
        String mid = (card.rank() == Rank.JACK || card.rank() == Rank.QUEEN
                   || card.rank() == Rank.KING)
                ? card.rank().label() : card.suit().glyph();
        FontMetrics fm = g.getFontMetrics();
        int sw = fm.stringWidth(mid);
        g.drawString(mid, x + (cardWidth() - sw) / 2,
                y + cardHeight() / 2 + fm.getAscent() / 2 - 6);
    }

    @Override
    public void paintCardBack(Graphics2D g, int x, int y) {
        Shape r = new RoundRectangle2D.Float(x, y, cardWidth(), cardHeight(), 14, 14);
        g.setColor(new Color(0x14213D));
        g.fill(r);
        g.setColor(new Color(0x1E3A5F));
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1.2f));
        Shape clip = g.getClip();
        g.setClip(r);
        for (int i = -cardHeight(); i < cardWidth() + cardHeight(); i += 8) {
            g.drawLine(x + i, y, x + i - cardHeight(), y + cardHeight());
            g.drawLine(x + i, y + cardHeight(), x + i - cardHeight(), y);
        }
        g.setClip(clip);
        g.setStroke(old);
        g.setColor(accent());
        g.draw(r);
        g.setColor(new Color(0xF1F1F1));
        Font f = g.getFont().deriveFont(Font.BOLD, 14f);
        g.setFont(f);
        String s = "BJ";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, x + (cardWidth() - fm.stringWidth(s)) / 2,
                y + cardHeight() / 2 + fm.getAscent() / 2 - 2);
    }

    @Override
    public void paintChip(Graphics2D g, int cx, int cy, int radius, int value) {
        Color edge, face;
        switch (value) {
            case 1   -> { face = new Color(0xECECEC); edge = new Color(0x9E9E9E); }
            case 5   -> { face = new Color(0xC0392B); edge = new Color(0x922B21); }
            case 25  -> { face = new Color(0x2E7D32); edge = new Color(0x1B5E20); }
            case 100 -> { face = new Color(0x1B1B1B); edge = new Color(0x000000); }
            case 500 -> { face = new Color(0x6A1B9A); edge = new Color(0x4A148C); }
            default  -> { face = new Color(0xF1C40F); edge = new Color(0xB7950B); }
        }
        g.setColor(edge);
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setColor(face);
        g.fillOval(cx - radius + 4, cy - radius + 4, radius * 2 - 8, radius * 2 - 8);
        g.setColor(Color.WHITE);
        Stroke os = g.getStroke();
        g.setStroke(new BasicStroke(2f));
        g.drawOval(cx - radius + 2, cy - radius + 2, radius * 2 - 4, radius * 2 - 4);
        g.setStroke(os);
        g.setColor(value == 100 || value == 500 ? Color.WHITE : Color.BLACK);
        Font f = g.getFont().deriveFont(Font.BOLD, 14f);
        g.setFont(f);
        String s = String.valueOf(value);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getAscent() / 2 - 2);
    }
}
