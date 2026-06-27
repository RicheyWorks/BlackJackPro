package com.richeyworks.blackjack.engine;

/** State machine for a single round. */
public enum Phase {
    BETTING,
    DEALING,
    INSURANCE,
    PLAYER,
    DEALER,
    SETTLE
}
