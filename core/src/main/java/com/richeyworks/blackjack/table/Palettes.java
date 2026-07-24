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

    /**
     * Weathered wood, deep harbour water, doubloon gold. The first of the
     * "crewed" themes: picking it also seats a matching cast of characters —
     * see {@code Casts.forPalette}.
     */
    public static TablePalette pirate() {
        return new TablePalette("pirate", "Pirate Cove",
                0x0F3E4A, 0x071E26, 0xE0B84C,
                0xF2E8D0, 0x2A211A, 0x9E2B25, 0x6B5637,
                0x3A2A1A, 0x8A6A3C, BackStyle.HATCH);
    }

    /** Dark saddle leather and whiskey light. Crewed: seats the frontier cast. */
    public static TablePalette saloon() {
        return new TablePalette("saloon", "Dusty Saloon",
                0x5E2F17, 0x2A1207, 0xD9A441,
                0xFDF6E3, 0x2E1D10, 0xB03A2E, 0x7A5A32,
                0x4A2412, 0x9A6C3A, BackStyle.STRIPES);
    }

    /**
     * Deep violet space and starlight. Crewed: seats the nebula cast. The
     * "red" suits are inked in nebula purple — the Ink theme set the precedent
     * that suit colour is the theme's to choose, provided it stays readable.
     */
    public static TablePalette nebula() {
        return new TablePalette("nebula", "Nebula",
                0x2A0E4E, 0x08021A, 0x7FD4FF,
                0xF0EEFA, 0x1A1230, 0x8B2FA8, 0x4A3F6E,
                0x0D0A22, 0x5A4A9E, BackStyle.CHEVRON);
    }

    /* ---------------------------------------------------------------------
     * The second wave. Every constraint the tests enforce (felt distance,
     * ink contrast, accent legibility) was validated numerically before
     * these values were committed, so tweak with the tests running.
     * ------------------------------------------------------------------ */

    /** Deep-sea blue, bioluminescent accent. */
    public static TablePalette abyss() {
        return new TablePalette("abyss", "The Abyss",
                0x062B45, 0x010C1B, 0x4FC3F7,
                0xE8F1F8, 0x12283A, 0x1B6FA8, 0x3A5A72,
                0x04182A, 0x2E6E96, BackStyle.WAVES);
    }

    /** Cherry-blossom pink over dark plum. Spring evening. */
    public static TablePalette sakura() {
        return new TablePalette("sakura", "Sakura",
                0xA85C6E, 0x5C2735, 0xFFD9E0,
                0xFFF5F7, 0x3A2430, 0xC2455A, 0x8A5A66,
                0x6E3242, 0xC97C90, BackStyle.DOTS);
    }

    /** Pale arctic blues. The lightest felt in the set. */
    public static TablePalette glacier() {
        return new TablePalette("glacier", "Glacier",
                0x7FA8C9, 0x3E5A75, 0xF0F8FF,
                0xFBFDFF, 0x1E3448, 0x3E7FB8, 0x7FA0BE,
                0x2E4A63, 0x8FB8D8, BackStyle.DIAMONDS);
    }

    /** Near-black scorch with molten orange. */
    public static TablePalette ember() {
        return new TablePalette("ember", "Ember",
                0x2E0A06, 0x120301, 0xFF7A3C,
                0xFFEFE4, 0x33150A, 0xC24E1E, 0x6E3A22,
                0x200705, 0x8A3A16, BackStyle.STARBURST);
    }

    /**
     * Phosphor green on CRT black, amber for the "red" suits. Like Ink and
     * Nebula, the suit colours belong to the theme — dark card faces with
     * glowing ink, same trick Neon pulls.
     */
    public static TablePalette arcade() {
        return new TablePalette("arcade", "Arcade",
                0x081F08, 0x010801, 0x39FF14,
                0x0A140A, 0x39FF14, 0xFFB000, 0x1E7A1E,
                0x041004, 0x2ECC10, BackStyle.DOTS);
    }

    /** Pumpkin and bonfire browns. October. */
    public static TablePalette harvest() {
        return new TablePalette("harvest", "Harvest Night",
                0x3D2206, 0x1A0C01, 0xFF9E2C,
                0xFFF3DC, 0x2E1B06, 0xC2571B, 0x7A5426,
                0x261204, 0xA86A1E, BackStyle.HATCH);
    }

    /** Deep pine and ribbon red. December. */
    public static TablePalette evergreen() {
        return new TablePalette("evergreen", "Evergreen",
                0x0B3D2E, 0x041A10, 0xD64545,
                0xFFFBF2, 0x1E3B2C, 0xB03030, 0x4A6E52,
                0x07261B, 0xB04848, BackStyle.RINGS);
    }

    /** Royal purple and old gold. The high-limit room. */
    public static TablePalette royal() {
        return new TablePalette("royal", "Royal Court",
                0x3B1B6E, 0x170A33, 0xE5B84B,
                0xF7F2FF, 0x2A1A4A, 0x7A3AB8, 0x6A5492,
                0x241048, 0x8A6ACA, BackStyle.STRIPES);
    }

    /** Dusky rose and burgundy. Softer than Crimson Room, and pinker. */
    public static TablePalette rosewood() {
        return new TablePalette("rosewood", "Rosewood",
                0x7A2440, 0x38101E, 0xF2A0B8,
                0xFFF0F4, 0x3D1626, 0xB83A5C, 0x8A4A5E,
                0x4A1628, 0xA05A72, BackStyle.RINGS);
    }

    /** Bright tropical teal. The daylight counterpart to The Abyss. */
    public static TablePalette lagoon() {
        return new TablePalette("lagoon", "Lagoon",
                0x128C8C, 0x065252, 0xFFE08A,
                0xF2FFFC, 0x0E3D3D, 0xD9633B, 0x3A7A72,
                0x0A4A4A, 0x3AAFA0, BackStyle.WAVES);
    }

    /** Cool slate grey with a safety-orange accent. Workshop, not casino. */
    public static TablePalette graphite() {
        return new TablePalette("graphite", "Graphite",
                0x3E4652, 0x1A1E26, 0xFF8A3C,
                0xF4F5F7, 0x22262E, 0xC24E2A, 0x5A6472,
                0x22262E, 0x5A6472, BackStyle.PLAIN);
    }

    /** Spring grass and buttercup. The friendliest felt in the set. */
    public static TablePalette meadow() {
        return new TablePalette("meadow", "Meadow",
                0x4F8A3C, 0x25491C, 0xFFF3B0,
                0xFCFFF0, 0x2C3D1E, 0xB84A32, 0x6E8A50,
                0x33581F, 0x7FB05A, BackStyle.DOTS);
    }

    /** Roasted browns and steamed cream. A cafe at the quiet end of town. */
    public static TablePalette cocoa() {
        return new TablePalette("cocoa", "Cocoa House",
                0x4A3226, 0x241812, 0xE8C9A0,
                0xFFF8F0, 0x38241A, 0xA85632, 0x7A5A42,
                0x2E1E14, 0x8A6A50, BackStyle.RINGS);
    }

    /** Polar night with a curtain of green light. */
    public static TablePalette aurora() {
        return new TablePalette("aurora", "Aurora",
                0x041E28, 0x020A0E, 0x7CFFB2,
                0xEFFBF4, 0x10312A, 0x3E9E7A, 0x3A6E5A,
                0x06222A, 0x2E8A6E, BackStyle.WAVES);
    }

    /** Every palette, in picker order. */
    public static List<TablePalette> all() {
        return List.of(classic(), neon(), midnight(), crimson(), desert(), ink(), deco(),
                       abyss(), sakura(), glacier(), ember(), arcade(), harvest(),
                       evergreen(), royal(), rosewood(), lagoon(), graphite(),
                       meadow(), cocoa(), aurora(),
                       pirate(), saloon(), nebula());
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
