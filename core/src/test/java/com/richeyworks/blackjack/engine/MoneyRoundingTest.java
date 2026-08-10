package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The game pays in whole dollars, so any ratio landing on a fraction has to
 * round somewhere. Every rounding goes the player's way: money owed to them
 * rounds up, money taken from them rounds down.
 *
 * <p>This is what the house does — where a half-dollar cannot be paid, rounding
 * up is the normal casino outcome — and it removes an edge the player never
 * agreed to. Flooring the 3:2 payout shorted every odd-dollar bet by exactly
 * $0.50, which with 1/5/25/100/500 chips is the common case: a single $5 chip,
 * a single $25 chip, or any odd number of $5 chips all landed on it.
 */
class MoneyRoundingTest {

    private static final int START = 100_000;

    private static Engine engine(long seed) {
        return new Engine(START, new Random(seed), new BlackjackRules());
    }

    /* ---------- blackjack 3:2 ---------- */

    @Test void naturalNeverPaysLessThanTheTrueValue() {
        BlackjackRules r = new BlackjackRules();
        for (int bet = 1; bet <= 2000; bet++) {
            double exact = bet * 1.5;
            int paid = r.blackjackPayout(bet);
            assertTrue(paid >= exact,
                    "bet " + bet + " paid " + paid + " for a true value of " + exact);
            assertTrue(paid - exact < 1.0,
                    "bet " + bet + " overpaid by a whole dollar or more");
        }
    }

    @Test void evenBetsArePaidExactly() {
        BlackjackRules r = new BlackjackRules();
        for (int bet = 2; bet <= 2000; bet += 2) {
            assertEquals(bet * 3 / 2, r.blackjackPayout(bet), "even bets have no fraction to round");
        }
    }

    @Test void theOddDollarShortfallIsGone() {
        BlackjackRules r = new BlackjackRules();
        // Exactly the bets the chip denominations make easy to compose.
        assertEquals(2,  r.blackjackPayout(1));
        assertEquals(8,  r.blackjackPayout(5));
        assertEquals(23, r.blackjackPayout(15));
        assertEquals(38, r.blackjackPayout(25));
        assertEquals(53, r.blackjackPayout(35));
    }

    @Test void aNaturalNeverPaysNothing() {
        BlackjackRules r = new BlackjackRules();
        assertTrue(r.blackjackPayout(1) > 0, "the smallest possible bet must still win something");
    }

    @Test void sixToFiveStillRoundsTheSameWay() {
        // Not the house rule, but the ratio is configurable and a plugin may set it.
        BlackjackRules r = new BlackjackRules();
        r.blackjackPayoutNum = 6;
        r.blackjackPayoutDen = 5;
        assertEquals(6, r.blackjackPayout(5), "6:5 of 5 is exact");
        assertEquals(2, r.blackjackPayout(1), "ceil(1*6/5) = 2");
        for (int bet = 1; bet <= 500; bet++) {
            assertTrue(r.blackjackPayout(bet) >= bet * 6.0 / 5.0, "bet " + bet);
        }
    }

    /* ---------- surrender ---------- */

    @Test void surrenderReturnsAtLeastHalf() {
        BlackjackRules r = new BlackjackRules();
        for (int bet = 1; bet <= 2000; bet++) {
            assertTrue(r.surrenderRefund(bet) >= bet / 2.0,
                    "bet " + bet + " refunded " + r.surrenderRefund(bet));
        }
        assertEquals(13, r.surrenderRefund(25), "a $25 surrender returns 13, not 12");
        assertEquals(50, r.surrenderRefund(100), "even stakes are exact");
    }

