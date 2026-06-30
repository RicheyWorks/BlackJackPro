package com.richeyworks.blackjack.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single hand of cards with bet and state tracking. Mutable — owned by the
 * engine. UI must treat the {@link #cards()} list as read-only.
 */
public final class Hand {
    private final List<Card> cards = new ArrayList<>(11);

    private int     bet;
    private boolean doubled;
    private boolean surrendered;
    private boolean fromSplit;
    private boolean splitAce;
    private boolean stood;

    /* ---------- cards ---------- */
    public List<Card> cards()   { return Collections.unmodifiableList(cards); }
    public int        size()    { return cards.size(); }
    public Card       first()   { return cards.get(0); }
    public boolean    isEmpty() { return cards.isEmpty(); }

    public void add(Card c)    { cards.add(c); }
    public Card removeLast()   { return cards.remove(cards.size() - 1); }

    public void reset() {
        cards.clear();
        bet = 0;
        doubled = surrendered = fromSplit = splitAce = stood = false;
    }

    /* ---------- bet ---------- */
    public int  bet()                 { return bet; }
    public void bet(int amount)       { this.bet = amount; }
    public void doubleBet()           { this.bet *= 2; this.doubled = true; }

    /* ---------- flags ---------- */
    public boolean doubled()     { return doubled; }
    public boolean surrendered() { return surrendered; }
    public void    surrender()   { this.surrendered = true; }
    public boolean fromSplit()   { return fromSplit; }
    public void    markFromSplit() { this.fromSplit = true; }
    public boolean splitAce()    { return splitAce; }
    public void    markSplitAce(){ this.splitAce = true; }
    public boolean stood()       { return stood; }
    public void    stand()       { this.stood = true; }

    /* ---------- valuation ---------- */

    /** Best legal value &le; 21, or smallest bust total if every option busts. */
    public int value() {
        int total = 0, aces = 0;
        for (Card c : cards) {
            total += c.rank().value();
            if (c.rank() == Rank.ACE) aces++;
        }
        while (total > 21 && aces > 0) { total -= 10; aces--; }
        return total;
    }

    /** True iff the hand has at least one ace counted as 11 in {@link #value()}. */
    public boolean isSoft() {
        int total = 0, aces = 0;
        for (Card c : cards) {
            total += c.rank().value();
            if (c.rank() == Rank.ACE) aces++;
        }
        // Demote aces from 11 to 1 while the hand would bust; mirror value().
        // The hand is "soft" iff at least one ace still counts as 11 afterward.
        while (total > 21 && aces > 0) { total -= 10; aces--; }
        return aces > 0;
    }

    public boolean isBust()      { return value() > 21; }

    /** Pair detection — same rank class (not suit). 10/J/Q/K all count as 10. */
    public boolean isPair() {
        if (cards.size() != 2) return false;
        return cards.get(0).rank().value() == cards.get(1).rank().value();
    }

    /** Natural 21 on two cards (and not from a split). */
    public boolean isBlackjack() {
        return cards.size() == 2 && value() == 21 && !fromSplit;
    }
}
