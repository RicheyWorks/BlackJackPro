package com.richeyworks.blackjack.table;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The shared table looks.
 *
 * <p>These live in {@code core} rather than the Swing module so the libGDX and
 * Android builds can use them — Android has no AWT, so a palette built on
 * {@code java.awt.Color} could not even be loaded there, which is why the mobile
 * build had one hardcoded green felt while the desktop had seven themes.
 *
 * <p>Because both front ends now render from these numbers, a mistake here is a
 * mistake on every platform at once. Hence the checks.
 */
class PalettesTest {

    @Test void everyPaletteHasAUniqueStableId() {
        Set<String> ids = new HashSet<>();
        for (TablePalette p : Palettes.all()) {
            assertNotNull(p.id());
            assertFalse(p.id().isBlank(), p.displayName() + " has no id");
            assertEquals(p.id().toLowerCase(Locale.ROOT), p.id(),
                    "ids are matched lowercase; keep them that way");
            assertTrue(ids.add(p.id()), "duplicate palette id: " + p.id());
        }
    }

    @Test void displayNamesAreDistinctAndNotTheIds() {
        Set<String> names = new HashSet<>();
        for (TablePalette p : Palettes.all()) {
            assertTrue(names.add(p.displayName()), "duplicate name: " + p.displayName());
            assertNotEquals(p.displayName(), p.id(),
                    p.displayName() + " uses its display name as its id, so renaming it "
                            + "would silently reset every saved preference");
        }
    }

    @Test void thereAreSevenThemesOnEveryPlatform() {
        assertEquals(7, Palettes.all().size());
    }

    @Test void noTwoThemesLookAlike() {
        // Two palettes that resolve to nearly the same felt are, to a player,
        // one theme listed twice. This caught Emerald Deco sitting 31 units
        // from Classic Felt, which is why Deco is teal now.
        List<TablePalette> all = Palettes.all();
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                int d = distance(all.get(i).feltTop(),    all.get(j).feltTop())
                      + distance(all.get(i).feltBottom(), all.get(j).feltBottom());
                assertTrue(d > 40, all.get(i).displayName() + " and " + all.get(j).displayName()
                        + " have nearly identical felt (distance " + d + ")");
            }
        }
    }

    @Test void cardInkIsReadableOnTheCardFace() {
        // A palette with dark ink on a dark card is unusable, and no renderer
        // would notice -- it would just draw an unreadable card.
        for (TablePalette p : Palettes.all()) {
            int face = TablePalette.luma(p.cardFace());
            assertTrue(Math.abs(face - TablePalette.luma(p.cardInk())) > 60,
                    p.displayName() + ": black-suit ink is too close to the card face");
            assertTrue(Math.abs(face - TablePalette.luma(p.cardRed())) > 60,
                    p.displayName() + ": red-suit ink is too close to the card face");
        }
    }

    @Test void accentStandsOutAgainstTheFelt() {
        for (TablePalette p : Palettes.all()) {
            assertTrue(Math.abs(TablePalette.luma(p.accent()) - TablePalette.luma(p.feltBottom())) > 40,
                    p.displayName() + ": accent is nearly invisible on its own felt");
        }
    }

    @Test void cardBacksAreDecoratedDistinctly() {
        // Varying the back style is what stops seven recolours reading as one
        // theme seven times, so at least three of the five should be in use.
        Set<TablePalette.BackStyle> used = new HashSet<>();
        for (TablePalette p : Palettes.all()) used.add(p.backStyle());
        assertTrue(used.size() >= 3, "only " + used.size() + " distinct card-back styles");
    }

    /* ---------- lookup ---------- */

    @Test void anUnknownIdFallsBackToClassicRatherThanFailing() {
        // A save file can name a theme from a plugin that is no longer
        // installed. That must not stop the game starting.
        assertEquals("classic", Palettes.byId("no-such-theme").id());
        assertEquals("classic", Palettes.byId(null).id());
        assertEquals("classic", Palettes.byId("").id());
    }

    @Test void lookupIsForgivingAboutCaseAndWhitespace() {
        assertEquals("midnight", Palettes.byId("MIDNIGHT").id());
        assertEquals("midnight", Palettes.byId("  midnight  ").id());
    }

    @Test void everyPaletteIsRetrievableById() {
        for (TablePalette p : Palettes.all()) {
            assertEquals(p.id(), Palettes.byId(p.id()).id());
        }
    }

    @Test void cyclingVisitsEveryThemeAndWrapsAround() {
        // The mobile picker cycles rather than opening a submenu, so a broken
        // next() would strand the player on a subset of the themes.
        String start = Palettes.all().get(0).id();
        Set<String> seen = new HashSet<>();
        String id = start;
        for (int i = 0; i < Palettes.all().size(); i++) {
            seen.add(id);
            id = Palettes.next(id).id();
        }
        assertEquals(Palettes.all().size(), seen.size(), "cycle misses themes: " + seen);
        assertEquals(start, id, "cycle does not return to where it started");
    }

    @Test void cyclingFromAnUnknownIdStillGoesSomewhere() {
        assertNotNull(Palettes.next("gone").id());
    }

    /* ---------- colour helpers ---------- */

    @Test void channelsUnpackCorrectly() {
        int c = 0x123456;
        assertEquals(0x12, TablePalette.red(c));
        assertEquals(0x34, TablePalette.green(c));
        assertEquals(0x56, TablePalette.blue(c));
    }

    @Test void contrastPicksTheReadableInk() {
        assertEquals(0x000000, TablePalette.contrastOn(0xFFFFFF), "black on white");
        assertEquals(0xFFFFFF, TablePalette.contrastOn(0x000000), "white on black");
        assertEquals(0xFFFFFF, TablePalette.contrastOn(0x2B2B2B), "white on dark grey");
    }

    private static int distance(int a, int b) {
        return Math.abs(TablePalette.red(a)   - TablePalette.red(b))
             + Math.abs(TablePalette.green(a) - TablePalette.green(b))
             + Math.abs(TablePalette.blue(a)  - TablePalette.blue(b));
    }
}