    @Test void surrenderCostsNoMoreThanHalfInPlay() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.rules().lateSurrender = true;
            e.addBet(25);
            e.deal();
            if (e.phase() == Phase.INSURANCE) e.takeInsurance(false);
            if (e.phase() != Phase.PLAYER || !e.canSurrender()) continue;
            e.surrender();
            assertEquals(-12, e.lastNet(), "a $25 surrender costs 12, not 13");
            return;
        }
        fail("no surrenderable deal found");
    }

    /* ---------- insurance ---------- */

    @Test void theInsurancePremiumRoundsDown() {
        BlackjackRules r = new BlackjackRules();
        for (int bet = 1; bet <= 2000; bet++) {
            assertTrue(r.insurancePremium(bet) <= bet / 2.0,
                    "the premium is money the player hands over; bet " + bet);
        }
        assertEquals(12, r.insurancePremium(25), "a $25 bet costs 12 to insure, not 13");
    }

    @Test void theOfferAndTheChargeAgree() {
        // canInsure() and takeInsurance() must price the premium identically, or
        // the game offers insurance it then refuses to sell.
        BlackjackRules r = new BlackjackRules();
        for (long seed = 0; seed < 5000; seed++) {
            Engine e = engine(seed);
            e.addBet(25);
            e.deal();
            if (e.phase() != Phase.INSURANCE) continue;
            if (!e.canInsure()) continue;
            assertDoesNotThrow(() -> e.takeInsurance(true),
                    "canInsure() said yes at seed " + seed);
            return;
        }
        fail("no insurance offer found");
    }

    @Test void insurancePaysTwoToOne() {
        BlackjackRules r = new BlackjackRules();
        assertEquals(100, r.insurancePayout(50));
        assertEquals(20,  r.insurancePayout(10));
        assertEquals(24,  r.insurancePayout(12), "2:1 is always exact, nothing to round");
    }

    /* ---------- overflow ---------- */

    @Test void aHugeBetDoesNotOverflowThePayout() {
        BlackjackRules r = new BlackjackRules();
        // bet * 3 overflows int above ~715 million; the long intermediate is
        // what keeps the result positive and correct.
        int huge = 1_000_000_000;
        assertTrue(r.blackjackPayout(huge) > 0, "payout went negative — multiplication overflowed");
        assertEquals(1_500_000_000, r.blackjackPayout(huge));
    }

    /* ---------- the ledger still balances ---------- */

    @Test void moneyIsStillConservedAcrossManyRounds() {
        Engine e = engine(4242);
        e.rules().lateSurrender = true;
        int start = e.bankroll();
        int guard = 0;
        for (int round = 0; round < 3000 && e.bankroll() >= 25; round++) {
            e.addBet(25);                       // an odd-dollar stake, on purpose
            e.deal();
            guard = 0;
            while (e.phase() != Phase.BETTING && guard++ < 40) {
                switch (e.phase()) {
                    case INSURANCE -> e.takeInsurance(round % 3 == 0 && e.canInsure());
                    case PLAYER -> {
                        if (round % 11 == 0 && e.canSurrender())      e.surrender();
                        else if (round % 7 == 0 && e.canSplit())      e.split();
                        else if (round % 5 == 0 && e.canDouble())     e.doubleDown();
                        else if (e.canHit() && e.active().value() < 17) e.hit();
                        else if (e.canStand())                        e.stand();
                        else if (e.canHit())                          e.hit();
                        else                                          round++;
                    }
                    default -> { }
                }
            }
            // The invariant the original audit established, still holding with
            // the new rounding: every dollar is accounted for.
            assertEquals(start - e.stats().totalWagered + e.stats().totalReturned, e.bankroll(),
                    "ledger drifted at round " + round);
        }
        assertTrue(e.stats().hands > 100, "expected a decent sample, got " + e.stats().hands);
    }

    @Test void everyRoundNetIsConsistentWithTheLedger() {
        Engine e = engine(99);
        for (int round = 0; round < 500 && e.bankroll() >= 5; round++) {
            int before = e.bankroll() + e.pendingBet();
            e.addBet(5);
            e.deal();
            int guard = 0;
            while (e.phase() != Phase.BETTING && guard++ < 40) {
                if (e.phase() == Phase.INSURANCE)   e.takeInsurance(false);
                else if (e.phase() == Phase.PLAYER) {
                    if (e.canHit() && e.active().value() < 17) e.hit();
                    else if (e.canStand()) e.stand();
                    else break;
                } else break;
            }
            assertEquals(e.bankroll() - before, e.lastNet(), "round " + round);
        }
    }

    @Test void payUpSaturatesInsteadOfWrappingNegative() {
        BlackjackRules r = new BlackjackRules();
        // 1.5e9 * 3/2 = 2.25e9 — used to cast to a negative int
        int winnings = r.blackjackPayout(1_500_000_000);
        assertTrue(winnings > 0, "winnings must not wrap negative, was " + winnings);
        assertEquals(Integer.MAX_VALUE, winnings);
    }
}
