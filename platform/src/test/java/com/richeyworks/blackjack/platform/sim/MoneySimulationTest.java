package com.richeyworks.blackjack.platform.sim;

import com.richeyworks.blackjack.platform.common.Asset;
import com.richeyworks.blackjack.platform.compliance.*;
import com.richeyworks.blackjack.platform.game.DefaultGameRoundService;
import com.richeyworks.blackjack.platform.game.GameRoundService;
import com.richeyworks.blackjack.platform.game.RoundRng;
import com.richeyworks.blackjack.platform.rng.Rng;
import com.richeyworks.blackjack.platform.wallet.InMemoryWallet;
import com.richeyworks.blackjack.platform.wallet.LedgerEntry;
import com.richeyworks.blackjack.platform.wallet.Wallet;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The money-plane soak, plus one named regression test per finding it exposed.
 *
 * <p>The soak is the part that generalises; the named tests are there so that a
 * future change which reintroduces one of these bugs fails with a message that
 * says what broke, rather than with "seed 31 escrow is negative".
 */
class MoneySimulationTest {

    @Test void aSoakOverRetriesAndAbandonedRoundsFindsNothing() {
        for (long seed = 0; seed < 25; seed++) {
            final long s = seed;
            assertDoesNotThrow(() -> {
                MoneySimulator sim = new MoneySimulator(s);
                sim.run(120);
                sim.checkEscrowFullyReleased();
            }, "money simulation failed at seed " + seed);
        }
    }

    @Test void theSoakActuallyExercisesRetriesAndSettlement() {
        // A simulation that silently stopped covering the interesting path
        // would pass forever. Assert it reaches it.
        MoneySimulator.Stats st = new MoneySimulator(7).run(200);
        assertTrue(st.retries > 10, "expected retries, got " + st.retries);
        assertTrue(st.roundsSettled > 100, "expected settlements, got " + st.roundsSettled);
        assertEquals(0, st.leftOpen, "the sweep should leave nothing open");
    }

    /* --------------------------- PL-1 ---------------------------------- */

    @Test void aRetriedStartReturnsTheSameRoundRatherThanDealingASecondOne() {
        Fixture f = new Fixture();
        GameRoundService.RoundState first  = f.service.startRound("p", Asset.USD, 10_000, "k1");
        GameRoundService.RoundState second = f.service.startRound("p", Asset.USD, 10_000, "k1");

        assertEquals(first.roundId(), second.roundId(),
                "a timed-out client retrying with its key must get its round back, not a new deal");
        // One stake held, not two.
        assertEquals(10_000, f.balance(Wallet.escrow("p")));
        assertEquals(1, f.service.openRounds());
    }

    @Test void twoDifferentKeysStillStartTwoRounds() {
        // The fix must not collapse genuinely distinct requests.
        Fixture f = new Fixture();
        String a = f.service.startRound("p", Asset.USD, 10_000, "k1").roundId();
        String b = f.service.startRound("p", Asset.USD, 10_000, "k2").roundId();
        assertNotEquals(a, b);
        assertEquals(20_000, f.balance(Wallet.escrow("p")));
    }

    /* --------------------------- PL-2 ---------------------------------- */

    @Test void whatThePlayerMayDoDoesNotDependOnHowBigTheirStakeIs() {
        // The notional bankroll the engine is handed used to be a flat
        // MAX_VALUE/4 = 536,870,911 regardless of stake -- barely five stakes at
        // the 100,000,000 cap. So a player betting near the cap was refused a
        // split or a double by a fictitious bankroll, while the same cards at a
        // small stake played fine. Legality must be scale-invariant.
        //
        // Same seed drives both, so both see identical cards.
        List<String> small = playGreedily(1_000L);
        List<String> large = playGreedily(100_000_000L);
        assertEquals(small, large,
                "identical cards allowed different actions at a larger stake, which"
                        + " means the notional bankroll -- not the wallet -- refused one");
    }

    /**
     * Plays rounds taking every stake-consuming action on offer, recording the
     * sequence of actions actually permitted. Scaled stakes should produce
     * identical sequences.
     */
    private static List<String> playGreedily(long stake) {
        Fixture f = new Fixture();
        f.fund("whale", 100_000_000_000L);
        List<String> trace = new ArrayList<>();
        for (int round = 0; round < 60; round++) {
            GameRoundService.RoundState st =
                    f.service.startRound("whale", Asset.USD, stake, "r" + round);
            for (int step = 0; step < 30 && !st.settled(); step++) {
                String took = null;
                for (GameRoundService.PlayerAction a : new GameRoundService.PlayerAction[]{
                        GameRoundService.PlayerAction.SPLIT,
                        GameRoundService.PlayerAction.DOUBLE,
                        GameRoundService.PlayerAction.HIT,
                        GameRoundService.PlayerAction.STAND}) {
                    try {
                        st = f.service.applyAction(st.roundId(), a);
                        took = a.name();
                        break;
                    } catch (RuntimeException refused) {
                        // try the next action down
                    }
                }
                if (took == null) break;
                trace.add(round + ":" + took);
            }
        }
        return trace;
    }

