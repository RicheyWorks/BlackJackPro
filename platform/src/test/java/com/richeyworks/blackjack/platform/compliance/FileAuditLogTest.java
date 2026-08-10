package com.richeyworks.blackjack.platform.compliance;

import com.richeyworks.blackjack.platform.common.Asset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileAuditLogTest {

    @TempDir Path dir;

    @Test void appendsAllowAndDenyLines() throws Exception {
        Path log = dir.resolve("audit.log");
        FileAuditLog audit = new FileAuditLog(log);

        PlayerComplianceState player = new PlayerComplianceState(
                "p1",
                PlayerComplianceState.KycStatus.VERIFIED,
                "NJ",
                true,
                false,
                new PlayerComplianceState.RgLimits(0, 0, 0));

        audit.record(
                new ComplianceGate.Action(player, ComplianceGate.Action.Type.WAGER, Asset.USD, 100),
                ComplianceGate.Decision.allow());
        audit.record(
                new ComplianceGate.Action(player, ComplianceGate.Action.Type.WAGER, Asset.BTC, 50),
                ComplianceGate.Decision.deny(ComplianceGate.DenialReason.CRYPTO_NOT_PERMITTED_IN_STATE));
        audit.record(null, ComplianceGate.Decision.deny(ComplianceGate.DenialReason.KYC_NOT_VERIFIED));

        assertEquals(3, audit.lineCount());
        String body = Files.readString(log);
        assertTrue(body.contains("ALLOW"));
        assertTrue(body.contains("DENY"));
        assertTrue(body.contains("p1"));
        assertTrue(body.contains("CRYPTO_NOT_PERMITTED_IN_STATE"));
    }

    @Test void writeFailureSurfacesSoGateCanFailClosed(@TempDir Path dir) {
        // Point at a path whose parent is a file — createDirectories / write must fail.
        Path notADir = dir.resolve("file-not-dir");
        try {
            Files.writeString(notADir, "x");
        } catch (Exception e) {
            fail(e);
        }
        FileAuditLog audit = new FileAuditLog(notADir.resolve("child.log"));
        assertThrows(IllegalStateException.class, () ->
                audit.record(null, ComplianceGate.Decision.allow()));
    }
}
