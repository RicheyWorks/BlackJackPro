package com.richeyworks.blackjack.platform.wallet;

import com.richeyworks.blackjack.platform.common.Asset;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryWalletTest {

    private static final Asset USD = Asset.USD;
    private final InMemoryWallet wallet = new InMemoryWallet();

    /** Fund a player's available account from the custodian (a balanced deposit). */
    private void fund(String player, long amountMinor, String key) {
        wallet.post(List.of(
                new LedgerEntry("d1", "tx-" + key, "custodian:hot", USD, -amountMinor, key, 0L),
                new LedgerEntry("d2", "tx-" + key, InMemoryWallet.availableAccount(player), USD, amountMinor, key, 0L)));
    }

    @Test
    void creditsIncreaseDerivedBalance() {
        fund("p1", 1_000, "dep-1");
        assertEquals(1_000, wallet.availableMinor("p1", USD));
    }

    @Test
    void rejectsUnbalancedPosting() {
        assertThrows(IllegalArgumentException.class, () -> wallet.post(List.of(
                new LedgerEntry("a", "t1", "custodian:hot", USD, -100, "k", 0L),
                new LedgerEntry("b", "t1", InMemoryWallet.availableAccount("p1"), USD, 90, "k", 0L))));
    }

    @Test
    void postIsIdempotent() {
        fund("p1", 1_000, "dep-1");
        fund("p1", 1_000, "dep-1");   // replay same key
        assertEquals(1_000, wallet.availableMinor("p1", USD));
    }

    @Test
    void holdMovesFundsOutOfAvailable() {
        fund("p1", 1_000, "dep-1");
        String tx = wallet.hold("p1", USD, 400, "hold-1");
        assertNotNull(tx);
        assertEquals(600, wallet.availableMinor("p1", USD));
    }

    @Test
    void holdRejectsInsufficientFunds() {
        fund("p1", 100, "dep-1");
        assertThrows(IllegalStateException.class, () -> wallet.hold("p1", USD, 500, "hold-1"));
    }

    @Test
    void holdIsIdempotent() {
        fund("p1", 1_000, "dep-1");
        String first = wallet.hold("p1", USD, 400, "hold-1");
        String again = wallet.hold("p1", USD, 400, "hold-1");
        assertEquals(first, again);
        assertEquals(600, wallet.availableMinor("p1", USD));   // not charged twice
    }

    @Test
    void holdRejectsSameKeyWithDifferentAmount() {
        fund("p1", 10_000, "dep-1");
        wallet.hold("p1", USD, 100, "hold-amt");
        assertThrows(IllegalArgumentException.class,
                () -> wallet.hold("p1", USD, 999, "hold-amt"));
        // First hold still the only one applied.
        assertEquals(9_900, wallet.availableMinor("p1", USD));
    }

    @Test
    void rejectsMixedIdempotencyKeys() {
        assertThrows(IllegalArgumentException.class, () -> wallet.post(List.of(
                new LedgerEntry("a", "t1", "x", USD, -100, "k1", 0L),
                new LedgerEntry("b", "t1", "y", USD, 100, "k2", 0L))));
    }

    // --- PL-12: checked arithmetic ------------------------------------------------

    /**
     * A balance pushed past {@link Long#MAX_VALUE} must throw, not wrap. Before the fix
     * the second posting succeeded and left the account holding a large NEGATIVE
     * balance — arithmetically explicable, indistinguishable from a real debt, and
     * silently spendable. Cents would never get here; wei-scaled tokens have roughly
     * nine units of headroom, so this is the asset class the check is for.
     */
    @Test
    void aBalanceThatWouldOverflowThrowsRatherThanWrappingNegative() {
        String vault = "custodian:hot";
        String player = InMemoryWallet.availableAccount("whale");

        wallet.post(List.of(
                new LedgerEntry("a1", "t1", vault, Asset.ETH, -Long.MAX_VALUE, "big-1", 0L),
                new LedgerEntry("a2", "t1", player, Asset.ETH, Long.MAX_VALUE, "big-1", 0L)));
        assertEquals(Long.MAX_VALUE, wallet.availableMinor("whale", Asset.ETH));

        assertThrows(ArithmeticException.class, () -> wallet.post(List.of(
                new LedgerEntry("b1", "t2", vault, Asset.ETH, -1, "big-2", 0L),
                new LedgerEntry("b2", "t2", player, Asset.ETH, 1, "big-2", 0L))));
    }

    /**
     * The overflow is detected on the second leg, so the first leg must not already be
     * applied when it fires. A half-posted transaction breaks the zero-sum invariant
     * and leaves the ledger looking like money was created.
     */
    @Test
    void overflowOnALaterLegDoesNotHalfPost() {
        String vault = "custodian:hot";
        String player = InMemoryWallet.availableAccount("whale");
        wallet.post(List.of(
                new LedgerEntry("a1", "t1", vault, Asset.ETH, -Long.MAX_VALUE, "big-1", 0L),
                new LedgerEntry("a2", "t1", player, Asset.ETH, Long.MAX_VALUE, "big-1", 0L)));
        int before = wallet.entries().size();
        assertThrows(ArithmeticException.class, () -> wallet.post(List.of(
                new LedgerEntry("b1", "t2", vault, Asset.ETH, -1, "big-2", 0L),
                new LedgerEntry("b2", "t2", player, Asset.ETH, 1, "big-2", 0L))));
        assertEquals(before, wallet.entries().size(), "rejected posting must leave no legs");
        wallet.reconcile();
    }
}
