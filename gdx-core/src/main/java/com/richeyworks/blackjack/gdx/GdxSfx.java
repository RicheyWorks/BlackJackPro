package com.richeyworks.blackjack.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.richeyworks.blackjack.media.GameSounds;
import com.richeyworks.blackjack.media.ToneSynth;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sound effects for the libGDX build.
 *
 * <p>The mobile port was completely silent, because the whole synthesiser lived
 * in the Swing module wrapped around {@code javax.sound}, which Android does
 * not have. Now that {@link GameSounds} renders plain PCM in {@code core},
 * this only has to hand those bytes to {@code Gdx.audio}.
 *
 * <p>Uses {@link AudioDevice} rather than {@code Gdx.audio.newSound}, because
 * the latter wants a file — these effects have no files, which is the point.
 *
 * <h2>Threading</h2>
 * {@code AudioDevice.writeSamples} blocks until the buffer has played, so it
 * must not run on the render thread or every effect would stall a frame for its
 * full duration. A single daemon worker plays them in order, which also stops
 * two effects overlapping into noise.
 */
public final class GdxSfx {

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "blackjack-gdx-sfx");
        t.setDaemon(true);
        return t;
    });

    /** Rendered once per effect and reused; the maths is identical every time. */
    private final Map<GameSounds, short[]> cache = new EnumMap<>(GameSounds.class);

    private AudioDevice device;
    private boolean unavailable;

    private volatile float   volume = 0.6f;
    private volatile boolean muted;

    public void setVolume(float v) { this.volume = Math.max(0f, Math.min(1f, v)); }
    public float volume()          { return volume; }
    public void setMuted(boolean m){ this.muted = m; }
    public boolean muted()         { return muted; }

    public void chipClick()        { play(GameSounds.CHIP_CLICK); }
    public void cardSnap()         { play(GameSounds.CARD_SNAP); }
    public void winSting()         { play(GameSounds.WIN); }
    public void loseSting()        { play(GameSounds.LOSE); }
    public void blackjackFanfare() { play(GameSounds.BLACKJACK); }
    public void pushBeep()         { play(GameSounds.PUSH); }
    public void achievement()      { play(GameSounds.ACHIEVEMENT); }

    /** Queue an effect. Returns immediately; playback happens on the worker. */
    public void play(GameSounds sound) {
        if (muted || unavailable) return;
        short[] samples = samplesFor(sound);
        if (samples.length == 0) return;
        try {
            pool.execute(() -> writeSamples(samples));
        } catch (RuntimeException alreadyShutDown) {
            // Racing app teardown; nothing worth reporting.
        }
    }

    /**
     * Effects are rendered at full volume and scaled at playback, so changing
     * the volume doesn't invalidate the cache.
     */
    private short[] samplesFor(GameSounds sound) {
        short[] cached = cache.get(sound);
        if (cached != null) return cached;
        byte[] pcm = sound.render(1.0f);
        short[] out = new short[pcm.length / ToneSynth.BYTES_PER_SAMPLE];
        for (int i = 0; i < out.length; i++) {
            // Little-endian, as ToneSynth writes it.
            out[i] = (short) ((pcm[2 * i] & 0xFF) | (pcm[2 * i + 1] << 8));
        }
        cache.put(sound, out);
        return out;
    }

    private void writeSamples(short[] samples) {
        try {
            AudioDevice d = device();
            if (d == null) return;
            float v = volume;
            short[] scaled = new short[samples.length];
            for (int i = 0; i < samples.length; i++) scaled[i] = (short) (samples[i] * v);
            d.writeSamples(scaled, 0, scaled.length);
        } catch (Throwable t) {
            // A device that has gone away (call interrupts audio, headset
            // unplugged, emulator with no audio) must never take the game with
            // it -- sound is decoration.
            unavailable = true;
            Gdx.app.error("GdxSfx", "sound unavailable, continuing silently", t);
        }
    }

    /** Opened lazily on the worker: Gdx.audio isn't ready during construction. */
    private AudioDevice device() {
        if (device == null && !unavailable) {
            device = Gdx.audio.newAudioDevice(ToneSynth.SAMPLE_RATE, true);
        }
        return device;
    }

    public void dispose() {
        pool.shutdownNow();
        if (device != null) {
            try { device.dispose(); } catch (RuntimeException ignored) { }
            device = null;
        }
        cache.clear();
    }
}