    /* --------------------------- PL-3 ---------------------------------- */

    @Test void anAbandonedRoundReleasesItsEscrowWhenSwept() {
        Fixture f = new Fixture();
        f.service.startRound("p", Asset.USD, 10_000, "k1");
        assertEquals(10_000, f.balance(Wallet.escrow("p")), "stake is held while the round is live");

        assertEquals(1, f.service.expireRounds(0));
        assertEquals(0, f.balance(Wallet.escrow("p")),
                "a player who disconnects must not have their stake stranded in escrow");
        assertEquals(0, f.service.openRounds(), "and the round must not be retained forever");
    }

    @Test void aLiveRoundIsNotSweptOutFromUnderThePlayer() {
        Fixture f = new Fixture();
        f.service.startRound("p", Asset.USD, 10_000, "k1");
        assertEquals(0, f.service.expireRounds(60_000),
                "a round started a moment ago is not abandoned");
        assertEquals(1, f.service.openRounds());
    }

    /* --------------------------- PL-7 ---------------------------------- */

    /**
     * Honest about what this proves: {@code BlackjackRules.insurancePremium} is
     * {@code bet / 2} today, which is exactly what the service used to compute
     * inline, so there is no behavioural difference to catch and this test
     * passes against the unfixed code too. PL-7 was <em>latent</em>, not live —
     * a second, independent copy of a money calculation, which is precisely how
     * the desktop's {@code canInsure()} and {@code takeInsurance()} drifted
     * apart. This pins the coupling so that a future change to the rules cannot
     * silently leave the service behind.
     */
    @Test void theEscrowedPremiumIsWhateverTheRulesSayItIs() {
        Fixture f = new Fixture();
        for (int i = 0; i < 400; i++) {
            GameRoundService.RoundState st =
                    f.service.startRound("p", Asset.USD, 101, "ins-" + i);
            if (!"INSURANCE".equals(st.phase())) continue;
            long before = f.balance(Wallet.escrow("p"));
            f.service.applyAction(st.roundId(), GameRoundService.PlayerAction.INSURANCE_TAKE);
            long held = f.balance(Wallet.escrow("p")) - before;
            assertEquals(f.rules().insurancePremium(101), held,
                    "the premium escrowed must be the one the rules define");
            return;
        }
        fail("no insurance offer in 400 rounds");
    }

    /* --------------------------- PL-8 ---------------------------------- */

    @Test void everyDrawGoesThroughTheCommittedSeedNotJavaUtilRandom() {
        // Only nextInt(bound) was routed. Everything else fell through to
        // java.util.Random's own state, seeded from System.nanoTime() -- so it
        // would have been unreproducible from the reveal, while still being
        // presented to the player as provably fair.
        List<Long> nonces = new ArrayList<>();
        Rng counting = new Rng() {
            public String commitServerSeed(String roundId) { return "c"; }
            public int nextInt(String roundId, String clientSeed, long nonce, int bound) {
                nonces.add(nonce);
                return 0;
            }
            public ServerSeedReveal reveal(String roundId) {
                return new ServerSeedReveal(roundId, "s", "c");
            }
        };

        RoundRng r = new RoundRng(counting, "r1", "cs");
        r.nextInt(52);
        r.nextLong();
        r.nextDouble();
        r.nextBoolean();
        r.nextFloat();
        r.nextInt();
        assertTrue(r.draws() >= 6,
                "each of these must consume the committed source; saw only " + r.draws());
        assertEquals(nonces.size(), r.draws(), "every draw must be nonced");
        assertEquals(new ArrayList<>(new LinkedHashSet<>(nonces)), nonces,
                "nonces must not repeat, or the reveal cannot be replayed");
    }

    @Test void twoRoundRngsOnTheSameSeedProduceTheSameShuffle() {
        assertEquals(drawSequence(), drawSequence(),
                "identical commitments must replay identically, which is the whole claim");
    }

    private static List<Number> drawSequence() {
        Rng fair = new Rng() {
            public String commitServerSeed(String roundId) { return "c"; }
            public int nextInt(String roundId, String clientSeed, long nonce, int bound) {
                return (int) Math.floorMod(nonce * 6364136223846793005L + 1442695040888963407L, bound);
            }
            public ServerSeedReveal reveal(String roundId) {
                return new ServerSeedReveal(roundId, "s", "c");
            }
        };
        RoundRng r = new RoundRng(fair, "r1", "cs");
        List<Number> out = new ArrayList<>();
        for (int i = 0; i < 20; i++) { out.add(r.nextInt(52)); out.add(r.nextLong()); out.add(r.nextDouble()); }
        return out;
    }

