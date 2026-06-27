package com.richeyworks.blackjack.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Pure rules engine. Owns the table state, accepts player actions, drives
 * dealer play, and settles every hand. UI code reads state through accessors
 * and never mutates fields directly.
 *
 * Single-threaded; the UI must marshal calls onto whichever thread owns the
 * engine (EDT for the Swing UI).
 */
public final class Engine {

    private final BlackjackRules rules;
    private final Shoe           shoe;
    private final Hand           dealer = new Hand();
    private final List<Hand>     player = new ArrayList<>();
    private final SessionStats   stats  = new SessionStats();
    private final Deque<String>  log    = new ArrayDeque<>(256);

    private int   activeHand;
    private int   bankroll;
    private int   pendingBet;
    private int   insuranceBet;
    private Phase phase = Phase.BETTING;

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

    /* ----------------------------------------------------------------------- */
    /* Accessors                                                               */
    /* ----------------------------------------------------------------------- */

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
    public Deque<String>  log()        { return log; }

    /** Direct bankroll setter for restore/load. Avoid in gameplay paths. */
    public void setBankroll(int b) { this.bankroll = b; }

    void log(String msg) {
        if (log.size() >= 250) log.pollFirst();
        log.addLast(msg);
    }

    /* ----------------------------------------------------------------------- */
    /* Legality probes — UI uses these to enable/disable buttons               */
    /* ----------------------------------------------------------------------- */

    public boolean canBet(int amount) {
        return phase == Phase.BETTING && amount > 0 && amount <= bankroll;
    }
    public boolean canDeal()        { return phase == Phase.BETTING && pendingBet > 0; }
    public boolean canHit() {
        Hand h = active();
        return phase == Phase.PLAYER && !h.isBust() && !h.stood() && !h.splitAce() && h.value() < 21;
    }
    public boolean canStand()       { return phase == Phase.PLAYER && !active().isBust(); }
    public boolean canDouble() {
        Hand h = active();
        return phase == Phase.PLAYER && h.size() == 2 && bankroll >= h.bet() && !h.splitAce()
                && (player.size() == 1 || rules.doubleAfterSplit);
    }
    public boolean canSplit() {
        Hand h = active();
        return phase == Phase.PLAYER && h.size() == 2 && h.isPair() && bankroll >= h.bet()
                && player.size() <= rules.maxSplits;
    }
    public boolean canSurrender() {
        Hand h = active();
        return phase == Phase.PLAYER && rules.lateSurrender && h.size() == 2 && player.size() == 1
                && !h.fromSplit() && !h.doubled();
    }

    /* ----------------------------------------------------------------------- */
    /* Betting                                                                 */
    /* ----------------------------------------------------------------------- */

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

    /* ----------------------------------------------------------------------- */
    /* Round flow                                                              */
    /* ----------------------------------------------------------------------- */

    public void deal() {
        if (!canDeal()) throw new IllegalStateException("cannot deal");
        if (shoe.needsShuffle()) shoe.reshuffle();

        // reset table
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

        // P, D, P, D
        first.add(shoe.deal());
        dealer.add(shoe.deal());
        first.add(shoe.deal());
        dealer.add(shoe.deal());

        if (rules.offerInsurance && dealer.first().rank() == Rank.ACE) {
            phase = Phase.INSURANCE;
            log("Dealer shows Ace — offering insurance.");
            return;
        }
        afterInsuranceCheck();
    }

    public void takeInsurance(boolean accept) {
        if (phase != Phase.INSURANCE) throw new IllegalStateException("not in insurance phase");
        Hand h = player.get(0);
        if (accept) {
            int cost = h.bet() / 2;
            if (bankroll < cost) throw new IllegalStateException("not enough chips for insurance");
            bankroll    -= cost;
            insuranceBet = cost;
        } else {
            insuranceBet = 0;
        }
        afterInsuranceCheck();
    }

    private void afterInsuranceCheck() {
        boolean dealerBJ = dealer.value() == 21 && dealer.size() == 2;
        if (dealerBJ) {
            // Pay insurance if taken (stake + 2:1 = 3x cost)
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
            insuranceBet = 0; // lost
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
            advanceHand();
            advanceHand();
        }
    }

    public void surrender() {
        if (!canSurrender()) throw new IllegalStateException("cannot surrender");
        Hand h = active();
        h.surrender();
        stats.surrenders++;
        int refund = h.bet() / 2;
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
            if (active().splitAce()) continue;   // single-card frozen hand
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

        for (Hand h : player) {
            if (h.surrendered()) {
                stats.losses++;
                continue;
            }
            if (h.isBust()) {
                stats.losses++;
                stats.busts++;
                continue;
            }
            if (h.isBlackjack() && !dealerBJ) {
                int payout = h.bet() + rules.blackjackPayout(h.bet());
                bankroll          += payout;
                stats.totalReturned += payout;
                stats.wins++;
                stats.blackjacks++;
                continue;
            }
            if (dealerBJ) {
                if (h.isBlackjack()) {
                    bankroll        += h.bet();
                    stats.totalReturned += h.bet();
                    stats.pushes++;
                } else {
                    stats.losses++;
                }
                continue;
            }
            int pv = h.value();
            if (dv > 21 || pv > dv) {
                int payout = h.bet() * 2;
                bankroll          += payout;
                stats.totalReturned += payout;
                stats.wins++;
            } else if (pv == dv) {
                bankroll        += h.bet();
                stats.totalReturned += h.bet();
                stats.pushes++;
            } else {
                stats.losses++;
            }
        }
        stats.peakBankroll = Math.max(stats.peakBankroll, bankroll);
        phase = Phase.BETTING;
    }
}
