package com.richeyworks.blackjack.persist;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Crash-safe file writes. Content is written to a sibling temp file and then
 * atomically renamed onto the target, so a crash, power loss, or full disk
 * mid-write leaves the previous file intact rather than a half-written one.
 *
 * <p>Falls back to a plain replace on the rare filesystem that can't do an
 * atomic move (e.g. across certain network mounts), and always cleans up the
 * temp file.
 */
public final class AtomicFiles {

    private AtomicFiles() {}

    /** Write {@code content} to {@code file} atomically, creating parent dirs as needed. */
    public static void writeString(Path file, String content) throws IOException {
        Path dir = file.toAbsolutePath().getParent();
        Files.createDirectories(dir);
        Path tmp = Files.createTempFile(dir, ".tmp-", ".swp");
        try {
            Files.writeString(tmp, content);
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);   // no-op once the move has consumed it
        }
    }
}
