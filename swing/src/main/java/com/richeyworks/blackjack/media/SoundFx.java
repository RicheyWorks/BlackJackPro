package com.richeyworks.blackjack.media;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Procedurally synthesized sound effects. No audio files required — every
 * sample is generated on the fly so the game ships with sound out of the box.
 *
 * Uses a small single-thread pool so playback never blocks the EDT and one SFX
 * doesn't queue behind another. Each Effect writes its sample buffer to a
 * fresh {@link SourceDataLine} and closes it when done.
 */
public final class SoundFx {

    private static final AudioFormat FORMAT =
            new AudioFormat(44_100f, 16, 1, true, false);

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "blackjack-sfx");
        t.setDaemon(true);
        return t;
    });

    /** Report an unusable mixer once, not once per effect. */
    private final AtomicBoolean audioWarned = new AtomicBoolean();

    // Written on the EDT (settings dialog), read on the "blackjack-sfx" thread.
    // volatile is what makes a volume or mute change visible to playback.
    private volatile float   volume = 0.6f;
    private volatile boolean muted;

    public void setVolume(float v) { this.volume = Math.max(0f, Math.min(1f, v)); }
    public float volume()          { return volume; }
    public void  setMuted(boolean m){ this.muted = m; }
    public boolean muted()         { return muted; }

    /* ----------------------------------------------------------------------- */
    /* Preset effects                                                          */
    /* ----------------------------------------------------------------------- */

    /** Soft tap when a chip is placed on the table. */
    public void chipClick() {
        playToneBurst(880f, 0.05, 0.4, ToneShape.SQUARE);
    }

    /** Crisp snap when a card is dealt. */
    public void cardSnap() {
        playToneBurst(2200f, 0.04, 0.5, ToneShape.NOISE);
    }

    /** Two-tone "win" sting. */
    public void winSting() {
        playSequence(new ToneSpec[]{
                new ToneSpec(523.25f, 0.10, ToneShape.SINE),    // C5
                new ToneSpec(659.25f, 0.10, ToneShape.SINE),    // E5
                new ToneSpec(783.99f, 0.18, ToneShape.SINE)     // G5
        });
    }

    /** Descending "loss" sting. */
    public void loseSting() {
        playSequence(new ToneSpec[]{
                new ToneSpec(440.00f, 0.10, ToneShape.SINE),
                new ToneSpec(349.23f, 0.12, ToneShape.SINE),
                new ToneSpec(261.63f, 0.22, ToneShape.SINE)
        });
    }

    /** Triumphant fanfare for a natural blackjack. */
    public void blackjackFanfare() {
        playSequence(new ToneSpec[]{
                new ToneSpec(523.25f, 0.08, ToneShape.SINE),
                new ToneSpec(659.25f, 0.08, ToneShape.SINE),
                new ToneSpec(783.99f, 0.08, ToneShape.SINE),
                new ToneSpec(1046.5f, 0.30, ToneShape.SINE)
        });
    }

    /** Soft "push" — neutral. */
    public void pushBeep() {
        playToneBurst(440f, 0.18, 0.35, ToneShape.SINE);
    }

    /** Achievement unlock chime. */
    public void achievement() {
        playSequence(new ToneSpec[]{
                new ToneSpec(880f,  0.06, ToneShape.SINE),
                new ToneSpec(1318f, 0.20, ToneShape.SINE)
        });
    }

    /* ----------------------------------------------------------------------- */
    /* Synthesis                                                               */
    /* ----------------------------------------------------------------------- */

    private void playToneBurst(float freq, double seconds, double gain, ToneShape shape) {
        if (muted) return;
        pool.submit(() -> writeSample(synthesize(freq, seconds, gain, shape)));
    }

    private void playSequence(ToneSpec[] notes) {
        if (muted) return;
        pool.submit(() -> {
            for (ToneSpec n : notes) {
                writeSample(synthesize(n.freq, n.seconds, 0.45, n.shape));
            }
        });
    }

    private byte[] synthesize(float freq, double seconds, double gain, ToneShape shape) {
        int samples = (int) (seconds * FORMAT.getSampleRate());
        byte[] data = new byte[samples * 2];
        double phaseStep = 2 * Math.PI * freq / FORMAT.getSampleRate();
        double phase = 0;
        java.util.concurrent.ThreadLocalRandom rng = java.util.concurrent.ThreadLocalRandom.current();

        for (int i = 0; i < samples; i++) {
            double v;
            switch (shape) {
                case SQUARE: v = Math.signum(Math.sin(phase)); break;
                case NOISE:  v = (rng.nextDouble() * 2 - 1);   break;
                case SAW:    v = (2 * (phase / (2 * Math.PI) - Math.floor(phase / (2 * Math.PI) + 0.5))); break;
                case SINE:
                default:     v = Math.sin(phase);
            }
            // simple ADSR-ish envelope: linear fade-in/out
            double env = envelope(i, samples);
            int   s   = (int) (v * env * gain * volume * Short.MAX_VALUE);
            data[2 * i    ] = (byte) (s & 0xFF);
            data[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
            phase += phaseStep;
        }
        return data;
    }

    private double envelope(int i, int total) {
        double attack  = Math.min(1.0, i / (total * 0.05));
        double release = Math.min(1.0, (total - i) / (total * 0.10));
        return Math.min(attack, release);
    }

    /**
     * Play one buffer on a fresh line, closing it whether or not playback
     * succeeded. The close has to be in a finally: a mixer that fails partway
     * through open/start/write/drain would otherwise leak the line, and enough
     * leaks exhaust the mixer and silence the game permanently.
     */
    private void writeSample(byte[] data) {
        SourceDataLine line = null;
        try {
            line = AudioSystem.getSourceDataLine(FORMAT);
            line.open(FORMAT);
            line.start();
            line.write(data, 0, data.length);
            line.drain();
        } catch (Exception e) {
            // Audio being unavailable is normal (headless, no device, muted OS),
            // so this is not fatal -- but report it once rather than swallowing
            // every failure silently.
            if (audioWarned.compareAndSet(false, true)) {
                System.err.println("Sound effects unavailable: " + e);
            }
        } finally {
            if (line != null) {
                try { line.close(); } catch (RuntimeException ignored) { }
            }
        }
    }

    public void shutdown() { pool.shutdownNow(); }

    private enum ToneShape { SINE, SQUARE, SAW, NOISE }
    private static final class ToneSpec {
        final float freq; final double seconds; final ToneShape shape;
        ToneSpec(float f, double s, ToneShape sh) { freq = f; seconds = s; shape = sh; }
    }
}
