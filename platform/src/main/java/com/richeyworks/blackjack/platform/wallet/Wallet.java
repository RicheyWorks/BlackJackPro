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

    /**
     * Available balance for a player + asset, in minor units.
     *
     * <p>The ledger is the authority: implementations MUST return exactly what summing
     * the account's entries would produce. They are free — and at production volume
     * expected — to serve that from a materialised balance rather than scanning, since
     * this is called on every wager and a scan is O(entries ever posted). The condition
     * is that the figure is updated in the same atomic unit as the posting that moved
     * it, and can be re-derived from the ledger on demand. A balance updated separately
     * from the ledger is a second source of truth, not a faster read of the first one.
     */
    long availableMinor(String playerId, Asset asset);

    /**
     * Place a hold/escrow for an in-flight wager; fails if available balance is insufficient.
     * Returns the transaction id of the hold posting.
     */
    String hold(String playerId, Asset asset, long amountMinor, String idempotencyKey);

    /**
     * Atomically append a balanced set of ledger legs. Must sum to zero per asset.
     *
     * <p>All-or-nothing: if any leg is rejected — unbalanced, replayed under a key that
     * carried different legs, or arithmetically out of range — none of them are applied.
     * Sums must be overflow-checked; a balance that silently wraps past
     * {@link Long#MAX_VALUE} is indistinguishable from a legitimate negative one.
     */
    void post(List<LedgerEntry> legs);

    /** Canonical account-name conventions shared across the platform. */
    String HOUSE_PNL = "house:pnl";

    static String available(String playerId) { return "player:" + playerId + ":available"; }

    static String escrow(String playerId) { return "player:" + playerId + ":escrow"; }
}
