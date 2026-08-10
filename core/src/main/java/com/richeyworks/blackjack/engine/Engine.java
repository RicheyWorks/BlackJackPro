package com.richeyworks.blackjack.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Engine {

    private final BlackjackRules rules;
    private final Shoe           shoe;
    private final Hand           dealer = new Hand();
    private final List<Hand>     player = new ArrayList<>();
    private final SessionStats   stats  = new SessionStats();

    private int   activeHand;
    private int   bankroll;
    private int   pendingBet;
    private int   insuranceBet;
    private Phase phase = Phase.BETTING;

    /** Per-hand results of the most recently settled round, parallel to {@link #hands()}. */
    private final List<Outcome> lastOutcomes = new ArrayList<>();
    /*
     * The round's own money, accumulated by engine operations only.
     *
     * This used to be a bankroll snapshot taken at deal() and subtracted at
     * settle() -- which silently absorbed anything the caller did to the
     * bankroll in between. The 21+3 side bet is settled by the front end with
     * setBankroll() right after the deal, so a side-bet win landed inside the
     * round's net and the table displayed the wrong result for the hand. A
     * doubled $50 loss alongside a $30 side-bet win reported -20 instead of -50.
     *
     * Tracking the wagers and returns the engine itself performs makes the
     * figure immune to whatever else touches the bankroll.
     */
    private int roundWagered;
    private int roundReturned;
    /** Published at settlement, so this describes the last completed round. */
    private int lastNet;

    public Engine(int startingBankroll, Random rng) {
        this(startingBankroll, rng, new BlackjackRules());
    }

    public Engine(int startingBankroll, Random rng, BlackjackRules rules) {
        this.rules    = rules;
        this.bankroll = Math.max(0, startingBankroll);
        this.shoe     = new Shoe(rules.decks, rng, rules.penetration);
        this.player.add(new Hand());
        stats.peakBankroll = Math.max(stats.peakBankroll, bankroll);
    }

    public BlackjackRules rules()      { return rules; }
    public Shoe           shoe()       { return shoe; }
    public Hand           dealer()     { return dealer; }
    public List<Hand>     hands()      { return Collections.unmodifiableList(player); }
    public Hand           active()     { return player.get(activeHand); }
    public int            activeIndex(){ return activeHand; }
    public SessionStats   stats()      { return stats; }
    public Phase          phase()      { return phase; }
    public int            bankroll()   { return bankroll; }
    public int            pendingBet() { return pendingBet; }
    public int            insuranceBet(){ return insuranceBet; }

    /**
     * Results of the most recently settled round, one entry per hand, in the
     * same order as {@link #hands()}. Recorded by {@link #settle()} as the money
     * moves, so it is the authoritative record of what the player was paid —
     * front ends should render this rather than re-comparing hand values.
     * Empty until the first round settles; the hands (and these outcomes)
     * survive into the following {@link Phase#BETTING} phase and are replaced
     * on the next {@link #deal()}.
     */
    public List<Outcome> lastOutcomes() { return Collections.unmodifiableList(lastOutcomes); }

    /**
     * Net change to the bankroll across the most recently settled round —
     * positive if the player finished ahead. Covers the main stake plus any
     * double, split, insurance, or surrender. Side bets settled outside the
     * engine are genuinely not included -- this counts only money the engine
     * itself moved, so a caller adjusting the bankroll mid-round cannot
     * contaminate it.
     */
    public int lastNet() { return lastNet; }

    public void setBankroll(int b) {
        // Saves clamp on load; the live API must too — a negative bankroll
        // makes canBet/canInsure nonsense and paints "-$N" in the HUD.
        this.bankroll = Math.max(0, b);
        // Keep peak honest after a reload / new-session bankroll write. Stats
        // reset() zeroes the peak first; without this lift a fresh $1000 table
        // reports peak $0 until the first hand settles.
        stats.peakBankroll = Math.max(stats.peakBankroll, bankroll);
    }

    public boolean canBet(int amount) {
        if (phase != Phase.BETTING || amount <= 0 || amount > bankroll) return false;
        // Refuse chip clicks that would wrap pendingBet.
        return pendingBet <= Integer.MAX_VALUE - amount;
    }

    public boolean canDeal()        { return phase == Phase.BETTING && pendingBet > 0; }

    /*
     * Every check below tests the phase BEFORE touching active().
     *
     * That ordering is load-bearing, not style. advanceHand() leaves activeHand
     * one past the end of the hands list when the last hand finishes, so once a
     * round settles active() is out of bounds. A can*() that dereferences first
     * therefore throws IndexOutOfBoundsException instead of answering "no" --
     * and these are exactly the methods a UI calls to decide which buttons to
     * enable, which it does immediately after every round.
     *
     * The desktop refresh() asked canHit() fourth in its sequence, so after 78%
     * of completed rounds it threw and every later button update, including the
     * table repaint, was silently skipped. On the Swing EDT an uncaught
     * exception aborts the handler without killing the app, which is why this
     * survived a full review and 249 tests: nothing crashed, the UI just quietly
     * stopped updating. Found by playing two hands.
     */

    public boolean canHit() {
        if (phase != Phase.PLAYER) return false;
        Hand h = active();
        return !h.isBust() && !h.stood() && !h.splitAce() && h.value() < 21;
    }
    public boolean canStand() {
        if (phase != Phase.PLAYER) return false;
        Hand h = active();
        // stood hands have already acted; allowing stand again would advance
        // past them a second time if anything left the cursor on a finished hand.
        return !h.isBust() && !h.stood() && !h.surrendered();
    }
    public boolean canDouble() {
        if (phase != Phase.PLAYER) return false;
        Hand h = active();
        // bet*2 must fit in an int — otherwise doubleBet() overflows to a
        // negative stake and settle() pays the house into the player's pocket.
        if (h.bet() > Integer.MAX_VALUE / 2) return false;
        return h.size() == 2 && bankroll >= h.bet() && !h.splitAce()
                && (player.size() == 1 || rules.doubleAfterSplit);
    }
    public boolean canSplit() {
        if (phase != Phase.PLAYER) return false;
        Hand h = active();
        return h.size() == 2 && h.isPair() && bankroll >= h.bet()
                && player.size() <= rules.maxSplits;
    }
    public boolean canSurrender() {
        if (phase != Phase.PLAYER) return false;
        Hand h = active();
        return rules.lateSurrender && h.size() == 2 && player.size() == 1
                && !h.fromSplit() && !h.doubled();
    }

    /** True iff insurance is offered, costs a real premium, and the player can pay it. */
    public boolean canInsure() {
        if (phase != Phase.INSURANCE || player.isEmpty()) return false;
        int premium = rules.insurancePremium(player.get(0).bet());
        // $1 main bets round the premium to $0. Offering "free" insurance lights
        // the Insure button for a pure no-op and confuses a broke all-in player.
        return premium > 0 && bankroll >= premium;
    }

    public void addBet(int amount) {
        if (!canBet(amount)) throw new IllegalStateException("cannot bet " + amount);
        // pendingBet can grow across chip clicks — refuse wrap rather than deal free.
        if (pendingBet > Integer.MAX_VALUE - amount) {
            throw new IllegalStateException("pending bet overflow");
        }
        bankroll  -= amount;
        pendingBet += amount;
    }

    public void clearBet() {
        if (phase != Phase.BETTING) return;
        creditBankrollOnly(pendingBet);
        pendingBet = 0;
    }


    /**
     * Force the table back to a clean {@link Phase#BETTING} state, discarding
     * any in-progress hands, insurance, and round bookkeeping.
     *
     * <p>Chips still sitting on the felt as {@link #pendingBet()} are returned
     * to the bankroll — they were never risked on a dealt hand. In-hand stakes
     * (cards already out) are <em>not</em> refunded; the caller owns the
     * bankroll after that (typically overwriting it for a new session).
     *
     * <p>Without this, a mid-round "New Session" left {@code phase == PLAYER},
     * a live hand with its old bet, and Deal disabled, while
     * {@code setBankroll(1000)} handed free chips on top of a hand that could
     * still be played and paid.
     */
    public void abandonRound() {
        // Return undelt chips. Zeroing pending without this line destroyed them
        // whenever a caller abandoned during betting without a later setBankroll.
        creditBankrollOnly(pendingBet);
        pendingBet   = 0;
        for (Hand h : player) h.reset();
        player.clear();
        player.add(new Hand());
        dealer.reset();
        activeHand   = 0;
        insuranceBet = 0;
        lastOutcomes.clear();
        roundWagered  = 0;
        roundReturned = 0;
        lastNet       = 0;
        phase = Phase.BETTING;
    }


    public void deal() {
        if (!canDeal()) throw new IllegalStateException("cannot deal");
        if (shoe.needsShuffle()) shoe.reshuffle();

        lastOutcomes.clear();
        roundWagered  = 0;
        roundReturned = 0;
        lastNet       = 0;

        for (Hand h : player) h.reset();
        player.clear();
        dealer.reset();
        Hand first = new Hand();
        first.bet(pendingBet);
        recordWager(first.bet());
        pendingBet = 0;
        player.add(first);
        activeHand   = 0;
        insuranceBet = 0;
        phase = Phase.DEALING;
        stats.hands++;

        first.add(shoe.deal());
        dealer.add(shoe.deal());
        first.add(shoe.deal());
        dealer.add(shoe.deal());

        if (rules.offerInsurance && dealer.first().rank() == Rank.ACE) {
            phase = Phase.INSURANCE;
            return;
        }
        afterInsuranceCheck();
    }

    public void takeInsurance(boolean accept) {
        if (phase != Phase.INSURANCE) throw new IllegalStateException("not in insurance phase");
        Hand h = player.get(0);
        if (accept) {
            // Same source as canInsure(): computing the premium in two places is
            // how the offer and the charge drift apart.
            int cost = rules.insurancePremium(h.bet());
            // Zero premium is not coverage — refuse rather than pretend.
            if (cost <= 0) throw new IllegalStateException("insurance premium is zero");
            if (bankroll < cost) throw new IllegalStateException("not enough chips for insurance");
            bankroll    -= cost;
            recordWager(cost);
            insuranceBet = cost;
        } else {
            insuranceBet = 0;
        }
        afterInsuranceCheck();
    }

    private void afterInsuranceCheck() {
        boolean dealerBJ = dealer.value() == 21 && dealer.size() == 2;
        if (dealerBJ) {
            if (insuranceBet > 0) {
                int payout = addExact(insuranceBet, rules.insurancePayout(insuranceBet));
                credit(payout);
            }
            insuranceBet = 0;
            phase = Phase.SETTLE;
            settle();
            return;
        }
        if (insuranceBet > 0) {
            insuranceBet = 0;
        }
        if (player.get(0).isBlackjack()) {
            phase = Phase.SETTLE;
            settle();
            return;
        }
        phase = Phase.PLAYER;
    }

    public void hit() {
        if (!canHit()) throw new IllegalStateException("cannot hit");
        Hand h = active();
        h.add(shoe.deal());
        if (h.isBust() || h.value() == 21) advanceHand();
    }

    public void stand() {
        if (!canStand()) throw new IllegalStateException("cannot stand");
        active().stand();
        advanceHand();
    }

    public void doubleDown() {
        if (!canDouble()) throw new IllegalStateException("cannot double");
        Hand h = active();
        bankroll -= h.bet();
        recordWager(h.bet());
        h.doubleBet();
        stats.doubles++;
        h.add(shoe.deal());
        advanceHand();
    }

    public void split() {
        if (!canSplit()) throw new IllegalStateException("cannot split");
        Hand h = active();
        Hand n = new Hand();
        n.add(h.removeLast());
        n.bet(h.bet());
        n.markFromSplit();
        h.markFromSplit();
        bankroll -= h.bet();
        recordWager(h.bet());
        stats.splits++;
        player.add(activeHand + 1, n);

        h.add(shoe.deal());
        n.add(shoe.deal());

        if (rules.splitAcesOneCard && h.first().rank() == Rank.ACE) {
            h.markSplitAce();
            n.markSplitAce();
            // Both hands are frozen; advanceHand() loops past every splitAce
            // hand on its own, so a single call moves to the next playable
            // hand (or the dealer). Calling it twice skipped a hand and, in the
            // common A,A case, ran playDealer()/settle() twice -- double-paying.
            advanceHand();
        }
    }

    public void surrender() {
        if (!canSurrender()) throw new IllegalStateException("cannot surrender");
        Hand h = active();
        h.surrender();
        stats.surrenders++;
        int refund = rules.surrenderRefund(h.bet());
        credit(refund);
        phase = Phase.SETTLE;
        settle();
    }

    private void advanceHand() {
        while (true) {
            activeHand++;
            if (activeHand >= player.size()) {
                phase = Phase.DEALER;
                playDealer();
                return;
            }
            if (active().splitAce()) continue;
            return;
        }
    }

    private void playDealer() {
        boolean anyLive = player.stream().anyMatch(h -> !h.isBust() && !h.surrendered());
        if (anyLive) {
            while (true) {
                int v = dealer.value();
                if (v < 17) { dealer.add(shoe.deal()); continue; }
                if (v == 17 && dealer.isSoft() && rules.dealerHitsSoft17) {
                    dealer.add(shoe.deal()); continue;
                }
                break;
            }
        }
        phase = Phase.SETTLE;
        settle();
    }

    private void settle() {
        int     dv          = dealer.value();
        boolean dealerBJ    = dealer.isBlackjack();

        // Each branch records an Outcome next to the money it moves, so the
        // result a front end displays is the same one that was paid.
        lastOutcomes.clear();

        for (Hand h : player) {
            if (h.surrendered()) {
                stats.losses++;
                lastOutcomes.add(Outcome.SURRENDER);
                continue;
            }
            if (h.isBust()) {
                stats.losses++;
                stats.busts++;
                lastOutcomes.add(Outcome.BUST);
                continue;
            }
            if (h.isBlackjack() && !dealerBJ) {
                int payout = addExact(h.bet(), rules.blackjackPayout(h.bet()));
                credit(payout);
                lastOutcomes.add(Outcome.BLACKJACK);
                stats.wins++;
                stats.blackjacks++;
                continue;
            }
            if (dealerBJ) {
                if (h.isBlackjack()) {
                    credit(h.bet());
                    stats.pushes++;
                    lastOutcomes.add(Outcome.PUSH);
                } else {
                    stats.losses++;
                    lastOutcomes.add(Outcome.LOSS);
                }
                continue;
            }
            int pv = h.value();
            if (dv > 21 || pv > dv) {
                // Even money via long math — bet*2 overflows int above ~1.07e9
                // and used to credit a *negative* payout.
                credit(evenMoneyReturn(h.bet()));
                stats.wins++;
                lastOutcomes.add(Outcome.WIN);
            } else if (pv == dv) {
                credit(h.bet());
                stats.pushes++;
                lastOutcomes.add(Outcome.PUSH);
            } else {
                stats.losses++;
                lastOutcomes.add(Outcome.LOSS);
            }
        }
        stats.peakBankroll = Math.max(stats.peakBankroll, bankroll);
        lastNet = roundReturned - roundWagered;
        // Point activeHand back at a live hand. advanceHand() leaves it one past
        // the end so can*() must phase-guard — but active() itself was still a
        // footgun: any post-round call (debug HUD, plugin, future UI) threw
        // IndexOutOfBoundsException while phase was already BETTING.
        activeHand = 0;
        phase = Phase.BETTING;
    }

    /** Stake + even-money win, saturating at {@link Integer#MAX_VALUE}. */
    private static int evenMoneyReturn(int bet) {
        long p = (long) bet * 2L;
        return p > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) p;
    }

    private static int addExact(int a, int b) {
        long s = (long) a + b;
        return s > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) s;
    }

    /** Record a wager without wrapping totalWagered / roundWagered. */
    private void recordWager(int amount) {
        if (amount <= 0) return;
        long tw = (long) stats.totalWagered + amount;
        stats.totalWagered = tw >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) tw;
        long rw = (long) roundWagered + amount;
        roundWagered = rw >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rw;
    }

    /** Return chips to bankroll without touching session wagered/returned ledgers. */
    private void creditBankrollOnly(int amount) {
        if (amount <= 0) return;
        long b = (long) bankroll + amount;
        bankroll = b >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) b;
        stats.peakBankroll = Math.max(stats.peakBankroll, bankroll);
    }

    /**
     * Credit chips to the player and the round ledger without int wrap.
     * A saturated credit is better than a negative bankroll after a win.
     */
    private void credit(int amount) {
        if (amount <= 0) return;
        creditBankrollOnly(amount);
        long tr = (long) stats.totalReturned + amount;
        stats.totalReturned = tr >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) tr;
        long rr = (long) roundReturned + amount;
        roundReturned = rr >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rr;
    }
}
