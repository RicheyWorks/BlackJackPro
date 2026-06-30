package com.richeyworks.blackjack.platform.compliance;

import com.richeyworks.blackjack.platform.common.Asset;

/**
 * The single, FAIL-CLOSED chokepoint every real-money action passes through.
 *
 * <p>No wager, deposit, or withdrawal may occur unless {@link #authorize} returns an
 * allowed {@link Decision}. There is intentionally no bypass path. Unknown or uncertain
 * inputs MUST deny (e.g. uncertain geolocation, pending KYC). Every decision — allow and
 * deny alike — is audit-logged by the implementation.
 */
public interface ComplianceGate {

    Decision authorize(Action action);

    /** A real-money action awaiting authorization. */
    record Action(PlayerComplianceState player, Type type, Asset asset, long amountMinor) {
        public enum Type { WAGER, DEPOSIT, WITHDRAWAL }
    }

    /** Outcome of a gate check. A denial carries a reason for audit + player messaging. */
    record Decision(boolean allowed, DenialReason reason) {
        public static Decision allow() { return new Decision(true, null); }
        public static Decision deny(DenialReason r) { return new Decision(false, r); }
    }

    enum DenialReason {
        KYC_NOT_VERIFIED,
        AGE_NOT_VERIFIED,
        STATE_NOT_LICENSED,
        LOCATION_UNCERTAIN,
        CRYPTO_NOT_PERMITTED_IN_STATE,
        SELF_EXCLUDED,
        LIMIT_EXCEEDED,
        SANCTIONS_OR_AML_HOLD
    }
}
