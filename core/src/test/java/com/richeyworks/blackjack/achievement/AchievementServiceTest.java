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
}
