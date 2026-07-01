package com.richeyworks.blackjack.engine;

/**
 * Basic strategy for a dealer who stands on soft 17, including late surrender.
 * Returns one of:
 *   H = hit,  S = stand,  D = double,  P = split,  R = surrender.
 *
 * D and R are only ever returned for a two-card hand (the only place either is
 * legal); multi-card hands get the correct hit/stand action directly, so the
 * caller can act on each result literally. Dependency-free for direct testing.
 */
public final class BasicStrategy {

    private BasicStrategy() {}

    public enum Action { H, S, D, P, R }

    public static Action recommend(Hand hand, Card dealerUp) {
        int dv = dealerUp.rank().value();
        boolean twoCard = hand.size() == 2;

        // ---- pair splits ----
        if (hand.isPair()) {
            int r = hand.first().rank().value();
            switch (r) {
                case 11: return Action.P;                                    // A,A
                case 10: return Action.S;                                    // 10,10
                case 9:  return (dv == 7 || dv == 10 || dv == 11) ? Action.S : Action.P;
                case 8:  return Action.P;                                    // 8,8
                case 7:  return dv <= 7 ? Action.P : Action.H;
                case 6:  return dv <= 6 ? Action.P : Action.H;
                case 5:  return dv <= 9 ? Action.D : Action.H;
                case 4:  return (dv == 5 || dv == 6) ? Action.P : Action.H;
                case 3:
                case 2:  return dv <= 7 ? Action.P : Action.H;
            }
        }

        // ---- soft totals ----
        if (hand.isSoft()) {
            int t = hand.value();
            switch (t) {
                case 20: return Action.S;
                case 19: return (twoCard && dv == 6) ? Action.D : Action.S;
                case 18:
                    if (twoCard && dv >= 3 && dv <= 6) return Action.D;  // double vs 3-6 (2 cards)
                    if (dv <= 8) return Action.S;                        // stand vs 2,7,8 (and 3-6 w/ 3+ cards)
                    return Action.H;                                     // hit vs 9,10,A
                case 17: return (twoCard && dv >= 3 && dv <= 6) ? Action.D : Action.H;
                case 16:
                case 15: return (twoCard && dv >= 4 && dv <= 6) ? Action.D : Action.H;
                case 14:
                case 13: return (twoCard && (dv == 5 || dv == 6)) ? Action.D : Action.H;
            }
        }

        // ---- hard totals ----
        int t = hand.value();
        // Late surrender (two cards only): the canonical S17 spots. Pairs are
        // handled above, so a hard 16 here is a non-pair total (e.g. 10-6, 9-7).
        if (twoCard && t == 16 && (dv == 9 || dv == 10 || dv == 11)) return Action.R;
        if (twoCard && t == 15 && dv == 10) return Action.R;
        if (t >= 17) return Action.S;
        if (t >= 13) return dv <= 6 ? Action.S : Action.H;
        if (t == 12) return (dv >= 4 && dv <= 6) ? Action.S : Action.H;
        if (t == 11) return twoCard ? Action.D : Action.H;
        if (t == 10) return (twoCard && dv <= 9) ? Action.D : Action.H;
        if (t ==  9) return (twoCard && dv >= 3 && dv <= 6) ? Action.D : Action.H;
        return Action.H;
    }
}
