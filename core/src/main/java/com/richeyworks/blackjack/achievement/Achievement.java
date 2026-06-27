package com.richeyworks.blackjack.achievement;

import java.util.Objects;

/** A single named milestone. {@code goal} is the target value tracked by {@link AchievementService}. */
public final class Achievement {

    private final String id;
    private final String name;
    private final String description;
    private final int    goal;

    private boolean unlocked;
    private int     progress;

    public Achievement(String id, String name, String description, int goal) {
        this.id          = Objects.requireNonNull(id);
        this.name        = Objects.requireNonNull(name);
        this.description = description == null ? "" : description;
        this.goal        = Math.max(1, goal);
    }

    public String  id()          { return id; }
    public String  name()        { return name; }
    public String  description() { return description; }
    public int     goal()        { return goal; }
    public int     progress()    { return progress; }
    public boolean unlocked()    { return unlocked; }

    /** Returns true iff the achievement just transitioned from locked to unlocked. */
    boolean record(int delta) {
        if (unlocked) return false;
        progress += delta;
        if (progress >= goal) {
            progress  = goal;
            unlocked  = true;
            return true;
        }
        return false;
    }

    void restore(int progress, boolean unlocked) {
        this.progress = Math.max(0, Math.min(goal, progress));
        this.unlocked = unlocked || this.progress >= goal;
    }
}
