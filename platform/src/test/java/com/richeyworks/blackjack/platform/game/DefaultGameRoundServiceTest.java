package com.richeyworks.blackjack.platform.game;

import com.richeyworks.blackjack.platform.common.Asset;
import com.richeyworks.blackjack.platform.compliance.ComplianceGate;
import com.richeyworks.blackjack.platform.compliance.DefaultComplianceGate;
import com.richeyworks.blackjack.platform.compliance.LicensingPolicy;
import com.richeyworks.blackjack.platform.compliance.PlayerComplianceState;
import com.richeyworks.blackjack.platform.compliance.PlayerComplianceState.KycStatus;
import com.richeyworks.blackjack.platform.compliance.PlayerComplianceState.RgLimits;
import com.richeyworks.blackjack.platform.compliance.PlayerDirectory;
import com.richeyworks.blackjack.platform.rng.Rng;
import com.richeyworks.blackjack.platform.wallet.InMemoryWallet;
import com.richeyworks.blackjack.platform.wallet.LedgerEntry;
import com.richeyworks.blackjack.platform.wallet.Wallet;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultGameRoundServiceTest {

    private static final Asset USD = Asset.USD;

    // Deterministic Rng so the shoe shuffles reproducibly in tests.
    private final Rng rng = new Rng() {
        public String commitServerSeed(String roundId) { return "commit"; }
        public int nextInt(String roundId, String clientSeed, long nonce, int bound) {
            return new Random(Objects.hash(roundId, clientSeed, nonce)).nextInt(bound);
        }
        public ServerSeedReveal reveal(String roundId) { return new ServerSeedReveal(roundId, "seed", "commit"); }
    };

    private final InMemoryWallet wallet = new InMemoryWallet();
    private final ComplianceGate gate = new DefaultComplianceGate(LicensingPolicy.usDefault(), (a, d) -> { });
    private final PlayerDirectory verified =
            id -> new PlayerComplianceState(id, KycStatus.VERIFIED, "NJ", true, false, new RgLimits(0, 0, 0));
    private final GameRoundService svc = new DefaultGameRoundService(gate, verified, wallet, rng);

    private void fund(long amountMinor) {
        wallet.post(List.of(
                new LedgerEntry("d1", "tx-seed", "custodian:hot", USD, -amountMinor, "seed", 0L),
                new LedgerEntry("d2", "tx-seed", Wallet.available("p1"), USD, amountMinor, "seed", 0L)));
    }

    private long escrowBalance() {
        long sum = 0;
        for (LedgerEntry e : wallet.entries()) {
            if (e.account().equals(Wallet.escrow("p1")) && e.asset() == USD) sum += e.amountMinor();
        }
        return sum;
    }

    private GameRoundService.RoundState standOut(GameRoundService.RoundState st) {
        int guard = 0;
        while (!st.settled() && guard++ < 20) {
            var action = "INSURANCE".equals(st.phase())
                    ? GameRoundService.PlayerAction.INSURANCE_DECLINE
                    : GameRoundService.PlayerAction.STAND;
            st = svc.applyAction(st.roundId(), action);
        }
        return st;
    }

    @Test
    void roundReconcilesToLedgerAndReleasesEscrow() {
        fund(100_000);
        GameRoundService.RoundState st = svc.startRound("p1", USD, 100, "round-key-1");
        st = standOut(st);
        assertTrue(st.settled(), "round should settle");
        assertEquals(0, escrowBalance(), "escrow released after settlement");
        // No double/split/insurance in this path, so wagered == base stake (100).
        assertEquals(100_000 - 100 + st.payoutMinor(), wallet.availableMinor("p1", USD));
    }

    @Test
    void deniesWagerForUnverifiedPlayer() {
        fund(100_000);
        GameRoundService denied = new DefaultGameRoundService(gate,
                id -> new PlayerComplianceState(id, KycStatus.PENDING, "NJ", true, false, new RgLimits(0, 0, 0)),
                wallet, rng);
        assertThrows(IllegalStateException.class, () -> denied.startRound("p1", USD, 100, "k2"));
    }

    @Test
    void rejectsStakeBeyondBalance() {
        fund(50);
        assertThrows(IllegalStateException.class, () -> svc.startRound("p1", USD, 100_000, "k3"));
    }

    @Test
    void expireDoesNotAllowFreeSecondRoundOnSameKey() {
        fund(50_000);
        GameRoundService.RoundState st = svc.startRound("p1", USD, 100, "expire-key");
        st = standOut(st);
        assertTrue(st.settled());
        long afterFirst = wallet.availableMinor("p1", USD);

        // Operator sweep forgets the live map entry but must keep the tombstone.
        ((DefaultGameRoundService) svc).expireRounds(0);

        // Same client key: must return the finished snapshot, not a free new deal.
        GameRoundService.RoundState again = svc.startRound("p1", USD, 100, "expire-key");
        assertTrue(again.settled());
        assertEquals(st.roundId(), again.roundId());
        assertEquals(afterFirst, wallet.availableMinor("p1", USD), "no second free round");
        assertEquals(0, escrowBalance());
        wallet.reconcile();
    }

    @Test
    void applyActionRejectsWrongPlayer() {
        fund(50_000);
        GameRoundService.RoundState st = svc.startRound("p1", USD, 100, "owner-key");
        if (st.settled()) return; // rare natural — nothing to apply
        assertThrows(IllegalStateException.class, () ->
                svc.applyAction("p2", st.roundId(), GameRoundService.PlayerAction.STAND, "a1"));
    }

    @Test
    void applyActionIsIdempotentOnActionKey() {
        fund(50_000);
        GameRoundService.RoundState st = svc.startRound("p1", USD, 100, "idem-act");
        if (st.settled()) return;
        var action = "INSURANCE".equals(st.phase())
                ? GameRoundService.PlayerAction.INSURANCE_DECLINE
                : GameRoundService.PlayerAction.STAND;
        GameRoundService.RoundState a = svc.applyAction("p1", st.roundId(), action, "same-key");
        GameRoundService.RoundState b = svc.applyAction("p1", st.roundId(), action, "same-key");
        assertEquals(a.phase(), b.phase());
        assertEquals(a.settled(), b.settled());
        assertEquals(a.publicView(), b.publicView());
    }

    @Test
    void roundIdsAreNotSequentialGuessable() {
        fund(50_000);
        var a = svc.startRound("p1", USD, 50, "id-a");
        // finish so next start is clean
        standOut(a);
        var b = svc.startRound("p1", USD, 50, "id-b");
        assertNotEquals(a.roundId(), b.roundId());
        assertTrue(a.roundId().startsWith("round-"));
        // UUID form, not "round-1" / "round-2"
        assertTrue(a.roundId().length() > 20);
    }
}
