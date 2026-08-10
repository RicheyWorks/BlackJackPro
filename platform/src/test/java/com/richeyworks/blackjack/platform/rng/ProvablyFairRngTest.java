package com.richeyworks.blackjack.platform.rng;

import com.richeyworks.blackjack.platform.game.RoundRng;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProvablyFairRngTest {

    @Test void commitmentMatchesRevealedSeed() {
        ProvablyFairRng rng = new ProvablyFairRng(new java.security.SecureRandom(new byte[]{1, 2, 3, 4}));
        String commit = rng.commitServerSeed("r1");
        int a = rng.nextInt("r1", "client", 0, 52);
        int b = rng.nextInt("r1", "client", 1, 52);
        Rng.ServerSeedReveal rev = rng.reveal("r1");
        assertEquals(commit, rev.commitmentHash());
        assertTrue(ProvablyFairRng.verifyCommitment(rev.serverSeed(), commit));
        assertEquals(a, ProvablyFairRng.recompute(rev.serverSeed(), "r1", "client", 0, 52));
        assertEquals(b, ProvablyFairRng.recompute(rev.serverSeed(), "r1", "client", 1, 52));
    }

    @Test void drawsAreInRange() {
        ProvablyFairRng rng = new ProvablyFairRng(new java.security.SecureRandom(new byte[]{9}));
        rng.commitServerSeed("r2");
        for (int i = 0; i < 200; i++) {
            int v = rng.nextInt("r2", "c", i, 13);
            assertTrue(v >= 0 && v < 13, "out of range: " + v);
        }
        Rng.ServerSeedReveal rev = rng.reveal("r2");
        for (int i = 0; i < 50; i++) {
            int expected = ProvablyFairRng.recompute(rev.serverSeed(), "r2", "c", i, 13);
            assertTrue(expected >= 0 && expected < 13);
        }
    }

    @Test void cannotDrawAfterReveal() {
        ProvablyFairRng rng = new ProvablyFairRng();
        rng.commitServerSeed("r3");
        rng.reveal("r3");
        assertThrows(IllegalStateException.class, () -> rng.nextInt("r3", "c", 0, 10));
    }

    @Test void cannotDrawWithoutCommit() {
        ProvablyFairRng rng = new ProvablyFairRng();
        assertThrows(IllegalStateException.class, () -> rng.nextInt("missing", "c", 0, 10));
    }

    @Test void recomputeRejectsBadCommitment() {
        assertFalse(ProvablyFairRng.verifyCommitment("00", "ff"));
        assertFalse(ProvablyFairRng.verifyCommitment("not-hex", "abc"));
    }

    @Test void shoeShuffleIsReproducibleFromReveal() {
        ProvablyFairRng rng = new ProvablyFairRng(new java.security.SecureRandom(new byte[]{7, 7, 7}));
        String commit = rng.commitServerSeed("shoe");
        RoundRng r1 = new RoundRng(rng, "shoe", "client-x");
        int[] first = new int[20];
        for (int i = 0; i < first.length; i++) first[i] = r1.nextInt(52 - (i % 10));
        Rng.ServerSeedReveal rev = rng.reveal("shoe");
        assertTrue(ProvablyFairRng.verifyCommitment(rev.serverSeed(), commit));
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i],
                    ProvablyFairRng.recompute(rev.serverSeed(), "shoe", "client-x", i, 52 - (i % 10)));
        }
    }
}
