package com.richeyworks.blackjack.platform.wallet;

import com.richeyworks.blackjack.platform.common.Asset;
import java.util.List;

/**
 * Authoritative balance view over the append-only double-entry ledger. Implementations
 * MUST be idempotent (keyed on {@code idempotencyKey}) and enforce the zero-sum invariant
 * on every {@link #post}: the legs of a transaction must net to zero per asset. Balances
 * are always derived from the ledger, never stored as a mutable figure.
 */
public interface Wallet {

    /** Derived available balance (ledger sum) for a player + asset, in minor units. */
    long availableMinor(String playerId, Asset asset);

    /**
     * Place a hold/escrow for an in-flight wager; fails if available balance is insufficient.
     * Returns the transaction id of the hold posting.
     */
    String hold(String playerId, Asset asset, long amountMinor, String idempotencyKey);

    /** Atomically append a balanced set of ledger legs. Must sum to zero per asset. */
    void post(List<LedgerEntry> legs);

    /** Canonical account-name conventions shared across the platform. */
    String HOUSE_PNL = "house:pnl";

    static String available(String playerId) { return "player:" + playerId + ":available"; }

    static String escrow(String playerId) { return "player:" + playerId + ":escrow"; }
}
