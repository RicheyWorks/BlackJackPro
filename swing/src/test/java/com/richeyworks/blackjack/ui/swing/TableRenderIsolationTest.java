package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.plugin.TableTheme;
import com.richeyworks.blackjack.plugins.builtin.NeonTheme;
import org.junit.jupiter.api.Test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A theme is plugin code. The renderer hands each paint call a scratch
 * {@link Graphics2D} so nothing a theme leaves behind — font, colour, stroke,
 * clip, transform — can reach the next card or the rest of the table.
 *
 * <p>These tests use a deliberately hostile theme: it records the state it was
 * handed and then trashes everything before returning.
 */
class TableRenderIsolationTest {

    private static final Font   PROBE_FONT   = new Font("SansSerif", Font.ITALIC, 13);
    private static final Color  PROBE_COLOR  = new Color(0xF8E9A1);
    private static final Stroke PROBE_STROKE = new BasicStroke(2.5f);

    /**
     * Records the graphics state handed to each paint call and, when
     * {@code vandalise} is set, trashes it as badly as it can before returning.
     *
     * <p>Tests compare a passive run against a vandalising one: the renderer
     * varies state between calls on purpose (the active hand is drawn in the
     * highlight colour), so the property that matters is not that every call
     * sees the same thing — it's that what a call sees never depends on what an
     * earlier call did to its graphics.
     */
    private static final class RecordingTheme implements TableTheme {
        final List<Font>   fonts   = new ArrayList<>();
        final List<Color>  colors  = new ArrayList<>();
        final List<Stroke> strokes = new ArrayList<>();
        final List<java.awt.geom.AffineTransform> transforms = new ArrayList<>();
        final List<java.awt.Shape> clips = new ArrayList<>();
        final List<Integer> chipX  = new ArrayList<>();
        final List<Integer> chipValues = new ArrayList<>();
        int cardBacks;

        private final boolean vandalise;
        RecordingTheme(boolean vandalise) { this.vandalise = vandalise; }

        private void visit(Graphics2D g) {
            fonts.add(g.getFont());
            colors.add(g.getColor());
            strokes.add(g.getStroke());
            transforms.add(g.getTransform());
            clips.add(g.getClipBounds());
            if (!vandalise) return;
            g.setFont(new Font("Monospaced", Font.BOLD, 72));
            g.setColor(Color.MAGENTA);
            g.setStroke(new BasicStroke(19f));
            g.setClip(0, 0, 1, 1);
            g.translate(500, 500);
            g.scale(3, 3);
            g.rotate(1.1);
        }

        @Override public String id()          { return "recording"; }
        @Override public String displayName() { return "Recording"; }
        @Override public Color  feltTop()     { return Color.BLACK; }
        @Override public Color  feltBottom()  { return Color.DARK_GRAY; }

        @Override public void paintCardFace(Graphics2D g, int x, int y, Card c) { visit(g); }
        @Override public void paintCardBack(Graphics2D g, int x, int y)         { cardBacks++; visit(g); }
        @Override public void paintChip(Graphics2D g, int cx, int cy, int r, int v) {
            chipX.add(cx);
            chipValues.add(v);
            visit(g);
        }
    }

    /** Render a panel once into an offscreen image with known starting state. */
    private static void render(TablePanel panel, Graphics2D g) {
        panel.setSize(1200, 820);
        g.setFont(PROBE_FONT);
        g.setColor(PROBE_COLOR);
        g.setStroke(PROBE_STROKE);
        panel.paint(g);
    }

    private static Graphics2D offscreen() {
        return new BufferedImage(1200, 820, BufferedImage.TYPE_INT_ARGB).createGraphics();
    }

    /** Render one round with the given theme and hand back what it recorded. */
    private static RecordingTheme run(long seed, int bet, boolean vandalise, boolean deal) {
        Engine e = new Engine(1000, new Random(seed));
        e.addBet(bet);
        if (deal) e.deal();

        RecordingTheme t = new RecordingTheme(vandalise);
        TablePanel panel = new TablePanel(e, t);
        Graphics2D g = offscreen();
        try {
            render(panel, g);
        } finally {
            g.dispose();
        }
        return t;
    }

    @Test void whatAThemeSeesDoesNotDependOnWhatEarlierCallsDid() {
        RecordingTheme passive  = run(4, 50, false, true);
        RecordingTheme hostile  = run(4, 50, true,  true);

        assertFalse(passive.fonts.isEmpty(), "the theme should have been asked to paint something");
        assertEquals(passive.fonts.size(), hostile.fonts.size(), "same number of paint calls");

        // Identical inputs, so if a vandalised context could reach the next call
        // these sequences would diverge from the second entry onwards.
        assertEquals(passive.fonts,      hostile.fonts,      "font leaked between calls");
        assertEquals(passive.colors,     hostile.colors,     "colour leaked between calls");
        assertEquals(passive.strokes,    hostile.strokes,    "stroke leaked between calls");
        assertEquals(passive.transforms, hostile.transforms, "transform leaked between calls");
        assertEquals(passive.clips,      hostile.clips,      "clip leaked between calls");
    }

