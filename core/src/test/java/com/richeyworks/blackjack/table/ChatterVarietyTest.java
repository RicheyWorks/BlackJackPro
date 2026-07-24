package com.richeyworks.blackjack.table;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards against the table going stale, which is the failure mode a dialogue
 * bank actually dies of. It isn't that the writing is bad — it's that a player
 * hears the same sentence for the second time and the illusion collapses.
 *
 * <p>Two things protect against it: enough lines, and a picker that doesn't
 * draw them uniformly at random. These tests hold both to account.
 */
class ChatterVarietyTest {

    /** Below this a bank starts to feel like a script rather than a person. */
    private static final int MIN_LINES_PER_EVENT = 6;

    /**
     * The same character with their real line bank but guaranteed to speak.
     *
     * <p>The shipped personas decline most draws by design (Priya speaks about
     * a quarter of the time), which is right for the game and useless for
     * testing the picker -- these tests are about which line gets chosen, not
     * whether anyone chooses to talk.
     */
    private static Persona alwaysTalks(Persona p) {
        Map<TableEvent, List<String>> lines = new EnumMap<>(TableEvent.class);
        for (TableEvent e : p.events()) lines.put(e, p.linesFor(e));
        return new Persona(p.id(), p.name(), p.seat(), 1.0, lines);
    }

    @Test void everyReactionHasEnoughLinesToNotFeelScripted() {
        for (List<Persona> cast : Casts.all()) {
            for (Persona p : cast) {
                for (TableEvent e : TableEvent.values()) {
                    List<String> lines = p.linesFor(e);
                    if (lines.isEmpty()) continue;      // not reacting at all is fine
                    assertTrue(lines.size() >= MIN_LINES_PER_EVENT,
                            p.name() + " has only " + lines.size() + " lines for " + e);
                }
            }
        }
    }

    @Test void everyTableHasSubstantialDepthOverall() {
        // Per cast, because the player only ever hears one cast at a time: a
        // thin pirate table is not rescued by the regulars being deep.
        for (List<Persona> cast : Casts.all()) {
            int total = 0;
            for (Persona p : cast) {
                for (TableEvent e : TableEvent.values()) total += p.linesFor(e).size();
            }
            assertTrue(total >= 400, cast.get(0).name() + "'s table has only "
                    + total + " lines across all three seats");
        }
    }

    @Test void theGameAsAWholeShipsADeepBank() {
        int total = 0;
        for (List<Persona> cast : Casts.all()) {
            for (Persona p : cast) {
                for (TableEvent e : TableEvent.values()) total += p.linesFor(e).size();
            }
        }
        assertTrue(total >= 5000, "only " + total + " lines across every cast");
    }

    @Test void everyPersonaCarriesItsShare() {
        // One character doing all the talking makes the other two furniture.
        for (List<Persona> cast : Casts.all()) {
            for (Persona p : cast) {
                int count = 0;
                for (TableEvent e : TableEvent.values()) count += p.linesFor(e).size();
                assertTrue(count >= 140, p.name() + " only has " + count + " lines");
            }
        }
    }

    /* ---------- the picker, not just the bank ---------- */

    @Test void aLineIsNotHeardTwiceWithinTheRecentWindow() {
        // The real anti-staleness property: consecutive draws from one bank
        // should not collide, which uniform random picking would happily do.
        Persona p = alwaysTalks(Personas.marge());
        TableChatter c = new TableChatter(List.of(p), new Random(31), 0, 0, 1_000);

        int bank = p.linesFor(TableEvent.PLAYER_BUST).size();
        int window = Math.min(5, bank - 1);

        List<String> seen = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            seen.add(c.react(TableEvent.PLAYER_BUST, 1_000_000 + i * 100L).orElseThrow().text());
        }
        for (int i = 0; i < seen.size(); i++) {
            int from = Math.max(0, i - window);
            for (int j = from; j < i; j++) {
                assertNotEquals(seen.get(j), seen.get(i),
                        "repeat within the recent window at draw " + i + ": " + seen.get(i));
            }
        }
    }

    @Test void thePickerEventuallyUsesTheWholeBank() {
        // A window that's too aggressive could strand lines permanently.
        Persona p = alwaysTalks(Personas.dutch());
        TableChatter c = new TableChatter(List.of(p), new Random(17), 0, 0, 1_000);

        Set<String> used = new HashSet<>();
        for (int i = 0; i < 400; i++) {
            used.add(c.react(TableEvent.PLAYER_WIN, 1_000_000 + i * 100L).orElseThrow().text());
        }
        assertEquals(p.linesFor(TableEvent.PLAYER_WIN).size(), used.size(),
                "some lines are never reachable");
    }

    @Test void theBankIsDrawnFairlyEvenly() {
        // No line should dominate; a lopsided distribution reads as repetition
        // even when the bank is technically large.
        Persona p = alwaysTalks(Personas.priya());
        TableChatter c = new TableChatter(List.of(p), new Random(5), 0, 0, 1_000);

        Map<String, Integer> counts = new HashMap<>();
        int draws = 3000;
        for (int i = 0; i < draws; i++) {
            String line = c.react(TableEvent.DEALER_BUST, 1_000_000 + i * 100L).orElseThrow().text();
            counts.merge(line, 1, Integer::sum);
        }
        int bank = p.linesFor(TableEvent.DEALER_BUST).size();
        double expected = (double) draws / bank;
        for (var entry : counts.entrySet()) {
            assertTrue(entry.getValue() > expected * 0.5 && entry.getValue() < expected * 1.6,
                    "\"" + entry.getKey() + "\" used " + entry.getValue()
                            + " times, expected around " + Math.round(expected));
        }
    }

    @Test void clearResetsTheHistorySoANewSessionStartsFresh() {
        Persona p = alwaysTalks(Personas.marge());
        TableChatter c = new TableChatter(List.of(p), new Random(8), 0, 0, 1_000);
        String first = c.react(TableEvent.PLAYER_WIN, 1_000).orElseThrow().text();
        c.clear();
        // With history wiped, the very first line is drawable again -- over
        // enough restarts it must reappear, or clear() isn't clearing.
        boolean seenAgain = false;
        for (int attempt = 0; attempt < 200 && !seenAgain; attempt++) {
            c.clear();
            seenAgain = first.equals(c.react(TableEvent.PLAYER_WIN, 2_000 + attempt).orElseThrow().text());
        }
        assertTrue(seenAgain, "clear() left the history in place");
    }

    /* ---------- how long until a player hears a repeat ---------- */

    @Test void aLongSessionKeepsProducingNewLines() {
        // The number that actually matters: play a few hundred hands and count
        // how much of what you hear is something you haven't heard before.
        TableChatter c = new TableChatter(Personas.defaults(), new Random(2026), 0, 0, 1_000);
        TableEvent[] events = TableEvent.values();
        Random pick = new Random(99);

        Set<String> distinct = new HashSet<>();
        int spoken = 0;
        for (int hand = 0; hand < 400; hand++) {
            var r = c.react(events[pick.nextInt(events.length)], 1_000_000 + hand * 1_000L);
            if (r.isPresent()) { spoken++; distinct.add(r.get().text()); }
        }
        assertTrue(spoken > 100, "not enough remarks to judge variety: " + spoken);
        assertTrue(distinct.size() > 120,
                "only " + distinct.size() + " distinct lines across " + spoken + " remarks");
    }
}
