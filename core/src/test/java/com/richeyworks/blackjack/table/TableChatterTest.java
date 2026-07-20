package com.richeyworks.blackjack.table;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The restraint rules, which are the whole difficulty of table chatter.
 * Characters who react to everything stop reading as people almost immediately,
 * so most of what {@link TableChatter} does is decline to speak.
 *
 * <p>Time is a parameter and the RNG is injected, so none of this is flaky.
 */
class TableChatterTest {

    /** A persona that always wants to talk, so cooldowns are what's under test. */
    private static Persona loud(String id, int seat, TableEvent event, String... lines) {
        return new Persona(id, id, seat, 1.0, Map.of(event, List.of(lines)));
    }

    private static TableChatter chatter(List<Persona> ps, long seed) {
        return new TableChatter(ps, new Random(seed));
    }

    @Test void nobodySpeaksAboutAnEventTheyHaveNoLineFor() {
        TableChatter c = chatter(List.of(loud("a", 0, TableEvent.PLAYER_WIN, "yes")), 1);
        assertTrue(c.react(TableEvent.DEALER_BUST, 100_000).isEmpty());
    }

    @Test void aNullEventIsIgnored() {
        TableChatter c = chatter(Personas.defaults(), 1);
        assertTrue(c.react(null, 100_000).isEmpty());
    }

    @Test void onlyOneVoicePerEvent() {
        // Three personas who all react to the same thing and all want to speak.
        List<Persona> all = List.of(
                loud("a", 0, TableEvent.PLAYER_WIN, "a1"),
                loud("b", 1, TableEvent.PLAYER_WIN, "b1"),
                loud("c", 2, TableEvent.PLAYER_WIN, "c1"));
        TableChatter c = chatter(all, 7);
        Optional<Remark> r = c.react(TableEvent.PLAYER_WIN, 500_000);
        assertTrue(r.isPresent());
        assertEquals(1, c.visibleAt(500_000).size(), "a chorus is never wanted");
    }

    @Test void theTableCooldownSilencesTheNextEvent() {
        List<Persona> all = List.of(
                loud("a", 0, TableEvent.PLAYER_WIN, "a1"),
                loud("b", 1, TableEvent.PLAYER_WIN, "b1"));
        TableChatter c = chatter(all, 3);
        long t = 1_000_000;
        assertTrue(c.react(TableEvent.PLAYER_WIN, t).isPresent());
        assertTrue(c.react(TableEvent.PLAYER_WIN, t + 100).isEmpty(),
                "back-to-back remarks stack up and stop pacing with the game");
        assertTrue(c.react(TableEvent.PLAYER_WIN,
                        t + TableChatter.DEFAULT_TABLE_COOLDOWN_MS + 1).isPresent());
    }

    @Test void aPersonaWillNotDominateTheTable() {
        // Only one persona reacts, so once they speak the table goes quiet for
        // their full cooldown rather than letting them narrate every hand.
        TableChatter c = chatter(List.of(loud("a", 0, TableEvent.PLAYER_WIN, "a1", "a2")), 5);
        long t = 2_000_000;
        assertTrue(c.react(TableEvent.PLAYER_WIN, t).isPresent());

        long afterTable = t + TableChatter.DEFAULT_TABLE_COOLDOWN_MS + 1;
        assertTrue(c.react(TableEvent.PLAYER_WIN, afterTable).isEmpty(),
                "table cooldown elapsed but the persona's own cooldown has not");

        long afterPersona = t + TableChatter.DEFAULT_PERSONA_COOLDOWN_MS + 1;
        assertTrue(c.react(TableEvent.PLAYER_WIN, afterPersona).isPresent());
    }

    @Test void aPersonaNeverRepeatsTheirLastLine() {
        Persona p = loud("a", 0, TableEvent.PLAYER_WIN, "first", "second");
        TableChatter c = new TableChatter(List.of(p), new Random(11), 0, 0, 5_000);

        String previous = null;
        for (int i = 0; i < 40; i++) {
            Remark r = c.react(TableEvent.PLAYER_WIN, 1_000_000 + i * 1_000L).orElseThrow();
            assertNotEquals(previous, r.text(), "repeated a line back-to-back at step " + i);
            previous = r.text();
        }
    }

    @Test void aSingleLineIsReusedRatherThanGoingSilent() {
        Persona p = loud("a", 0, TableEvent.PLAYER_WIN, "the only thing I say");
        TableChatter c = new TableChatter(List.of(p), new Random(2), 0, 0, 5_000);
        for (int i = 0; i < 5; i++) {
            assertEquals("the only thing I say",
                    c.react(TableEvent.PLAYER_WIN, 1_000_000 + i * 1_000L).orElseThrow().text());
        }
    }

    @Test void chattinessOfZeroMeansSilence() {
        Persona mute = new Persona("mute", "Mute", 0, 0.0,
                Map.of(TableEvent.PLAYER_WIN, List.of("never said")));
        TableChatter c = chatter(List.of(mute), 1);
        for (int i = 0; i < 50; i++) {
            assertTrue(c.react(TableEvent.PLAYER_WIN, 1_000_000 + i * 60_000L).isEmpty());
        }
    }

