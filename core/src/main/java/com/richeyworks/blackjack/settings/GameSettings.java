package com.richeyworks.blackjack.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Persisted user preferences. Plain {@link Properties} on disk so it's
 * trivial to inspect and edit by hand.
 */
public final class GameSettings {

    private final Path file;

    public boolean dealerHitsSoft17 = false;
    public boolean lateSurrender    = true;
    public boolean offerInsurance   = true;
    public boolean sfxEnabled       = true;
    public boolean musicEnabled     = true;
    public float   sfxVolume        = 0.6f;
    public float   musicVolume      = 0.4f;
    public String  themeId          = "classic";
    public String  aiPersonality    = "cautious";

    public GameSettings(Path file) {
        this.file = file;
        load();
    }

    public void load() {
        if (file == null || !Files.exists(file)) return;
        try {
            Properties p = new Properties();
            try (var in = Files.newBufferedReader(file)) { p.load(in); }
            dealerHitsSoft17 = bool(p, "dealerHitsSoft17", dealerHitsSoft17);
            lateSurrender    = bool(p, "lateSurrender",    lateSurrender);
            offerInsurance   = bool(p, "offerInsurance",   offerInsurance);
            sfxEnabled       = bool(p, "sfxEnabled",       sfxEnabled);
            musicEnabled     = bool(p, "musicEnabled",     musicEnabled);
            sfxVolume        = flt(p, "sfxVolume",        sfxVolume);
            musicVolume      = flt(p, "musicVolume",      musicVolume);
            themeId          = p.getProperty("themeId",       themeId);
            aiPersonality    = p.getProperty("aiPersonality", aiPersonality);
        } catch (IOException ignored) { }
    }

    public void save() {
        if (file == null) return;
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Properties p = new Properties();
            p.setProperty("dealerHitsSoft17", Boolean.toString(dealerHitsSoft17));
            p.setProperty("lateSurrender",    Boolean.toString(lateSurrender));
            p.setProperty("offerInsurance",   Boolean.toString(offerInsurance));
            p.setProperty("sfxEnabled",       Boolean.toString(sfxEnabled));
            p.setProperty("musicEnabled",     Boolean.toString(musicEnabled));
            p.setProperty("sfxVolume",        Float.toString(sfxVolume));
            p.setProperty("musicVolume",      Float.toString(musicVolume));
            p.setProperty("themeId",          themeId);
            p.setProperty("aiPersonality",    aiPersonality);
            try (var out = Files.newBufferedWriter(file)) { p.store(out, "BlackJack Pro settings"); }
        } catch (IOException ignored) { }
    }

    private boolean bool(Properties p, String k, boolean def) {
        String v = p.getProperty(k);
        return v == null ? def : Boolean.parseBoolean(v);
    }
    private float flt(Properties p, String k, float def) {
        String v = p.getProperty(k);
        try { return v == null ? def : Float.parseFloat(v); }
        catch (NumberFormatException e) { return def; }
    }
}
