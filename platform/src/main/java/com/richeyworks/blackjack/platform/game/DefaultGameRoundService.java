package com.richeyworks.blackjack.platform.game;

import com.richeyworks.blackjack.engine.BlackjackRules;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.platform.common.Asset;
import com.richeyworks.blackjack.platform.compliance.ComplianceGate;
import com.richeyworks.blackjack.platform.compliance.PlayerComplianceState;
import com.richeyworks.blackjack.platform.compliance.PlayerDirectory;
import com.richeyworks.blackjack.platform.rng.Rng;
import com.richeyworks.blackjack.platform.wallet.LedgerEntry;
import com.richeyworks.blackjack.platform.wallet.Wallet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reference {@link GameRoundService} that ties the three planes together for a blackjack round:
 * <ol>
 *   <li><b>Compliance</b> — every wager (base and each double/split/insurance) is authorized by
 *       the {@link ComplianceGate} first.</li>
 *   <li><b>Money</b> — each authorized wager is held into escrow via the {@link Wallet}; at
 *       settlement the round nets out through balanced double-entry postings.</li>
 *   <li><b>Game</b> — the reused {@code :core} {@link Engine} resolves the round, shuffling from
 *       the certified / provably-fair {@link Rng} via {@link RoundRng}.</li>
 * </ol>
 *
 * <p>Money mapping: 1 wallet minor unit == 1 engine chip. The engine runs on a large notional
 * bankroll purely so its own affordability checks pass; the WALLET is the real authority — a
 * hold that exceeds available funds throws and the engine action is not taken. Settlement
 * debits the amount actually held into escrow and credits the engine-returned amount so
 * escrow cannot go negative from a hold/engine mismatch.
 */
public final class DefaultGameRoundService implements GameRoundService {

    private static final long MAX_STAKE = 100_000_000L; // $1,000,000.00 in cents

    /**
     * Headroom the engine needs beyond the base stake: four hands after three
     * splits, each doubled, is eight stakes — plus insurance of half a stake
     * on the opening hand, so nine. Using 8 left a funded wallet unable to
     * complete the full tree after insurance.
     */
    private static final int STAKE_MULTIPLE = 9;

    /** How long an unfinished round may sit before it can be expired. */
    private static final long DEFAULT_ROUND_TTL_MS = 30 * 60 * 1000L;

    private final ComplianceGate gate;
    private final PlayerDirectory players;
    private final Wallet wallet;
    private final Rng rng;
    private final BlackjackRules rules;
    private final Map<String, Round> rounds = new HashMap<>();
    /**
     * idempotency key -> round id (or tombstone marker after retirement).
     *
     * <p>Keys outlive the live round map. Dropping a key on expire used to let
     * a client retry {@code startRound} with the same key: the service treated
     * it as new, the wallet hold was a no-op (key still known), and a free hand
     * was dealt against empty escrow.
     */
    private final Map<String, String> roundByKey = new HashMap<>();
    /** Keys whose rounds finished; replay returns a synthetic settled snapshot. */
    private final Map<String, RoundState> finishedByKey = new HashMap<>();
    /** actionKey -> last snapshot, so applyAction retries do not re-hit / re-hold. */
    private final Map<String, RoundState> actionByKey = new HashMap<>();
    private final AtomicLong seq = new AtomicLong();

    public DefaultGameRoundService(ComplianceGate gate, PlayerDirectory players, Wallet wallet, Rng rng) {
        this(gate, players, wallet, rng, freeze(new BlackjackRules()));
    }

    public DefaultGameRoundService(ComplianceGate gate, PlayerDirectory players, Wallet wallet, Rng rng,
                                   BlackjackRules rules) {
        this.gate = Objects.requireNonNull(gate);
        this.players = Objects.requireNonNull(players);
        this.wallet = Objects.requireNonNull(wallet);
        this.rng = Objects.requireNonNull(rng);
        // Snapshot rules so a shared mutable instance cannot change mid-flight payouts.
        this.rules = freeze(Objects.requireNonNull(rules));
    }

