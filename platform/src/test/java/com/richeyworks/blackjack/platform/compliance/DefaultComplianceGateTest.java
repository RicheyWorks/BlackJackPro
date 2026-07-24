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

    // --- PL-13: audit-sink failure ------------------------------------------------

    /** A sink that is down: every write fails. */
    private static final AuditLog BROKEN =
            (a, d) -> { throw new IllegalStateException("audit store unreachable"); };

    /**
     * Before the fix, a failing sink threw out of {@code authorize} and produced no
     * {@link Decision} at all — so whether that ended up fail-open or fail-closed was
     * decided by whatever the caller did with the exception. A fail-closed gate cannot
     * leave that to the caller.
     */
    @Test
    void anUnavailableAuditSinkDeniesInsteadOfThrowing() {
        var g = new DefaultComplianceGate(LicensingPolicy.usDefault(), BROKEN);
        Decision d = g.authorize(new Action(verifiedIn("NJ"), Action.Type.WAGER, Asset.USD, 100));
        assertFalse(d.allowed(), "unlogged action was authorized");
        assertEquals(DenialReason.AUDIT_UNAVAILABLE, d.reason());
    }

    /**
     * The trade-off, stated as a test so it is a decision rather than an accident: an
     * audit outage stops play. It must never do the reverse and permit play that no
     * one can evidence.
     */
    @Test
    void anUnavailableAuditSinkNeverTurnsADenialIntoAnAllow() {
        var g = new DefaultComplianceGate(LicensingPolicy.usDefault(), BROKEN);
        for (Action a : new Action[]{
                new Action(verifiedIn("TX"), Action.Type.WAGER, Asset.USD, 100),        // unlicensed
                new Action(verifiedIn("NJ"), Action.Type.WITHDRAWAL, Asset.USD, 100),   // otherwise fine
                new Action(null, Action.Type.DEPOSIT, Asset.USD, 100)}) {
            assertFalse(g.authorize(a).allowed());
        }
    }

    /**
     * {@code action} is null exactly when the request was malformed — the moment the log
     * matters most. A sink that rejects null (most will, unguarded) previously turned
     * that into an NPE escaping the gate; now it is a denial like any other sink failure.
     */
    @Test
    void aSinkThatRejectsNullActionsStillYieldsADecision() {
        AuditLog npeOnNull = (a, d) -> a.type();   // NPEs when the action is null
        var g = new DefaultComplianceGate(LicensingPolicy.usDefault(), npeOnNull);
        Decision d = g.authorize(null);
        assertFalse(d.allowed());
        assertEquals(DenialReason.AUDIT_UNAVAILABLE, d.reason());
    }

    /** A healthy sink is handed the null action rather than being skipped. */
    @Test
    void aNullActionIsStillOfferedToTheAuditLog() {
        int[] count = {0};
        var g = new DefaultComplianceGate(LicensingPolicy.usDefault(), (a, d) -> count[0]++);
        Decision d = g.authorize(null);
        assertEquals(1, count[0], "the malformed request went unlogged");
        assertEquals(DenialReason.KYC_NOT_VERIFIED, d.reason());
    }
}
