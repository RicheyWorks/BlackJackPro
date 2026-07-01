package com.richeyworks.blackjack.persist;

import com.richeyworks.blackjack.achievement.AchievementService;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.settings.GameSettings;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persistence-hardening coverage (CODE_REVIEW CR-5/CR-6/CR-7): clamped loads,
 * atomic writes, and a save loader that tolerates a single corrupt line.
 */
class PersistHardeningTest {

    @Test void negativeBankrollClampedToZeroOnLoad() throws IOException {
        Path f = Files.createTempFile("save", ".txt");
        Files.writeString(f, "bankroll=-500\nhands=7\n");
        Engine e = new Engine(1000, new Random(1));
        new SaveManager(f).load(e);
        assertEquals(0, e.bankroll(), "a negative saved bankroll must clamp to 0");
        assertEquals(7, e.stats().hands, "valid stats still load");
        Files.deleteIfExists(f);
    }

    @Test void achievementLoadSkipsCorruptLineButKeepsTheRest() throws IOException {
        Path f = Files.createTempFile("ach", ".txt");
        // good line, corrupt line (non-numeric progress), then a good line again:
        // the last line must still load, proving one bad line no longer aborts
        // the whole read.
        Files.writeString(f, "first_win|1|true\nfirst_hand|NOPE|true\nten_wins|3|false\n");
        AchievementService svc = new AchievementService(f);
        assertTrue(svc.get("first_win").unlocked(), "line before the corrupt one loads");
        assertEquals(3, svc.get("ten_wins").progress(), "line after the corrupt one still loads");
        Files.deleteIfExists(f);
    }

    @Test void atomicWriteCreatesAndReplaces() throws IOException {
        Path f = Files.createTempFile("atom", ".txt");
        Files.deleteIfExists(f);                     // start from "no file present"
        AtomicFiles.writeString(f, "first");
        assertEquals("first", Files.readString(f));
        AtomicFiles.writeString(f, "second");
        assertEquals("second", Files.readString(f), "atomic write replaces prior contents");
        Files.deleteIfExists(f);
    }

    @Test void volumesClampedOnSettingsLoad() throws IOException {
        Path f = Files.createTempFile("settings", ".properties");
        Files.writeString(f, "sfxVolume=5.0\nmusicVolume=-1\n");
        GameSettings gs = new GameSettings(f);       // constructor loads
        assertEquals(1.0f, gs.sfxVolume,   1e-6, "over-range volume clamps to 1");
        assertEquals(0.0f, gs.musicVolume, 1e-6, "negative volume clamps to 0");
        Files.deleteIfExists(f);
    }
}
