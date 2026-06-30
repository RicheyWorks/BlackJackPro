package com.richeyworks.blackjack.platform.compliance;

import com.richeyworks.blackjack.platform.common.Asset;
import com.richeyworks.blackjack.platform.compliance.ComplianceGate.Action;
import com.richeyworks.blackjack.platform.compliance.ComplianceGate.Decision;
import com.richeyworks.blackjack.platform.compliance.ComplianceGate.DenialReason;
import com.richeyworks.blackjack.platform.compliance.PlayerComplianceState.KycStatus;
import com.richeyworks.blackjack.platform.compliance.PlayerComplianceState.RgLimits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultComplianceGateTest {

    private final ComplianceGate gate =
            new DefaultComplianceGate(LicensingPolicy.usDefault(), (a, d) -> { });

    private static PlayerComplianceState verifiedIn(String state) {
        return new PlayerComplianceState("p1", KycStatus.VERIFIED, state, true, false,
                new RgLimits(0, 0, 0));
    }

    private Decision wager(PlayerComplianceState p, Asset asset, long amt) {
        return gate.authorize(new Action(p, Action.Type.WAGER, asset, amt));
    }

    @Test
    void allowsVerifiedPlayerInLicensedState() {
        assertTrue(wager(verifiedIn("NJ"), Asset.USD, 500).allowed());
    }

    @Test
    void deniesUnverifiedKyc() {
        var p = new PlayerComplianceState("p", KycStatus.PENDING, "NJ", true, false, new RgLimits(0, 0, 0));
        Decision d = wager(p, Asset.USD, 500);
        assertFalse(d.allowed());
        assertEquals(DenialReason.KYC_NOT_VERIFIED, d.reason());
    }

    @Test
    void deniesUnverifiedAge() {
        var p = new PlayerComplianceState("p", KycStatus.VERIFIED, "NJ", false, false, new RgLimits(0, 0, 0));
        assertEquals(DenialReason.AGE_NOT_VERIFIED, wager(p, Asset.USD, 500).reason());
    }

    @Test
    void deniesSelfExcludedPlayer() {
        var p = new PlayerComplianceState("p", KycStatus.VERIFIED, "NJ", true, true, new RgLimits(0, 0, 0));
        assertEquals(DenialReason.SELF_EXCLUDED, wager(p, Asset.USD, 500).reason());
    }

    @Test
    void deniesUncertainLocation() {
        assertEquals(DenialReason.LOCATION_UNCERTAIN, wager(verifiedIn(null), Asset.USD, 500).reason());
    }

    @Test
    void deniesUnlicensedState() {
        assertEquals(DenialReason.STATE_NOT_LICENSED, wager(verifiedIn("TX"), Asset.USD, 500).reason());
    }

    @Test
    void deniesCryptoInProhibitedState() {
        // A state that is licensed for play but prohibits crypto wagering.
        var policy = new LicensingPolicy(java.util.Set.of("NJ"), java.util.Set.of("NJ"));
        var g = new DefaultComplianceGate(policy, (a, d) -> { });
        var p = verifiedIn("NJ");
        assertEquals(DenialReason.CRYPTO_NOT_PERMITTED_IN_STATE,
                g.authorize(new Action(p, Action.Type.WAGER, Asset.BTC, 1)).reason());
        // ...but fiat in the same state is fine.
        assertTrue(g.authorize(new Action(p, Action.Type.WAGER, Asset.USD, 500)).allowed());
    }

    @Test
    void deniesDepositOverFiatCap() {
        var p = new PlayerComplianceState("p", KycStatus.VERIFIED, "NJ", true, false,
                new RgLimits(10_000, 0, 0)); // $100/day deposit cap
        Decision over = gate.authorize(new Action(p, Action.Type.DEPOSIT, Asset.USD, 20_000));
        assertEquals(DenialReason.LIMIT_EXCEEDED, over.reason());
        assertTrue(gate.authorize(new Action(p, Action.Type.DEPOSIT, Asset.USD, 5_000)).allowed());
    }

    @Test
    void auditLogReceivesEveryDecision() {
        int[] count = {0};
        var g = new DefaultComplianceGate(LicensingPolicy.usDefault(), (a, d) -> count[0]++);
        g.authorize(new Action(verifiedIn("NJ"), Action.Type.WAGER, Asset.USD, 100));
        g.authorize(new Action(verifiedIn("TX"), Action.Type.WAGER, Asset.USD, 100));
        assertEquals(2, count[0]);
    }
}
