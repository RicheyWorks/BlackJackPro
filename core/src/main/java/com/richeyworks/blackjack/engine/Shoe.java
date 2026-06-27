package com.richeyworks.blackjack.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Multi-deck shoe with a configurable cut card. Deals from the top, reshuffles
 * when the dealt cursor passes the cut. Thread-unsafe — the engine is the
 * single owner.
 */
public final class Shoe {
    private final List<Card> cards = new ArrayList<>();
    private final int        decks;
    private final Random     rng;
    private final double     penetration;   // fraction of shoe to deal before reshuffle
    private int              cutIndex;

    public Shoe(int decks, Random rng) {
        this(decks, rng, 0.75);
    }

    public Shoe(int decks, Random rng, double penetration) {
        if (decks < 1)                          throw new IllegalArgumentException("decks must be >= 1");
        if (penetration <= 0 || penetration > 1) throw new IllegalArgumentException("penetration must be in (0,1]");
        this.decks       = decks;
        this.rng         = rng;
        this.penetration = penetration;
        reshuffle();
    }

    /** Reset to a fresh, shuffled shoe. */
    public void reshuffle() {
        cards.clear();
        for (int d = 0; d < decks; d++)
            for (Suit s : Suit.values())
                for (Rank r : Rank.values())
                    cards.add(new Card(r, s));
        Collections.shuffle(cards, rng);
        cutIndex = (int) (cards.size() * (1.0 - penetration));
    }

    /** True once the cut card has been reached. */
    public boolean needsShuffle() { return cards.size() <= cutIndex; }

    /** Deal one card from the top. Auto-reshuffles if empty (defensive). */
    public Card deal() {
        if (cards.isEmpty()) reshuffle();
        return cards.remove(cards.size() - 1);
    }

    public int decks()     { return decks; }
    public int remaining() { return cards.size(); }
    public int dealt()     { return decks * 52 - cards.size(); }
}
