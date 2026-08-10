package com.richeyworks.blackjack.strategy;

import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.engine.Phase;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Feeds a {@link HiLoCounter} from the cards currently visible on the table,
 * without double-counting.
 *
 * <p>The old UI approach kept an integer cursor into a flattened list of
 * player-then-dealer cards. That works until a split: the second card of the
 * original hand moves into a new hand, the flat order changes, and the cursor
 * re-observes the dealer's up-card (and can skip a newly dealt card). Verified
 * with seed 17: K-10 vs 8 counted {@code -2} after the deal and {@code -1}
 * after the split, while the true visible count is {@code 0}.
 *
 * <p>This tracker keys on {@link System#identityHashCode card identity} — the
 * same {@link Card} instances the shoe dealt — so a split that reorders the
 * table is a no-op for cards already seen, and only genuinely new cards are
 * observed. A reshuffle (remaining count jumps up) clears both the count and
 * the identity set.
 */
public final class TableObservation {

    private final HiLoCounter counter;
    private final Set<Card> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    private int lastRemaining = -1;
    private boolean reshuffled;

    public TableObservation(HiLoCounter counter) {
        this.counter = counter;
    }

    public HiLoCounter counter() { return counter; }

    /** True if the most recent {@link #sync(Engine)} detected a shoe refill. */
    public boolean reshuffled() { return reshuffled; }

    /** Drop every observation (new session / explicit reset). */
    public void reset() {
        counter.resetCount();
        seen.clear();
        lastRemaining = -1;
        reshuffled = false;
    }

    /**
     * Observe every card the player can currently see. Idempotent: calling
     * twice with no new cards is a no-op for the running count.
     */
    public void sync(Engine engine) {
        reshuffled = false;
        int remaining = engine.shoe().remaining();
        // Remaining only rises when the shoe refills. Mid-round deals lower it.
        if (lastRemaining >= 0 && remaining > lastRemaining) {
            counter.resetCount();
            seen.clear();
            reshuffled = true;
        }
        lastRemaining = remaining;

        for (Hand h : engine.hands()) {
            for (Card c : h.cards()) observeOnce(c);
        }

        // The hole card is face-down until the dealer plays; counting it early
        // would show the player information they cannot see at the table.
        boolean holeHidden = engine.phase() == Phase.DEALING
                || engine.phase() == Phase.INSURANCE
                || engine.phase() == Phase.PLAYER;
        List<Card> dealer = engine.dealer().cards();
        int visible = holeHidden ? Math.min(1, dealer.size()) : dealer.size();
        for (int i = 0; i < visible; i++) observeOnce(dealer.get(i));
    }

    private void observeOnce(Card c) {
        if (seen.add(c)) counter.observe(c);
    }
}
