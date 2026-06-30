package com.richeyworks.blackjack.platform.payments;

import com.richeyworks.blackjack.platform.common.Asset;

/**
 * Custodial crypto deposits/withdrawals through a REGULATED custodian — private keys are
 * never held in-app. Operating this makes the business a FinCEN Money Services Business:
 * every movement is screened (sanctions/OFAC + chain analytics), may trigger CTR/SAR
 * filing, and is subject to travel-rule data sharing. Crypto is feature-flagged off per
 * state where prohibited; the {@code ComplianceGate} remains the authority on every action.
 */
public interface CryptoCashier {

    /** Allocate (or return) a custodial deposit address for a player + asset. */
    String depositAddress(String playerId, Asset asset);

    /**
     * Request a withdrawal. Returns the screening outcome; funds move only after it clears.
     * A non-cleared result holds the funds pending manual review or blocks them.
     */
    ScreeningResult requestWithdrawal(String playerId, Asset asset, long amountMinor,
                                      String destination, String idempotencyKey);

    record ScreeningResult(Status status, String reference) {
        public enum Status { CLEARED, MANUAL_REVIEW, BLOCKED_SANCTIONS, BLOCKED_AML }
    }
}
