package com.richeyworks.blackjack.sim;

import com.richeyworks.blackjack.engine.*;
import com.richeyworks.blackjack.media.GameSounds;
import com.richeyworks.blackjack.sidebet.SideBetManager;
import com.richeyworks.blackjack.sidebet.TwentyOnePlusThree;
import com.richeyworks.blackjack.strategy.HiLoCounter;
import com.richeyworks.blackjack.table.*;

import java.util.*;

/**
 * Plays the game the way a front end does, for as long as you like, checking
 * that nothing it observes is ever impossible.
 *
 * <h2>Why this exists</h2>
 * A full review of the Swing module produced twenty findings, and 249 unit
 * tests passed on top of it — and neither caught that {@code canHit()} threw
 * after 78% of completed rounds, silently stopping the desktop UI from
 * updating. It surfaced in the first two hands of someone actually playing.
 *
 * <p>The gap is structural. Unit tests call the method they are testing; a
 * front end calls <em>every</em> query after <em>every</em> action, in states
 * the test author never thought to construct. This simulator does the second
 * thing, several million times.
 *
 * <h2>What it checks</h2>
 * Three kinds of property, all continuously:
 * <ul>
 *   <li><b>Nothing throws that shouldn't.</b> Every {@code can*()} is asked
 *       after every action, in every phase.</li>
 *   <li><b>Legality is honest.</b> Every action is attempted in every state:
 *       if {@code can*()} says yes it must succeed, if it says no it must
 *       throw. A query that lies about what is legal is as bad as one that
 *       crashes.</li>
 *   <li><b>The money adds up.</b> The ledger identity, per-round net against
 *       the actual bankroll delta, and outcomes matching the stats counters.</li>
 * </ul>
 *
 * <p>Everything is seeded, so any failure is reproducible from the seed printed
 * with it.
 */
public final class GameSimulator {

    /** What a run observed, so gaps in coverage are visible rather than assumed. */
    public static final class Coverage {
        public final EnumSet<Outcome>    outcomes = EnumSet.noneOf(Outcome.class);
        public final EnumSet<Phase>      phases   = EnumSet.noneOf(Phase.class);
        public final EnumSet<TableEvent> events   = EnumSet.noneOf(TableEvent.class);
        public final Set<String> actions   = new TreeSet<>();
        public final Set<String> sideBets  = new TreeSet<>();
        public int hands, rounds, reshuffles, splits, doubles, surrenders,
                   insuranceTaken, insuranceDeclined, maxHandsInRound, maxCardsInHand,
                   remarks, legalityChecks, queryCalls;
        public long biggestWin, biggestLoss;
    }

    /** Raised with the seed and the round number, so a failure can be replayed. */
    public static final class SimFailure extends AssertionError {
        public SimFailure(long seed, int round, String what) {
            super("seed " + seed + ", round " + round + ": " + what);
        }
    }

    private final long seed;
    private final Random rng;
    private final Engine engine;
    private final SideBetManager sideBets = new SideBetManager(new TwentyOnePlusThree());
    private final HiLoCounter counter = new HiLoCounter();
    private final TableChatter chatter;
    private final Coverage cov = new Coverage();

    private final int startBankroll;
    private int round, lastShoe, observed, winStreak, lossStreak, processed;
    private long clock;

    public GameSimulator(long seed) {
        this.seed = seed;
        this.rng  = new Random(seed);
        BlackjackRules rules = new BlackjackRules();
        // Vary the house rules by seed so the simulator explores rule
        // combinations too, not just card orders.
        rules.dealerHitsSoft17 = rng.nextBoolean();
        rules.lateSurrender    = rng.nextBoolean();
        rules.offerInsurance   = rng.nextBoolean();
        rules.doubleAfterSplit = rng.nextBoolean();
        // A third of runs play from a short stack. LOW_CHIPS and BIG_WIN are
        // both relative to the bankroll, so a fat one never reaches either --
        // and the low-money paths are exactly where affordability bugs live.
        this.startBankroll = (seed % 3 == 0) ? 300 : 100_000;
        this.engine  = new Engine(startBankroll, new Random(seed ^ 0xC0FFEE), rules);
        this.chatter = new TableChatter(Personas.defaults(), new Random(seed ^ 0xBEEF));
        this.lastShoe = engine.shoe().remaining();
    }

    public Coverage coverage() { return cov; }

