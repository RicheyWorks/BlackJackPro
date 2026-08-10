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
        return state != null && licensedStates.contains(normalize(state));
    }

    /**
     * Crypto is allowed only in licensed states that are not on the prohibition list.
     *
     * <p>The previous check was {@code !cryptoProhibited.contains(state)}, which
     * fail-opened for every unlisted/unlicensed jurisdiction (including typos and
     * null-adjacent blanks that slipped past earlier checks). Real-money crypto
     * must fail closed: unknown state ⇒ no crypto.
     */
    public boolean cryptoAllowed(String state) {
        if (state == null || state.isBlank()) return false;
        String s = normalize(state);
        if (!licensedStates.contains(s)) return false;
        return !cryptoProhibitedStates.contains(s);
    }

    private static String normalize(String state) {
        return state.trim().toUpperCase(java.util.Locale.US);
    }

    /**
     * Snapshot of US real-money online-casino states that are accepting play.
     * ME is legalized but not yet operational — omitted until go-live.
     * Crypto gambling is prohibited in CA. Verify with counsel before relying on this.
     */
    public static LicensingPolicy usDefault() {
        return new LicensingPolicy(
                Set.of("NJ", "PA", "MI", "WV", "CT", "DE", "RI"),
                Set.of("CA"));
    }
}
