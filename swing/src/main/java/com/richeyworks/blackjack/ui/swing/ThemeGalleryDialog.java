package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.engine.Suit;
import com.richeyworks.blackjack.plugin.TableTheme;
import com.richeyworks.blackjack.table.Casts;
import com.richeyworks.blackjack.table.Persona;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * A visual theme picker: every table look rendered live as a miniature — real
 * felt gradient, real card back, real card face, real chip — instead of a
 * two-dozen-entry text menu the player has to try one line at a time.
 *
 * <p>Each tile paints with the theme's <em>own</em> renderer, so the preview
 * cannot drift from the table: whatever {@code paintCardBack} draws at full
 * size is exactly what the tile shows at half scale. Themes that seat their
 * own cast (see {@link Casts}) carry a badge naming the crew, because that is
 * a bigger difference than any colour.
 *
 * <p>Selection applies immediately through the callback — the table behind
 * the dialog changes as you click, which is the whole point of a gallery: try
 * looks, keep the one that's on screen when you close it.
 */
public final class ThemeGalleryDialog extends JDialog {

    private static final int TILE_W = 176, TILE_H = 156;

    private String selectedId;

    public ThemeGalleryDialog(JFrame owner, List<TableTheme> themes,
                              String currentId, Consumer<TableTheme> onPick) {
        super(owner, "Theme Gallery", false);
        this.selectedId = currentId;

        JPanel grid = new JPanel(new GridLayout(0, 4, 10, 10));
        grid.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        grid.setBackground(new Color(0x1E1E1E));

        for (TableTheme t : themes) {
            Tile tile = new Tile(t);
            tile.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    selectedId = t.id();
                    onPick.accept(t);
                    grid.repaint();
                }
            });
            grid.add(tile);
        }

        JScrollPane scroll = new JScrollPane(grid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // Escape closes the gallery — found missing by the deploy audit's
        // scripted play-through, which is exactly the kind of thing it's for.
        getRootPane().registerKeyboardAction(e -> dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        int rows = (themes.size() + 3) / 4;
        int height = Math.min(720, rows * (TILE_H + 10) + 60);
        setSize(4 * (TILE_W + 10) + 44, height);
        setLocationRelativeTo(owner);
    }

    /** One theme, painted by that theme. */
    private final class Tile extends JComponent {

        private final TableTheme theme;
        private final String crewNote;
        private boolean hover;

        Tile(TableTheme theme) {
            this.theme = theme;
            List<Persona> cast = Casts.forPalette(theme.id());
            this.crewNote = cast.get(0).id().equals(Casts.forPalette(null).get(0).id())
                    ? null
                    : cast.get(0).name() + ", " + cast.get(1).name() + " & " + cast.get(2).name();
            setPreferredSize(new Dimension(TILE_W, TILE_H));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(crewNote == null
                    ? theme.displayName()
                    : theme.displayName() + " — seats " + crewNote);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                RoundRectangle2D r = new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 14, 14);

                // The felt itself is the tile background, motif included, so
                // the preview shows the same surface the table will.
                g.setPaint(new GradientPaint(0, 0, theme.feltTop(), 0, h, theme.feltBottom()));
                g.fill(r);
                g.clip(r);
                Graphics2D decor = scratch(g, 1.0);
                theme.paintFeltDecor(decor, w, h);
                decor.dispose();

                // Cards and a chip at half scale, drawn by the theme's own
                // renderer through scratch graphics it may freely trash.
                Graphics2D s = scratch(g, 0.52);
                theme.paintCardBack(s, 22, 26);
                s.dispose();
                s = scratch(g, 0.52);
                theme.paintCardFace(s, 22 + theme.cardWidth() + 14, 26,
                        new Card(Rank.ACE, Suit.SPADES));
                s.dispose();
                s = scratch(g, 1.0);
                theme.paintChip(s, w - 30, h - 46, 15, 25);
                s.dispose();

                // Name plate along the bottom, dark for legibility on any felt.
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRoundRect(2, h - 34, w - 4, 32, 14, 14);
                g.setColor(Color.WHITE);
                g.setFont(getFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(theme.displayName(), 10, h - 34 + fm.getAscent() + 2);
                if (crewNote != null) {
                    g.setColor(theme.accent());
                    g.setFont(getFont().deriveFont(Font.PLAIN, 10f));
                    g.drawString("★ seats " + crewNote, 10, h - 6);
                }

                // Selection and hover feedback.
                if (theme.id().equals(selectedId)) {
                    g.setColor(theme.accent());
                    g.setStroke(new java.awt.BasicStroke(3f));
                    g.draw(r);
                } else if (hover) {
                    g.setColor(new Color(255, 255, 255, 120));
                    g.setStroke(new java.awt.BasicStroke(2f));
                    g.draw(r);
                }
            } finally {
                g.dispose();
            }
        }

        /** A scratch child of {@code g} at {@code scale}, per the theme contract. */
        private Graphics2D scratch(Graphics2D g, double scale) {
            Graphics2D s = (Graphics2D) g.create();
            s.scale(scale, scale);
            return s;
        }
    }
}
