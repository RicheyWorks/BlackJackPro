package com.richeyworks.blackjack.table;

import java.util.List;
import java.util.Locale;

/**
 * Which characters sit at which table.
 *
 * <p>Most themes are decoration: pick Midnight and the same three regulars —
 * Marge, Dutch and Priya — carry on talking. The "crewed" themes go further:
 * Pirate Cove seats a pirate crew, Dusty Saloon a frontier crowd, and Nebula a
 * starship watch, each with their own full dialogue banks in the theme's own
 * voice. The felt and the talk change together, which is what makes a theme
 * feel like a place rather than a recolour.
 *
 * <p>This mapping lives in {@code core} for the same reason {@link Palettes}
 * does: both front ends must agree on it, and neither should carry a private
 * copy that can drift. The writing rule (react to what happened, never steer
 * the next bet) applies to every cast and is enforced across all of them by
 * {@code ChatterToneTest}.
 */
public final class Casts {

    private Casts() {}

    /**
     * The cast seated by {@code paletteId}. Every shipped theme seats a crowd
     * that talks like the theme; sibling themes share a fitting cast (Neon and
     * Arcade both get the arcade crowd, the three velvet-rope rooms share the
     * society trio). Unknown or null ids get the default regulars — a save
     * file naming a theme that no longer exists must still produce a talking
     * table. The regulars keep the neutral looks: Classic, Midnight, Ink, and
     * Graphite are *their* rooms.
     */
    public static List<Persona> forPalette(String paletteId) {
        if (paletteId == null) return Personas.defaults();
        return switch (paletteId.trim().toLowerCase(Locale.ROOT)) {
            case "neon", "arcade"            -> ArcadeCast.cast();
            case "abyss", "lagoon"           -> ReefCast.cast();
            case "glacier", "aurora"         -> PolarCast.cast();
            case "harvest"                   -> SpookyCast.cast();
            case "ember"                     -> EmberCast.cast();
            case "evergreen"                 -> HolidayCast.cast();
            case "sakura", "meadow"          -> GardenCast.cast();
            case "crimson", "royal", "deco", "rosewood" -> SocietyCast.cast();
            case "cocoa"                     -> CafeCast.cast();
            case "pirate"                    -> PirateCast.cast();
            case "saloon", "desert"          -> FrontierCast.cast();
            case "nebula"                    -> NebulaCast.cast();
            default                          -> Personas.defaults();
        };
    }

    /** Every cast the game ships, for tests that police the writing rules. */
    public static List<List<Persona>> all() {
        return List.of(Personas.defaults(),
                       ArcadeCast.cast(), ReefCast.cast(), PolarCast.cast(),
                       SpookyCast.cast(), EmberCast.cast(), HolidayCast.cast(),
                       GardenCast.cast(), SocietyCast.cast(), CafeCast.cast(),
                       PirateCast.cast(), FrontierCast.cast(), NebulaCast.cast());
    }
}