    @Test void aRoundsShuffleCannotBeReseeded() {
        RoundRng r = new RoundRng(new Rng() {
            public String commitServerSeed(String id) { return "c"; }
            public int nextInt(String id, String cs, long n, int b) { return 0; }
            public ServerSeedReveal reveal(String id) { return new ServerSeedReveal(id, "s", "c"); }
        }, "r1", "cs");
        assertThrows(UnsupportedOperationException.class, () -> r.setSeed(42L),
                "reseeding would hand control of the shuffle to the caller");
    }

    /* ------------------------- PL-9 / PL-10 ---------------------------- */

    @Test void aPostingWithoutAnIdempotencyKeyIsRefused() {
        InMemoryWallet w = new InMemoryWallet();
        List<LedgerEntry> legs = List.of(
                new LedgerEntry("e1", "tx1", Wallet.available("p"), Asset.USD,  100, null, 0),
                new LedgerEntry("e2", "tx1", "house:custodian",     Asset.USD, -100, null, 0));
        assertThrows(IllegalArgumentException.class, () -> w.post(legs),
                "an unkeyed posting has no replay protection at all");
    }

    @Test void reusingAKeyForADifferentPostingIsRefusedRatherThanIgnored() {
        // The silent no-op is the dangerous part: the caller is told the second
        // transaction succeeded when it was never applied.
        InMemoryWallet w = new InMemoryWallet();
        w.post(List.of(
                new LedgerEntry("e1", "tx1", Wallet.available("p"), Asset.USD,  100, "k", 0),
                new LedgerEntry("e2", "tx1", "house:custodian",     Asset.USD, -100, "k", 0)));

        List<LedgerEntry> different = List.of(
                new LedgerEntry("e3", "tx2", Wallet.available("p"), Asset.USD,  999, "k", 0),
                new LedgerEntry("e4", "tx2", "house:custodian",     Asset.USD, -999, "k", 0));
        assertThrows(IllegalArgumentException.class, () -> w.post(different));
        assertEquals(100, w.availableMinor("p", Asset.USD), "and nothing was applied");
    }

    @Test void aGenuineReplayIsStillANoOp() {
        InMemoryWallet w = new InMemoryWallet();
        List<LedgerEntry> legs = List.of(
                new LedgerEntry("e1", "tx1", Wallet.available("p"), Asset.USD,  100, "k", 0),
                new LedgerEntry("e2", "tx1", "house:custodian",     Asset.USD, -100, "k", 0));
        w.post(legs);
        assertDoesNotThrow(() -> w.post(legs));
        assertEquals(100, w.availableMinor("p", Asset.USD), "posted once, not twice");
    }

    /* ------------------------------------------------------------------ */

    /** A wired-up service over an in-memory wallet, with one funded player. */
    private static final class Fixture {
        final InMemoryWallet wallet = new InMemoryWallet();
        final DefaultGameRoundService service;

        Fixture() {
            Random r = new Random(99);
            service = new DefaultGameRoundService(
                    new DefaultComplianceGate(LicensingPolicy.usDefault(), (a, d) -> {}),
                    id -> new PlayerComplianceState(id, PlayerComplianceState.KycStatus.VERIFIED,
                            "NJ", true, false, new PlayerComplianceState.RgLimits(0, 0, 0)),
                    wallet,
                    new Rng() {
                        public String commitServerSeed(String roundId) { return "c-" + roundId; }
                        public int nextInt(String roundId, String cs, long n, int bound) {
                            return r.nextInt(bound);
                        }
                        public ServerSeedReveal reveal(String roundId) {
                            return new ServerSeedReveal(roundId, "s", "c-" + roundId);
                        }
                    });
            fund("p", 5_000_000);
        }

        com.richeyworks.blackjack.engine.BlackjackRules rules() {
            return new com.richeyworks.blackjack.engine.BlackjackRules();
        }

        void fund(String player, long cents) {
            String k = "fund-" + player + "-" + UUID.randomUUID();
            wallet.post(List.of(
                    new LedgerEntry("a", k, Wallet.available(player), Asset.USD,  cents, k, 0),
                    new LedgerEntry("b", k, "house:custodian",        Asset.USD, -cents, k, 0)));
        }

        long balance(String account) {
            long sum = 0;
            for (LedgerEntry e : wallet.entries())
                if (e.account().equals(account)) sum += e.amountMinor();
            return sum;
        }
    }
}
