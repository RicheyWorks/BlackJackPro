package com.richeyworks.blackjack.table;

/**
 * A moment at the table worth remarking on.
 *
 * <p>These are derived from engine state by the front end and handed to
 * {@link TableChatter}, which decides whether anyone actually says something.
 * Deliberately about <em>what happened</em> rather than what a character should
 * say — the personas own the voice, this owns the trigger.
 */
public enum TableEvent {

    /** The player was dealt a natural. */
    PLAYER_BLACKJACK,

    /** The player beat the dealer. */
    PLAYER_WIN,

    /** The player lost on total, without busting. */
    PLAYER_LOSS,

    /** The player went over 21. */
    PLAYER_BUST,

    /** Tied the dealer. */
    PUSH,

    /** The player took late surrender. */
    PLAYER_SURRENDER,

    /** The player split a pair. */
    PLAYER_SPLIT,

    /** The player doubled down. */
    PLAYER_DOUBLE,

    /** The dealer went over 21. */
    DEALER_BUST,

    /** The dealer turned over a natural. */
    DEALER_BLACKJACK,

    /** Dealer is showing an ace and insurance is on offer. */
    INSURANCE_OFFERED,

    /** The shoe was reshuffled. */
    SHUFFLE,

    /** Several winning rounds in a row. */
    HOT_STREAK,

    /** Several losing rounds in a row. */
    COLD_STREAK,

    /** The player is nearly out of chips. */
    LOW_CHIPS,

    /* ---- moments added so the table has more than the round result to notice ---- */

    /** First hand of a session, or the first after a reset. A greeting. */
    SESSION_START,

    /** Dealer is showing a 4, 5, or 6 — the up-cards that most often bust them. */
    DEALER_WEAK_CARD,

    /** The player drew to five or more cards without going over. */
    FIVE_CARD_HAND,

    /** The player reached 21 the hard way, on three cards or more. */
    TWENTY_ONE,

    /** Lost by a single point, which stings more than losing badly. */
    CLOSE_CALL,

    /** A win large relative to what the player is carrying. */
    BIG_WIN,

    /** A doubled hand came in. */
    DOUBLE_WIN,

    /** The player has been at the table a good while. */
    LONG_SESSION,

    /** The player is well up on where they started. */
    RUNNING_WELL
}
