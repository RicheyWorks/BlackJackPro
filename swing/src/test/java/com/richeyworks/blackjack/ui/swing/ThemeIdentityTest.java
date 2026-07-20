package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.plugin.TableTheme;
import com.richeyworks.blackjack.plugins.builtin.NeonTheme;
import com.richeyworks.blackjack.settings.GameSettings;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The launcher restores the player's theme by matching {@code settings.themeId}
 * against each theme's id. That match previously used the display name, so the
 * stored default {@code "classic"} could never equal {@code "Classic Felt"} and
 * the saved preference was silently ignored on every launch.
 */
class ThemeIdentityTest {

    private static final List<TableTheme> SHIPPED = List.of(new ClassicTheme(), new NeonTheme());

    @Test void shippedThemesHaveStableIds() {
        assertEquals("classic", new ClassicTheme().id());
        assertEquals("neon",    new NeonTheme().id());
    }

    @Test void theDefaultSettingMatchesAShippedTheme() {
        String defaultId = new GameSettings(null).themeId;
        assertTrue(SHIPPED.stream().anyMatch(t -> t.id().equalsIgnoreCase(defaultId)),
                "settings default \"" + defaultId + "\" matches no shipped theme");
    }

    @Test void idsAreUnique() {
        Set<String> seen = new HashSet<>();
        for (TableTheme t : SHIPPED) {
            assertTrue(seen.add(t.id()), "duplicate theme id: " + t.id());
        }
    }

    @Test void idIsIndependentOfDisplayName() {
        // Renaming a theme in the picker must not reset everyone's preference,
        // so the two must not be the same string.
        for (TableTheme t : SHIPPED) {
            assertNotEquals(t.displayName(), t.id(),
                    t.displayName() + " uses its display name as its id");
        }
    }

    @Test void aThemeWithoutAnExplicitIdFallsBackToItsName() {
        TableTheme minimal = new TableTheme() {
            @Override public String displayName() { return "Sunset Room"; }
            @Override public Color feltTop()      { return Color.RED; }
            @Override public Color feltBottom()   { return Color.BLACK; }
            @Override public void paintCardFace(Graphics2D g, int x, int y,
                                                com.richeyworks.blackjack.engine.Card c) { }
            @Override public void paintCardBack(Graphics2D g, int x, int y) { }
            @Override public void paintChip(Graphics2D g, int cx, int cy, int r, int v) { }
        };
        assertEquals("sunset room", minimal.id(), "existing third-party themes keep working");
    }

    @Test void settingsRoundTripAThemeId() {
        GameSettings s = new GameSettings(null);
        s.themeId = new NeonTheme().id();
        assertTrue(SHIPPED.stream().anyMatch(t -> t.id().equalsIgnoreCase(s.themeId)));
    }
}
