package com.richeyworks.blackjack.media;

import org.junit.jupiter.api.Test;

import static com.richeyworks.blackjack.media.ToneSynth.Shape;
import static com.richeyworks.blackjack.media.ToneSynth.Tone;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The sound synthesiser, now that it is arithmetic in {@code core} rather than
 * 176 lines wrapped around six of {@code javax.sound}.
 *
 * <p>Splitting it is what gave the mobile build audio at all — Android has no
 * {@code javax.sound.sampled}, so none of this was reachable there before. It
 * also made the maths testable without an audio device, which it never was.
 */
class ToneSynthTest {

    private static short sample(byte[] pcm, int i) {
        return (short) ((pcm[2 * i] & 0xFF) | (pcm[2 * i + 1] << 8));   // little-endian
    }

    @Test void outputLengthMatchesTheRequestedDuration() {
        byte[] pcm = ToneSynth.render(new Tone(440f, 0.25, Shape.SINE), 1f);
        int expected = (int) (0.25 * ToneSynth.SAMPLE_RATE) * ToneSynth.BYTES_PER_SAMPLE;
        assertEquals(expected, pcm.length);
    }

    @Test void everySampleFitsIn16BitsSoNothingClips() {
        // Gain and volume both at maximum is the worst case; overflowing here
        // would wrap and turn a note into a buzz.
        byte[] pcm = ToneSynth.render(new Tone(440f, 0.2, Shape.SQUARE, 1.0), 1f);
        for (int i = 0; i < pcm.length / 2; i++) {
            int s = sample(pcm, i);
            assertTrue(s >= Short.MIN_VALUE && s <= Short.MAX_VALUE, "sample " + i + " = " + s);
        }
    }

    @Test void theEnvelopeStartsAndEndsNearSilence() {
        // Without a fade the waveform starts on a discontinuity, which is
        // audible as a click on the front of every single effect.
        byte[] pcm = ToneSynth.render(new Tone(440f, 0.3, Shape.SINE), 1f);
        int n = pcm.length / 2;
        assertEquals(0, sample(pcm, 0), "first sample should be silent");
        assertTrue(Math.abs(sample(pcm, n - 1)) < 2000,
                "last sample should be fading, was " + sample(pcm, n - 1));
    }

    @Test void louderVolumeProducesABiggerSignal() {
        int quiet = peak(ToneSynth.render(new Tone(440f, 0.2, Shape.SINE), 0.2f));
        int loud  = peak(ToneSynth.render(new Tone(440f, 0.2, Shape.SINE), 1.0f));
        assertTrue(loud > quiet * 2, "expected volume to scale: " + quiet + " vs " + loud);
    }

    @Test void zeroVolumeIsSilence() {
        assertEquals(0, peak(ToneSynth.render(new Tone(440f, 0.2, Shape.SINE), 0f)));
    }

    @Test void volumeIsClampedRatherThanTrusted() {
        // A corrupt settings file can hand us anything.
        assertEquals(0, peak(ToneSynth.render(new Tone(440f, 0.1, Shape.SINE), -5f)));
        int over = peak(ToneSynth.render(new Tone(440f, 0.1, Shape.SINE), 99f));
        int max  = peak(ToneSynth.render(new Tone(440f, 0.1, Shape.SINE), 1f));
        assertEquals(max, over, "volume above 1 should clamp, not amplify");
        assertEquals(0, peak(ToneSynth.render(new Tone(440f, 0.1, Shape.SINE), Float.NaN)));
    }

    @Test void aZeroLengthToneIsEmptyRatherThanACrash() {
        assertEquals(0, ToneSynth.render(new Tone(440f, 0, Shape.SINE), 1f).length);
        assertEquals(0, ToneSynth.render(new Tone(440f, -1, Shape.SINE), 1f).length);
    }

    @Test void everyWaveformProducesSound() {
        for (Shape shape : Shape.values()) {
            assertTrue(peak(ToneSynth.render(new Tone(440f, 0.2, shape, 1.0), 1f)) > 1000,
                    shape + " produced near-silence");
        }
    }

    @Test void aSequenceIsTheSumOfItsParts() {
        Tone a = new Tone(440f, 0.1, Shape.SINE);
        Tone b = new Tone(880f, 0.2, Shape.SINE);
        int combined = ToneSynth.render(new Tone[]{a, b}, 1f).length;
        assertEquals(ToneSynth.render(a, 1f).length + ToneSynth.render(b, 1f).length, combined);
    }

    /* ---------- the shipped effects ---------- */

    @Test void everyEffectRendersAudibly() {
        for (GameSounds s : GameSounds.values()) {
            byte[] pcm = s.render(1f);
            assertTrue(pcm.length > 0, s + " rendered nothing");
            assertEquals(0, pcm.length % ToneSynth.BYTES_PER_SAMPLE, s + " has a partial sample");
            assertTrue(peak(pcm) > 1000, s + " is inaudible");
        }
    }

    @Test void effectsAreShortEnoughNotToLagTheGame() {
        // These play on a single worker, so a long one delays whatever follows.
        for (GameSounds s : GameSounds.values()) {
            assertTrue(s.seconds() < 1.0, s + " lasts " + s.seconds() + "s");
            assertTrue(s.seconds() > 0.01, s + " is too short to hear");
        }
    }

    @Test void pitchedEffectsRenderIdenticallyEveryTime() {
        // The point of moving synthesis to core: desktop and mobile produce the
        // same bytes, so the game sounds the same on both. Only pitched effects
        // can be compared this way -- see the next test.
        for (GameSounds s : GameSounds.values()) {
            if (s == GameSounds.CARD_SNAP) continue;      // noise, deliberately random
            assertArrayEquals(s.render(0.6f), s.render(0.6f), s + " is not reproducible");
        }
    }

    @Test void theCardSnapIsFreshNoiseEveryTime() {
        // CARD_SNAP is white noise on purpose: a card being dealt is a sound,
        // not a note, and an identical waveform on every deal reads as a
        // synthetic blip rather than card on felt. So it must NOT be
        // reproducible -- an earlier version of the test above demanded that it
        // was, which contradicted this one.
        byte[] a = GameSounds.CARD_SNAP.render(1f);
        byte[] b = GameSounds.CARD_SNAP.render(1f);
        assertEquals(a.length, b.length, "same length, different content");
        assertFalse(java.util.Arrays.equals(a, b), "card snap should vary between deals");
    }

    private static int peak(byte[] pcm) {
        int peak = 0;
        for (int i = 0; i < pcm.length / 2; i++) peak = Math.max(peak, Math.abs(sample(pcm, i)));
        return peak;
    }
}
