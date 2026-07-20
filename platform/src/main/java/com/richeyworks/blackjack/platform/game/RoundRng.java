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
    private final transient boolean constructed;

    public RoundRng(Rng rng, String roundId, String clientSeed) {
        super();
        this.rng = rng;
        this.roundId = roundId;
        this.clientSeed = clientSeed;
        this.constructed = true;
    }

    @Override
    public int nextInt(int bound) {
        return rng.nextInt(roundId, clientSeed, nonce++, bound);
    }

    /**
     * The primitive every other {@link Random} method is built from.
     *
     * <p>This override is the load-bearing one. Only {@code nextInt(int)} was
     * overridden before, so {@code nextLong()}, {@code nextDouble()},
     * {@code nextBoolean()}, the {@code ints()/doubles()} streams and
     * {@code nextInt(origin, bound)} all fell through to {@code java.util.Random}'s
     * own state -- seeded, in the no-arg constructor, from
     * {@code System.nanoTime()}. Any shuffle that reached one of them would have
     * been drawn from an uncommitted local PRNG while still being presented to
     * the player as provably fair, and nothing would have reported it. The
     * engine happens to use only {@code nextInt(int)} today, which is a fact
     * about the current implementation, not a property of the interface.
     */
    @Override
    protected int next(int bits) {
        if (bits <= 0 || bits > 32) throw new IllegalArgumentException("bits: " + bits);
        if (bits == 32) {
            // 1 << 32 overflows, so compose from two halves. Both draws are
            // nonced, so they remain reproducible from the revealed seed.
            int hi = rng.nextInt(roundId, clientSeed, nonce++, 1 << 16);
            int lo = rng.nextInt(roundId, clientSeed, nonce++, 1 << 16);
            return (hi << 16) | lo;
        }
        return rng.nextInt(roundId, clientSeed, nonce++, 1 << bits);
    }

    /**
     * Reseeding would let a caller take control of the shuffle, so it is refused
     * once construction is done. {@code Random}'s own constructor calls this
     * before our fields are assigned, which the flag allows through.
     */
    @Override
    public synchronized void setSeed(long seed) {
        if (!constructed) { super.setSeed(seed); return; }
        throw new UnsupportedOperationException(
                "a round's shuffle comes from the committed server seed and cannot be reseeded");
    }

    /** How many draws this round has consumed -- part of what the reveal must reproduce. */
    public long draws() { return nonce; }
}