    @Test void aVandalisingThemeDoesNotCorruptTheCallersGraphics() {
        Engine e = new Engine(1000, new Random(9));
        e.addBet(25);
        e.deal();

        // Compare against a passive render rather than the probe values: Swing's
        // own paint machinery installs the component's font and foreground, so
        // the baseline is "whatever a well-behaved theme leaves", not the probe.
        Graphics2D control = offscreen();
        Graphics2D subject = offscreen();
        try {
            render(new TablePanel(e, new RecordingTheme(false)), control);
            render(new TablePanel(e, new RecordingTheme(true)),  subject);

            assertEquals(control.getFont(),      subject.getFont(),      "font corrupted");
            assertEquals(control.getColor(),     subject.getColor(),     "colour corrupted");
            assertEquals(control.getStroke(),    subject.getStroke(),    "stroke corrupted");
            assertEquals(control.getTransform(), subject.getTransform(), "transform corrupted");
            assertEquals(control.getClipBounds(), subject.getClipBounds(), "clip corrupted");
        } finally {
            control.dispose();
            subject.dispose();
        }
    }

    @Test void theShippedNeonThemeRendersCleanly() {
        // NeonTheme deliberately does not restore graphics state -- the renderer
        // is what makes that safe, so this is the regression guard for it.
        Engine e = new Engine(1000, new Random(11));
        e.addBet(25);
        e.deal();

        Graphics2D control = offscreen();
        Graphics2D subject = offscreen();
        try {
            render(new TablePanel(e, new RecordingTheme(false)), control);
            assertDoesNotThrow(() -> render(new TablePanel(e, new NeonTheme()), subject));
            assertEquals(control.getTransform(), subject.getTransform());
            assertEquals(control.getClipBounds(), subject.getClipBounds());
        } finally {
            control.dispose();
            subject.dispose();
        }
    }

    @Test void betStackIsCentredWhateverTheDenomination() {
        // A $1 bet used to render 200px right of centre and a $500 bet at centre,
        // because absent denominations still consumed a column.
        for (int bet : new int[]{1, 5, 25, 100, 500}) {
            Engine e = new Engine(1000, new Random(2));
            e.addBet(bet);

            RecordingTheme t = new RecordingTheme(false);
            TablePanel panel = new TablePanel(e, t);
            Graphics2D g = offscreen();
            try {
                render(panel, g);
            } finally {
                g.dispose();
            }

            assertEquals(1, t.chipX.size(), "one chip for a single-denomination bet of $" + bet);
            assertEquals(600, t.chipX.get(0),
                    "$" + bet + " stack should sit on the panel centre");
            assertEquals(bet, t.chipValues.get(0));
        }
    }

    @Test void multiDenominationStackStraddlesTheCentre() {
        Engine e = new Engine(1000, new Random(2));
        e.addBet(500);
        e.addBet(100);
        e.addBet(25);
        e.addBet(5);
        e.addBet(1);            // $631 -> five columns

        RecordingTheme t = new RecordingTheme(false);
        TablePanel panel = new TablePanel(e, t);
        Graphics2D g = offscreen();
        try {
            render(panel, g);
        } finally {
            g.dispose();
        }

        assertEquals(5, t.chipX.size());
        int min = t.chipX.stream().mapToInt(Integer::intValue).min().orElseThrow();
        int max = t.chipX.stream().mapToInt(Integer::intValue).max().orElseThrow();
        assertEquals(600, (min + max) / 2, "the run of columns is centred on the panel");
    }

    @Test void hiddenHoleCardIsPaintedFaceDownWhileThePlayerActs() {
        Engine e = new Engine(1000, new Random(4));
        e.addBet(50);
        e.deal();
        org.junit.jupiter.api.Assumptions.assumeTrue(
                e.phase() == com.richeyworks.blackjack.engine.Phase.PLAYER,
                "need a hand still in play");

        RecordingTheme t = new RecordingTheme(false);
        TablePanel panel = new TablePanel(e, t);
        Graphics2D g = offscreen();
        try {
            render(panel, g);
        } finally {
            g.dispose();
        }
        assertEquals(1, t.cardBacks, "exactly one card -- the hole card -- is face down");
    }
}
