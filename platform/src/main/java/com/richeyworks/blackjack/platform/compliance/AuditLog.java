package com.richeyworks.blackjack.platform.compliance;

/**
 * Sink for compliance decisions. EVERY gate decision — allow and deny alike — must be
 * recorded to a tamper-evident, immutable store for regulatory audit. This interface is
 * the seam; production wires it to the append-only audit log.
 *
 * <p><b>Contract for implementations:</b>
 * <ul>
 *   <li>{@code action} may be {@code null}. That is not a caller bug — it is precisely
 *       what a malformed or absent request looks like, and it is the case most worth
 *       having in the log. Record it; do not throw on it.</li>
 *   <li>Throwing is a supported failure mode, but an expensive one: a
 *       {@link ComplianceGate} that cannot record a decision must deny it. Throw when
 *       the record genuinely did not land, and only then. Do not throw for
 *       recoverable conditions a retry or buffer would handle.</li>
 * </ul>
 */
@FunctionalInterface
public interface AuditLog {
    /**
     * @param action   the action judged, or {@code null} if the request was itself absent
     *                 or malformed
     * @param decision the gate's decision; never {@code null}
     */
    void record(ComplianceGate.Action action, ComplianceGate.Decision decision);
}
