package com.richeyworks.blackjack.platform.sim;

import com.richeyworks.blackjack.platform.common.Asset;
import com.richeyworks.blackjack.platform.compliance.*;
import com.richeyworks.blackjack.platform.game.DefaultGameRoundService;
import com.richeyworks.blackjack.platform.game.GameRoundService;
import com.richeyworks.blackjack.platform.rng.Rng;
import com.richeyworks.blackjack.platform.wallet.InMemoryWallet;
import com.richeyworks.blackjack.platform.wallet.LedgerEntry;
import com.richeyworks.blackjack.platform.wallet.Wallet;

import java.util.*;

/**
 * Soaks the money plane: wallet, compliance gate and round service, under the
 * conditions a real client actually produces — including retries.
 *
 * <h2>Why the obvious invariant is not enough</h2>
 * The ledger is zero-sum by construction: {@code post()} rejects any set of legs
 * that doesn't net to zero per asset. So "does the ledger balance" is true even
 * when money has gone badly wrong, and monitoring built on it would report green
 * forever. The review found exactly that — a retried {@code startRound} played
 * two rounds on one stake, the player finished 35,000 up having staked 10,000
 * once, and the ledger summed to zero throughout.
 *
 * <p>What that bug actually leaves behind is an <b>escrow account holding a debt
 * nobody funded</b>. So the load-bearing check here is per-account rather than
 * global: escrow is a transient, and once every round a player started has
 * settled, their escrow balance must be exactly zero. That is the property the
 * zero-sum invariant cannot express.
 */
public final class MoneySimulator {

    public static final class Stats {
        public int roundsStarted, roundsSettled, retries, denials, insufficientFunds;
        public int expired, leftOpen;
        public long biggestPayout;
        public final Set<String> phases = new TreeSet<>();
    }

    public static final class MoneyFailure extends AssertionError {
        public MoneyFailure(long seed, String what) { super("seed " + seed + ": " + what); }
    }

    private static final Asset ASSET = Asset.USD;

    private final long seed;
    private final Random rng;
    private final InMemoryWallet wallet = new InMemoryWallet();
    private final DefaultGameRoundService service;
    private final Stats stats = new Stats();
    private final List<String> players = new ArrayList<>();
    private long funded;

    public MoneySimulator(long seed) {
        this.seed = seed;
        this.rng  = new Random(seed);
        for (int i = 0; i < 4; i++) players.add("p" + i);
        this.service = new DefaultGameRoundService(
                new DefaultComplianceGate(LicensingPolicy.usDefault(), (a, d) -> {}),
                this::lookup, wallet, rng(seed));
        for (String p : players) fund(p, 5_000_000);
    }

    private PlayerComplianceState lookup(String id) {
        return new PlayerComplianceState(id, PlayerComplianceState.KycStatus.VERIFIED,
                "NJ", true, false, new PlayerComplianceState.RgLimits(0, 0, 0));
    }

    private static Rng rng(long seed) {
        Random r = new Random(seed ^ 0xD1CE);
        return new Rng() {
            public String commitServerSeed(String roundId) { return "commit-" + roundId; }
            public int nextInt(String roundId, String clientSeed, long nonce, int bound) {
                return r.nextInt(bound);
            }
            public ServerSeedReveal reveal(String roundId) {
                return new ServerSeedReveal(roundId, "seed", "commit-" + roundId);
            }
        };
    }

    private void fund(String player, long cents) {
        String key = "fund-" + player + "-" + UUID.randomUUID();
        wallet.post(List.of(
                new LedgerEntry("e1", key, Wallet.available(player), ASSET,  cents, key, 0),
                new LedgerEntry("e2", key, "house:custodian",        ASSET, -cents, key, 0)));
        funded += cents;
    }

    public Stats run(int rounds) {
        for (int i = 0; i < rounds; i++) {
            playRound();
            checkInvariants("after round " + i);
        }
        // Players disconnect. Every round left mid-hand is still holding its
        // stake in escrow, so the sweep has to be able to recover all of them --
        // otherwise the money is stranded and the map grows forever.
        stats.expired = service.expireRounds(0);
        stats.leftOpen = service.openRounds();
        checkInvariants("after expiring abandoned rounds");
        return stats;
    }

    private void playRound() {
        String player = players.get(rng.nextInt(players.size()));
        long stake = 100L * (1 + rng.nextInt(50));
        String key = "round-" + seed + "-" + stats.roundsStarted + "-" + rng.nextInt(1_000_000);

        GameRoundService.RoundState state;
        try {
            state = service.startRound(player, ASSET, stake, key);
            stats.roundsStarted++;
        } catch (IllegalStateException refused) {
            if (refused.getMessage() != null && refused.getMessage().contains("insufficient")) {
                stats.insufficientFunds++;
                fund(player, 5_000_000);
            } else {
                stats.denials++;
            }
            return;
        }

        // A client that times out retries with the same key. This is ordinary
        // behaviour, not an attack -- the idempotency key exists for it.
        if (rng.nextInt(4) == 0) {
            stats.retries++;
            GameRoundService.RoundState again = service.startRound(player, ASSET, stake, key);
            if (!again.roundId().equals(state.roundId())) {
                throw new MoneyFailure(seed, "a retry with the same idempotency key started a"
                        + " second round (" + state.roundId() + " then " + again.roundId()
                        + ") -- both will settle against one stake");
            }
            state = again;
        }

        state = playOut(state);
        if (state.settled()) {
            stats.roundsSettled++;
            stats.biggestPayout = Math.max(stats.biggestPayout, state.payoutMinor());
        }
        stats.phases.add(state.phase());
    }

