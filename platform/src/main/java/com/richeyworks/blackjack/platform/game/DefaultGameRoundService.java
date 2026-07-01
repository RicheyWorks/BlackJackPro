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
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * reconciles by the engine's wagered/returned deltas:
 * {@code escrow -= wagered; available += returned; house += (wagered - returned)} — which nets
 * to zero. Because every wager was escrowed, escrow returns to zero on settlement.
 */
public final class DefaultGameRoundService implements GameRoundService {

    private static final int NOTIONAL_BANKROLL = Integer.MAX_VALUE / 4;
    private static final long MAX_STAKE = 100_000_000L; // $1,000,000.00 in cents

    private final ComplianceGate gate;
    private final PlayerDirectory players;
    private final Wallet wallet;
    private final Rng rng;
    private final BlackjackRules rules;
    private final Map<String, Round> rounds = new HashMap<>();
    private final AtomicLong seq = new AtomicLong();

    public DefaultGameRoundService(ComplianceGate gate, PlayerDirectory players, Wallet wallet, Rng rng) {
        this(gate, players, wallet, rng, new BlackjackRules());
    }

    public DefaultGameRoundService(ComplianceGate gate, PlayerDirectory players, Wallet wallet, Rng rng,
                                   BlackjackRules rules) {
        this.gate = Objects.requireNonNull(gate);
        this.players = Objects.requireNonNull(players);
        this.wallet = Objects.requireNonNull(wallet);
        this.rng = Objects.requireNonNull(rng);
        this.rules = Objects.requireNonNull(rules);
    }

    @Override
    public synchronized RoundState startRound(String playerId, Asset asset, long stakeMinor, String idempotencyKey) {
        if (stakeMinor <= 0 || stakeMinor > MAX_STAKE) {
            throw new IllegalArgumentException("stake out of range: " + stakeMinor);
        }
        authorizeWager(playerId, asset, stakeMinor);
        wallet.hold(playerId, asset, stakeMinor, idempotencyKey);   // escrow base stake (throws if short)

        String roundId = "round-" + seq.incrementAndGet();
        String clientSeed = idempotencyKey;   // a real client supplies its own seed; threaded here for the envelope
        rng.commitServerSeed(roundId);
        Engine engine = new Engine(NOTIONAL_BANKROLL, new RoundRng(rng, roundId, clientSeed), rules);

        Round r = new Round(roundId, engine, playerId, asset,
                engine.stats().totalWagered, engine.stats().totalReturned);
        rounds.put(roundId, r);

        engine.addBet((int) stakeMinor);
        engine.deal();
        settleIfComplete(r);
        return snapshot(r);
    }

    @Override
    public synchronized RoundState applyAction(String roundId, PlayerAction action) {
        Round r = rounds.get(roundId);
        if (r == null) throw new IllegalArgumentException("unknown round: " + roundId);
        if (r.settled) throw new IllegalStateException("round already settled: " + roundId);
        Engine e = r.engine;

        switch (action) {
            case HIT -> e.hit();
            case STAND -> e.stand();
            case SURRENDER -> e.surrender();
            case DOUBLE -> {
                if (!e.canDouble()) throw new IllegalStateException("cannot double now");
                holdExtra(r, e.active().bet());     // money before the action; engine notional follows
                e.doubleDown();
            }
            case SPLIT -> {
                if (!e.canSplit()) throw new IllegalStateException("cannot split now");
                holdExtra(r, e.active().bet());
                e.split();
            }
            case INSURANCE_TAKE -> {
                if (e.phase() != Phase.INSURANCE) throw new IllegalStateException("cannot take insurance now");
                holdExtra(r, e.hands().get(0).bet() / 2);
                e.takeInsurance(true);
            }
            case INSURANCE_DECLINE -> e.takeInsurance(false);
        }
        settleIfComplete(r);
        return snapshot(r);
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
    private void holdExtra(Round r, long extraMinor) {
        if (extraMinor <= 0) return;
        authorizeWager(r.playerId, r.asset, extraMinor);
        wallet.hold(r.playerId, r.asset, extraMinor, r.roundId + ":hold:" + seq.incrementAndGet());
    }

    private void settleIfComplete(Round r) {
        if (r.settled || r.engine.phase() != Phase.BETTING) return;
        long wagered  = (long) r.engine.stats().totalWagered  - r.wageredAtStart;
        long returned = (long) r.engine.stats().totalReturned - r.returnedAtStart;
        String tx = r.roundId + ":settle";
        wallet.post(List.of(
                new LedgerEntry("e-" + seq.incrementAndGet(), tx, Wallet.escrow(r.playerId),   r.asset, -wagered,            tx, now()),
                new LedgerEntry("e-" + seq.incrementAndGet(), tx, Wallet.available(r.playerId), r.asset,  returned,           tx, now()),
                new LedgerEntry("e-" + seq.incrementAndGet(), tx, Wallet.HOUSE_PNL,             r.asset,  wagered - returned, tx, now())));
        r.settled = true;
        r.payoutMinor = returned;
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

    private static long now() { return System.currentTimeMillis(); }

    private static final class Round {
        final String roundId;
        final Engine engine;
        final String playerId;
        final Asset asset;
        final long wageredAtStart;
        final long returnedAtStart;
        boolean settled;
        long payoutMinor;

        Round(String roundId, Engine engine, String playerId, Asset asset, long wageredAtStart, long returnedAtStart) {
            this.roundId = roundId;
            this.engine = engine;
            this.playerId = playerId;
            this.asset = asset;
            this.wageredAtStart = wageredAtStart;
            this.returnedAtStart = returnedAtStart;
        }
    }
}
