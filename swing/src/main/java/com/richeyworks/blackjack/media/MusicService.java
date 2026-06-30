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
import java.util.stream.Stream;

/**
 * Loads any WAV file in the given directory and plays them in rotation.
 *
 * MP3 is intentionally skipped — Java's stock audio system doesn't decode it
 * and we don't want a hard dependency on JLayer. Convert any tracks to WAV
 * if you want them in this build.
 */
public final class MusicService {

    private final List<Path> tracks = new ArrayList<>();
    private int   index;
    private Clip  clip;
    private float volume = 0.5f;
    private boolean muted;

    public MusicService(Path dir) {
        if (dir == null || !Files.isDirectory(dir)) return;
        try (Stream<Path> s = Files.list(dir)) {
            s.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".wav"))
             .sorted()
             .forEach(tracks::add);
        } catch (IOException ignored) { }
    }

    public boolean hasTracks() { return !tracks.isEmpty(); }
    public boolean isMuted()   { return muted; }

    public void play() {
        if (tracks.isEmpty()) return;
        stop();
        try (AudioInputStream in = AudioSystem.getAudioInputStream(tracks.get(index).toFile())) {
            clip = AudioSystem.getClip();
            clip.open(in);
            applyVolume();
            clip.addLineListener(ev -> {
                if (ev.getType() == LineEvent.Type.STOP) {
                    Clip src = (Clip) ev.getSource();
                    if (src.getFrameLength() > 0 && src.getFramePosition() >= src.getFrameLength()) next();
                }
            });
            if (!muted) clip.start();
        } catch (Exception ignored) { }
    }

    public void next() {
        if (tracks.isEmpty()) return;
        index = (index + 1) % tracks.size();
        play();
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    public void toggleMute() {
        muted = !muted;
        if (clip == null) return;
        if (muted) clip.stop();
        else       clip.start();
    }

    public void setVolume(float v) {
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