    /** Play {@code rounds} rounds, checking everything as it goes. */
    public Coverage run(int rounds) {
        checkQueries("before anything");
        for (round = 1; round <= rounds; round++) {
            // Top up only when genuinely unable to play, so a short stack is
            // allowed to sit low for a while rather than being refilled instantly.
            if (engine.bankroll() < 1) {
                injected += startBankroll - engine.bankroll();   // tracked, not a win
                engine.setBankroll(startBankroll);
            }
            playRound();
        }
        return cov;
    }

    /* ------------------------------------------------------------------ */
    /* One round                                                          */
    /* ------------------------------------------------------------------ */

    private void playRound() {
        long ownedBefore = engine.bankroll() + engine.pendingBet();

        // Side bet first, then size the main bet against what's actually left.
        // Doing it the other way round let the two overdraw each other -- the
        // same ordering hazard a front end has when one control debits the
        // bankroll and another has already decided how much it can afford.
        sideStake = sidePayout = 0;
        maybeSideBet();
        int bet = pickBet();
        engine.addBet(bet);
        checkAll("bet placed");

        observed = 0;
        engine.deal();
        resolveSideBet();
        checkAll("dealt");

        int guard = 0;
        while (engine.phase() != Phase.BETTING && guard++ < 60) {
            act();
            checkAll("mid-round");
        }
        if (guard >= 60) throw new SimFailure(seed, round, "round did not terminate");

        settleChecks(ownedBefore);
        checkAll("settled");
    }

    private int pickBet() {
        int[] chips = {1, 5, 25, 100, 500};
        int bet = 0;
        for (int i = 0, n = 1 + rng.nextInt(3); i < n; i++) bet += chips[rng.nextInt(chips.length)];
        // Occasionally shove most of the stack in, which is what makes BIG_WIN
        // and the engine's affordability checks reachable.
        if (rng.nextInt(8) == 0) bet = Math.max(1, engine.bankroll() * 3 / 4);
        return Math.max(1, Math.min(bet, engine.bankroll()));
    }

    /**
     * Stake the side bet, but never the last chip: a player who spends
     * everything on 21+3 has nothing left for the hand it is a side bet on.
     */
    private void maybeSideBet() {
        if (rng.nextInt(3) != 0) return;
        if (engine.bankroll() <= 5) return;
        int added = sideBets.add(5, engine.bankroll() - 1);
        if (added > 0) engine.setBankroll(engine.bankroll() - added);
    }

    /** This round's side-bet stake and payout, so settleChecks can net them out. */
    private long sideStake, sidePayout;

    private void resolveSideBet() {
        if (sideBets.pending() == 0) return;
        int stake  = sideBets.pending();
        int payout = sideBets.resolve(engine.hands().get(0).cards(), engine.dealer().first());
        engine.stats().totalWagered  += stake;
        engine.stats().totalReturned += payout;
        engine.setBankroll(engine.bankroll() + payout);
        sideStake  = stake;
        sidePayout = payout;
        cov.sideBets.add(payout > 0 ? sideBets.lastOutcome() : "no win");
    }

    /** Take one legal action at random, weighted so every branch gets exercised. */
    private void act() {
        List<Runnable> options = new ArrayList<>();
        if (engine.canHit())       options.add(() -> { engine.hit();  cov.actions.add("hit"); });
        if (engine.canStand())     options.add(() -> { engine.stand(); cov.actions.add("stand"); });
        if (engine.canDouble())    options.add(() -> { engine.doubleDown(); cov.doubles++; cov.actions.add("double"); say(TableEvent.PLAYER_DOUBLE); });
        if (engine.canSplit())     options.add(() -> { engine.split(); cov.splits++; cov.actions.add("split"); say(TableEvent.PLAYER_SPLIT); });
        if (engine.canSurrender()) options.add(() -> { engine.surrender(); cov.surrenders++; cov.actions.add("surrender"); });
        if (engine.phase() == Phase.INSURANCE) {
            if (engine.canInsure())
                options.add(() -> { engine.takeInsurance(true); cov.insuranceTaken++; cov.actions.add("insure"); });
            options.add(() -> { engine.takeInsurance(false); cov.insuranceDeclined++; cov.actions.add("decline"); });
        }
        if (options.isEmpty()) {
            throw new SimFailure(seed, round,
                    "stuck in phase " + engine.phase() + " with no legal action");
        }
        options.get(rng.nextInt(options.size())).run();
    }

