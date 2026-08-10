package com.richeyworks.blackjack.platform.rng;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Reference {@link Rng} that implements a full commit → draw → reveal loop.
 *
 * <p>This is the floor a real-money product needs before independent certification:
 * every draw is a deterministic HMAC of {@code (serverSeed, roundId, clientSeed, nonce)},
 * the pre-round commitment is {@code SHA-256(serverSeed)}, and {@link #reveal} hands the
 * player enough to recompute the sequence. It is <b>not</b> a substitute for GLI-19 (or
 * equivalent) certification — that is a process and a lab, not a class file.
 *
 * <p>Seeds live only in process memory. A production deploy would store commitments
 * durably and never log the pre-reveal seed.
 */
public final class ProvablyFairRng implements Rng {

    private static final HexFormat HEX = HexFormat.of();

    private final SecureRandom entropy;
    private final Map<String, RoundSeed> rounds = new ConcurrentHashMap<>();

    public ProvablyFairRng() {
        this(new SecureRandom());
    }

    public ProvablyFairRng(SecureRandom entropy) {
        this.entropy = Objects.requireNonNull(entropy, "entropy");
    }

    @Override
    public String commitServerSeed(String roundId) {
        Objects.requireNonNull(roundId, "roundId");
        if (roundId.isBlank()) throw new IllegalArgumentException("roundId blank");
        if (rounds.containsKey(roundId)) {
            return rounds.get(roundId).commitment;
        }
        byte[] seed = new byte[32];
        entropy.nextBytes(seed);
        String commitment = sha256Hex(seed);
        rounds.put(roundId, new RoundSeed(seed, commitment, false));
        return commitment;
    }

    @Override
    public int nextInt(String roundId, String clientSeed, long nonce, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive: " + bound);
        RoundSeed rs = rounds.get(roundId);
        if (rs == null) {
            throw new IllegalStateException("no committed seed for round " + roundId);
        }
        if (rs.revealed) {
            throw new IllegalStateException("round " + roundId + " already revealed");
        }
        String material = roundId + "\0" + nullToEmpty(clientSeed) + "\0" + nonce;
        // Rejection sampling so the distribution is uniform over [0, bound).
        long limit = (1L << 32) - ((1L << 32) % bound);
        while (true) {
            long sample = Integer.toUnsignedLong(hmacInt(rs.seed, material));
            if (sample < limit) return (int) (sample % bound);
            material = material + "\0r"; // rare; re-HMAC with a salt suffix
        }
    }

    @Override
    public ServerSeedReveal reveal(String roundId) {
        RoundSeed rs = rounds.get(roundId);
        if (rs == null) {
            throw new IllegalArgumentException("unknown round: " + roundId);
        }
        rs.revealed = true;
        return new ServerSeedReveal(roundId, HEX.formatHex(rs.seed), rs.commitment);
    }

    /**
     * Pure verification helper: recompute the commitment hash of a revealed seed
     * and check it matches what was published pre-round.
     */
    public static boolean verifyCommitment(String serverSeedHex, String commitmentHash) {
        if (serverSeedHex == null || commitmentHash == null) return false;
        try {
            byte[] seed = HEX.parseHex(serverSeedHex);
            return sha256Hex(seed).equalsIgnoreCase(commitmentHash);
        } catch (IllegalArgumentException badHex) {
            return false;
        }
    }

    /**
     * Recompute a single draw from a revealed seed — what a client would run to
     * audit the shoe after the round.
     */
    public static int recompute(String serverSeedHex, String roundId, String clientSeed,
                                long nonce, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        byte[] seed = HEX.parseHex(serverSeedHex);
        String material = roundId + "\0" + nullToEmpty(clientSeed) + "\0" + nonce;
        long limit = (1L << 32) - ((1L << 32) % bound);
        while (true) {
            long sample = Integer.toUnsignedLong(hmacInt(seed, material));
            if (sample < limit) return (int) (sample % bound);
            material = material + "\0r";
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static int hmacInt(byte[] key, String material) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] out = mac.doFinal(material.getBytes(StandardCharsets.UTF_8));
            return ((out[0] & 0xff) << 24)
                    | ((out[1] & 0xff) << 16)
                    | ((out[2] & 0xff) << 8)
                    | (out[3] & 0xff);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HEX.formatHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static final class RoundSeed {
        final byte[] seed;
        final String commitment;
        volatile boolean revealed;

        RoundSeed(byte[] seed, String commitment, boolean revealed) {
            this.seed = seed;
            this.commitment = commitment;
            this.revealed = revealed;
        }
    }
}
