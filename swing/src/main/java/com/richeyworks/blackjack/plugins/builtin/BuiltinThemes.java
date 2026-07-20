package com.richeyworks.blackjack.plugins.builtin;

import com.richeyworks.blackjack.plugin.TableTheme;
import com.richeyworks.blackjack.table.Palettes;
import com.richeyworks.blackjack.table.TablePalette;

import java.util.ArrayList;
import java.util.List;

/**
 * The Swing side of the shipped table looks.
 *
 * <p>The colours themselves live in {@link Palettes} in {@code core}, so the
 * libGDX and Android builds draw the same seven themes from the same numbers.
 * This module only supplies the {@code Graphics2D} renderer.
 *
 * <p>{@code ClassicTheme} and {@code NeonTheme} keep their hand-written
 * renderers on the desktop — Neon's scanline card backs in particular don't
 * reduce to a palette — but their colours are still defined once in
 * {@code Palettes}, so mobile gets a recognisable version of both.
 */
public final class BuiltinThemes {

    private BuiltinThemes() {}

    /**
     * Palette-driven themes only. Classic and Neon are contributed separately by
     * their own renderers, so listing them here would show each twice.
     */
    public static List<TableTheme> all() {
        List<TableTheme> out = new ArrayList<>();
        for (TablePalette p : Palettes.all()) {
            if (p.id().equals("classic") || p.id().equals("neon")) continue;
            out.add(new PaletteTheme(p));
        }
        return List.copyOf(out);
    }

    /** A single palette theme by id, for tests and plugins. */
    public static TableTheme byId(String id) {
        return new PaletteTheme(Palettes.byId(id));
    }
}
