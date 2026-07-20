package com.richeyworks.blackjack.media;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates the game's sound effects as raw PCM. No audio device, no file
 * formats, no platform API — just numbers.
 *
 * <p>This was 176 of the 182 lines in the Swing {@code SoundFx}, wrapped around
 * six lines of {@code javax.sound}, which meant the mobile build shipped
 * completely silent: Android has no {@code javax.sound.sampled}, so none of it
 * could be reused. Splitting the arithmetic from the playback lets both front
 * ends make exactly the same noises — Swing through a {@code SourceDataLine},
 * libGDX through {@code Gdx.audio}.
 *
 * <p>Output is 16-bit signed mono little-endian at 44.1 kHz, which both
 * backends accept without conversion.
 */
public final class ToneSynth {

    private ToneSynth() {}

    /** Samples per second. */
    public static final int SAMPLE_RATE = 44_100;
    /** Bytes per sample: 16-bit mono. */
    public static final int BYTES_PER_SAMPLE = 2;

    /** Waveform. Shapes the character of a tone more than its pitch does. */
    public enum Shape { SINE, SQUARE, SAW, NOISE }

    /** One note: a frequency held for a duration, with a waveform. */
    public static final class Tone {
        public final float freq;
        public final double seconds;
        public final Shape shape;
        public final double gain;

        public Tone(float freq, double seconds, Shape shape, double gain) {
            this.freq    = freq;
            this.seconds = seconds;
            this.shape   = shape;
            this.gain    = gain;
        }

        public Tone(float freq, double seconds, Shape shape) {
            this(freq, seconds, shape, 0.45);
        }
    }

    /**
     * Render one tone to PCM at the given master volume.
     *
     * @param volume 0..1, applied on top of the tone's own gain
     */
    public static byte[] render(Tone tone, float volume) {
        int samples = (int) (tone.seconds * SAMPLE_RATE);
        if (samples <= 0) return new byte[0];

        byte[] data = new byte[samples * BYTES_PER_SAMPLE];
        double phaseStep = 2 * Math.PI * tone.freq / SAMPLE_RATE;
        double phase = 0;
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        for (int i = 0; i < samples; i++) {
            double v;
            switch (tone.shape) {
                case SQUARE -> v = Math.signum(Math.sin(phase));
                case NOISE  -> v = rng.nextDouble() * 2 - 1;
                case SAW    -> v = 2 * (phase / (2 * Math.PI)
                                        - Math.floor(phase / (2 * Math.PI) + 0.5));
                default     -> v = Math.sin(phase);
            }
            int s = (int) (v * envelope(i, samples) * tone.gain * clamp(volume) * Short.MAX_VALUE);
            data[2 * i]     = (byte) (s & 0xFF);
            data[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
            phase += phaseStep;
        }
        return data;
    }

    /** Render a sequence back to back into one buffer. */
    public static byte[] render(Tone[] tones, float volume) {
        byte[][] parts = new byte[tones.length][];
        int total = 0;
        for (int i = 0; i < tones.length; i++) {
            parts[i] = render(tones[i], volume);
            total += parts[i].length;
        }
        byte[] out = new byte[total];
        int at = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, out, at, part.length);
            at += part.length;
        }
        return out;
    }

    /**
     * Linear fade in and out. Without it every tone starts and ends on a
     * discontinuity, which is audible as a click on top of the note.
     */
    private static double envelope(int i, int total) {
        if (total <= 1) return 0;
        double attack  = Math.min(1.0, i / (total * 0.05));
        double release = Math.min(1.0, (total - i) / (total * 0.10));
        return Math.min(attack, release);
    }

    private static float clamp(float v) {
        if (Float.isNaN(v)) return 0f;
        return Math.max(0f, Math.min(1f, v));
    }
}
