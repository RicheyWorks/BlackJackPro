package com.richeyworks.blackjack.table;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A character sitting at the table. Talks; never plays a hand.
 *
 * <p>The engine is single-seat and deliberately stays that way — these are
 * company, not opponents, so none of the money path had to change to seat them.
 *
 * <p>A persona is immutable and holds no state about the conversation;
 * {@link TableChatter} owns cooldowns and recent history. That keeps a persona
 * shareable and makes the chatter the only thing that needs testing for
 * timing behaviour.
 *
 * <h2>On tone</h2>
 * These lines run alongside real wagering, so personas react to what happened
 * and never push the player toward the next bet. Nothing here says raise your
 * stake, chase a loss, or that a win is owed. A losing streak draws sympathy or
 * a change of subject, not encouragement to keep going.
 */
public final class Persona {

    private final String id;
    private final String name;
    private final int    seat;
    private final double chattiness;
    private final Map<TableEvent, List<String>> lines;

    /**
     * @param id         stable identifier, for settings and tests
     * @param name       display name shown above the seat
     * @param seat       seat index, left to right, used for bubble placement
     * @param chattiness 0..1 — the chance this persona speaks when an event it
     *                   has a line for occurs. A table where everyone comments
     *                   on everything stops feeling like people very quickly.
     * @param lines      what they might say, keyed by event
     */
    public Persona(String id, String name, int seat, double chattiness,
                   Map<TableEvent, List<String>> lines) {
        this.id         = id;
        this.name       = name;
        this.seat       = seat;
        this.chattiness = Math.max(0.0, Math.min(1.0, chattiness));
        EnumMap<TableEvent, List<String>> copy = new EnumMap<>(TableEvent.class);
        lines.forEach((k, v) -> {
            if (v != null && !v.isEmpty()) copy.put(k, List.copyOf(v));
        });
        this.lines = Collections.unmodifiableMap(copy);
    }

    public String id()         { return id; }
    public String name()       { return name; }
    public int    seat()       { return seat; }
    public double chattiness() { return chattiness; }

    /** True if this persona has anything to say about {@code event}. */
    public boolean reactsTo(TableEvent event) {
        return lines.containsKey(event);
    }

    /** Everything this persona might say about {@code event}; never null. */
    public List<String> linesFor(TableEvent event) {
        return lines.getOrDefault(event, List.of());
    }

    /** Every event this persona has a line for. */
    public Iterable<TableEvent> events() { return lines.keySet(); }

    @Override public String toString() { return name + " (seat " + seat + ")"; }
}
