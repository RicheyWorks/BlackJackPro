package com.richeyworks.blackjack.platform.rng;

/**
 * Randomness source for real-money play. {@code SecureRandom} is the FLOOR, not the
 * deliverable: production requires an independently certified RNG (e.g. GLI-19) and/or a
 * provably-fair scheme so players can verify outcomes were not tampered with.
 *
 * <p>Provably-fair flow: the server commits to a hashed server seed before the round
 * ({@link #commitServerSeed}), combines it with a client-supplied seed and a per-draw
 * nonce on each {@link #nextInt}, then reveals the server seed afterward ({@link #reveal})
 * so the player can recompute and verify every draw.
 */
public interface Rng {

    /** Pre-round commitment: a hash of the still-secret server seed for this round. */
    String commitServerSeed(String roundId);

    /** Draw an integer in [0, bound) bound to the round, the client seed, and a nonce. */
    int nextInt(String roundId, String clientSeed, long nonce, int bound);

    /** Post-round reveal of the server seed so the player can verify the draws. */
    ServerSeedReveal reveal(String roundId);

    record ServerSeedReveal(String roundId, String serverSeed, String commitmentHash) {}
}
