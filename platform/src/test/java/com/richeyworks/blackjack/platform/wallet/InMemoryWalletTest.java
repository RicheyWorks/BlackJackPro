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
    void rejectsMixedIdempotencyKeys() {
        assertThrows(IllegalArgumentException.class, () -> wallet.post(List.of(
                new LedgerEntry("a", "t1", "x", USD, -100, "k1", 0L),
                new LedgerEntry("b", "t1", "y", USD, 100, "k2", 0L))));
    }
}
