package com.richeyworks.blackjack.platform.game;

import com.richeyworks.blackjack.platform.common.Asset;
import com.richeyworks.blackjack.platform.rng.Rng;

/**
 * Server-authoritative round resolution. Wraps the existing {@code :core} Engine (the same
 * deterministic blackjack rules the desktop build uses) and draws cards from the certified
 * {@link com.richeyworks.blackjack.platform.rng.Rng}. The client sends intents only; it
 * never computes outcomes or balances.
 *
 * <p>Contract: a round MUST be authorized by the {@code ComplianceGate} and its stake held
 * via the {@code Wallet} before {@link #startRound} accepts it, and settlement MUST flow
 * back through the ledger. Outcomes are reproducible from the RNG reveal for audit.
 */
public interface GameRoundService {

    /** Begin a round after the wager has been authorized and the stake held. */
    RoundState startRound(String playerId, Asset asset, long stakeMinor, String idempotencyKey);

    /**
     * Apply one player action to an active round owned by {@code playerId}.
     *
     * <p>The player id is required so a guessed/enumerated round id cannot move
     * another player's money. Pass a stable {@code actionKey} so a client retry
     * does not double-hit or double-hold.
     */
    RoundState applyAction(String playerId, String roundId, PlayerAction action, String actionKey);

    /**
     * Convenience for single-shot callers that mint a fresh action key.
     * Prefer the four-arg form when the client can supply its own key.
     */
    default RoundState applyAction(String roundId, PlayerAction action) {
        throw new UnsupportedOperationException(
                "use applyAction(playerId, roundId, action, actionKey) — round actions must bind a player");
    }

    /**
     * Post-settlement reveal of the server seed for a finished round.
     * Throws if the round is still open or unknown.
     */
    Rng.ServerSeedReveal reveal(String roundId);

    enum PlayerAction { HIT, STAND, DOUBLE, SPLIT, SURRENDER, INSURANCE_TAKE, INSURANCE_DECLINE }

    /**
     * Opaque, server-owned snapshot returned to the thin client for rendering.
     *
     * <p>{@code commitmentHash} is published as soon as the round starts so the
     * client can pin the server's seed before cards land. {@code serverSeedReveal}
     * is non-null only after settlement — that is when verification becomes possible.
     */
    record RoundState(
            String roundId,
            String phase,
            String publicView,
            boolean settled,
            long payoutMinor,
            String commitmentHash,
            String serverSeedReveal) {}
}
