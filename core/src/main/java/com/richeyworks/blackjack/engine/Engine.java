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
    /** Bankroll (including chips already on the felt) when the current round was dealt. */
    private int roundStartBankroll;
    /** Net change to the bankroll across the most recently settled round. */
    private int lastNet;

    public Engine(int startingBankroll, Random rng) {
        this(startingBankroll, rng, new BlackjackRules());
    }

    public Engine(int startingBankroll, Random rng, BlackjackRules rules) {
        this.rules    = rules;
        this.bankroll = startingBankroll;
        this.shoe     = new Shoe(rules.decks, rng, rules.penetration);
        this.player.add(new Hand());
        stats.peakBankroll = Math.max(stats.peakBankroll, startingBankroll);
    }

    public BlackjackRules rules()      { return rules; }
    public Shoe           shoe()       { return shoe; }
    public Hand           dealer()     { return dealer; }
    public List<Hand>     hands()      { return player; }
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
     * engine are not included.
     */
    public int lastNet() { return lastNet; }

    public void setBankroll(int b) { this.bankroll = b; }

    public boolean canBet(int amount) {
        return phase == Phase.BETTING && amount > 0 && amount <= bankroll;
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
    public boolean canStand()       { return phase == Phase.PLAYER && !active().isBust(); }
    public boolean canDouble() {
        if (phase != Phase.PLAYER) return false;
        Hand h = active();
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

    /** True iff insurance is offered and the player can afford the half-bet premium. */
    public boolean canInsure() {
        return phase == Phase.INSURANCE && !player.isEmpty()
                && bankroll >= rules.insurancePremium(player.get(0).bet());
    }

    public void addBet(int amount) {
        if (!canBet(amount)) throw new IllegalStateException("cannot bet " + amount);
        bankroll  -= amount;
        pendingBet += amount;
    }

    public void clearBet() {
        if (phase != Phase.BETTING) return;
        bankroll  += pendingBet;
        pendingBet = 0;
    }

    public void deal() {
        if (!canDeal()) throw new IllegalStateException("cannot deal");
        if (shoe.needsShuffle()) shoe.reshuffle();

        // Everything the player owns right now, including the chips already
        // moved onto the felt -- the baseline for this round's net result.
        roundStartBankroll = bankroll + pendingBet;
        lastOutcomes.clear();
        lastNet = 0;

        for (Hand h : player) h.reset();
        player.clear();
        dealer.reset();
        Hand first = new Hand();
        first.bet(pendingBet);
        stats.totalWagered += first.bet();
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
            if (bankroll < cost) throw new IllegalStateException("not enough chips for insurance");
            bankroll    -= cost;
            stats.totalWagered += cost;   // insurance is a wager; keep accounting consistent
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
                int payout = insuranceBet + rules.insurancePayout(insuranceBet);
                bankroll  += payout;
                stats.totalReturned += payout;
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
        stats.totalWagered += h.bet();
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
        stats.totalWagered += h.bet();
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
        bankroll  += refund;
        stats.totalReturned += refund;
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
                int payout = h.bet() + rules.blackjackPayout(h.bet());
                bankroll          += payout;
                stats.totalReturned += payout;
                stats.wins++;
                stats.blackjacks++;
                lastOutcomes.add(Outcome.BLACKJACK);
                continue;
            }
            if (dealerBJ) {
                if (h.isBlackjack()) {
                    bankroll        += h.bet();
                    stats.totalReturned += h.bet();
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
                int payout = h.bet() * 2;
                bankroll          += payout;
                stats.totalReturned += payout;
                stats.wins++;
                lastOutcomes.add(Outcome.WIN);
            } else if (pv == dv) {
                bankroll        += h.bet();
                stats.totalReturned += h.bet();
                stats.pushes++;
                lastOutcomes.add(Outcome.PUSH);
            } else {
                stats.losses++;
                lastOutcomes.add(Outcome.LOSS);
            }
        }
        stats.peakBankroll = Math.max(stats.peakBankroll, bankroll);
        lastNet = bankroll - roundStartBankroll;
        phase = Phase.BETTING;
    }
}
