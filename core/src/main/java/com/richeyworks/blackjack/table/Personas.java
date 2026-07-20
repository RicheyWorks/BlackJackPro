package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The characters who ship with the game.
 *
 * <h2>Why they never tell you to bet</h2>
 * This is a wagering game, and a table of characters cheering the player toward
 * a bigger stake would be a nasty thing to build. So the writing follows one
 * rule throughout: <b>react to what happened, never steer what happens next.</b>
 *
 * <p>Concretely, nothing here says raise your bet, chase a loss, that a win is
 * "due", or that the shoe owes anybody anything. A cold streak draws sympathy
 * or a change of subject. A hot streak draws pleasure in the moment, not a
 * suggestion to press it. Dutch is superstitious about the cards because that
 * is a real and funny thing people are at tables — but the others quietly
 * needle him for it, so the game never endorses it. {@code LOW_CHIPS} is the
 * one event where the table gets gentler rather than louder.
 *
 * <p>{@code ChatterToneTest} enforces the rule mechanically, so a well-meaning
 * future line cannot quietly break it.
 */
public final class Personas {

    private Personas() {}

    /** Seat indices run left to right across the felt; the player sits centre. */
    public static final int SEAT_LEFT   = 0;
    public static final int SEAT_RIGHT  = 1;
    public static final int SEAT_FAR    = 2;

    /**
     * Retired schoolteacher. Dry, unhurried, notices everything. Plays two
     * dollars a hand and has done for eleven years.
     */
    public static Persona marge() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);
        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Well now. That's the good one.",
                "Oh, lovely. Doesn't that just make an evening.",
                "Twenty-one on the deal. I've been here since seven."));
        l.put(TableEvent.PLAYER_WIN, List.of(
                "There you go, dear.",
                "That's the way.",
                "Nicely done."));
        l.put(TableEvent.PLAYER_BUST, List.of(
                "Ooh. One too many.",
                "Ah well. It happens to the best of us.",
                "That's a shame. It was a decent hand up to then."));
        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Rotten luck.",
                "The dealer does that.",
                "Hmph. Nothing you could have done there."));
        l.put(TableEvent.PUSH, List.of(
                "A tie. How very diplomatic.",
                "Nobody wins, nobody cries.",
                "Push. That's the polite outcome."));
        l.put(TableEvent.DEALER_BUST, List.of(
                "Ha! Over she goes.",
                "The house doesn't like that one bit.",
                "Twenty-two. Serves them right."));
        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Sensible. Not everyone would.",
                "Knowing when to stop is a skill, dear."));
        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands now. Ambitious.",
                "Splitting them up, are we?"));
        l.put(TableEvent.COLD_STREAK, List.of(
                "The cards have gone cold. They do that.",
                "Rough patch. It isn't anything you're doing.",
                "This is usually when I get a cup of tea."));
        l.put(TableEvent.HOT_STREAK, List.of(
                "You're having a lovely run.",
                "Someone's evening is going well."));
        l.put(TableEvent.LOW_CHIPS, List.of(
                "Getting thin there. No shame in calling it a night.",
                "That's most of it gone, dear. It's only a game."));
        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Everyone pretend that means something."));
        return new Persona("marge", "Marge", SEAT_LEFT, 0.40, l);
    }

    /**
     * Old-timer with a system, a lucky coin, and forty years of opinions about
     * the shoe. Warm, loud, completely wrong about probability.
     */
    public static Persona dutch() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);
        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "HA! Now that's a card.",
                "Blackjack! I felt that one coming.",
                "See, I told you this shoe had one in it."));
        l.put(TableEvent.PLAYER_WIN, List.of(
                "Attaboy.",
                "That's how it's done.",
                "Take it, take it."));
        l.put(TableEvent.PLAYER_BUST, List.of(
                "Oof. Right over the top.",
                "The deck did that, not you.",
                "That card had no business being in there."));
        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Bah. Dealer's got horseshoes tonight.",
                "That one stung.",
                "I've seen that hand win a hundred times."));
        l.put(TableEvent.PUSH, List.of(
                "A push. Nobody's hurt.",
                "Standoff. I'll take it."));
        l.put(TableEvent.DEALER_BUST, List.of(
                "THERE it is! Busted!",
                "Ha ha! Over twenty-one!",
                "That's what I like to see."));
        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Course they did.",
                "Ace up. I knew it. I knew it."));
        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Ace showing. Do what you like, I never take it.",
                "Insurance. Forty years, never once."));
        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Bold.",
                "Now we'll see something."));
        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split 'em!",
                "Two hands. I like your nerve."));
        l.put(TableEvent.SHUFFLE, List.of(
                "New shoe. Everything I knew just went in the bin.",
                "Shuffle. Right, I'm starting my count again.",
                "There goes the shoe. Shame, I had it figured."));
        l.put(TableEvent.HOT_STREAK, List.of(
                "You're on a run, kid.",
                "Somebody's got the touch tonight."));
        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold table. It'll turn, or it won't.",
                "Rough stretch. Happens to everybody."));
        l.put(TableEvent.LOW_CHIPS, List.of(
                "Getting low there. Only ever play what you don't mind losing.",
                "Careful now. It's meant to be a night out, not a job."));
        return new Persona("dutch", "Dutch", SEAT_RIGHT, 0.50, l);
    }

    /**
     * Quiet, exact, reads a paperback between hands. Speaks rarely; when she
     * does it's the sharpest thing said all night.
     */
    public static Persona priya() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);
        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Three to two. Nice.",
                "That's the one hand the house can't argue with."));
        l.put(TableEvent.PLAYER_BUST, List.of(
                "Mm. Close.",
                "The odds were with you. They just weren't obliged."));
        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer had to draw. That's the whole game, really.",
                "That's what the rules cost them."));
        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Good. That hand was a loser.",
                "Half is better than most of what that was going to do."));
        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance is a side bet on the hole card. Priced in the house's favour.",
                "It's a separate wager wearing a helpful name."));
        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Right spot for it.",
                "That's the correct double."));
        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Nothing to be done about that one.",
                "That was decided before anyone acted."));
        l.put(TableEvent.COLD_STREAK, List.of(
                "Variance. It isn't personal, it just looks it.",
                "Streaks are what randomness looks like up close."));
        l.put(TableEvent.HOT_STREAK, List.of(
                "Enjoy it. It doesn't mean anything, but enjoy it.",
                "The cards don't know you're winning. Still nice though."));
        l.put(TableEvent.LOW_CHIPS, List.of(
                "You're near the end of it. Worth stopping while it's still fun.",
                "That's the stack most of the way down. Good time to think."));
        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Dutch's system resets to zero.",
                "New shoe. Statistically, everything Dutch just said is gone."));
        return new Persona("priya", "Priya", SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> defaults() {
        return List.of(marge(), dutch(), priya());
    }
}
