package com.richeyworks.blackjack.media;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Plays the game's sound effects on the desktop.
 *
 * <p>The effects themselves live in {@link GameSounds} in {@code core}: this
 * class is now only the {@code javax.sound} plumbing. That split is what let
 * the mobile build get audio — Android has no {@code javax.sound.sampled}, so
 * while the synthesis lived here the libGDX port was silent.
 *
 * <p>Uses a small single-thread pool so playback never blocks the EDT and one
 * effect doesn't queue behind another. Each effect writes its buffer to a fresh
 * {@link SourceDataLine} and closes it in a finally.
 */
public final class SoundFx {

    private static final AudioFormat FORMAT = new AudioFormat(
            ToneSynth.SAMPLE_RATE, 16, 1, true, false);

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
    /* Effects                                                                 */
    /* ----------------------------------------------------------------------- */

    public void chipClick()         { play(GameSounds.CHIP_CLICK); }
    public void cardSnap()          { play(GameSounds.CARD_SNAP); }
    public void winSting()          { play(GameSounds.WIN); }
    public void loseSting()         { play(GameSounds.LOSE); }
    public void blackjackFanfare()  { play(GameSounds.BLACKJACK); }
    public void pushBeep()          { play(GameSounds.PUSH); }
    public void achievement()       { play(GameSounds.ACHIEVEMENT); }

    /** Render and play an effect off the EDT. Silent when muted. */
    public void play(GameSounds sound) {
        if (muted) return;
        final float v = volume;
        pool.submit(() -> writeSample(sound.render(v)));
    }

    /* ----------------------------------------------------------------------- */
    /* Playback                                                                */
    /* ----------------------------------------------------------------------- */

    /**
     * Play one buffer on a fresh line, closing it whether or not playback
     * succeeded. The close has to be in a finally: a mixer that fails partway
     * through open/start/write/drain would otherwise leak the line, and enough
     * leaks exhaust the mixer and silence the game permanently.
     */
    private void writeSample(byte[] data) {
        if (data.length == 0) return;
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
}