    /** Defensive copy — public fields on BlackjackRules are otherwise live. */
    private static BlackjackRules freeze(BlackjackRules src) {
        if (src.decks < 1) throw new IllegalArgumentException("decks must be >= 1");
        if (src.penetration <= 0 || src.penetration > 1)
            throw new IllegalArgumentException("penetration must be in (0,1]");
        if (src.blackjackPayoutDen <= 0 || src.insurancePayoutDen <= 0)
            throw new IllegalArgumentException("payout denominators must be positive");
        BlackjackRules r = new BlackjackRules();
        r.decks = src.decks;
        r.penetration = src.penetration;
        r.dealerHitsSoft17 = src.dealerHitsSoft17;
        r.lateSurrender = src.lateSurrender;
        r.offerInsurance = src.offerInsurance;
        r.maxSplits = src.maxSplits;
        r.doubleAfterSplit = src.doubleAfterSplit;
        r.splitAcesOneCard = src.splitAcesOneCard;
        r.blackjackPayoutNum = src.blackjackPayoutNum;
        r.blackjackPayoutDen = src.blackjackPayoutDen;
        r.insurancePayoutNum = src.insurancePayoutNum;
        r.insurancePayoutDen = src.insurancePayoutDen;
        return r;
    }


    @Override
    public synchronized RoundState startRound(String playerId, Asset asset, long stakeMinor, String idempotencyKey) {
        if (stakeMinor <= 0 || stakeMinor > MAX_STAKE) {
            throw new IllegalArgumentException("stake out of range: " + stakeMinor);
        }
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(asset, "asset");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        if (idempotencyKey.isBlank()) throw new IllegalArgumentException("idempotencyKey blank");

        // Keys are scoped per player so B cannot harvest A's finished snapshot.
        String scopedKey = playerId + "\0" + idempotencyKey;

        RoundState finished = finishedByKey.get(scopedKey);
        if (finished != null) return finished;

        String existingId = roundByKey.get(scopedKey);
        if (existingId != null) {
            Round existing = rounds.get(existingId);
            if (existing != null) return snapshot(existing);
            throw new IllegalStateException(
                    "round " + existingId + " for this key has already been retired");
        }
        authorizeWager(playerId, asset, stakeMinor);

        // Build the engine BEFORE holding so a bad rules/shoe config cannot
        // leave orphan escrow with no round to expire.
        String roundId = "round-" + UUID.randomUUID();
        String clientSeed = idempotencyKey;
        rng.commitServerSeed(roundId);
        long notionalLong = Math.min(Integer.MAX_VALUE / 2L, stakeMinor * (long) STAKE_MULTIPLE);
        int notional = (int) notionalLong;
        Engine engine = new Engine(notional, new RoundRng(rng, roundId, clientSeed), freeze(rules));

        String holdKey = "hold:base:" + scopedKey;
        wallet.hold(playerId, asset, stakeMinor, holdKey);

        Round r = new Round(roundId, engine, playerId, asset, scopedKey,
                engine.stats().totalWagered, engine.stats().totalReturned, now());
        r.heldMinor = stakeMinor;
        rounds.put(roundId, r);
        roundByKey.put(scopedKey, roundId);

        engine.addBet((int) stakeMinor);
        engine.deal();
        settleIfComplete(r);
        return snapshot(r);
    }

    @Override
    public synchronized RoundState applyAction(String playerId, String roundId, PlayerAction action, String actionKey) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(roundId, "roundId");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(actionKey, "actionKey");
        if (actionKey.isBlank()) throw new IllegalArgumentException("actionKey must be non-blank");

        // Ownership first — never short-circuit on a foreign actionKey.
        Round r = rounds.get(roundId);
        if (r == null) {
            // Maybe already finished under this key
            String scopedAction = playerId + "\0" + actionKey;
            RoundState prior = actionByKey.get(scopedAction);
            if (prior != null) return prior;
            throw new IllegalArgumentException("unknown round: " + roundId);
        }
        if (!r.playerId.equals(playerId)) {
            throw new IllegalStateException("round " + roundId + " is not owned by player " + playerId);
        }

