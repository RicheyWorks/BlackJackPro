package com.richeyworks.blackjack.platform.compliance;

/**
 * Sink for compliance decisions. EVERY gate decision — allow and deny alike — must be
 * recorded to a tamper-evident, immutable store for regulatory audit. This interface is
 * the seam; production wires it to the append-only audit log.
 */
@FunctionalInterface
public interface AuditLog {
    void record(ComplianceGate.Action action, ComplianceGate.Decision decision);
}
