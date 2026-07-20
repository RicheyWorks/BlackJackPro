package com.richeyworks.blackjack.table;

/**
 * Something a {@link Persona} said, and when.
 *
 * <p>Carries the speaker rather than just their name so a renderer can place
 * the bubble by {@link Persona#seat()} without a second lookup.
 */
public final class Remark {

    private final Persona speaker;
    private final String  text;
    private final long    saidAtMillis;
    private final long    linger;

    Remark(Persona speaker, String text, long saidAtMillis, long linger) {
        this.speaker      = speaker;
        this.text         = text;
        this.saidAtMillis = saidAtMillis;
        this.linger       = linger;
    }

    public Persona speaker()      { return speaker; }
    public String  text()         { return text; }
    public long    saidAtMillis() { return saidAtMillis; }

    /** How long this stays on screen, in milliseconds. */
    public long lingerMillis()    { return linger; }

    /** True while the bubble should still be drawn at {@code nowMillis}. */
    public boolean isVisibleAt(long nowMillis) {
        return nowMillis - saidAtMillis < linger;
    }

    /**
     * 1.0 when fresh, falling to 0.0 as it expires — for fading the bubble out.
     * Holds at full opacity for the first 70% of its life so the text is easy
     * to read, then fades over the remainder.
     */
    public float opacityAt(long nowMillis) {
        long age = nowMillis - saidAtMillis;
        if (age <= 0)      return 1f;
        if (age >= linger) return 0f;
        double hold = linger * 0.7;
        if (age <= hold)   return 1f;
        return (float) (1.0 - (age - hold) / (linger - hold));
    }

    @Override public String toString() { return speaker.name() + ": " + text; }
}
