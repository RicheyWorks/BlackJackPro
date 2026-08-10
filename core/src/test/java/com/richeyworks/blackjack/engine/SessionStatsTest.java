package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionStatsTest {

    @Test
    void winRateUsesResolvedHandsNotDeals() {
        SessionStats s = new SessionStats();
        s.hands = 1;          // one deal
        s.wins = 3;           // three-way split all won
        s.losses = 0;
        s.pushes = 0;
        assertEquals(1.0, s.winRate(), 1e-9, "3 wins / 3 resolved hands = 100%, not 300%");
    }

    @Test
    void winRateZeroWhenNothingResolved() {
        SessionStats s = new SessionStats();
        s.hands = 5;
        assertEquals(0.0, s.winRate(), 1e-9);
    }

    @Test
    void winRatePartial() {
        SessionStats s = new SessionStats();
        s.wins = 1;
        s.losses = 1;
        s.pushes = 2;
        assertEquals(0.25, s.winRate(), 1e-9);
    }
}
