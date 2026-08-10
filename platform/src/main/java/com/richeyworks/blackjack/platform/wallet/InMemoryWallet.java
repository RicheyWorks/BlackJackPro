package com.richeyworks.blackjack.platform.wallet;

import com.richeyworks.blackjack.platform.common.Asset;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reference, in-memory {@link Wallet} backed by an append-only double-entry ledger.
 *
 * <p>Invariants enforced:
 * <ul>
 *   <li><b>Ledger-derived balances</b> — the ledger is the authority. Balances are
 *       maintained as an index over it, never as an independently writable figure, and
 *       {@link #reconcile()} proves the index and the ledger still agree.</li>
 *   <li><b>Zero-sum</b> — every {@link #post} must net to zero per asset, or it is rejected.</li>
 *   <li><b>Idempotency</b> — a posting (or hold) replayed with the same idempotency key is a
 *       no-op and returns the original transaction id. A replay with a <em>different</em>
 *       amount/player/asset is rejected — silent success would lie about money that never moved.</li>
 *   <li><b>Hold safety</b> — {@link #hold} fails if available funds are insufficient.</li>
 *   <li><b>Checked arithmetic</b> — every sum uses {@link Math#addExact}, so a balance that
 *       would exceed the signed 64-bit range throws instead of wrapping to a negative
 *       number. Cents have headroom to spare; wei-scaled tokens do not.</li>
 * </ul>
 *
 * <p>For production this maps onto a transactional store (e.g. Postgres) with the same
 * invariants enforced in the schema; the in-memory version is for tests and local wiring.
 * Source/liability accounts (custodian, house) may go negative; a player's available
 * account cannot be overdrawn via {@link #hold}.
 */
public final class InMemoryWallet implements Wallet {

    private final List<LedgerEntry> ledger = new ArrayList<>();

    /**
     * Materialised balances: account -> asset -> minor units, updated in the same
     * synchronized block that appends the legs which moved them.
     *
     * <p>This exists because {@link #hold} reads a balance on every wager, and a full
     * ledger scan per wager is O(entries ever posted) — fine for a test fixture, wrong
     * for anything with a real trading day behind it. It is an index, not a second
     * source of truth: nothing writes to it except {@link #post}, and {@link #reconcile()}
     * re-derives from the ledger and fails loudly on any divergence.
     */
    private final Map<String, Map<Asset, Long>> balances = new HashMap<>();

    private final Map<String, String> txByIdempotencyKey = new HashMap<>();
    /**
     * key -> fingerprint of the legs first posted under it, so a replay can be
     * checked rather than assumed. A silent no-op for a posting that differs
     * from the original tells the caller "already applied" about a transaction
     * that was never applied at all.
     */
    private final Map<String, String> legsByIdempotencyKey = new HashMap<>();
    /**
     * hold key -> fingerprint of (player, asset, amount). Holds used to short-circuit
     * on {@link #txByIdempotencyKey} alone, so a second hold with the same key but a
     * different stake reported success and moved nothing — free money if the service
     * then opened a larger round.
     */
    private final Map<String, String> holdByIdempotencyKey = new HashMap<>();
    private final AtomicLong seq = new AtomicLong();

    /** Account, asset and amount of every leg, in a stable order. */
    private static String fingerprint(List<LedgerEntry> legs) {
        List<String> parts = new ArrayList<>(legs.size());
        for (LedgerEntry e : legs) parts.add(e.account() + "|" + e.asset() + "|" + e.amountMinor());
        parts.sort(null);
        return String.join(",", parts);
    }

    private static String holdFingerprint(String playerId, Asset asset, long amountMinor) {
        return playerId + "|" + asset + "|" + amountMinor;
    }

    /**
     * {@code a + b}, refusing to wrap. An overflow here is a corrupt balance that would
     * otherwise be indistinguishable from a legitimate one, so it must be an exception
     * and not a value.
     */
    private static long addExact(long a, long b, String account, Asset asset) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException overflow) {
            throw new ArithmeticException("ledger overflow on " + account + "/" + asset
                    + ": " + a + " + " + b + " leaves the signed 64-bit range");
        }
    }

    public static String availableAccount(String playerId) { return Wallet.available(playerId); }
    public static String escrowAccount(String playerId)    { return Wallet.escrow(playerId); }

    @Override
    public synchronized long availableMinor(String playerId, Asset asset) {
        return balanceOf(availableAccount(playerId), asset);
    }

    /** O(1) read of the materialised balance. See {@link #reconcile()} for the proof it is right. */
    private long balanceOf(String account, Asset asset) {
        Map<Asset, Long> perAsset = balances.get(account);
        if (perAsset == null) return 0L;
        return perAsset.getOrDefault(asset, 0L);
    }

    @Override
    public synchronized String hold(String playerId, Asset asset, long amountMinor, String idempotencyKey) {
        if (amountMinor <= 0) throw new IllegalArgumentException("hold amount must be positive");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(asset, "asset");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        if (idempotencyKey.isBlank()) throw new IllegalArgumentException("hold needs a non-blank idempotency key");

        String holdFp = holdFingerprint(playerId, asset, amountMinor);
        String seenHold = holdByIdempotencyKey.get(idempotencyKey);
        if (seenHold != null) {
            if (!seenHold.equals(holdFp)) {
                throw new IllegalArgumentException("idempotency key " + idempotencyKey
                        + " was already used for a different hold; replaying it would"
                        + " report success for a stake that was never escrowed");
            }
            String existing = txByIdempotencyKey.get(idempotencyKey);
            if (existing == null) {
                throw new IllegalStateException("hold key " + idempotencyKey + " has a fingerprint but no tx");
            }
            return existing; // genuine hold replay
        }

        // Key already used by a post() that was not a hold of this shape.
        if (txByIdempotencyKey.containsKey(idempotencyKey)) {
            throw new IllegalArgumentException("idempotency key " + idempotencyKey
                    + " was already used for a different posting");
        }

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
        // Only record after a successful post so a failed post leaves the key free.
        holdByIdempotencyKey.put(idempotencyKey, holdFp);
        return txId;
    }

    @Override
    public synchronized void post(List<LedgerEntry> legs) {
        if (legs == null || legs.isEmpty()) throw new IllegalArgumentException("no ledger legs");

        String key = legs.get(0).idempotencyKey();
        String tx  = legs.get(0).transactionId();
        if (key == null || key.isBlank()) {
            // Without a key there is no replay protection at all, and a retried
            // network call posts the money twice. Nothing in this codebase
            // needs an unkeyed posting, so require one rather than silently
            // offering a mode that is never safe.
            throw new IllegalArgumentException("a posting must carry an idempotency key");
        }
        for (LedgerEntry e : legs) {
            if (!Objects.equals(e.idempotencyKey(), key)) {
                throw new IllegalArgumentException("all legs of a posting must share one idempotency key");
            }
            if (!Objects.equals(e.transactionId(), tx)) {
                throw new IllegalArgumentException("all legs of a posting must share one transaction id");
            }
        }
        String fingerprint = fingerprint(legs);
        String seen = legsByIdempotencyKey.get(key);
        if (seen != null) {
            if (!seen.equals(fingerprint)) {
                throw new IllegalArgumentException("idempotency key " + key
                        + " was already used for a different posting; replaying it would"
                        + " report success for a transaction that was never applied");
            }
            return;                                                   // genuine replay
        }

        Map<Asset, Long> perAsset = new EnumMap<>(Asset.class);
        for (LedgerEntry e : legs) {
            perAsset.merge(e.asset(), e.amountMinor(),
                    (a, b) -> addExact(a, b, "<posting>", e.asset()));
        }
        for (Map.Entry<Asset, Long> sum : perAsset.entrySet()) {
            if (sum.getValue() != 0L) {
                throw new IllegalArgumentException("posting not balanced for " + sum.getKey() + " (net " + sum.getValue() + ")");
            }
        }

        // Compute every resulting balance BEFORE mutating anything. An overflow on the
        // last leg must not leave the first leg applied — a half-posted transaction is
        // worse than a rejected one, and this is the only place that can produce one.
        Map<String, Map<Asset, Long>> updated = new LinkedHashMap<>();
        for (LedgerEntry e : legs) {
            Map<Asset, Long> pending = updated.computeIfAbsent(e.account(), a -> new EnumMap<>(Asset.class));
            long current = pending.containsKey(e.asset())
                    ? pending.get(e.asset())
                    : balanceOf(e.account(), e.asset());
            pending.put(e.asset(), addExact(current, e.amountMinor(), e.account(), e.asset()));
        }

        ledger.addAll(legs);
        for (Map.Entry<String, Map<Asset, Long>> account : updated.entrySet()) {
            balances.computeIfAbsent(account.getKey(), a -> new EnumMap<>(Asset.class))
                    .putAll(account.getValue());
        }
        txByIdempotencyKey.put(key, tx);
        legsByIdempotencyKey.put(key, fingerprint);
    }

    /**
     * Re-derive every balance from the ledger and verify the materialised figures match.
     *
     * <p>Deliberately O(ledger): this is the audit path, not the hot path. Run it at
     * shift boundaries, after recovery, and in tests. It is the check that keeps the
     * balance map honest — without it, "materialised balance" is just a cache with a
     * nicer name.
     *
     * @throws IllegalStateException on the first account/asset that disagrees
     */
    public synchronized void reconcile() {
        Map<String, Map<Asset, Long>> derived = new HashMap<>();
        for (LedgerEntry e : ledger) {
            derived.computeIfAbsent(e.account(), a -> new EnumMap<>(Asset.class))
                    .merge(e.asset(), e.amountMinor(),
                            (a, b) -> addExact(a, b, e.account(), e.asset()));
        }
        for (Map.Entry<String, Map<Asset, Long>> account : derived.entrySet()) {
            for (Map.Entry<Asset, Long> byAsset : account.getValue().entrySet()) {
                long fromLedger = byAsset.getValue();
                long materialised = balanceOf(account.getKey(), byAsset.getKey());
                if (fromLedger != materialised) {
                    throw new IllegalStateException("balance drift on " + account.getKey()
                            + "/" + byAsset.getKey() + ": ledger says " + fromLedger
                            + ", materialised balance says " + materialised);
                }
            }
        }
        // The other direction: a materialised figure with no ledger behind it at all.
        for (Map.Entry<String, Map<Asset, Long>> account : balances.entrySet()) {
            for (Map.Entry<Asset, Long> byAsset : account.getValue().entrySet()) {
                boolean backedByLedger = derived.containsKey(account.getKey())
                        && derived.get(account.getKey()).containsKey(byAsset.getKey());
                if (!backedByLedger && byAsset.getValue() != 0L) {
                    throw new IllegalStateException("materialised balance on " + account.getKey()
                            + "/" + byAsset.getKey() + " of " + byAsset.getValue()
                            + " has no ledger entries behind it");
                }
            }
        }
    }

    /** Read-only snapshot of the ledger for audit / reconciliation. */
    public synchronized List<LedgerEntry> entries() {
        return List.copyOf(ledger);
    }
}
