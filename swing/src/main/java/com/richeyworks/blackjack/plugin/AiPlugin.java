package com.richeyworks.blackjack.plugin;

import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;

/**
 * An AI opponent's decision policy. Plugins implement this to ship custom
 * personalities (cautious, card-counter, drunk pirate, etc.).
 */
public interface AiPlugin {

    /** Display name shown in the AI picker. */
    String displayName();

    /** True if the AI should hit given the engine state. */
    boolean shouldHit(Engine engine, Hand hand);

    /** Returns a bet amount given the available bankroll. */
    int chooseBet(Engine engine, int bankroll);
}
