package com.richeyworks.blackjack.platform.compliance;

import com.richeyworks.blackjack.platform.common.Asset;
import java.util.Objects;

/**
 * Fail-closed reference implementation of {@link ComplianceGate}.
 *
 * <p>Checks run in a fixed order; the FIRST failing check denies. Anything unknown or
 * uncertain — a null action/player, a null or blank located state — denies. Every
 * decision is written to the {@link AuditLog} before it is returned: there is no path
 * that returns without auditing, and no bypass of the checks.
 *
 * <p>Scope note: deposit/loss/session limits that depend on running history and FX live
 * in the responsible-gambling ledger and are out of scope for this snapshot-only gate.
 * The representative immediate check implemented here is the fiat per-deposit cap.
 */
public final class DefaultComplianceGate implements ComplianceGate {

    private final LicensingPolicy policy;
    private final AuditLog audit;

    public DefaultComplianceGate(LicensingPolicy policy, AuditLog audit) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.audit  = Objects.requireNonNull(audit, "audit");
    }

    @Override
    public Decision authorize(Action action) {
        Decision decision = evaluate(action);
        audit.record(action, decision);
        return decision;
    }

    private Decision evaluate(Action action) {
        if (action == null || action.player() == null) {
            return Decision.deny(DenialReason.KYC_NOT_VERIFIED);
        }
        PlayerComplianceState p = action.player();

        if (p.kyc() != PlayerComplianceState.KycStatus.VERIFIED) {
            return Decision.deny(DenialReason.KYC_NOT_VERIFIED);
        }
        if (!p.ageVerified()) {
            return Decision.deny(DenialReason.AGE_NOT_VERIFIED);
        }
        if (p.selfExcluded()) {
            return Decision.deny(DenialReason.SELF_EXCLUDED);
        }
        String state = p.locatedState();
        if (state == null || state.isBlank()) {
            return Decision.deny(DenialReason.LOCATION_UNCERTAIN);
        }
        if (!policy.isLicensed(state)) {
            return Decision.deny(DenialReason.STATE_NOT_LICENSED);
        }
        if (action.asset() != null && action.asset().isCrypto() && !policy.cryptoAllowed(state)) {
            return Decision.deny(DenialReason.CRYPTO_NOT_PERMITTED_IN_STATE);
        }
        if (exceedsDepositCap(action, p)) {
            return Decision.deny(DenialReason.LIMIT_EXCEEDED);
        }
        return Decision.allow();
    }

    /** Representative immediate limit: fiat per-deposit cap in cents. 0 = no cap set. */
    private boolean exceedsDepositCap(Action action, PlayerComplianceState p) {
        long cap = (p.limits() == null) ? 0L : p.limits().dailyDepositCents();
        return action.type() == Action.Type.DEPOSIT
                && action.asset() == Asset.USD
                && cap > 0
                && action.amountMinor() > cap;
    }
}
