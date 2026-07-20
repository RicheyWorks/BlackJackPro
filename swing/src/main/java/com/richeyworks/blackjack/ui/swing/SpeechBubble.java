package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.table.Remark;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws a character's remark as a speech bubble on the felt.
 *
 * <p>Kept out of {@link TablePanel} because bubble layout — wrapping, tail
 * direction, fading — is fiddly enough to be worth isolating, and because it
 * needs no game state at all: hand it a {@link Remark} and a spot.
 */
final class SpeechBubble {

    private SpeechBubble() {}

    private static final Color FILL    = new Color(0xF6F1DF);
    private static final Color BORDER  = new Color(0x3A2E14);
    private static final Color TEXT    = new Color(0x1E1A12);
    private static final Color NAME    = new Color(0x6B5A2E);

    private static final int MAX_W   = 250;   // px before wrapping
    private static final int PAD     = 10;
    private static final int LINE_H  = 15;
    private static final int TAIL    = 9;

    /** Which way the tail points, i.e. which side of the bubble the speaker is. */
    enum Side { LEFT, RIGHT }

    /**
     * Draw {@code remark} with its tail anchored at ({@code anchorX},
     * {@code anchorY}). The bubble grows away from the anchor, so a LEFT-side
     * speaker gets a bubble extending right and vice versa.
     *
     * @param opacity 0..1, from {@link Remark#opacityAt(long)}
     */
    static void paint(Graphics2D g2, Remark remark, int anchorX, int anchorY,
                      Side side, float opacity) {
        if (opacity <= 0f) return;

        Graphics2D g = (Graphics2D) g2.create();
        try {
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, opacity))));

            Font bodyFont = g.getFont().deriveFont(Font.PLAIN, 12.5f);
            Font nameFont = g.getFont().deriveFont(Font.BOLD, 10.5f);

            g.setFont(bodyFont);
            List<String> lines = wrap(remark.text(), g.getFontMetrics(), MAX_W);

            int textW = 0;
            for (String line : lines) textW = Math.max(textW, g.getFontMetrics().stringWidth(line));
            g.setFont(nameFont);
            textW = Math.max(textW, g.getFontMetrics().stringWidth(remark.speaker().name()));

            int boxW = textW + PAD * 2;
            int boxH = PAD * 2 + LINE_H + lines.size() * LINE_H;
            int boxX = side == Side.LEFT ? anchorX + TAIL : anchorX - TAIL - boxW;
            int boxY = anchorY - boxH / 2;

            RoundRectangle2D box = new RoundRectangle2D.Float(boxX, boxY, boxW, boxH, 14, 14);
            g.setColor(FILL);
            g.fill(box);

            // Tail first, then the border, so the seam where they meet is hidden
            // under the outline rather than showing as a notch.
            Polygon tail = new Polygon();
            if (side == Side.LEFT) {
                tail.addPoint(anchorX, anchorY);
                tail.addPoint(boxX + 1, anchorY - TAIL);
                tail.addPoint(boxX + 1, anchorY + TAIL);
            } else {
                tail.addPoint(anchorX, anchorY);
                tail.addPoint(boxX + boxW - 1, anchorY - TAIL);
                tail.addPoint(boxX + boxW - 1, anchorY + TAIL);
            }
            g.setColor(FILL);
            g.fill(tail);
            g.setColor(BORDER);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(box);

            g.setFont(nameFont);
            g.setColor(NAME);
            g.drawString(remark.speaker().name(), boxX + PAD, boxY + PAD + 9);

            g.setFont(bodyFont);
            g.setColor(TEXT);
            int y = boxY + PAD + LINE_H + 10;
            for (String line : lines) {
                g.drawString(line, boxX + PAD, y);
                y += LINE_H;
            }

            g.setComposite(old);
        } finally {
            g.dispose();
        }
    }

    /** Greedy word wrap. Splits an over-long single word rather than overflowing. */
    private static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
                continue;
            }
            if (line.length() > 0) {
                out.add(line.toString());
                line.setLength(0);
            }
            if (fm.stringWidth(word) <= maxWidth) {
                line.append(word);
            } else {
                // Pathological single word: hard-break it so nothing escapes the box.
                StringBuilder chunk = new StringBuilder();
                for (char c : word.toCharArray()) {
                    if (fm.stringWidth(chunk.toString() + c) > maxWidth && chunk.length() > 0) {
                        out.add(chunk.toString());
                        chunk.setLength(0);
                    }
                    chunk.append(c);
                }
                line.append(chunk);
            }
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }
}