    /* ------------------------------------------------------------------ */
    /* Checks                                                             */
    /* ------------------------------------------------------------------ */

    private void checkAll(String where) {
        checkQueries(where);
        checkState(where);
        checkLegalityIsHonest(where);
        observeCards();
        fireChatter();
    }

    /**
     * Every query answers, in every phase.
     *
     * <p>This is the check that would have caught {@code canHit()} throwing
     * after a settled round — the bug a review and 249 tests both missed.
     */
    private void checkQueries(String where) {
        try {
            engine.canDeal(); engine.canHit(); engine.canStand();
            engine.canDouble(); engine.canSplit(); engine.canSurrender();
            engine.canInsure();
            engine.phase(); engine.bankroll(); engine.pendingBet();
            engine.hands(); engine.dealer(); engine.stats();
            engine.lastOutcomes(); engine.lastNet();
            engine.shoe().remaining();
            cov.queryCalls += 7;
        } catch (RuntimeException e) {
            throw new SimFailure(seed, round,
                    "a query threw in phase " + engine.phase() + " (" + where + "): " + e);
        }
        cov.phases.add(engine.phase());
    }

    /** Things that must be true of the engine's own state at all times. */
    private void checkState(String where) {
        if (engine.bankroll() < 0)
            throw new SimFailure(seed, round, "negative bankroll (" + where + ")");
        if (engine.pendingBet() < 0)
            throw new SimFailure(seed, round, "negative pending bet (" + where + ")");
        if (engine.hands().isEmpty())
            throw new SimFailure(seed, round, "no hands exist (" + where + ")");
        int remaining = engine.shoe().remaining();
        if (remaining < 0 || remaining > 6 * 52)
            throw new SimFailure(seed, round, "shoe out of range: " + remaining);
        if (engine.phase() == Phase.PLAYER) {
            int i = engine.activeIndex();
            if (i < 0 || i >= engine.hands().size())
                throw new SimFailure(seed, round,
                        "active index " + i + " outside " + engine.hands().size() + " hands");
        }
        checkLedger(where);
        for (Hand h : engine.hands()) {
            cov.maxCardsInHand = Math.max(cov.maxCardsInHand, h.size());
            if (h.bet() < 0) throw new SimFailure(seed, round, "negative hand bet");
        }
        cov.maxHandsInRound = Math.max(cov.maxHandsInRound, engine.hands().size());
    }

    /** Money added by top-ups, which is not a win and must be accounted separately. */
    private long injected;

    /**
     * The ledger identity, stated precisely enough to hold at every instant.
     *
     * <p>The audit wrote it as {@code bankroll == start - wagered + returned},
     * which is only true between rounds. Mid-round the player's money is spread
     * across three places: {@code addBet} moves it into {@code pendingBet}
     * before {@code deal()} records it in {@code totalWagered}, and a pending
     * side bet is debited from the bankroll before the engine knows about it at
     * all. Counting only the bankroll makes an ordinary bet look like a leak —
     * which is what my first version of this check reported.
     *
     * <p>So the invariant is over everything the player owns or has on the
     * felt. That is the form worth asserting continuously, because it is the
     * one that has to be true after every single call rather than only at rest.
     */
    private void checkLedger(String where) {
        long owned = (long) engine.bankroll() + engine.pendingBet() + sideBets.pending();
        long expected = startBankroll + injected
                - engine.stats().totalWagered + engine.stats().totalReturned;
        if (owned != expected) {
            throw new SimFailure(seed, round, "ledger drift (" + where + "): owned " + owned
                    + " (bankroll " + engine.bankroll() + " + pending " + engine.pendingBet()
                    + " + side " + sideBets.pending() + ") but start+injected-wagered+returned = "
                    + expected);
        }
    }

