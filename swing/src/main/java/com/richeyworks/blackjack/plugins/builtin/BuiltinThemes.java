package com.richeyworks.blackjack.plugins.builtin;

import com.richeyworks.blackjack.plugin.TableTheme;

import java.awt.Color;
import java.util.List;

import static com.richeyworks.blackjack.plugins.builtin.PaletteTheme.BackStyle;

/**
 * The table looks that ship with the game.
 *
 * <p>Each is a palette handed to {@link PaletteTheme}, so a new one costs a
 * dozen lines instead of a whole renderer. The card-back style varies alongside
 * the colours, which is what keeps a set of palettes from reading as the same
 * theme recoloured five times.
 *
 * <p>Chip colours stay constant across every theme by design — see
 * {@link PaletteTheme#chipColour(int)}.
 */
public final class BuiltinThemes {

    private BuiltinThemes() {}

    /** Deep navy and silver. Quiet, late, easy on the eyes. */
    public static TableTheme midnight() {
        return new PaletteTheme(
                "midnight", "Midnight",
                new Color(0x18243A), new Color(0x070C16),   // felt
                new Color(0xBFC7D5),                        // accent
                new Color(0xF4F6FA), new Color(0x1B2430),   // card face, black ink
                new Color(0xB4405A), new Color(0x39465C),   // red ink, card edge
                new Color(0x101A2C), new Color(0x243550),   // back fill, back line
                BackStyle.RINGS);
    }

    /** Deep red and old gold. The velvet-rope end of the casino. */
    public static TableTheme crimson() {
        return new PaletteTheme(
                "crimson", "Crimson Room",
                new Color(0x5A1220), new Color(0x230709),
                new Color(0xD4AF37),
                new Color(0xFBF3E4), new Color(0x2A1B14),
                new Color(0xA8232B), new Color(0x6A4A2A),
                new Color(0x3A0C15), new Color(0x6E2130),
                BackStyle.CHEVRON);
    }

    /** Warm sand and terracotta. Afternoon light, low stakes, no hurry. */
    public static TableTheme desert() {
        return new PaletteTheme(
                "desert", "Desert Sun",
                new Color(0xB07A46), new Color(0x5C3B1E),
                new Color(0xF2C14E),
                new Color(0xFFF8EC), new Color(0x3B2A18),
                new Color(0xC2452D), new Color(0x8A6538),
                new Color(0x7A4B24), new Color(0xA6763C),
                BackStyle.HATCH);
    }

    /**
     * Near-black and white, maximum contrast. Also the most legible option for
     * anyone who finds the coloured felts hard to read.
     */
    public static TableTheme ink() {
        return new PaletteTheme(
                "ink", "Ink",
                new Color(0x2B2B2B), new Color(0x0B0B0B),
                new Color(0xF0F0F0),
                new Color(0xFFFFFF), new Color(0x000000),
                new Color(0x000000), new Color(0x555555),   // red suits in black too
                new Color(0x161616), new Color(0x3C3C3C),
                BackStyle.PLAIN);
    }

    /**
     * Teal and brass, hard geometry. Art-deco, 1930s.
     *
     * <p>Teal rather than the emerald this started as: emerald sat within 31
     * colour-units of Classic Felt, which to a player is the same theme listed
     * twice. Teal and brass is just as period-correct and unmistakably its own
     * thing next to the green.
     */
    public static TableTheme deco() {
        return new PaletteTheme(
                "deco", "Brass Deco",
                new Color(0x0E6B6B), new Color(0x03303A),
                new Color(0xC9A96A),
                new Color(0xF7F3E8), new Color(0x1E2A26),
                new Color(0xA8443C), new Color(0x8A7440),
                new Color(0x0A3C44), new Color(0x1F7A78),
                BackStyle.STRIPES);
    }

    /** Every palette theme, in menu order. */
    public static List<TableTheme> all() {
        return List.of(midnight(), crimson(), desert(), ink(), deco());
    }
}
