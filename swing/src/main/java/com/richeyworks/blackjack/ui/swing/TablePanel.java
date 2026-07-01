package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.plugin.TableTheme;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/** Renders the felt, dealer/player hands, and a chip stack representation. */
public final class TablePanel extends JPanel {

    private static final Color LABEL_GOLD = new Color(0xF8E9A1);

    private final Engine engine;
    private TableTheme   theme;

    public TablePanel(Engine engine, TableTheme theme) {
        this.engine = engine;
        this.theme  = theme;
        setBackground(theme.feltBottom());
        setPreferredSize(new Dimension(1200, 540));
    }

    public void setTheme(TableTheme theme) {
        this.theme = theme;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g2.setPaint(new GradientPaint(0, 0, theme.feltTop(), 0, h, theme.feltBottom()));
        g2.fillRect(0, 0, w, h);

        g2.setColor(theme.accent());
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawArc(w / 2 - 480, 40, 960, 700, 0, 180);
        g2.setFont(g2.getFont().deriveFont(Font.ITALIC, 13f));
        drawCentered(g2, "BLACKJACK PAYS 3 TO 2", w / 2, 80);
        drawCentered(g2, "DEALER " + (engine.rules().dealerHitsSoft17 ? "HITS" : "STANDS") + " ON SOFT 17",
                w / 2, h - 36);

        // Hide the hole card only while the player is still acting (or during the
        // deal / insurance prompt). Reveal it once the dealer plays, on settlement,
        // and in the post-round BETTING view (the dealer hand is still on the table).
        Phase phase = engine.phase();
        boolean hideHole = phase == Phase.DEALING || phase == Phase.INSURANCE || phase == Phase.PLAYER;
        g2.setColor(LABEL_GOLD);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        String dealerCaption;
        if (engine.dealer().isEmpty()) dealerCaption = "Dealer";
        else if (hideHole)             dealerCaption = "Dealer (showing " + engine.dealer().first().rank().label() + ")";
        else                           dealerCaption = "Dealer (" + engine.dealer().value() + ")";
        drawCentered(g2, dealerCaption, w / 2, 110);

        paintHand(g2, engine.dealer(), w / 2, 130, false, hideHole);

        List<Hand> hands = engine.hands();
        int n = hands.size();
        int yPlayer = h - theme.cardHeight() - 80;
        int handGap = Math.min(360, (w - 80) / Math.max(1, n));
        int startX  = w / 2 - ((n - 1) * handGap) / 2;
        boolean playing = engine.phase() == Phase.PLAYER;
        for (int i = 0; i < n; i++) {
            Hand hh = hands.get(i);
            int cx = startX + i * handGap;
            boolean active = playing && i == engine.activeIndex();
            paintHand(g2, hh, cx, yPlayer, active, false);
            if (hh.size() == 0) continue;            // nothing dealt yet (betting view)
            String prefix = n > 1 ? ("Hand " + (i + 1) + ":  ") : "";
            String state  = hh.surrendered() ? "  ·  surrendered"
                          : hh.doubled()     ? "  ·  doubled" : "";
            String label  = prefix + hh.value() + (hh.isSoft() ? " soft" : "")
                          + "  ·  $" + hh.bet() + state;
            // paintCardFace leaves the theme's large glyph font on g2 -- set ours.
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 15f));
            g2.setColor(active ? Color.WHITE : LABEL_GOLD);
            drawCentered(g2, label, cx, yPlayer + theme.cardHeight() + 28);
        }

        if (engine.phase() == Phase.BETTING && engine.pendingBet() > 0) {
            paintBetStack(g2, w / 2, h / 2 + 20, engine.pendingBet());
        }
        g2.dispose();
    }

    /** Draw a string horizontally centered on {@code cx} at baseline {@code y}. */
    private static void drawCentered(Graphics2D g2, String s, int cx, int y) {
        g2.drawString(s, cx - g2.getFontMetrics().stringWidth(s) / 2, y);
    }

    private void paintHand(Graphics2D g2, Hand hand, int cx, int topY,
                           boolean highlight, boolean hideHole) {
        int count   = hand.size();
        int spread  = 26;
        int totalW  = Math.max(theme.cardWidth(), (count - 1) * spread + theme.cardWidth());
        int x       = cx - totalW / 2;
        if (highlight) {
            g2.setColor(theme.highlight());
            g2.fillRoundRect(x - 8, topY - 8, totalW + 16, theme.cardHeight() + 16, 16, 16);
        }
        for (int i = 0; i < count; i++) {
            int cardX = x + i * spread;
            if (hideHole && i == 1) theme.paintCardBack(g2, cardX, topY);
            else                    theme.paintCardFace(g2, cardX, topY, hand.cards().get(i));
        }
    }

    private void paintBetStack(Graphics2D g2, int cx, int cy, int amount) {
        int[] denoms = {500, 100, 25, 5, 1};
        int x = cx;
        for (int d : denoms) {
            int count = amount / d;
            amount %= d;
            for (int i = 0; i < count && i < 8; i++) {
                theme.paintChip(g2, x, cy - i * 5, 22, d);
            }
            x += 50;
        }
    }
}
