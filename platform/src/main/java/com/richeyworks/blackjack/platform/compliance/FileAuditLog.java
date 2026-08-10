package com.richeyworks.blackjack.platform.compliance;

import com.richeyworks.blackjack.persist.AtomicFiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Objects;

/**
 * Append-only file sink for {@link AuditLog}.
 *
 * <p>Every decision is one UTF-8 line: timestamp, allow/deny, reason, action type,
 * player id, state, asset, amount. Lines are never rewritten in place. A failed
 * write throws so the fail-closed gate can deny rather than act unlogged.
 *
 * <p>This is a durable reference for local/dev operators — not a regulated
 * WORM archive. Production still needs a tamper-evident store with retention
 * policy and access controls.
 */
public final class FileAuditLog implements AuditLog {

    private final Path file;

    public FileAuditLog(Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    @Override
    public synchronized void record(ComplianceGate.Action action, ComplianceGate.Decision decision) {
        Objects.requireNonNull(decision, "decision");
        String line = format(action, decision);
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            // Create the file if missing, then append. Avoid AtomicFiles for the
            // hot path — a full rewrite on every decision would lose concurrency
            // and thrash disk; append is the audit-log primitive.
            if (!Files.exists(file)) {
                AtomicFiles.writeString(file, line + "\n");
                return;
            }
            Files.writeString(file, line + "\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("audit log write failed: " + file, e);
        }
    }

    /** How many lines are currently on disk — for tests and health checks. */
    public long lineCount() throws IOException {
        if (!Files.exists(file)) return 0;
        try (var lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return lines.count();
        }
    }

    public Path path() { return file; }

    private static String format(ComplianceGate.Action action, ComplianceGate.Decision decision) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(Instant.now().toString());
        sb.append('\t').append(decision.allowed() ? "ALLOW" : "DENY");
        sb.append('\t').append(decision.reason() == null ? "-" : decision.reason().name());
        if (action == null) {
            sb.append("\t-\t-\t-\t-\t0");
            return sb.toString();
        }
        sb.append('\t').append(action.type() == null ? "-" : action.type().name());
        var player = action.player();
        sb.append('\t').append(player == null || player.playerId() == null ? "-" : player.playerId());
        sb.append('\t').append(player == null || player.locatedState() == null ? "-" : player.locatedState());
        sb.append('\t').append(action.asset() == null ? "-" : action.asset().name());
        sb.append('\t').append(action.amountMinor());
        return sb.toString();
    }
}