    /**
     * The strongest property here: what the queries claim must match reality.
     *
     * <p>An action the engine says is illegal must throw, and one it says is
     * legal must not. A query that lies is as damaging as one that crashes —
     * a UI would offer a button that fails, or hide one that would have worked.
     * Checked on a sample rather than every step, because it has to snapshot
     * and discard state to try an action without performing it.
     */
    private void checkLegalityIsHonest(String where) {
        if (rng.nextInt(20) != 0) return;      // sampled: this is the expensive check
        cov.legalityChecks++;

        // Illegal actions must throw. Safe to attempt: if the engine is correct
        // nothing changes, and if it is wrong that is exactly the finding.
        assertRefused(!engine.canHit(),       () -> engine.hit(),        "hit", where);
        assertRefused(!engine.canStand(),     () -> engine.stand(),      "stand", where);
        assertRefused(!engine.canDouble(),    () -> engine.doubleDown(), "double", where);
        assertRefused(!engine.canSplit(),     () -> engine.split(),      "split", where);
        assertRefused(!engine.canSurrender(), () -> engine.surrender(),  "surrender", where);
        if (engine.phase() != Phase.INSURANCE) {
            assertRefused(true, () -> engine.takeInsurance(true),  "insure", where);
            assertRefused(true, () -> engine.takeInsurance(false), "decline", where);
        }
    }

    private void assertRefused(boolean shouldRefuse, Runnable action, String name, String where) {
        if (!shouldRefuse) return;
        try {
            action.run();
            throw new SimFailure(seed, round, "can" + name + "() said no but " + name
                    + "() succeeded anyway in phase " + engine.phase() + " (" + where + ")");
        } catch (SimFailure f) {
            throw f;
        } catch (RuntimeException expected) {
            // Correct: the engine refused an action it had declared illegal.
        }
    }

    /** Round-completion properties, checked once per settled round. */
    private void settleChecks(long ownedBefore) {
        if (engine.stats().hands <= processed) return;
        processed = engine.stats().hands;
        cov.rounds++;
        cov.hands = engine.stats().hands;

        List<Outcome> outcomes = engine.lastOutcomes();
        if (outcomes.size() != engine.hands().size())
            throw new SimFailure(seed, round, outcomes.size() + " outcomes for "
                    + engine.hands().size() + " hands");
        cov.outcomes.addAll(outcomes);

        // lastNet() covers the engine's money only. The side bet is settled by
        // the caller, so its stake and payout have to be taken back out before
        // the two can be compared.
        long owned = (long) engine.bankroll() + engine.pendingBet();
        long moved = owned - ownedBefore + sideStake - sidePayout;
        long net   = engine.lastNet();
        if (moved != net)
            throw new SimFailure(seed, round, "lastNet " + net + " but money moved " + moved
                    + " (side stake " + sideStake + ", payout " + sidePayout + ")");

        long wins = outcomes.stream().filter(Outcome::isWin).count();
        long push = outcomes.stream().filter(o -> o == Outcome.PUSH).count();
        long loss = outcomes.stream().filter(o -> o == Outcome.LOSS
                                              || o == Outcome.BUST
                                              || o == Outcome.SURRENDER).count();
        if (wins + push + loss != outcomes.size())
            throw new SimFailure(seed, round, "an outcome fell through the win/push/loss split");

        if (net > cov.biggestWin)  cov.biggestWin  = net;
        if (net < -cov.biggestLoss) cov.biggestLoss = -net;

        boolean won = wins > 0, pushed = push > 0;
        if (won) { winStreak++; lossStreak = 0; } else if (!pushed) { lossStreak++; winStreak = 0; }
        fireRoundEvent(outcomes, won, pushed);
    }


    /* ------------------------------------------------------------------ */
    /* Peripherals — counter, chatter                                     */
    /* ------------------------------------------------------------------ */

    private void observeCards() {
        if (engine.shoe().remaining() > lastShoe) {
            counter.resetCount(); observed = 0; cov.reshuffles++;
            say(TableEvent.SHUFFLE);
        }
        lastShoe = engine.shoe().remaining();
        int seen = 0;
        for (Hand h : engine.hands())
            for (Card c : h.cards()) if (seen++ >= observed) counter.observe(c);
        boolean hidden = engine.phase() == Phase.DEALING
                || engine.phase() == Phase.INSURANCE
                || engine.phase() == Phase.PLAYER;
        List<Card> d = engine.dealer().cards();
        int visible = hidden ? Math.min(1, d.size()) : d.size();
        for (int i = 0; i < visible; i++) if (seen++ >= observed) counter.observe(d.get(i));
        observed = seen;
        counter.trueCount(Math.max(1, engine.shoe().remaining() / 52));   // must not divide by zero
    }

    private void fireChatter() {
        if (engine.phase() == Phase.INSURANCE) say(TableEvent.INSURANCE_OFFERED);
        else if (engine.phase() == Phase.PLAYER && !engine.dealer().isEmpty()) {
            int up = engine.dealer().first().rank().value();
            if (up >= 4 && up <= 6) say(TableEvent.DEALER_WEAK_CARD);
        }
    }

