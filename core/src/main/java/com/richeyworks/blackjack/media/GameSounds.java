package com.richeyworks.blackjack.media;

import static com.richeyworks.blackjack.media.ToneSynth.Shape;
import static com.richeyworks.blackjack.media.ToneSynth.Tone;

/**
 * The game's sound effects, defined once as tone sequences.
 *
 * <p>Every effect is synthesised rather than loaded, so the game ships with
 * sound and no audio assets — nothing to package, nothing to license, nothing
 * to go missing from an installer.
 *
 * <p>Defined here rather than in a front end so the desktop and mobile builds
 * make identical noises. Each returns PCM ready for whatever the platform
 * hands to its audio device.
 */
public enum GameSounds {

    /** Soft tap as a chip lands on the felt. */
    CHIP_CLICK(new Tone[]{ new Tone(880f, 0.05, Shape.SQUARE, 0.40) }),

    /** Crisp snap as a card is dealt. Noise, not a pitch — it's a sound, not a note. */
    CARD_SNAP(new Tone[]{ new Tone(2200f, 0.04, Shape.NOISE, 0.50) }),

    /** Rising three-note sting for a won hand. */
    WIN(new Tone[]{
            new Tone(523.25f, 0.10, Shape.SINE),   // C5
            new Tone(659.25f, 0.10, Shape.SINE),   // E5
            new Tone(783.99f, 0.18, Shape.SINE)    // G5
    }),

    /** Falling three-note sting for a lost hand. */
    LOSE(new Tone[]{
            new Tone(440.00f, 0.10, Shape.SINE),
            new Tone(349.23f, 0.12, Shape.SINE),
            new Tone(261.63f, 0.22, Shape.SINE)
    }),

    /** Four-note fanfare for a natural. The best thing that happens in the game. */
    BLACKJACK(new Tone[]{
            new Tone(523.25f, 0.08, Shape.SINE),
            new Tone(659.25f, 0.08, Shape.SINE),
            new Tone(783.99f, 0.08, Shape.SINE),
            new Tone(1046.5f, 0.30, Shape.SINE)
    }),

    /** Neutral beep for a push — deliberately not a win or a loss. */
    PUSH(new Tone[]{ new Tone(440f, 0.18, Shape.SINE, 0.35) }),

    /** Two-note chime when an achievement unlocks. */
    ACHIEVEMENT(new Tone[]{
            new Tone(880f,  0.06, Shape.SINE),
            new Tone(1318f, 0.20, Shape.SINE)
    });

    private final Tone[] tones;

    GameSounds(Tone[] tones) { this.tones = tones; }

    /** PCM for this effect at the given master volume. */
    public byte[] render(float volume) {
        return ToneSynth.render(tones, volume);
    }

    /** How many tones make up this effect. */
    public int toneCount() { return tones.length; }

    /** Total duration in seconds. */
    public double seconds() {
        double total = 0;
        for (Tone t : tones) total += t.seconds;
        return total;
    }
}
