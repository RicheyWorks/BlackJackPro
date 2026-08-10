package com.richeyworks.blackjack.persist;

import com.richeyworks.blackjack.engine.Engine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    @Test void peakBankrollIsNeverBelowBankrollAfterLoad(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("save.txt");
        // peak line after bankroll, deliberately lower — the shape a partial
        // write or a hand-edit can produce.
        Files.writeString(f, """
                bankroll=5000
                peakBankroll=1000
                hands=1
                wins=0
                losses=0
                pushes=0
                blackjacks=0
                busts=0
                doubles=0
                splits=0
                surrenders=0
                totalWagered=0
                totalReturned=0
                """);
        Engine e = new Engine(1000, new Random(1));
        new SaveManager(f).load(e);
        assertEquals(5000, e.bankroll());
        assertTrue(e.stats().peakBankroll >= e.bankroll(),
                "peak=" + e.stats().peakBankroll + " bank=" + e.bankroll());
    }
}