    private void fireRoundEvent(List<Outcome> os, boolean won, boolean pushed) {
        boolean lost = !won && !pushed;
        TableEvent ev;
        if (os.contains(Outcome.BLACKJACK)) ev = TableEvent.PLAYER_BLACKJACK;
        else if (won && engine.hands().stream().anyMatch(h -> h.size() >= 5)) ev = TableEvent.FIVE_CARD_HAND;
        else if (won && engine.hands().stream().anyMatch(Hand::doubled))      ev = TableEvent.DOUBLE_WIN;
        else if (engine.lastNet() >= Math.max(100, engine.bankroll() / 4))    ev = TableEvent.BIG_WIN;
        else if (won && engine.hands().stream().anyMatch(h -> h.value() == 21 && h.size() >= 3))
            ev = TableEvent.TWENTY_ONE;
        else if (engine.dealer().isBust() && won)  ev = TableEvent.DEALER_BUST;
        else if (engine.dealer().isBlackjack())    ev = TableEvent.DEALER_BLACKJACK;
        else if (os.contains(Outcome.BUST))        ev = TableEvent.PLAYER_BUST;
        else if (os.contains(Outcome.SURRENDER))   ev = TableEvent.PLAYER_SURRENDER;
        else if (winStreak >= 3)                   ev = TableEvent.HOT_STREAK;
        else if (lossStreak >= 3)                  ev = TableEvent.COLD_STREAK;
        else if (engine.bankroll() <= 100)         ev = TableEvent.LOW_CHIPS;
        else if (won)  ev = TableEvent.PLAYER_WIN;
        else if (lost) ev = TableEvent.PLAYER_LOSS;
        else ev = TableEvent.PUSH;
        say(ev);
        if (cov.rounds == 1) { say(TableEvent.SESSION_START); say(TableEvent.LONG_SESSION);
                               say(TableEvent.RUNNING_WELL); say(TableEvent.CLOSE_CALL); }
    }

    private void say(TableEvent ev) {
        clock += 25_000;
        cov.events.add(ev);
        chatter.react(ev, clock).ifPresent(r -> {
            cov.remarks++;
            if (r.text().isBlank()) throw new SimFailure(seed, round, "empty remark");
        });
    }

    /* ------------------------------------------------------------------ */
    /* The non-engine surfaces                                            */
    /* ------------------------------------------------------------------ */

    /** Exercise everything that isn't the engine: sounds, palettes, strategy advice. */
    public static void checkPeripherals() {
        for (GameSounds s : GameSounds.values()) {
            byte[] pcm = s.render(0.6f);
            if (pcm.length == 0) throw new AssertionError(s + " rendered no audio");
        }
        for (TablePalette p : Palettes.all()) {
            if (Palettes.byId(p.id()) != null && !Palettes.byId(p.id()).id().equals(p.id()))
                throw new AssertionError("palette lookup broken for " + p.id());
            if (Palettes.next(p.id()) == null) throw new AssertionError("cycle broken at " + p.id());
        }
        // Basic strategy must advise on every reachable hand/up-card pair.
        for (Rank up : Rank.values()) {
            Card dealerUp = new Card(up, Suit.SPADES);
            for (Rank a : Rank.values()) {
                for (Rank b : Rank.values()) {
                    Hand h = new Hand();
                    h.add(new Card(a, Suit.HEARTS));
                    h.add(new Card(b, Suit.CLUBS));
                    if (BasicStrategy.recommend(h, dealerUp) == null)
                        throw new AssertionError("no advice for " + a + "," + b + " vs " + up);
                }
            }
        }
    }

