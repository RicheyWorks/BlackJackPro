package com.richeyworks.blackjack.platform.compliance;

/**
 * Snapshot of everything the {@link ComplianceGate} needs to authorize a real-money
 * action. Assembled server-side by the Identity/KYC and Geolocation contexts and never
 * trusted from the client. A {@code null} {@link #locatedState()} means the player's
 * location is uncertain — which the gate MUST treat as a denial.
 */
public record PlayerComplianceState(
        String playerId,
        KycStatus kyc,
        String locatedState,     // 2-letter US state from the geolocation vendor; null if uncertain
        boolean ageVerified,
        boolean selfExcluded,
        RgLimits limits) {

    public enum KycStatus { UNVERIFIED, PENDING, VERIFIED, REJECTED }

    /** Responsible-gambling limits in fiat minor units (cents). Zero = not set. */
    public record RgLimits(long dailyDepositCents, long dailyLossCents, long sessionMinutes) {}
}
