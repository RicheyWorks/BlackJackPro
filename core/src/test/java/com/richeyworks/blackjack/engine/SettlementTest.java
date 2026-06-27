package com.richeyworks.blackjack.engine;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;
class SettlementTest {
    private static final int START = 1000, BET = 100;
    private static Engine engine(long seed){ return new Engine(START, new Random(seed), new BlackjackRules()); }

    @Test void playerNaturalBlackjackPaysThreeToTwo() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed); e.addBet(BET); e.deal();
            if (e.phase() == Phase.INSURANCE) continue;
            Hand p = e.hands().get(0);
            boolean playerBJ = p.size()==2 && p.value()==21;
            boolean dealerBJ = e.dealer().value()==21 && e.dealer().size()==2;
            if (!playerBJ || dealerBJ) continue;
            assertEquals(Phase.BETTING, e.phase(), "natural BJ settles immediately");
            assertEquals(START + BET*3/2, e.bankroll(), "3:2 natural payout");
            assertEquals(1, e.stats().blackjacks);
            return;
        }
        fail("no player natural blackjack found");
    }

    @Test void playerBustLosesEntireStake() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed); e.addBet(BET); e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER) continue;
            boolean busted = false;
            while (e.phase() == Phase.PLAYER && e.canHit()) {
                e.hit();
                if (e.hands().get(0).isBust()) { busted = true; break; }
            }
            if (!busted) continue;
            assertEquals(Phase.BETTING, e.phase());
            assertEquals(START - BET, e.bankroll(), "bust forfeits stake");
            assertTrue(e.stats().busts >= 1);
            return;
        }
        fail("no bustable deal found");
    }

    @Test void moneyIsConservedAcrossManyRounds() {
        Engine e = engine(20260626L);
        for (int round = 0; round < 300 && e.bankroll() >= BET; round++) {
            e.addBet(BET); e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            while (e.phase() == Phase.PLAYER) {
                if (e.canDouble()) e.doubleDown();
                else if (e.canStand()) e.stand();
                else break;
            }
            assertEquals(Phase.BETTING, e.phase(), "round " + round + " must resolve");
            SessionStats s = e.stats();
            assertEquals(START - s.totalWagered + s.totalReturned, e.bankroll(),
                    "accounting drift at round " + round);
        }
        assertTrue(e.stats().hands > 0);
    }

    /** Accepting insurance must keep the wagered/returned ledger consistent. */
    @Test void insuranceIsReflectedInTotals() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed); e.addBet(BET); e.deal();
            if (e.phase() != Phase.INSURANCE) continue;   // need a dealer Ace up
            e.takeInsurance(true);                         // accept insurance
            while (e.phase() == Phase.PLAYER) { if (e.canStand()) e.stand(); else break; }
            assertEquals(Phase.BETTING, e.phase());
            SessionStats s = e.stats();
            assertEquals(START - s.totalWagered + s.totalReturned, e.bankroll(),
                    "insurance stake must be reflected in totals");
            return;
        }
        fail("no dealer-ace deal found in seed range");
    }
}
