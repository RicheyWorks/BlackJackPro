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
    /**
     * Cards that must remain before a round starts so a multi-split / deep-hit
     * hand cannot empty the shoe mid-round. Verified: with a 1-deck shoe near
     * the cut, four-way splits routinely drove {@link #deal()} into the empty
     * path, which auto-reshuffled mid-hand and mixed two shoes into one count.
     *
     * <p>40 covers dealer + four player hands with generous hit depth. The cut
     * card still governs when more than this remains.
     */
    public static final int MIN_CARDS_FOR_ROUND = 40;

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

    /**
     * True once the cut card has been reached, or once too few cards remain to
     * safely finish a multi-hand round without mid-hand reshuffle.
     */
    public boolean needsShuffle() {
        // Floor and cut both inclusive: remaining==cutIndex and remaining==MIN
        // must reshuffle before the next deal starts.
        return cards.size() <= Math.max(cutIndex, MIN_CARDS_FOR_ROUND);
    }

    /**
     * Deal one card from the top.
     *
     * <p>Auto-reshuffles if empty as a last-resort defensive path. Callers that
     * honour {@link #needsShuffle()} before a round should never hit it; if they
     * do, a mid-hand reshuffle has already corrupted any running count.
     */
    public Card deal() {
        if (cards.isEmpty()) reshuffle();
        return cards.remove(cards.size() - 1);
    }

    public int decks()     { return decks; }
    public int remaining() { return cards.size(); }
    public int dealt()     { return decks * 52 - cards.size(); }
}