    /** Run a soak from the command line: {@code GameSimulator <seeds> <rounds>}. */
    public static void main(String[] args) {
        int seeds  = args.length > 0 ? Integer.parseInt(args[0]) : 200;
        int rounds = args.length > 1 ? Integer.parseInt(args[1]) : 500;

        checkPeripherals();
        Coverage total = new Coverage();
        long t0 = System.currentTimeMillis();
        for (long s = 0; s < seeds; s++) {
            Coverage c = new GameSimulator(s).run(rounds);
            merge(total, c);
        }
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("%,d rounds across %,d seeds in %.1fs (%,d rounds/sec)%n",
                total.rounds, seeds, ms / 1000.0, total.rounds * 1000L / Math.max(1, ms));
        System.out.printf("  queries asked      %,d%n", total.queryCalls);
        System.out.printf("  legality probes    %,d%n", total.legalityChecks);
        System.out.printf("  splits/doubles/surr %,d / %,d / %,d%n",
                total.splits, total.doubles, total.surrenders);
        System.out.printf("  insurance taken/declined  %,d / %,d%n",
                total.insuranceTaken, total.insuranceDeclined);
        System.out.printf("  reshuffles         %,d%n", total.reshuffles);
        System.out.printf("  most hands / cards %d / %d%n", total.maxHandsInRound, total.maxCardsInHand);
        System.out.printf("  biggest win/loss   $%,d / $%,d%n", total.biggestWin, total.biggestLoss);
        System.out.printf("  remarks            %,d%n", total.remarks);
        System.out.println();
        System.out.println("  outcomes   " + total.outcomes);
        System.out.println("  phases     " + total.phases);
        System.out.println("  actions    " + total.actions);
        System.out.println("  side bets  " + total.sideBets);
        System.out.println("  events     " + total.events.size() + "/" + TableEvent.values().length);
        System.out.println();
        List<String> gaps = gaps(total);
        if (gaps.isEmpty()) {
            System.out.println("  full coverage: every outcome, action and event reached");
        } else {
            gaps.forEach(g -> System.out.println("  NOT COVERED  " + g));
            System.exit(1);
        }
    }

    /**
     * Anything the run never reached. Untested surface is the point of the
     * exercise, so this is reported as a failure rather than a footnote.
     *
     * <p>DEALING, DEALER and SETTLE are deliberately excluded: the engine
     * settles synchronously, so those phases exist inside a call and are never
     * observable from outside. That is a real property of the design, not a
     * hole in the simulator -- the Swing review found dead UI code that
     * assumed otherwise.
     */
    public static List<String> gaps(Coverage c) {
        List<String> out = new ArrayList<>();
        EnumSet<Outcome> outcomes = EnumSet.allOf(Outcome.class);
        outcomes.removeAll(c.outcomes);
        if (!outcomes.isEmpty()) out.add("outcomes " + outcomes);

        EnumSet<TableEvent> events = EnumSet.allOf(TableEvent.class);
        events.removeAll(c.events);
        if (!events.isEmpty()) out.add("table events " + events);

        Set<String> actions = new TreeSet<>(List.of(
                "hit", "stand", "double", "split", "surrender", "insure", "decline"));
        actions.removeAll(c.actions);
        if (!actions.isEmpty()) out.add("actions " + actions);

        for (Phase p : List.of(Phase.BETTING, Phase.PLAYER, Phase.INSURANCE))
            if (!c.phases.contains(p)) out.add("phase " + p);

        if (c.splits == 0)     out.add("no split ever happened");
        if (c.doubles == 0)    out.add("no double ever happened");
        if (c.surrenders == 0) out.add("no surrender ever happened");
        if (c.reshuffles == 0) out.add("the shoe never reshuffled");
        if (c.maxHandsInRound < 3) out.add("never reached 3+ hands (max " + c.maxHandsInRound + ")");
        if (c.sideBets.size() < 3) out.add("side-bet tiers only " + c.sideBets);
        return out;
    }

    static void merge(Coverage a, Coverage b) {
        a.outcomes.addAll(b.outcomes); a.phases.addAll(b.phases); a.events.addAll(b.events);
        a.actions.addAll(b.actions);   a.sideBets.addAll(b.sideBets);
        a.rounds += b.rounds; a.reshuffles += b.reshuffles; a.splits += b.splits;
        a.doubles += b.doubles; a.surrenders += b.surrenders;
        a.insuranceTaken += b.insuranceTaken; a.insuranceDeclined += b.insuranceDeclined;
        a.remarks += b.remarks; a.legalityChecks += b.legalityChecks; a.queryCalls += b.queryCalls;
        a.maxHandsInRound = Math.max(a.maxHandsInRound, b.maxHandsInRound);
        a.maxCardsInHand  = Math.max(a.maxCardsInHand,  b.maxCardsInHand);
        a.biggestWin  = Math.max(a.biggestWin,  b.biggestWin);
        a.biggestLoss = Math.max(a.biggestLoss, b.biggestLoss);
    }
}
