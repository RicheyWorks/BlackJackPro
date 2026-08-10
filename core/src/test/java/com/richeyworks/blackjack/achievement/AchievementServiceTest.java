package com.richeyworks.blackjack.achievement;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AchievementServiceTest {

    @Test void incrementsToUnlock() {
        AchievementService svc = new AchievementService(null);
        AtomicReference<Achievement> unlocked = new AtomicReference<>();
        svc.onUnlock(unlocked::set);

        for (int i = 0; i < 9; i++) svc.increment("ten_wins");
        assertNull(unlocked.get(), "should not yet have fired at 9/10");
        svc.increment("ten_wins");
        assertNotNull(unlocked.get());
        assertEquals("ten_wins", unlocked.get().id());
        assertTrue(svc.get("ten_wins").unlocked());
    }

    @Test void unlockOnlyFiresOnce() {
        AchievementService svc = new AchievementService(null);
        AtomicReference<Integer> hits = new AtomicReference<>(0);
        svc.onUnlock(a -> hits.updateAndGet(v -> v + 1));

        svc.increment("first_hand");
        svc.increment("first_hand");
        svc.increment("first_hand");
        assertEquals(1, hits.get(), "an unlocked achievement should not fire again");
    }

    @Test void setProgressClampsToGoal() {
        AchievementService svc = new AchievementService(null);
        svc.setProgress("bankroll_5k", 999_999);
        assertTrue(svc.get("bankroll_5k").unlocked());
        assertEquals(5000, svc.get("bankroll_5k").progress());
    }

    @Test void setProgressCanLowerAStreakOnLoss() {
        AchievementService svc = new AchievementService(null);
        svc.setProgress("survived_bust_streak", 3);
        assertEquals(3, svc.get("survived_bust_streak").progress());
        // A loss used to call setProgress(0) and leave the value at 3 because
        // only positive deltas applied — Heart of Stone could never reset.
        svc.setProgress("survived_bust_streak", 0);
        assertEquals(0, svc.get("survived_bust_streak").progress());
    }

    @Test void liftProgressNeverLowers() {
        AchievementService svc = new AchievementService(null);
        svc.liftProgress("bankroll_5k", 4000);
        svc.liftProgress("bankroll_5k", 1500);   // stack dipped
        assertEquals(4000, svc.get("bankroll_5k").progress());
    }
}
