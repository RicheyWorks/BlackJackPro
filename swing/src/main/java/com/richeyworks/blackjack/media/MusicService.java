package com.richeyworks.blackjack.media;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Loads any WAV file in the given directory and plays them in rotation.
 *
 * MP3 is intentionally skipped — Java's stock audio system doesn't decode it
 * and we don't want a hard dependency on JLayer. Convert any tracks to WAV
 * if you want them in this build.
 *
 * <p><b>Threading:</b> playback control is reachable from two directions — the
 * EDT (menu items, settings dialog) and the Java Sound daemon thread that
 * delivers {@link LineEvent}s at end of track. Every method that touches
 * {@code clip} or {@code index} is therefore synchronized, and the end-of-track
 * advance is handed to a private worker rather than run inline: closing a line
 * from inside its own event callback is documented as deadlock-prone.
 */
public final class MusicService {

    private final List<Path> tracks = new ArrayList<>();
    private int   index;
    private Clip  clip;
    private float volume = 0.5f;
    private boolean muted;
    private boolean stopped;

    /** Runs track advances off the Java Sound callback thread. */
    private final ExecutorService advancer = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "blackjack-music");
        t.setDaemon(true);
        return t;
    });

    public MusicService(Path dir) {
        if (dir == null || !Files.isDirectory(dir)) return;
        try (Stream<Path> s = Files.list(dir)) {
            s.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".wav"))
             .sorted()
             .forEach(tracks::add);
        } catch (IOException ignored) { }
    }

    public boolean hasTracks() { return !tracks.isEmpty(); }

    public synchronized boolean isMuted() { return muted; }

    public synchronized void play() {
        if (tracks.isEmpty() || stopped) return;
        stop();
        try (AudioInputStream in = AudioSystem.getAudioInputStream(tracks.get(index).toFile())) {
            Clip c = AudioSystem.getClip();
            c.open(in);
            clip = c;
            applyVolume();
            c.addLineListener(ev -> {
                if (ev.getType() != LineEvent.Type.STOP) return;
                Clip src = (Clip) ev.getSource();
                // Only a track that actually ran to its end advances the rotation;
                // an explicit stop() or a mute mid-track must not skip a track.
                if (src.getFrameLength() <= 0 || src.getFramePosition() < src.getFrameLength()) return;
                advance(src);
            });
            if (!muted) c.start();
        } catch (Exception ignored) { }
    }

    /**
     * Queue a move to the next track, but only if {@code finished} is still the
     * clip we're playing — a stale event from a clip already replaced must not
     * bump the rotation a second time. Runs on the worker, never on the caller's
     * (Java Sound callback) thread.
     */
    private void advance(Clip finished) {
        synchronized (this) {
            if (stopped || finished != clip) return;
        }
        try {
            advancer.execute(this::next);
        } catch (RuntimeException ignored) { /* pool already shut down */ }
    }

    public synchronized void next() {
        if (tracks.isEmpty() || stopped) return;
        index = (index + 1) % tracks.size();
        play();
    }

    public synchronized void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    /**
     * Stop playback for good and release the worker thread. Idempotent — after
     * this, {@link #play()} and {@link #next()} are no-ops, so a track ending
     * during shutdown can't resurrect the rotation.
     */
    public synchronized void shutdown() {
        stopped = true;
        stop();
        advancer.shutdownNow();
    }

    public synchronized void toggleMute() {
        muted = !muted;
        if (clip == null) return;
        if (muted) clip.stop();
        else       clip.start();
    }

    public synchronized void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        applyVolume();
    }

    private void applyVolume() {
        if (clip == null) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = volume <= 0.0001f
                    ? gain.getMinimum()
                    : (float) (20.0 * Math.log10(volume));
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
        } catch (Exception ignored) { }
    }
}
