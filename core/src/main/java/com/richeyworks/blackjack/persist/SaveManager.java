package com.richeyworks.blackjack.persist;

import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.SessionStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists bankroll + lifetime stats to a flat key=value file. Plain text on
 * purpose — easy to inspect, no JSON dependency in the persistence path.
 */
public final class SaveManager {

    private final Path file;

    public SaveManager(Path file) { this.file = file; }

    public void save(Engine e) {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            SessionStats s = e.stats();
            StringBuilder sb = new StringBuilder();
            sb.append("bankroll=").append(e.bankroll()).append('\n');
            sb.append("hands=").append(s.hands).append('\n');
            sb.append("wins=").append(s.wins).append('\n');
            sb.append("losses=").append(s.losses).append('\n');
            sb.append("pushes=").append(s.pushes).append('\n');
            sb.append("blackjacks=").append(s.blackjacks).append('\n');
            sb.append("busts=").append(s.busts).append('\n');
            sb.append("doubles=").append(s.doubles).append('\n');
            sb.append("splits=").append(s.splits).append('\n');
            sb.append("surrenders=").append(s.surrenders).append('\n');
            sb.append("peakBankroll=").append(s.peakBankroll).append('\n');
            sb.append("totalWagered=").append(s.totalWagered).append('\n');
            sb.append("totalReturned=").append(s.totalReturned).append('\n');
            AtomicFiles.writeString(file, sb.toString());
        } catch (IOException ignored) { /* save failures shouldn't crash the game */ }
    }

    public void load(Engine e) {
        try {
            if (!Files.exists(file)) return;
            for (String line : Files.readAllLines(file)) {
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String k = line.substring(0, eq).trim();
                int n;
                try { n = Integer.parseInt(line.substring(eq + 1).trim()); }
                catch (NumberFormatException ex) { continue; }
                if (n < 0) n = 0;   // reject negative bankroll/counters from a hand-edited save
                SessionStats s = e.stats();
                switch (k) {
                    case "bankroll"      -> e.setBankroll(n);
                    case "hands"         -> s.hands = n;
                    case "wins"          -> s.wins = n;
                    case "losses"        -> s.losses = n;
                    case "pushes"        -> s.pushes = n;
                    case "blackjacks"    -> s.blackjacks = n;
                    case "busts"         -> s.busts = n;
                    case "doubles"       -> s.doubles = n;
                    case "splits"        -> s.splits = n;
                    case "surrenders"    -> s.surrenders = n;
                    case "peakBankroll"  -> s.peakBankroll = n;
                    case "totalWagered"  -> s.totalWagered = n;
                    case "totalReturned" -> s.totalReturned = n;
                    default              -> { /* ignore unknown keys for forward compat */ }
                }
            }
        } catch (IOException ignored) { }
    }
}
