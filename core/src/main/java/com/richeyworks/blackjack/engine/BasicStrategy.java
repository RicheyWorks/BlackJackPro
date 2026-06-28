package com.richeyworks.blackjack.engine;

/**
 * Basic strategy (S17, no surrender chart). Returns one of:
 *   H = hit,  S = stand,  D = double (else hit),  P = split,  R = surrender (else hit).
 *
 * Intentionally exhaustive and dependency-free so it can be unit-tested directly.
 */
public final class BasicStrategy {

    private BasicStrategy() {}

    public enum Action { H, S, D, P, R }

    public static Action recommend(Hand hand, Card dealerUp) {
        int dv = dealerUp.rank().value();

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
                case 19: return dv == 6 ? Action.D : Action.S;
                case 18:
                    if (dv >= 3 && dv <= 6) return Action.D;  // double vs 3-6
                    if (dv <= 8) return Action.S;             // stand vs 2,7,8
                    return Action.H;                          // hit vs 9,10,A
                case 17: return (dv >= 3 && dv <= 6) ? Action.D : Action.H;  // A,6: hit vs 2
                case 16:
                case 15: return (dv >= 4 && dv <= 6) ? Action.D : Action.H;
                case 14:
                case 13: return (dv == 5 || dv == 6) ? Action.D : Action.H;
            }
        }

        // ---- hard totals ----
        int t = hand.value();
        if (t >= 17) return Action.S;
        if (t >= 13) return dv <= 6 ? Action.S : Action.H;
        if (t == 12) return (dv >= 4 && dv <= 6) ? Action.S : Action.H;
        if (t == 11) return Action.D;
        if (t == 10) return dv <= 9 ? Action.D : Action.H;
        if (t ==  9) return (dv >= 3 && dv <= 6) ? Action.D : Action.H;
        return Action.H;
    }
}
