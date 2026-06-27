package com.richeyworks.blackjack.engine;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;
class SplitAceSettlementTest {
    @Test void splittingAcesSettlesEachHandExactlyOnce() {
        for (long seed = 0; seed < 5000; seed++) {
            BlackjackRules r = new BlackjackRules();
            Engine e = new Engine(1000, new Random(seed), r);
            e.addBet(10); e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER) continue;
            Hand h = e.active();
            boolean aces = h.size()==2 && h.cards().get(0).rank()==Rank.ACE && h.cards().get(1).rank()==Rank.ACE;
            if (!aces || !e.canSplit()) continue;
            e.split();
            assertEquals(Phase.BETTING, e.phase());
            assertEquals(2, e.hands().size());
            SessionStats s = e.stats();
            assertEquals(2, s.wins + s.losses + s.pushes, "each split-ace hand settles once");
            assertEquals(1, s.splits);
            assertTrue(e.bankroll() >= 980 && e.bankroll() <= 1020, "bankroll: " + e.bankroll());
            return;
        }
        fail("no pair-of-aces deal found");
    }
}
