package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.persist.SaveManager;
import com.richeyworks.blackjack.plugin.SideBet;
import com.richeyworks.blackjack.plugin.SideBetManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chips staked but not yet played live in {@code pendingBet} and in the side-bet
 * manager, and the save file records only the bankroll — so anything left on the
 * felt when the session ends is destroyed unless it is returned first.
 *
 * <p>These tests pin the money down across a save/reload cycle, which is what a
 * player actually experiences when they close the window mid-bet.
 */
class SessionExitTest {

    private static final int START = 1000;

    private static Engine engine() {
        return new Engine(START, new Random(1));
    }

    /** Minimal always-losing side bet so the manager is "available". */
    private static SideBetManager sideBets() {
        return new SideBetManager(new SideBet() {
            @Override public String displayName() { return "test"; }
            @Override public String lastOutcome() { return ""; }
            @Override public List<String> payoutTable() { return List.of(); }
            @Override public int settle(List<Card> p, Card up, int bet) { return 0; }
        });
    }

    @Test void pendingMainBetIsReturned() {
        Engine e = engine();
        e.addBet(100);
        e.addBet(100);
        assertEquals(800, e.bankroll());

        int returned = BlackJackProApp.returnStakedChips(e, sideBets());

        assertEquals(200, returned);
        assertEquals(START, e.bankroll(), "chips on the felt come back");
        assertEquals(0, e.pendingBet());
    }

    @Test void pendingSideBetIsReturned() {
        Engine e = engine();
        SideBetManager sb = sideBets();
        int added = sb.add(25, e.bankroll());
        e.setBankroll(e.bankroll() - added);
        assertEquals(975, e.bankroll());

        int returned = BlackJackProApp.returnStakedChips(e, sb);

        assertEquals(25, returned);
        assertEquals(START, e.bankroll());
        assertEquals(0, sb.pending());
    }

    @Test void mainAndSideStakesAreBothReturned() {
        Engine e = engine();
        SideBetManager sb = sideBets();
        e.addBet(50);
        int added = sb.add(10, e.bankroll());
        e.setBankroll(e.bankroll() - added);
        assertEquals(940, e.bankroll());

        assertEquals(60, BlackJackProApp.returnStakedChips(e, sb));
        assertEquals(START, e.bankroll());
    }

    @Test void nothingStakedIsANoOp() {
        Engine e = engine();
        assertEquals(0, BlackJackProApp.returnStakedChips(e, sideBets()));
        assertEquals(START, e.bankroll());
    }

    @Test void midRoundStakeIsLeftAlone() {
        Engine e = engine();
        e.addBet(100);
        e.deal();
        if (e.phase() == Phase.BETTING) return;      // settled instantly; nothing to test
        int before = e.bankroll();

        // The stake now belongs to a live hand, not to pendingBet. Engine.clearBet
        // is phase-guarded, so walking away mid-hand forfeits it as it would at a
        // real table -- but nothing else may move.
        assertEquals(0, BlackJackProApp.returnStakedChips(e, sideBets()));
        assertEquals(before, e.bankroll());
    }

    @Test void savingAfterReturningPreservesTheStakeAcrossRelaunch() throws IOException {
        Path file = Files.createTempFile("bjp-save", ".txt");
        try {
            Engine e = engine();
            e.addBet(200);
            BlackJackProApp.returnStakedChips(e, sideBets());
            new SaveManager(file).save(e);

            Engine reloaded = engine();
            new SaveManager(file).load(reloaded);

            assertEquals(START, reloaded.bankroll(),
                    "closing the window mid-bet must not destroy chips");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test void savingWithoutReturningIsWhatUsedToLoseTheMoney() throws IOException {
        // Documents the regression this guards against: the same sequence minus
        // the return step silently drops the stake.
        Path file = Files.createTempFile("bjp-save", ".txt");
        try {
            Engine e = engine();
            e.addBet(200);
            new SaveManager(file).save(e);          // no returnStakedChips()

            Engine reloaded = engine();
            new SaveManager(file).load(reloaded);

            assertEquals(START - 200, reloaded.bankroll());
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
