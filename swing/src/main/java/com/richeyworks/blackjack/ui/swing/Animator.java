package com.richeyworks.blackjack.ui.swing;

import javax.swing.Timer;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A tiny animation framework used to drive card-deal slides and chip pulses.
 * Each {@link Tween} runs to completion on the Swing EDT via a {@link Timer},
 * calling its update callback on each tick and invoking {@code onComplete}
 * when the animation finishes.
 *
 * The animator does not own the component — it just repaints whatever it's
 * told to.  Callers are responsible for storing intermediate state where
 * their painter can find it.
 */
public final class Animator {

    private final List<Tween> running = new ArrayList<>();
    private final Component   repaintTarget;
    private final Timer       ticker;

    private static final int TICK_MS = 16; // ~60 fps

    public Animator(Component repaintTarget) {
        this.repaintTarget = repaintTarget;
        this.ticker = new Timer(TICK_MS, e -> tick());
        this.ticker.start();
    }

    public Tween start(int durationMs, Consumer<Float> update, Runnable onComplete) {
        Tween t = new Tween(durationMs, update, onComplete);
        running.add(t);
        return t;
    }

    private void tick() {
        if (running.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (int i = running.size() - 1; i >= 0; i--) {
            Tween t = running.get(i);
            float p = (float) (now - t.startMs) / t.durationMs;
            if (p >= 1f) {
                t.update.accept(1f);
                if (t.onComplete != null) t.onComplete.run();
                running.remove(i);
            } else {
                t.update.accept(easeOutCubic(p));
            }
        }
        repaintTarget.repaint();
    }

    /** Standard ease-out for natural-feeling card slides. */
    public static float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }

    public static final class Tween {
        final int             durationMs;
        final long            startMs = System.currentTimeMillis();
        final Consumer<Float> update;
        final Runnable        onComplete;

        Tween(int duration, Consumer<Float> update, Runnable onComplete) {
            this.durationMs = duration;
            this.update     = update;
            this.onComplete = onComplete;
        }
    }
}
