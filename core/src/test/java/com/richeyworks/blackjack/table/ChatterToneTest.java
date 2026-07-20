package com.richeyworks.blackjack.table;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enforces the writing rule for table chatter mechanically.
 *
 * <p>This is a wagering game. A table of characters egging the player toward a
 * bigger stake would be a genuinely nasty thing to ship, and it is the kind of
 * thing that creeps in one well-meaning line at a time — a "go on, press it"
 * written for colour six months from now reads as harmless in isolation.
 *
 * <p>So the rule is checked rather than merely documented: personas react to
 * what happened and never steer what happens next. No line urges a bigger bet,
 * frames a win as owed, or suggests recovering a loss.
 */
class ChatterToneTest {

    /**
     * Phrases that push the player's stake or frame gambling as recovery.
     * Word-boundary matched so ordinary words containing them don't false-fire.
     */
    private static final List<Pattern> BANNED = List.of(
            // urging a larger stake
            Pattern.compile("\\bbet (more|bigger|it all|the lot)\\b"),
            Pattern.compile("\\braise (your|the) (bet|stake)\\b"),
            Pattern.compile("\\bpress (it|your luck|on)\\b"),
            Pattern.compile("\\blet it ride\\b"),
            Pattern.compile("\\ball[- ]in\\b"),
            Pattern.compile("\\bgo big\\b"),
            Pattern.compile("\\bdouble or nothing\\b"),
            Pattern.compile("\\bmax(imum)? bet\\b"),
            Pattern.compile("\\bup (your|the) (bet|stake)\\b"),
            // the gambler's fallacy, and chasing
            Pattern.compile("\\byou'?re due\\b"),
            Pattern.compile("\\bdue (a|for) (win|one)\\b"),
            Pattern.compile("\\bowes? you\\b"),
            Pattern.compile("\\bchase\\b"),
            Pattern.compile("\\bwin it back\\b"),
            Pattern.compile("\\bget it back\\b"),
            Pattern.compile("\\bmake it back\\b"),
            Pattern.compile("\\bone more (hand|round)\\b"),
            Pattern.compile("\\bcan'?t lose\\b"),
            Pattern.compile("\\bsure thing\\b"),
            Pattern.compile("\\bguaranteed\\b"));

    private static List<String> everyLine() {
        List<String> out = new ArrayList<>();
        for (Persona p : Personas.defaults()) {
            for (TableEvent e : TableEvent.values()) out.addAll(p.linesFor(e));
        }
        return out;
    }

    @Test void noLineUrgesABiggerBetOrChasingALoss() {
        for (Persona p : Personas.defaults()) {
            for (TableEvent e : TableEvent.values()) {
                for (String line : p.linesFor(e)) {
                    String lower = line.toLowerCase(Locale.ROOT);
                    for (Pattern banned : BANNED) {
                        assertFalse(banned.matcher(lower).find(),
                                p.name() + " on " + e + " matches banned pattern "
                                        + banned.pattern() + ": \"" + line + "\"");
                    }
                }
            }
        }
    }

    @Test void theTableGetsGentlerWhenChipsRunLow() {
        // The one moment where a real table would either go quiet or say
        // something kind. Nothing here should read as excitement.
        int covered = 0;
        for (Persona p : Personas.defaults()) {
            List<String> lines = p.linesFor(TableEvent.LOW_CHIPS);
            if (lines.isEmpty()) continue;
            covered++;
            for (String line : lines) {
                assertFalse(line.contains("!"),
                        p.name() + " is excited about the player running out: \"" + line + "\"");
            }
        }
        assertEquals(3, covered, "every persona should have something kind for LOW_CHIPS");
    }

    @Test void aColdStreakIsNotCheeredOn() {
        for (Persona p : Personas.defaults()) {
            for (String line : p.linesFor(TableEvent.COLD_STREAK)) {
                assertFalse(line.contains("!"),
                        p.name() + " is excited about a losing run: \"" + line + "\"");
            }
        }
    }

    @Test void everyLineIsPresentableOnScreen() {
        for (String line : everyLine()) {
            assertNotNull(line);
            assertFalse(line.isBlank(), "blank line would render an empty bubble");
            assertEquals(line.trim(), line, "stray whitespace: \"" + line + "\"");
            // Bubbles are drawn on the felt beside a seat; long lines either
            // wrap into the cards or get clipped.
            assertTrue(line.length() <= 78, "too long for a bubble (" + line.length() + "): " + line);
        }
    }

    @Test void noLineIsDuplicatedAcrossCharacters() {
        // Two characters sharing a line makes both feel like the same script.
        List<String> all = everyLine();
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                assertNotEquals(all.get(i), all.get(j), "duplicated line: " + all.get(i));
            }
        }
    }

    @Test void everyReactionHasEnoughVarietyToNotFeelScripted() {
        for (Persona p : Personas.defaults()) {
            for (TableEvent e : TableEvent.values()) {
                List<String> lines = p.linesFor(e);
                if (lines.isEmpty()) continue;
                assertEquals(lines.size(), lines.stream().distinct().count(),
                        p.name() + " repeats itself within " + e);
            }
        }
    }

    @Test void theCharactersActuallySoundDifferent() {
        // A crude but real check: three people writing in the same voice would
        // share most of their vocabulary.
        List<Persona> ps = Personas.defaults();
        for (int i = 0; i < ps.size(); i++) {
            for (int j = i + 1; j < ps.size(); j++) {
                var a = words(ps.get(i));
                var b = words(ps.get(j));
                var shared = new java.util.HashSet<>(a);
                shared.retainAll(b);
                double overlap = (double) shared.size() / Math.min(a.size(), b.size());
                assertTrue(overlap < 0.5,
                        ps.get(i).name() + " and " + ps.get(j).name()
                                + " share " + Math.round(overlap * 100) + "% of their vocabulary");
            }
        }
    }

    private static java.util.Set<String> words(Persona p) {
        var out = new java.util.HashSet<String>();
        for (TableEvent e : TableEvent.values()) {
            for (String line : p.linesFor(e)) {
                for (String w : line.toLowerCase(Locale.ROOT).split("[^a-z']+")) {
                    if (w.length() > 3) out.add(w);
                }
            }
        }
        return out;
    }
}