    private GameRoundService.RoundState playOut(GameRoundService.RoundState state) {
        GameRoundService.PlayerAction[] actions = GameRoundService.PlayerAction.values();
        for (int i = 0; i < 40 && !state.settled(); i++) {
            GameRoundService.PlayerAction a = actions[rng.nextInt(actions.length)];
            try {
                state = service.applyAction(state.roundId(), a);
                stats.phases.add(state.phase());
            } catch (RuntimeException refused) {
                // An illegal action for the current phase; try another. The
                // service is expected to refuse rather than corrupt anything.
                try {
                    state = service.applyAction(state.roundId(), GameRoundService.PlayerAction.STAND);
                } catch (RuntimeException alsoRefused) {
                    break;   // round is over or stuck; the invariants will say which
                }
            }
        }
        return state;
    }

    /* ------------------------------------------------------------------ */

    private void checkInvariants(String where) {
        // 1. The global identity. True by construction, checked anyway: if this
        //    ever breaks, post() has stopped enforcing zero-sum.
        long total = 0;
        for (LedgerEntry e : wallet.entries()) total += e.amountMinor();
        if (total != 0)
            throw new MoneyFailure(seed, "ledger does not sum to zero (" + total + ") " + where);

        // 2. The one that matters. Escrow is transient: money sits there only
        //    while a round is live. With no round in flight it must be exactly
        //    zero for every player, and a negative balance means more was
        //    settled out of it than was ever held into it.
        for (String p : players) {
            long escrow = balance(Wallet.escrow(p));
            if (escrow < 0)
                throw new MoneyFailure(seed, "escrow for " + p + " is negative (" + escrow
                        + ") " + where + " -- settled more than was ever held");
        }

        // 3. Nobody's spendable balance may go negative: hold() is the only
        //    path that debits it and it checks funds first.
        for (String p : players) {
            long avail = balance(Wallet.available(p));
            if (avail < 0)
                throw new MoneyFailure(seed, "available for " + p + " is negative (" + avail
                        + ") " + where);
        }

        // 4. The house cannot have created money from nothing: everything the
        //    players hold plus the house position must equal what was funded in.
        long players_ = 0;
        for (String p : players) players_ += balance(Wallet.available(p)) + balance(Wallet.escrow(p));
        long house = balance(Wallet.HOUSE_PNL) + balance("house:custodian");
        if (players_ + house != 0)
            throw new MoneyFailure(seed, "players " + players_ + " + house " + house
                    + " != 0 " + where);
    }

    private long balance(String account) {
        long sum = 0;
        for (LedgerEntry e : wallet.entries())
            if (e.account().equals(account) && e.asset() == ASSET) sum += e.amountMinor();
        return sum;
    }

    /**
     * The property the zero-sum ledger cannot express: with nothing in flight,
     * every player's escrow must be exactly zero. Money that stops here is not
     * missing from the ledger, it is simply no longer reachable by the player
     * who owns it.
     */
    public void checkEscrowFullyReleased() {
        for (String p : players) {
            long escrow = balance(Wallet.escrow(p));
            if (escrow != 0)
                throw new MoneyFailure(seed, "escrow for " + p + " left at " + escrow
                        + " with no round in flight -- a stake was never released");
        }
    }

    public static void main(String[] args) {
        int seeds  = args.length > 0 ? Integer.parseInt(args[0]) : 50;
        int rounds = args.length > 1 ? Integer.parseInt(args[1]) : 200;

        Stats total = new Stats();
        int failures = 0;
        String firstFailure = null;
        for (long s = 0; s < seeds; s++) {
            try {
                MoneySimulator sim = new MoneySimulator(s);
                Stats st = sim.run(rounds);
                sim.checkEscrowFullyReleased();
                merge(total, st);
            } catch (MoneyFailure f) {
                failures++;
                if (firstFailure == null) firstFailure = f.getMessage();
            }
        }
        System.out.printf("%,d rounds started, %,d settled, %,d retries across %d seeds%n",
                total.roundsStarted, total.roundsSettled, total.retries, seeds);
        System.out.printf("  denials %d, insufficient-funds %d, biggest payout %,d%n",
                total.denials, total.insufficientFunds, total.biggestPayout);
        System.out.printf("  %,d abandoned rounds swept, %d left open%n",
                total.expired, total.leftOpen);
        System.out.println("  phases seen " + total.phases);
        System.out.println();
        if (failures == 0) {
            System.out.println("  clean");
        } else {
            System.out.printf("  %d of %d seeds FAILED%n", failures, seeds);
            System.out.println("  first: " + firstFailure);
            System.exit(1);
        }
    }

    static void merge(Stats a, Stats b) {
        a.roundsStarted += b.roundsStarted; a.roundsSettled += b.roundsSettled;
        a.retries += b.retries; a.denials += b.denials;
        a.insufficientFunds += b.insufficientFunds;
        a.expired += b.expired; a.leftOpen += b.leftOpen;
        a.biggestPayout = Math.max(a.biggestPayout, b.biggestPayout);
        a.phases.addAll(b.phases);
    }
}
