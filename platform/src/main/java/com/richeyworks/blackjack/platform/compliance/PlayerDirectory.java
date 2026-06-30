package com.richeyworks.blackjack.platform.compliance;

/**
 * Looks up a player's current compliance snapshot. The seam between the game/money planes
 * and the Identity/KYC + Geolocation contexts: implementations assemble a fresh
 * {@link PlayerComplianceState} (verified KYC, current located state, RG limits, exclusion)
 * for each call so the {@link ComplianceGate} always decides on live data.
 */
@FunctionalInterface
public interface PlayerDirectory {
    PlayerComplianceState lookup(String playerId);
}