        String scopedAction = playerId + "\0" + actionKey;
        RoundState prior = actionByKey.get(scopedAction);
        if (prior != null) return prior;

        if (r.settled) throw new IllegalStateException("round already settled: " + roundId);
        r.lastActionAt = now();
        Engine e = r.engine;


        switch (action) {
            case HIT -> e.hit();
            case STAND -> e.stand();
            case SURRENDER -> e.surrender();
            case DOUBLE -> {
                if (!e.canDouble()) throw new IllegalStateException("cannot double now");
                holdExtra(r, e.active().bet(), actionKey);
                e.doubleDown();
            }
            case SPLIT -> {
                if (!e.canSplit()) throw new IllegalStateException("cannot split now");
                holdExtra(r, e.active().bet(), actionKey);
                e.split();
            }
            case INSURANCE_TAKE -> {
                if (e.phase() != Phase.INSURANCE) throw new IllegalStateException("cannot take insurance now");
                if (!e.canInsure()) throw new IllegalStateException("cannot take insurance now");
                long premium = rules.insurancePremium(e.hands().get(0).bet());
                holdExtra(r, premium, actionKey);
                e.takeInsurance(true);
            }
            case INSURANCE_DECLINE -> e.takeInsurance(false);
        }
        settleIfComplete(r);
        RoundState st = snapshot(r);
        actionByKey.put(scopedAction, st);
        return st;
    }

    /**
     * Convenience that still binds the real owner — not a public authz bypass.
     * Generates a fresh action key so retries of this overload are not safe;
     * production clients must use the four-arg form.
     */
    @Override
    public synchronized RoundState applyAction(String roundId, PlayerAction action) {
        Round r = rounds.get(roundId);
        if (r == null) throw new IllegalArgumentException("unknown round: " + roundId);
        return applyAction(r.playerId, roundId, action, roundId + ":act:" + seq.incrementAndGet());
    }


    private void authorizeWager(String playerId, Asset asset, long amountMinor) {
        PlayerComplianceState state = players.lookup(playerId);
        ComplianceGate.Decision d = gate.authorize(
                new ComplianceGate.Action(state, ComplianceGate.Action.Type.WAGER, asset, amountMinor));
        if (!d.allowed()) {
            throw new IllegalStateException("wager denied by compliance gate: " + d.reason());
        }
    }

    /** Authorize and escrow an additional in-round wager (double/split/insurance). */
    private void holdExtra(Round r, long extraMinor, String actionKey) {
        if (extraMinor <= 0) return;
        authorizeWager(r.playerId, r.asset, extraMinor);
        // Hold key derived from actionKey so a retry after hold-but-before-ack
        // does not double-escrow.
        String holdKey = r.roundId + ":hold:" + actionKey;
        wallet.hold(r.playerId, r.asset, extraMinor, holdKey);
        // Only bump heldMinor on the first successful hold for this action key.
        if (r.holdKeys.add(holdKey)) {
            r.heldMinor = Math.addExact(r.heldMinor, extraMinor);
        }
    }



    private void settleIfComplete(Round r) {
        if (r.settled || r.engine.phase() != Phase.BETTING) return;
        long engineWagered = (long) r.engine.stats().totalWagered  - r.wageredAtStart;
        long returned      = (long) r.engine.stats().totalReturned - r.returnedAtStart;
        // Fail closed if the engine wagered more than we held — never debit empty escrow.
        if (engineWagered > r.heldMinor) {
            throw new IllegalStateException("engine wagered " + engineWagered
                    + " but only " + r.heldMinor + " was held in escrow");
        }
        // Debit what we actually held; house nets held - returned (not engine wagered).
        // Any unused hold headroom (should be 0 in normal play) returns to available.
        long held = r.heldMinor;
        long unusedHold = held - engineWagered;
        long toAvailable = returned + unusedHold;
        long houseNet = held - toAvailable; // == engineWagered - returned when unused==0
        String tx = r.roundId + ":settle";
        wallet.post(List.of(
                new LedgerEntry("e-" + seq.incrementAndGet(), tx, Wallet.escrow(r.playerId),   r.asset, -held,        tx, now()),
                new LedgerEntry("e-" + seq.incrementAndGet(), tx, Wallet.available(r.playerId), r.asset,  toAvailable, tx, now()),
                new LedgerEntry("e-" + seq.incrementAndGet(), tx, Wallet.HOUSE_PNL,             r.asset,  houseNet,    tx, now())));
        r.settled = true;
        r.payoutMinor = returned;
        finishedByKey.put(r.startKey, snapshot(r));
    }

    private RoundState snapshot(Round r) {
        return new RoundState(r.roundId, r.engine.phase().name(), publicView(r), r.settled, r.payoutMinor);
    }

    private String publicView(Round r) {
        Engine e = r.engine;
        StringBuilder sb = new StringBuilder();
        if (!e.dealer().cards().isEmpty()) {
            sb.append(r.settled
                    ? "dealer = " + e.dealer().value()
                    : "dealer shows " + e.dealer().first().rank());
        }
        sb.append(" | hands:");
        for (int i = 0; i < e.hands().size(); i++) {
            sb.append(i == 0 ? " " : ", ").append(e.hands().get(i).value());
        }
        return sb.toString();
    }

    /**
     * Settle rounds abandoned longer than {@code ttlMs} ago, releasing their
     * escrow, and forget rounds that finished long enough ago to be safely
     * beyond retry.
     *
     * <p>Idempotency keys are retained in {@link #finishedByKey} so a client
     * retry after expiry cannot open a free second round.
     *
     * @return how many rounds were retired
     */
    public synchronized int expireRounds(long ttlMs) {
        long cutoff = now() - ttlMs;
        int retired = 0;
        for (Iterator<Map.Entry<String, Round>> it = rounds.entrySet().iterator(); it.hasNext(); ) {
            Round r = it.next().getValue();
            // Idle-based, not start-based — a long multi-split hand must not be
            // force-stood while the player is still acting.
            if (r.lastActionAt > cutoff) continue;
            if (!r.settled) {
                int guard = 0;
                while (r.engine.phase() != Phase.BETTING && guard++ < 40) {
                    if (r.engine.phase() == Phase.INSURANCE) r.engine.takeInsurance(false);
                    else if (r.engine.canStand()) r.engine.stand();
                    else break;
                }
                settleIfComplete(r);
            }
            if (r.settled) {
                finishedByKey.putIfAbsent(r.startKey, snapshot(r));
                it.remove();
                retired++;
            }
        }
        roundByKey.entrySet().removeIf(e -> !rounds.containsKey(e.getValue())
                && finishedByKey.containsKey(e.getKey()));
        // Prune action keys for rounds that no longer exist (finished or never mapped).
        actionByKey.entrySet().removeIf(e -> {
            String view = e.getValue().roundId();
            return !rounds.containsKey(view);
        });
        // Soft cap on tombstones: drop oldest-looking entries if map is huge.
        if (finishedByKey.size() > 50_000) {
            finishedByKey.clear(); // rare; operator should call with normal TTL
        }
        return retired;
    }


    /** Live rounds, for operational visibility. */
    public synchronized int openRounds() { return rounds.size(); }

    private static long now() { return System.currentTimeMillis(); }

    private static final class Round {
        final String roundId;
        final Engine engine;
        final String playerId;
        final Asset asset;
        final String startKey;
        final long wageredAtStart;
        final long returnedAtStart;
        final long startedAt;
        long lastActionAt;
        final java.util.Set<String> holdKeys = new java.util.HashSet<>();
        long heldMinor;
        boolean settled;
        long payoutMinor;

        Round(String roundId, Engine engine, String playerId, Asset asset, String startKey,
              long wageredAtStart, long returnedAtStart, long startedAt) {
            this.roundId = roundId;
            this.engine = engine;
            this.playerId = playerId;
            this.asset = asset;
            this.startKey = startKey;
            this.wageredAtStart = wageredAtStart;
            this.returnedAtStart = returnedAtStart;
            this.startedAt = startedAt;
            this.lastActionAt = startedAt;
        }
    }
}
