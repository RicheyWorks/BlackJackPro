package com.richeyworks.blackjack.platform.compliance;

import java.util.Set;

/**
 * Per-jurisdiction policy the gate consults. State codes are 2-letter US codes.
 *
 * <p>This is configuration, not law — it MUST be maintained against current licensing.
 * The licensed-state list and crypto bans change frequently (see ADR-0001); treat this
 * as a living artifact reviewed with counsel, not a constant.
 */
public record LicensingPolicy(Set<String> licensedStates, Set<String> cryptoProhibitedStates) {

    public boolean isLicensed(String state) {
        return state != null && licensedStates.contains(state);
    }

    public boolean cryptoAllowed(String state) {
        return state != null && !cryptoProhibitedStates.contains(state);
    }

    /**
     * Snapshot of US real-money online-casino states as of mid-2026: NJ, PA, MI, WV, CT,
     * DE, RI, and ME (Maine legalized but not yet operational). Crypto gambling is
     * prohibited in CA. Verify with counsel before relying on this.
     */
    public static LicensingPolicy usDefault() {
        return new LicensingPolicy(
                Set.of("NJ", "PA", "MI", "WV", "CT", "DE", "RI", "ME"),
                Set.of("CA"));
    }
}
