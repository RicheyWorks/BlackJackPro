package com.richeyworks.blackjack.sim;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs {@link GameSimulator} as part of the normal suite.
 *
 * <p>Bounded so CI stays fast — a few seconds for tens of thousands of rounds.
 * For a real soak use the {@code main} method, which takes seeds and rounds as
 * arguments and runs millions.
 *
 * <p>The reason this exists at all: a full module review and 249 unit tests
 * both passed while {@code canHit()} threw after 78% of completed rounds. Unit
 * tests call the method under test; a front end calls every query after every
 * action, in states nobody thought to construct. Those are different things,
 * and only the second one found that bug.
 */
class SimulationTest {

    @Test void aShortSoakFindsNothing() {
        // Seeds 0-59 cover both bankroll sizes and every rule combination the
        // simulator varies, since it derives the rules from the seed.
        for (long s = 0; s < 60; s++) {
            final long seed = s;
            assertDoesNotThrow(() -> new GameSimulator(seed).run(200),
                    "simulation failed at seed " + seed);
        }
    }

    @Test void aSoakReachesEveryOutcomeActionAndEvent() {
        GameSimulator.Coverage total = new GameSimulator.Coverage();
        for (long seed = 0; seed < 120; seed++) {
            GameSimulator.merge(total, new GameSimulator(seed).run(300));
        }
        List<String> gaps = GameSimulator.gaps(total);
        assertTrue(gaps.isEmpty(), "the simulation never reached: " + gaps);
    }

    @Test void theNonEngineSurfacesAllRespond() {
        // Sounds, palettes, and basic-strategy advice for every possible
        // two-card hand against every up-card.
        assertDoesNotThrow(GameSimulator::checkPeripherals);
    }

    @Test void aFailureNamesTheSeedSoItCanBeReplayed() {
        GameSimulator.SimFailure f = new GameSimulator.SimFailure(42, 7, "something impossible");
        assertTrue(f.getMessage().contains("seed 42"), f.getMessage());
        assertTrue(f.getMessage().contains("round 7"), f.getMessage());
    }

    @Test void theSimulationIsReproducible() {
        // A run that cannot be repeated is a run whose failures cannot be
        // investigated, which would make the whole exercise decorative.
        GameSimulator.Coverage a = new GameSimulator(20260720L).run(400);
        GameSimulator.Coverage b = new GameSimulator(20260720L).run(400);
        assertEquals(a.rounds, b.rounds);
        assertEquals(a.splits, b.splits);
        assertEquals(a.doubles, b.doubles);
        assertEquals(a.surrenders, b.surrenders);
        assertEquals(a.outcomes, b.outcomes);
        assertEquals(a.events, b.events);
        assertEquals(a.biggestWin, b.biggestWin);
        assertEquals(a.biggestLoss, b.biggestLoss);
    }
}
