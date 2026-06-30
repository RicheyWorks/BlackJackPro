package com.richeyworks.blackjack.platform.wallet;

import com.richeyworks.blackjack.platform.common.Asset;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reference, in-memory {@link Wallet} backed by an append-only double-entry ledger.
 *
 * <p>Invariants enforced:
 * <ul>
 *   <li><b>Derived balances</b> — a balance is the sum of an account's ledger entries; it
 *       is never stored or mutated in place.</li>
 *   <li><b>Zero-sum</b> — every {@link #post} must net to zero per asset, or it is rejected.</li>
 *   <li><b>Idempotency</b> — a posting (or hold) replayed with the same idempotency key is a
 *       no-op and returns the original transaction id.</li>
 *   <li><b>Hold safety</b> — {@link #hold} fails if available funds are insufficient.</li>
 * </ul>
 *
 * <p>For production this maps onto a transactional store (e.g. Postgres) with the same
 * invariants enforced in the schema; the in-memory version is for tests and local wiring.
 * Source/liability accounts (custodian, house) may go negative; a player's available
 * account cannot be overdrawn via {@link #hold}.
 */
public final class InMemoryWallet implements Wallet {

    private final List<LedgerEntry> ledger = new ArrayList<>();
    private final Map<String, String> txByIdempotencyKey = new HashMap<>();
    private final AtomicLong seq = new AtomicLong();

    public static String availableAccount(String playerId) { return Wallet.available(playerId); }
    public static String escrowAccount(String playerId)    { return Wallet.escrow(playerId); }

    @Override
    public synchronized long availableMinor(String playerId, Asset asset) {
        return balanceOf(availableAccount(playerId), asset);
    }

    private long balanceOf(String account, Asset asset) {
        long sum = 0L;
        for (LedgerEntry e : ledger) {
            if (e.account().equals(account) && e.asset() == asset) sum += e.amountMinor();
        }
        return sum;
    }

    @Override
    public synchronized String hold(String playerId, Asset asset, long amountMinor, String idempotencyKey) {
        if (amountMinor <= 0) throw new IllegalArgumentException("hold amount must be positive");
        String existing = txByIdempotencyKey.get(idempotencyKey);
        if (existing != null) return existing;                       // idempotent replay

        long available = availableMinor(playerId, asset);
        if (available < amountMinor) {
            throw new IllegalStateException("insufficient funds: have " + available + ", need " + amountMinor);
        }
        String txId = "tx-" + seq.incrementAndGet();
        long now = System.currentTimeMillis();
        List<LedgerEntry> legs = List.of(
                new LedgerEntry("e-" + seq.incrementAndGet(), txId, availableAccount(playerId), asset, -amountMinor, idempotencyKey, now),
                new LedgerEntry("e-" + seq.incrementAndGet(), txId, escrowAccount(playerId),    asset,  amountMinor, idempotencyKey, now));
        post(legs);
        return txId;
    }

    @Override
    public synchronized void post(List<LedgerEntry> legs) {
        if (legs == null || legs.isEmpty()) throw new IllegalArgumentException("no ledger legs");

        String key = legs.get(0).idempotencyKey();
        String tx  = legs.get(0).transactionId();
        for (LedgerEntry e : legs) {
            if (!Objects.equals(e.idempotencyKey(), key)) {
                throw new IllegalArgumentException("all legs of a posting must share one idempotency key");
            }
            if (!Objects.equals(e.transactionId(), tx)) {
                throw new IllegalArgumentException("all legs of a posting must share one transaction id");
            }
        }
        if (key != null && txByIdempotencyKey.containsKey(key)) return;   // idempotent no-op

        Map<Asset, Long> perAsset = new EnumMap<>(Asset.class);
        for (LedgerEntry e : legs) perAsset.merge(e.asset(), e.amountMinor(), Long::sum);
        for (Map.Entry<Asset, Long> sum : perAsset.entrySet()) {
            if (sum.getValue() != 0L) {
                throw new IllegalArgumentException("posting not balanced for " + sum.getKey() + " (net " + sum.getValue() + ")");
            }
        }

        ledger.addAll(legs);
        if (key != null) txByIdempotencyKey.put(key, tx);
    }

    /** Read-only snapshot of the ledger for audit / reconciliation. */
    public synchronized List<LedgerEntry> entries() {
        return List.copyOf(ledger);
    }
}