    @Test void chattinessIsRespectedInAggregate() {
        // A quiet persona should speak sometimes, and clearly less than always.
        Persona quiet = new Persona("q", "Quiet", 0, 0.25,
                Map.of(TableEvent.PLAYER_WIN, List.of("x", "y")));
        TableChatter c = new TableChatter(List.of(quiet), new Random(99), 0, 0, 1_000);
        int spoke = 0;
        for (int i = 0; i < 2000; i++) {
            if (c.react(TableEvent.PLAYER_WIN, 1_000_000 + i * 10L).isPresent()) spoke++;
        }
        assertTrue(spoke > 300 && spoke < 700,
                "expected roughly a quarter of 2000, got " + spoke);
    }

    /* ---------- visibility ---------- */

    @Test void remarksExpire() {
        TableChatter c = chatter(List.of(loud("a", 0, TableEvent.PLAYER_WIN, "hi")), 1);
        long t = 3_000_000;
        c.react(TableEvent.PLAYER_WIN, t);
        assertEquals(1, c.visibleAt(t).size());
        assertEquals(1, c.visibleAt(t + TableChatter.DEFAULT_LINGER_MS - 1).size());
        assertEquals(0, c.visibleAt(t + TableChatter.DEFAULT_LINGER_MS).size(), "bubble should be gone");
    }

    @Test void opacityFadesRatherThanBlinkingOut() {
        TableChatter c = chatter(List.of(loud("a", 0, TableEvent.PLAYER_WIN, "hi")), 1);
        long t = 4_000_000;
        Remark r = c.react(TableEvent.PLAYER_WIN, t).orElseThrow();

        assertEquals(1f, r.opacityAt(t), 0.001, "fully opaque when fresh");
        assertEquals(1f, r.opacityAt(t + (long) (r.lingerMillis() * 0.5)), 0.001,
                "holds full opacity while it's being read");
        float late = r.opacityAt(t + (long) (r.lingerMillis() * 0.9));
        assertTrue(late > 0f && late < 1f, "should be mid-fade, was " + late);
        assertEquals(0f, r.opacityAt(t + r.lingerMillis()), 0.001);
        assertFalse(r.isVisibleAt(t + r.lingerMillis()));
    }

    @Test void clearForgetsEverything() {
        TableChatter c = chatter(List.of(loud("a", 0, TableEvent.PLAYER_WIN, "hi")), 1);
        long t = 5_000_000;
        c.react(TableEvent.PLAYER_WIN, t);
        c.clear();
        assertTrue(c.visibleAt(t).isEmpty());
        // Cooldowns are cleared too, so a new session starts talkative.
        assertTrue(c.react(TableEvent.PLAYER_WIN, t + 1).isPresent());
    }

    /* ---------- determinism ---------- */

    @Test void sameSeedSameConversation() {
        StringBuilder a = new StringBuilder();
        StringBuilder b = new StringBuilder();
        for (StringBuilder out : List.of(a, b)) {
            TableChatter c = chatter(Personas.defaults(), 20260720L);
            long t = 6_000_000;
            for (TableEvent e : TableEvent.values()) {
                t += 5_000;
                c.react(e, t).ifPresent(r -> out.append(r).append('\n'));
            }
        }
        assertEquals(a.toString(), b.toString(), "chatter must be reproducible for tests");
        assertFalse(a.toString().isEmpty(), "the default table should have said something");
    }

    /* ---------- the shipped table ---------- */

    @Test void theDefaultTableIsThreeDistinctSeats() {
        List<Persona> ps = Personas.defaults();
        assertEquals(3, ps.size());
        assertEquals(3, ps.stream().map(Persona::id).distinct().count(),   "ids must be unique");
        assertEquals(3, ps.stream().map(Persona::seat).distinct().count(), "seats must not collide");
        assertEquals(3, ps.stream().map(Persona::name).distinct().count(), "names must be distinct");
    }

    @Test void everyEventHasSomeoneWhoCaresAboutIt() {
        List<Persona> ps = Personas.defaults();
        for (TableEvent e : TableEvent.values()) {
            assertTrue(ps.stream().anyMatch(p -> p.reactsTo(e)),
                    "nothing in the game would ever respond to " + e);
        }
    }

    @Test void theTableTalksOverARealisticSession() {
        // A long session should produce a steady trickle, not a flood and not
        // silence. Hands are spaced 10-25s apart with variance rather than a
        // fixed interval -- a fixed 20s lands exactly on the table cooldown,
        // which is a degenerate case that flatters the numbers.
        TableChatter c = chatter(Personas.defaults(), 4242);
        TableEvent[] events = TableEvent.values();
        Random pick = new Random(1);
        int remarks = 0;
        long t = 7_000_000;
        for (int hand = 0; hand < 200; hand++) {
            t += 10_000 + pick.nextInt(15_000);
            if (c.react(events[pick.nextInt(events.length)], t).isPresent()) remarks++;
        }
        // Tuned against a simulated session. The first cut of these cooldowns
        // drew a remark on 89% of hands, which read as constant nattering, so
        // the upper bound is the one that actually matters here.
        assertTrue(remarks > 20, "table felt dead: " + remarks + " remarks in 200 hands");
        assertTrue(remarks < 100, "table talked too much: " + remarks + " remarks in 200 hands");
    }
}
