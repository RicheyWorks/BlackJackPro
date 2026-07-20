package com.richeyworks.blackjack.table;

import java.util.List;
import java.util.Locale;

import static com.richeyworks.blackjack.table.TablePalette.BackStyle;

/**
 * Every table look the game ships, defined once for every platform.
 *
 * <p>These used to live in the Swing module as {@code java.awt.Color} constants,
 * which is why the mobile build had a single hardcoded green felt and no theme
 * picker at all. Moving them here means desktop and mobile render the same seven
 * themes from the same numbers.
 *
 * <p>Chip colours are deliberately <em>not</em> part of a palette: a player
 * learns that green means 25 once, and re-tinting the chips per theme would
 * make them harder to read in exchange for decoration.
 */
public final class Palettes {

    private Palettes() {}

    /** The default. Traditional casino green. */
    public static TablePalette classic() {
        return new TablePalette("classic", "Classic Felt",
                0x14513B, 0x062418, 0xC9A227,
                0xFFFFFF, 0x2B2B2B, 0xC0392B, 0x2B2B2B,
                0x14213D, 0x1E3A5F, BackStyle.HATCH);
    }

    /** Cyberpunk purple and cyan. */
    public static TablePalette neon() {
        return new TablePalette("neon", "Neon",
                0x2C003E, 0x000814, 0x18FFFF,
                0x101820, 0x18FFFF, 0xFF2CB6, 0xFF2CB6,
                0x14002A, 0xFF2CB6, BackStyle.CHEVRON);
    }

    /** Deep navy and silver. Quiet, late, easy on the eyes. */
    public static TablePalette midnight() {
        return new TablePalette("midnight", "Midnight",
                0x18243A, 0x070C16, 0xBFC7D5,
                0xF4F6FA, 0x1B2430, 0xB4405A, 0x39465C,
                0x101A2C, 0x243550, BackStyle.RINGS);
    }

    /** Deep red and old gold. The velvet-rope end of the casino. */
    public static TablePalette crimson() {
        return new TablePalette("crimson", "Crimson Room",
                0x5A1220, 0x230709, 0xD4AF37,
                0xFBF3E4, 0x2A1B14, 0xA8232B, 0x6A4A2A,
                0x3A0C15, 0x6E2130, BackStyle.CHEVRON);
    }

    /** Warm sand and terracotta. Afternoon light, no hurry. */
    public static TablePalette desert() {
        return new TablePalette("desert", "Desert Sun",
                0xB07A46, 0x5C3B1E, 0xF2C14E,
                0xFFF8EC, 0x3B2A18, 0xC2452D, 0x8A6538,
                0x7A4B24, 0xA6763C, BackStyle.HATCH);
    }

    /**
     * Near-black and white at maximum contrast, with all four suits inked in
     * black. Also the most legible option for anyone who finds the coloured
     * felts hard to read.
     */
    public static TablePalette ink() {
        return new TablePalette("ink", "Ink",
                0x2B2B2B, 0x0B0B0B, 0xF0F0F0,
                0xFFFFFF, 0x000000, 0x000000, 0x555555,
                0x161616, 0x3C3C3C, BackStyle.PLAIN);
    }

    /**
     * Teal and brass, hard geometry. Art-deco, 1930s.
     *
     * <p>Teal rather than the emerald this began as: emerald landed within 31
     * colour-units of Classic Felt, which to a player is the same theme listed
     * twice. A test catches that now.
     */
    public static TablePalette deco() {
        return new TablePalette("deco", "Brass Deco",
                0x0E6B6B, 0x03303A, 0xC9A96A,
                0xF7F3E8, 0x1E2A26, 0xA8443C, 0x8A7440,
                0x0A3C44, 0x1F7A78, BackStyle.STRIPES);
    }

    /** Every palette, in picker order. */
    public static List<TablePalette> all() {
        return List.of(classic(), neon(), midnight(), crimson(), desert(), ink(), deco());
    }

    /** The palette saved in settings, or {@link #classic()} if the id is unknown. */
    public static TablePalette byId(String id) {
        if (id != null) {
            String wanted = id.trim().toLowerCase(Locale.ROOT);
            for (TablePalette p : all()) {
                if (p.id().equals(wanted)) return p;
            }
        }
        return classic();
    }

    /**
     * The palette after {@code id} in picker order, wrapping at the end. Lets a
     * front end offer theme switching with a single control, which is what the
     * mobile menu needs.
     */
    public static TablePalette next(String id) {
        List<TablePalette> all = all();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id().equals(id)) return all.get((i + 1) % all.size());
        }
        return all.get(0);
    }
}
