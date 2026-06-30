package com.richeyworks.blackjack.platform.game;

import com.richeyworks.blackjack.platform.rng.Rng;

import java.util.Random;

/**
 * Adapts the platform {@link Rng} to the {@link java.util.Random} the {@code :core} engine's
 * shoe expects, so card shuffling draws from the certified / provably-fair source rather than
 * a plain PRNG. Each call advances a per-round nonce; combined with the round's committed
 * server seed and the client seed, the sequence is reproducible for post-round verification.
 *
 * <p>{@link java.util.Collections#shuffle} uses only {@link #nextInt(int)}, which is the one
 * method overridden here.
 */
public final class RoundRng extends Random {

    private final transient Rng rng;
    private final String roundId;
    private final String clientSeed;
    private long nonce;

    public RoundRng(Rng rng, String roundId, String clientSeed) {
        super();
        this.rng = rng;
        this.roundId = roundId;
        this.clientSeed = clientSeed;
    }

    @Override
    public int nextInt(int bound) {
        return rng.nextInt(roundId, clientSeed, nonce++, bound);
    }
}
