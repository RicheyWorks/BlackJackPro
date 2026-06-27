package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Card;
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
        int w = getWidth(), h = getHeight();

        g2.setPaint(new GradientPaint(0, 0, theme.feltTop(), 0, h, theme.feltBottom()));
        g2.fillRect(0, 0, w, h);

        g2.setColor(theme.accent());
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawArc(w / 2 - 480, 40, 960, 700, 0, 180);
        g2.setFont(g2.getFont().deriveFont(Font.ITALIC, 13f));
        g2.drawString("BLACKJACK PAYS 3 TO 2", w / 2 - 100, 80);
        g2.drawString("DEALER " + (engine.rules().dealerHitsSoft17 ? "HITS" : "STANDS") + " ON SOFT 17",
                w / 2 - 130, h - 36);

        boolean hideHole = engine.phase().ordinal() < Phase.DEALER.ordinal();
        int dealerCaptionY = 110;
        g2.setColor(new Color(0xF8E9A1));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        String dealerCaption = hideHole
                ? "Dealer (showing " + (engine.dealer().isEmpty() ? 0 : engine.dealer().first().rank().value()) + ")"
                : "Dealer (" + engine.dealer().value() + ")";
        g2.drawString(dealerCaption, w / 2 - 60, dealerCaptionY);

        paintHand(g2, engine.dealer(), w / 2, 130, false, hideHole);

        List<Hand> hands = engine.hands();
        int n = hands.size();
        int yPlayer = h - theme.cardHeight() - 80;
        int handGap = Math.min(360, (w - 80) / Math.max(1, n));
        int startX  = w / 2 - ((n - 1) * handGap) / 2;
        for (int i = 0; i < n; i++) {
            Hand hh = hands.get(i);
            paintHand(g2, hh, startX + i * handGap, yPlayer,
                    i == engine.activeIndex() && engine.phase() == Phase.PLAYER,
                    false);
            String label = "Hand " + (n > 1 ? (i + 1) : "")
                    + "  (" + hh.value() + (hh.isSoft() ? "s" : "") + ")  $" + hh.bet()
                    + (hh.surrendered() ? " surrendered"
                       : hh.doubled() ? " doubled" : "");
            g2.setColor(new Color(0xF8E9A1));
            g2.drawString(label, startX + i * handGap - 60, yPlayer + theme.cardHeight() + 22);
        }

        if (engine.phase() == Phase.BETTING && engine.pendingBet() > 0) {
            paintBetStack(g2, w / 2, h / 2 + 20, engine.pendingBet());
        }
        g2.dispose();
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
