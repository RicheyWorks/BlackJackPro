package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.plugin.TableTheme;
import com.richeyworks.blackjack.table.Personas;
import com.richeyworks.blackjack.table.Remark;
import com.richeyworks.blackjack.table.TableChatter;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;

/** Renders the felt, dealer/player hands, and a chip stack representation. */
public final class TablePanel extends JPanel {

    private static final Color  LABEL_GOLD = new Color(0xF8E9A1);
    private static final Stroke ARC_STROKE = new BasicStroke(2.5f);

    /** Chip denominations drawn in the bet stack, largest first. */
    private static final int[] STACK_DENOMS = {500, 100, 25, 5, 1};
    private static final int   STACK_COLUMN = 50;   // px between denomination columns
    private static final int   STACK_MAX    = 8;    // chips drawn per column

    private final Engine engine;
    private TableTheme   theme;
    /** Optional; null leaves the table silent. */
    private TableChatter chatter;

    // The felt gradient depends only on the theme and the panel height; rebuilding
    // it every repaint is pure churn, so cache it and invalidate on either change.
    private GradientPaint felt;
    private TableTheme    feltTheme;
    private int           feltHeight = -1;

    public TablePanel(Engine engine, TableTheme theme) {
        this.engine = engine;
        this.theme  = theme;
        setBackground(theme.feltBottom());
        setPreferredSize(new Dimension(1200, 540));
    }

    public void setTheme(TableTheme theme) {
        this.theme = theme;
        setBackground(theme.feltBottom());
        repaint();
    }

    /** Seat the characters. Passing null empties the table. */
    public void setChatter(TableChatter chatter) {
        this.chatter = chatter;
        repaint();
    }

    private GradientPaint felt(int h) {
        if (felt == null || feltTheme != theme || feltHeight != h) {
            felt       = new GradientPaint(0, 0, theme.feltTop(), 0, h, theme.feltBottom());
            feltTheme  = theme;
            feltHeight = h;
        }
        return felt;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            paintTable(g2);
        } finally {
            g2.dispose();
        }
    }

    private void paintTable(Graphics2D g2) {
        int w = getWidth(), h = getHeight();

        g2.setPaint(felt(h));
        g2.fillRect(0, 0, w, h);

        g2.setColor(theme.accent());
        g2.setStroke(ARC_STROKE);
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
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 15f));
            g2.setColor(active ? Color.WHITE : LABEL_GOLD);
            drawCentered(g2, label, cx, yPlayer + theme.cardHeight() + 28);
        }

        if (engine.phase() == Phase.BETTING && engine.pendingBet() > 0) {
            paintBetStack(g2, w / 2, h / 2 + 20, engine.pendingBet());
        }

        paintChatter(g2, w, h);
    }

    /**
     * Draw whatever the table is saying.
     *
     * <p>Seats are pinned to the edges, clear of the dealer at top-centre and
     * the player's hands at bottom-centre, so a bubble never covers a card.
     */
    private void paintChatter(Graphics2D g2, int w, int h) {
        if (chatter == null) return;
        long now = System.currentTimeMillis();
        for (Remark r : chatter.visibleAt(now)) {
            int seat = r.speaker().seat();
            boolean left = seat != Personas.SEAT_RIGHT;
            int x = left ? 46 : w - 46;
            int y = switch (seat) {
                case Personas.SEAT_LEFT  -> (int) (h * 0.52);
                case Personas.SEAT_RIGHT -> (int) (h * 0.52);
                default                  -> (int) (h * 0.30);   // far seat, higher up
            };
            SpeechBubble.paint(g2, r, x, y,
                    left ? SpeechBubble.Side.LEFT : SpeechBubble.Side.RIGHT,
                    r.opacityAt(now));
        }
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
            // Each theme call gets a scratch context. A theme is third-party code
            // that may leave any font, colour, stroke, clip, or transform behind;
            // isolating it here means neither the renderer's own state nor the
            // next card's can be corrupted, whatever the theme does.
            Graphics2D s = (Graphics2D) g2.create();
            try {
                if (hideHole && i == 1) theme.paintCardBack(s, cardX, topY);
                else                    theme.paintCardFace(s, cardX, topY, hand.cards().get(i));
            } finally {
                s.dispose();
            }
        }
    }

    /**
     * Draw {@code amount} as columns of chips, largest denomination first, with
     * the whole run centred on {@code cx}. Only denominations actually present
     * occupy a column — advancing past absent ones pushed a $1 bet 200 px
     * off-centre and made the stack jump sideways as the bet changed.
     */
    private void paintBetStack(Graphics2D g2, int cx, int cy, int amount) {
        int[] counts  = new int[STACK_DENOMS.length];
        int   columns = 0;
        int   left    = amount;
        for (int i = 0; i < STACK_DENOMS.length; i++) {
            counts[i] = left / STACK_DENOMS[i];
            left     %= STACK_DENOMS[i];
            if (counts[i] > 0) columns++;
        }
        if (columns == 0) return;

        int x = cx - ((columns - 1) * STACK_COLUMN) / 2;
        for (int i = 0; i < STACK_DENOMS.length; i++) {
            if (counts[i] == 0) continue;
            for (int k = 0; k < counts[i] && k < STACK_MAX; k++) {
                Graphics2D s = (Graphics2D) g2.create();
                try {
                    theme.paintChip(s, x, cy - k * 5, 22, STACK_DENOMS[i]);
                } finally {
                    s.dispose();
                }
            }
            x += STACK_COLUMN;
        }
    }
}
