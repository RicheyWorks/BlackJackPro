package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused accounting tests for round actions that the original suite exercised
 * only indirectly: late surrender, the two insurance pay-off branches (dealer
 * has / lacks blackjack), and double-down wagering. Each scenario is reached
 * deterministically by scanning fixed RNG seeds, so the tests are repeatable.
 */
class RoundActionsTest {

    private static final int START = 1000, BET = 100;

    private static Engine engine(long seed) {
        return new Engine(START, new Random(seed), new BlackjackRules());
    }

    private static boolean isBlackjack(Hand h) {
        return h.size() == 2 && h.value() == 21;
    }

    /** Surrender forfeits half the stake, books a loss, and ends the round. */
    @Test void lateSurrenderRefundsHalfTheStake() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() != Phase.PLAYER) continue;     // skip naturals / insurance offers
            if (!e.canSurrender()) continue;
            e.surrender();
            assertEquals(Phase.BETTING, e.phase(), "surrender settles the round");
            assertEquals(START - BET / 2, e.bankroll(), "half the stake is returned");
            assertEquals(1, e.stats().surrenders);
            assertEquals(1, e.stats().losses, "surrender books a loss");
            SessionStats s = e.stats();
            assertEquals(START - s.totalWagered + s.totalReturned, e.bankroll(),
                    "surrender keeps the ledger balanced");
            return;
        }
        fail("no surrenderable deal found in seed range");
    }

    /** Insuring a dealer blackjack is even money: the round nets to zero. */
    @Test void insuredDealerBlackjackBreaksEven() {
        for (long seed = 0; seed < 60000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() != Phase.INSURANCE) continue;
            if (isBlackjack(e.hands().get(0))) continue;  // a player BJ pushes; out of scope here
            e.takeInsurance(true);
            if (e.phase() != Phase.BETTING) continue;     // only when the dealer actually had BJ
            assertTrue(e.dealer().isBlackjack(), "settling out of insurance implies dealer BJ");
            assertEquals(START, e.bankroll(),
                    "insurance pays 2:1 and exactly offsets the lost main bet");
            SessionStats s = e.stats();
            assertEquals(START - s.totalWagered + s.totalReturned, e.bankroll(),
                    "insured-loss ledger stays balanced");
            return;
        }
        fail("no dealer-blackjack-with-ace-up deal found in seed range");
    }

    /** Declining insurance against a dealer blackjack loses only the main bet. */
    @Test void declinedInsuranceDealerBlackjackLosesMainBetOnly() {
        for (long seed = 0; seed < 60000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() != Phase.INSURANCE) continue;
            if (isBlackjack(e.hands().get(0))) continue;
            e.takeInsurance(false);
            if (e.phase() != Phase.BETTING) continue;     // dealer had BJ -> round settled
            assertTrue(e.dealer().isBlackjack());
            assertEquals(START - BET, e.bankroll(), "no insurance => full main stake lost");
            return;
        }
        fail("no dealer-blackjack deal found in seed range");
    }

    /** Insurance is offerable only when the half-bet premium is affordable. */
    @Test void canInsureRequiresAffordablePremium() {
        for (long seed = 0; seed < 60000; seed++) {
            Engine e = engine(seed);
            e.addBet(800);                       // premium would be 400, leaving only 200
            e.deal();
            if (e.phase() != Phase.INSURANCE) continue;
            assertFalse(e.canInsure(), "200 left cannot cover a 400 insurance premium");
            assertThrows(IllegalStateException.class, () -> e.takeInsurance(true),
                    "accepting unaffordable insurance is rejected by the engine");
            return;
        }
        fail("no dealer-ace deal found for the large bet");
    }

    /** A normal-size bet leaves plenty for the premium, so insurance is offerable. */
    @Test void canInsureTrueWhenAffordable() {
        for (long seed = 0; seed < 60000; seed++) {
            Engine e = engine(seed);
            e.addBet(100);                       // premium 50, 900 left
            e.deal();
            if (e.phase() != Phase.INSURANCE) continue;
            assertTrue(e.canInsure());
            return;
        }
        fail("no dealer-ace deal found");
    }

    /** Doubling adds a second equal stake to the ledger and draws exactly one card. */
    @Test void doubleDownDoublesTheWagerAndDrawsOneCard() {
        for (long seed = 0; seed < 20000; seed++) {
            Engine e = engine(seed);
            e.addBet(BET);
            e.deal();
            if (e.phase() != Phase.PLAYER) continue;
            if (e.hands().size() != 1 || !e.canDouble()) continue;
            assertEquals(BET, e.stats().totalWagered, "only the opening bet is wagered pre-double");
            e.doubleDown();
            Hand h = e.hands().get(0);
            assertEquals(3, h.size(), "double draws exactly one card");
            assertEquals(2 * BET, e.stats().totalWagered, "double adds a second equal stake");
            assertEquals(1, e.stats().doubles);
            SessionStats s = e.stats();
            assertEquals(START - s.totalWagered + s.totalReturned, e.bankroll(),
                    "double-down ledger stays balanced");
            return;
        }
        fail("no doubleable deal found in seed range");
    }
}
