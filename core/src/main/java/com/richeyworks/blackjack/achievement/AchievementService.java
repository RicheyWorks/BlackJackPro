package com.richeyworks.blackjack.achievement;

import com.richeyworks.blackjack.persist.AtomicFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Tracks achievement progress for the current session and persists it across
 * launches.  Backend is a flat file (one line per achievement); a future
 * Steamworks plug-in can replace this with the Steam stats API.
 *
 * Listeners can subscribe via {@link #onUnlock(Consumer)} to show toast
 * notifications, play a chime, etc.
 */
public final class AchievementService {

    private final Map<String, Achievement> all = new LinkedHashMap<>();
    private final List<Consumer<Achievement>> listeners = new ArrayList<>();
    private final Path file;

    public AchievementService(Path persistFile) {
        this.file = persistFile;
        registerBuiltins();
        load();
    }

    private void registerBuiltins() {
        register(new Achievement("first_hand",       "First Hand",        "Play your first round.",              1));
        register(new Achievement("first_win",        "Beginner's Luck",   "Win your first round.",                1));
        register(new Achievement("first_blackjack",  "Natural Twenty-One","Land a natural blackjack.",            1));
        register(new Achievement("ten_wins",         "On a Roll",         "Win 10 rounds.",                       10));
        register(new Achievement("fifty_wins",       "Card Sharp",        "Win 50 rounds.",                       50));
        register(new Achievement("first_split",      "Splitsville",       "Split your first pair.",               1));
        register(new Achievement("first_double",     "Going Big",         "Double down for the first time.",      1));
        register(new Achievement("first_surrender",  "Tactical Retreat",  "Surrender a hand for the first time.", 1));
        register(new Achievement("survived_bust",    "Close Call",        "Win after the dealer busts.",          1));
        register(new Achievement("bankroll_5k",      "Five Large",        "Reach a bankroll of $5,000.",          5000));
        register(new Achievement("bankroll_10k",     "High Roller",       "Reach a bankroll of $10,000.",         10000));
        register(new Achievement("survived_bust_streak", "Heart of Stone","Win 5 hands in a row.",                5));
    }

    public void register(Achievement a)            { all.put(a.id(), a); }
    public Achievement get(String id)              { return all.get(id); }
    public List<Achievement> all()                 { return Collections.unmodifiableList(new ArrayList<>(all.values())); }

    public void onUnlock(Consumer<Achievement> l)  { listeners.add(l); }

    /** Add progress to an achievement.  No-op if unknown id. */
    public void record(String id, int delta) {
        Achievement a = all.get(id);
        if (a == null) return;
        if (a.record(delta)) {
            for (Consumer<Achievement> l : listeners) l.accept(a);
            save();
        }
    }

    /** Convenience: increment by 1. */
    public void increment(String id)               { record(id, 1); }

    /** Set absolute progress (used by bankroll milestones). */
    public void setProgress(String id, int value) {
        Achievement a = all.get(id);
        if (a == null || a.unlocked()) return;
        int delta = value - a.progress();
        if (delta > 0) record(id, delta);
    }

    private void load() {
        if (file == null || !Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split("\\|");
                if (parts.length < 3) continue;
                Achievement a = all.get(parts[0]);
                if (a == null) continue;
                try {
                    a.restore(Integer.parseInt(parts[1].trim()), Boolean.parseBoolean(parts[2].trim()));
                } catch (NumberFormatException ex) {
                    // Skip a single corrupt line and keep loading the rest.
                }
            }
        } catch (IOException ignored) { }
    }

    public void save() {
        if (file == null) return;
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            StringBuilder sb = new StringBuilder();
            for (Achievement a : all.values()) {
                sb.append(a.id()).append('|')
                  .append(a.progress()).append('|')
                  .append(a.unlocked()).append('\n');
            }
            AtomicFiles.writeString(file, sb.toString());
        } catch (IOException ignored) { }
    }
}
