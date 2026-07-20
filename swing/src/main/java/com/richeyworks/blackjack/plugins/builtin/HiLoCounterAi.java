package com.richeyworks.blackjack.plugins.builtin;

import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.plugin.AiPlugin;
import com.richeyworks.blackjack.strategy.HiLoCounter;

/**
 * Exposes the shared {@link HiLoCounter} through the Swing plugin API.
 *
 * <p>The counting itself moved to {@code core}, because it is arithmetic over
 * cards and has nothing to do with a user interface — leaving it in the plugin
 * package is why the mobile build had no card counter despite the code being
 * entirely platform-neutral.
 *
 * <p>What remains here is genuinely Swing-specific: {@link AiPlugin} is part of
 * the desktop plugin contract, so this adapter keeps that contract intact and
 * gives third-party plugins something to look at without dragging the plugin
 * interfaces into {@code core}.
 */
public final class HiLoCounterAi implements AiPlugin {

    private final HiLoCounter counter = new HiLoCounter();

    /** The underlying counter, so a front end can display the running count. */
    public HiLoCounter counter() { return counter; }

    @Override public String displayName() { return counter.displayName(); }

    @Override public boolean shouldHit(Engine engine, Hand hand) {
        return counter.shouldHit(engine, hand);
    }

    @Override public int chooseBet(Engine engine, int bankroll) {
        return counter.chooseBet(engine, bankroll);
    }
}
