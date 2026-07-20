package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.engine.Suit;
import com.richeyworks.blackjack.plugin.TableTheme;
import com.richeyworks.blackjack.plugins.builtin.BuiltinThemes;
import com.richeyworks.blackjack.plugins.builtin.NeonTheme;
import com.richeyworks.blackjack.settings.GameSettings;
import com.richeyworks.blackjack.table.Palettes;
import com.richeyworks.blackjack.table.TablePalette;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The shipped table looks.
 *
 * <p>Most are palette-driven, which is what makes a new one cheap — and also
 * what makes it easy to add one with a duplicate id, an unreadable card, or a
 * felt the same colour as everything else. These check the things a palette can
 * plausibly get wrong.
 */
class BuiltinThemesTest {

    /** Every theme the game offers, including the two hand-written ones. */
    private static List<TableTheme> allThemes() {
        List<TableTheme> out = new ArrayList<>();
        out.add(new ClassicTheme());
        out.add(new NeonTheme());
        out.addAll(BuiltinThemes.all());
        return out;
    }

    @Test void thereAreSubstantiallyMoreThemesThanBefore() {
        assertTrue(allThemes().size() >= 7,
                "only " + allThemes().size() + " themes");
    }

    @Test void idsAreUniqueAndStable() {
        Set<String> seen = new HashSet<>();
        for (TableTheme t : allThemes()) {
            assertNotNull(t.id());
            assertFalse(t.id().isBlank(), t.displayName() + " has no id");
            assertTrue(seen.add(t.id()), "duplicate theme id: " + t.id());
            assertEquals(t.id().toLowerCase(java.util.Locale.ROOT), t.id(),
                    "ids are compared case-insensitively; keep them lowercase");
        }
    }

    @Test void displayNamesAreUniqueAndDistinctFromIds() {
        Set<String> names = new HashSet<>();
        for (TableTheme t : allThemes()) {
            assertTrue(names.add(t.displayName()), "duplicate name: " + t.displayName());
            assertNotEquals(t.displayName(), t.id(),
                    t.displayName() + " uses its display name as its id, so renaming it "
                            + "in the picker would reset everyone's saved preference");
        }
    }

    @Test void theSavedDefaultStillResolves() {
        String defaultId = new GameSettings(null).themeId;
        assertTrue(allThemes().stream().anyMatch(t -> t.id().equalsIgnoreCase(defaultId)),
                "settings default \"" + defaultId + "\" matches no theme");
    }

    @Test void everyThemeLooksDifferentFromTheOthers() {
        // Two palettes that resolve to nearly the same felt are, to a player,
        // the same theme listed twice.
        List<TableTheme> themes = allThemes();
        for (int i = 0; i < themes.size(); i++) {
            for (int j = i + 1; j < themes.size(); j++) {
                int distance = colourDistance(themes.get(i).feltBottom(), themes.get(j).feltBottom())
                             + colourDistance(themes.get(i).feltTop(),    themes.get(j).feltTop());
                assertTrue(distance > 40,
                        themes.get(i).displayName() + " and " + themes.get(j).displayName()
                                + " have nearly identical felt (distance " + distance + ")");
            }
        }
    }

    @Test void cardFacesStayReadableAgainstTheirOwnBackground() {
        // A palette with dark ink on a dark card is unusable, and nothing else
        // in the build would catch it.
        for (TableTheme t : BuiltinThemes.all()) {
            BufferedImage img = new BufferedImage(200, 260, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            try {
                t.paintCardFace(g, 10, 10, new Card(Rank.KING, Suit.SPADES));
            } finally {
                g.dispose();
            }
            // Sample the middle of the card, which carries the large glyph, and
            // check the face has both light and dark pixels -- i.e. ink shows.
            Set<Integer> tones = new HashSet<>();
            for (int x = 20; x < 100; x += 4) {
                for (int y = 40; y < 130; y += 4) {
                    tones.add(luma(new Color(img.getRGB(x, y))) > 128 ? 1 : 0);
                }
            }
            assertEquals(2, tones.size(),
                    t.displayName() + " card face has no visible contrast between ink and card");
        }
    }

    @Test void everyThemeRendersWithoutThrowing() {
        for (TableTheme t : allThemes()) {
            BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                for (Suit s : Suit.values()) {
                    for (Rank r : Rank.values()) {
                        final TableTheme theme = t;
                        assertDoesNotThrow(() -> theme.paintCardFace(g, 5, 5, new Card(r, s)),
                                t.displayName() + " failed on " + r + " of " + s);
                    }
                }
                assertDoesNotThrow(() -> t.paintCardBack(g, 5, 5), t.displayName() + " card back");
                for (int v : new int[]{1, 5, 25, 100, 500, 7}) {
                    assertDoesNotThrow(() -> t.paintChip(g, 60, 60, 24, v),
                            t.displayName() + " chip " + v);
                }
            } finally {
                g.dispose();
            }
        }
    }

    @Test void tinyChipsAreDeclinedRatherThanDrawnWrong() {
        // Negative-size ovals are the classic failure here; the renderer takes
        // radii from layout arithmetic that can go small.
        BufferedImage img = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            for (TableTheme t : BuiltinThemes.all()) {
                for (int radius : new int[]{0, 1, 3, 4}) {
                    assertDoesNotThrow(() -> t.paintChip(g, 30, 30, radius, 25),
                            t.displayName() + " at radius " + radius);
                }
            }
        } finally {
            g.dispose();
        }
    }

    @Test void highlightIsTranslucentSoCardsStayVisibleUnderIt() {
        for (TableTheme t : allThemes()) {
            int alpha = t.highlight().getAlpha();
            assertTrue(alpha > 0 && alpha < 160,
                    t.displayName() + " highlight alpha is " + alpha
                            + "; opaque would hide the active hand it's meant to mark");
        }
    }


    @Test void desktopExposesExactlyTheThemesCoreDefines() {
        // The whole point of moving palettes to core: if these lists diverge,
        // a player has themes on one platform and not the other, which is the
        // situation this change existed to end.
        Set<String> desktop = new HashSet<>();
        for (TableTheme t : allThemes()) desktop.add(t.id());
        Set<String> shared = new HashSet<>();
        for (TablePalette p : Palettes.all()) shared.add(p.id());
        assertEquals(shared, desktop,
                "desktop themes and core palettes have drifted apart");
    }

    @Test void desktopColoursMatchTheSharedDefinition() {
        // Swing keeps hand-written renderers for Classic and Neon, but their
        // colours must still come from the one definition, or "Neon" means two
        // different things depending on which build you opened.
        for (TableTheme t : allThemes()) {
            TablePalette p = Palettes.byId(t.id());
            assertEquals(new Color(p.feltTop()),    t.feltTop(),
                    t.displayName() + " felt top differs from the shared palette");
            assertEquals(new Color(p.feltBottom()), t.feltBottom(),
                    t.displayName() + " felt bottom differs from the shared palette");
            assertEquals(new Color(p.accent()),     t.accent(),
                    t.displayName() + " accent differs from the shared palette");
        }
    }

    private static int luma(Color c) {
        return (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
    }

    private static int colourDistance(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed())
             + Math.abs(a.getGreen() - b.getGreen())
             + Math.abs(a.getBlue() - b.getBlue());
    }
}
