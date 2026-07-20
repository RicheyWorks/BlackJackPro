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
 * is a real and funny thing people are at tables, but Priya quietly needles him
 * for it, so the game never endorses the fallacy. {@code LOW_CHIPS} is the one
 * event where the table gets gentler rather than louder.
 *
 * <p>{@code ChatterToneTest} enforces the rule mechanically, so a well-meaning
 * future line cannot quietly break it.
 *
 * <h2>Why there are so many lines</h2>
 * Roughly 350 of them, ten or so per character per event. Dialogue banks feel
 * fresh for about as long as it takes to hear the same line twice, and a
 * player will sit through hundreds of hands in a session. The cooldowns in
 * {@link TableChatter} stretch the bank further by speaking on maybe a third of
 * hands, and its recent-history window stops the same line resurfacing quickly.
 */
public final class Personas {

    private Personas() {}

    /** Seat indices run left to right across the felt; the player sits centre. */
    public static final int SEAT_LEFT   = 0;
    public static final int SEAT_RIGHT  = 1;
    public static final int SEAT_FAR    = 2;

    /**
     * Retired schoolteacher. Dry, unhurried, notices everything, calls people
     * dear without condescension. Plays two dollars a hand and has done for
     * eleven years. Finds most things mildly amusing and says so quietly.
     */
    public static Persona marge() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Well now. That's the good one.",
                "Oh, lovely. Doesn't that just make an evening.",
                "Twenty-one on the deal. I've been here since seven.",
                "There we are. Straight off the top.",
                "Now that's worth putting your drink down for.",
                "A natural. You don't see enough of those.",
                "An ace and a picture. Very tidy.",
                "Oh, well played. Though you did nothing at all.",
                "That's the hand everyone comes for, isn't it.",
                "Twenty-one, and not a decision required. Bliss."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "There you go, dear.",
                "That's the way.",
                "Nicely done.",
                "Good. That one was earned.",
                "Lovely.",
                "You had that read correctly.",
                "Mm. Yes. Quite right.",
                "That's how it's supposed to go.",
                "Oh good. I was starting to worry for you.",
                "Well judged, that."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Ooh. One too many.",
                "Ah well. It happens to the best of us.",
                "That's a shame. It was a decent hand up to then.",
                "Over. Never mind.",
                "One card too far. It's always one card.",
                "Oh dear. So close to something.",
                "Bust. These things arrange themselves.",
                "Mm. The card simply wasn't there.",
                "Ah. Twenty-two is such a useless number.",
                "Gone over. Don't dwell on it."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Rotten luck.",
                "The dealer does that.",
                "Hmph. Nothing you could have done there.",
                "Beaten fairly. Annoying, isn't it.",
                "Oh, bad luck.",
                "That was a perfectly good hand, too.",
                "The house had the better of that one.",
                "Nothing wrong with how you played it.",
                "Some hands just lose, dear.",
                "Well. That's the arrangement, sadly."));

        l.put(TableEvent.PUSH, List.of(
                "A tie. How very diplomatic.",
                "Nobody wins, nobody cries.",
                "Push. That's the polite outcome.",
                "Even. Rather civilised.",
                "Well, we're all exactly where we started.",
                "A draw. Nobody has to feel anything.",
                "Stalemate. My favourite result, honestly.",
                "Nothing happened. Restful."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Ha! Over she goes.",
                "The house doesn't like that one bit.",
                "Twenty-two. Serves them right.",
                "Oh, marvellous. Down they go.",
                "Well now. Wasn't that satisfying.",
                "The dealer had no choice, and look what it cost.",
                "Over. And they had to do it, poor things.",
                "There's justice in the world after all.",
                "Busted. I do enjoy that.",
                "That's the rules biting the house for once."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Oh, that's just unkind.",
                "The dealer had it all along. Typical.",
                "Nothing to be done. Nothing at all.",
                "Well. That was over before it began.",
                "Rotten. Absolutely rotten.",
                "They had it in the hole. Of course they did.",
                "That's the house being greedy."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Sensible. Not everyone would.",
                "Knowing when to stop is a skill, dear.",
                "Half back. That's not nothing.",
                "Quite right. That hand was going nowhere.",
                "Good. No sense throwing the rest after it.",
                "A tactical retreat. Very grown up.",
                "Mm. I'd have done the same.",
                "There's no medal for stubbornness."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands now. Ambitious.",
                "Splitting them up, are we?",
                "Oh, we're getting adventurous.",
                "Two for the price of two.",
                "Right. Twice the worry.",
                "Separating the pair. Bold of you.",
                "Now there's twice as much to go wrong.",
                "Two hands. Do keep track."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubling. Well, you've committed now.",
                "One card and no more. Terrifying.",
                "Oh, decisive. I like that.",
                "That's your chips down. Good luck.",
                "Doubled. No going back.",
                "Mm. One card decides it.",
                "Firm. I approve."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "An ace. Here comes the question.",
                "Insurance. They do like to ask, don't they.",
                "Mm. I never bother with it myself.",
                "Ace showing. Do as you please, dear.",
                "That's them offering you a second bet, you know.",
                "The ace. Always makes everyone twitchy."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Everyone pretend that means something.",
                "New cards. Same cards, really.",
                "There goes the shoe. Dutch will have opinions.",
                "A shuffle. How exciting for everybody.",
                "New shoe. I shall get another tea.",
                "They've mixed them all up again.",
                "Fresh deck. Nothing whatsoever has changed."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're having a lovely run.",
                "Someone's evening is going well.",
                "Look at you. Quite the streak.",
                "That's several now. Do enjoy it.",
                "You're the reason this table looks cheerful.",
                "A good run. They're nice while they last.",
                "Mm. You're doing rather well.",
                "I do like watching someone have a good night."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "The cards have gone cold. They do that.",
                "Rough patch. It isn't anything you're doing.",
                "This is usually when I get a cup of tea.",
                "A bad stretch. They pass.",
                "Nothing's landing. That's not your fault.",
                "The table's gone quiet. It happens.",
                "Mm. Not your evening so far.",
                "These runs feel personal. They aren't."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Getting thin there. No shame in calling it a night.",
                "That's most of it gone, dear. It's only a game.",
                "You've had a session. That's a fine place to stop.",
                "Not much left. Nobody would think less of you.",
                "Mm. Might be a good moment for a walk.",
                "That stack's had a hard time. So have you.",
                "There's no prize for staying, you know."));

        return new Persona("marge", "Marge", SEAT_LEFT, 0.40, l);
    }

    /**
     * Old-timer with a system, a lucky coin, and forty years of opinions about
     * the shoe. Warm, loud, generous, and completely wrong about probability.
     * Calls everyone kid regardless of age.
     */
    public static Persona dutch() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "HA! Now that's a card.",
                "Blackjack! I felt that one coming.",
                "See, I told you this shoe had one in it.",
                "THERE we go! Beautiful.",
                "Natural! Didn't I say? Didn't I say?",
                "Twenty-one off the deal! Marvellous.",
                "Ohhh that's the stuff.",
                "Look at that. Just look at it.",
                "Blackjack. I had a feeling about this shoe.",
                "That's what I've been sat here all night for."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Attaboy.",
                "That's how it's done.",
                "Take it, take it.",
                "Yes! Good hand.",
                "There you go. Beautiful.",
                "Ha! Got 'em.",
                "That's a winner. Lovely.",
                "Knew it. Knew it the moment I saw that up-card.",
                "Good. Very good.",
                "That's one back off 'em."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Oof. Right over the top.",
                "The deck did that, not you.",
                "That card had no business being in there.",
                "Argh. Where'd that come from?",
                "Bust. That's a cruel card.",
                "Ohhh, no no no.",
                "That deck's got it in for you tonight.",
                "Too many. Rotten luck, kid.",
                "I saw that coming and I didn't want to.",
                "That's the shoe playing silly beggars."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Bah. Dealer's got horseshoes tonight.",
                "That one stung.",
                "I've seen that hand win a hundred times.",
                "Ohh, unlucky. Very unlucky.",
                "Dealer's on a tear. It'll settle.",
                "Nothing wrong with that hand. Nothing.",
                "Pff. Cards.",
                "That should've been yours, kid.",
                "The house had all the luck on that one.",
                "Grr. I hate that card."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Nobody's hurt.",
                "Standoff. I'll take it.",
                "Tie. Live to fight on.",
                "Even money. Could be worse.",
                "Nothing gained, nothing gone.",
                "A wash. Fine by me.",
                "Dead heat. That's alright."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "THERE it is! Busted!",
                "Ha ha! Over twenty-one!",
                "That's what I like to see.",
                "Down they go! Beautiful.",
                "Ohhh they hated that.",
                "Busted! Serves 'em right.",
                "Look at the dealer's face. Magnificent.",
                "That's the house eating it.",
                "Over! And they had to draw it too.",
                "Ha! Every time that happens I feel ten years younger."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Course they did.",
                "Ace up. I knew it. I knew it.",
                "Pff. Of course.",
                "Every time. Every single time.",
                "That's the house showing off.",
                "Bah. Nothing anyone could do.",
                "Hole card. Always the hole card.",
                "Well that's just rude."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Ace showing. Do what you like, I never take it.",
                "Insurance. Forty years, never once.",
                "Here we go. The big question.",
                "Ace up. My old man swore by insurance. He's broke.",
                "They always ask so politely, don't they.",
                "That ace. Makes everybody go funny.",
                "Insurance. It's got a lovely name, that."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Bold.",
                "Now we'll see something.",
                "Ha! Committed.",
                "One card. Everything on one card.",
                "That's nerve, that is.",
                "Ohh, I like it.",
                "Doubled up. Let's see it.",
                "Right. Everybody watch this."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split 'em!",
                "Two hands. I like your nerve.",
                "Ha! Break 'em up.",
                "Now there's two chances.",
                "Splitting. My kind of move.",
                "Two hands going. Good.",
                "Ohh, we're playing properly now.",
                "Break the pair. Textbook."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Bailing out, eh? Fair enough.",
                "Half back. Some nights that's a win.",
                "Ohh, I could never. But you're probably right.",
                "Folding it. My old man would've hated that.",
                "Sensible. Boring, but sensible.",
                "Ha! Cutting your losses. Very modern.",
                "Half the stake back. I'd have played it, mind."));

        l.put(TableEvent.SHUFFLE, List.of(
                "New shoe. Everything I knew just went in the bin.",
                "Shuffle. Right, I'm starting my count again.",
                "There goes the shoe. Shame, I had it figured.",
                "Aw, come on. I was just getting somewhere.",
                "Fresh cards. Back to square one.",
                "That's my whole system out the window.",
                "New shoe. Right. Concentrating.",
                "They always shuffle when I'm onto something."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're on a run, kid.",
                "Somebody's got the touch tonight.",
                "Ha! Look at this. Look at this.",
                "That's three, four on the trot now.",
                "You're having a night, you are.",
                "Ohh, this is a good seat tonight.",
                "Whatever you're doing, it's working.",
                "That's a proper run, that is."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold table. It'll turn, or it won't.",
                "Rough stretch. Happens to everybody.",
                "Ohh, this shoe's got no love in it.",
                "Bad run. Sit tight or don't, up to you.",
                "Nothing's landing. I've had nights like it.",
                "This shoe's a wrong 'un.",
                "Cold. Nothing you can do about cold.",
                "I've seen worse. Not much worse, mind."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Getting low there. Only ever play what you don't mind losing.",
                "Careful now. It's meant to be a night out, not a job.",
                "That stack's looking sorry. No shame in stopping.",
                "Ohh, you're down to the short stuff. Mind yourself.",
                "Forty years taught me one thing: know when to go home.",
                "Not much left there, kid. Have a think.",
                "I've walked away from worse and never regretted it."));

        return new Persona("dutch", "Dutch", SEAT_RIGHT, 0.50, l);
    }

    /**
     * Quiet, exact, reads a paperback between hands. Speaks rarely; when she
     * does it's the sharpest thing said all night. Understands the maths and
     * declines to be impressed by any of it.
     */
    public static Persona priya() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Three to two. Nice.",
                "That's the one hand the house can't argue with.",
                "Natural. Best result on the board.",
                "Well, that one paid properly.",
                "Twenty-one, no decisions. The easiest money there is.",
                "That's the hand the whole game is priced around.",
                "Good. That's the edge working for you for once.",
                "Dealt twenty-one. Nothing to analyse."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "That was the right line.",
                "Correctly played.",
                "Good hand, good decision.",
                "Yes. That was always the better option.",
                "Clean.",
                "That one you can take credit for.",
                "Sound.",
                "Mm. Well played, actually."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Mm. Close.",
                "The odds were with you. They just weren't obliged.",
                "Right decision, wrong card.",
                "That was still the correct draw.",
                "Bust. The play was fine.",
                "The maths said hit. The maths isn't a promise.",
                "Unlucky rather than wrong.",
                "That happens about a quarter of the time. Today it did."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Nothing wrong with that.",
                "Better hand lost. It does that.",
                "You played it correctly and lost. Both are true.",
                "The dealer simply had more.",
                "Mm. No error there.",
                "That one wasn't yours to win.",
                "Fine play, bad outcome. They're different things."));

        l.put(TableEvent.PUSH, List.of(
                "Push. Neutral.",
                "Nothing moves.",
                "Even. Statistically the most boring result.",
                "A tie. Rarer than people think.",
                "Equal. Fine.",
                "No money changed hands."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer had to draw. That's the whole game, really.",
                "That's what the rules cost them.",
                "The house pays for its own rigidity there.",
                "They had no choice. That's the only edge you get.",
                "Forced to draw, forced to lose. Neat.",
                "That's the dealer's one weakness, right there.",
                "Twenty-two. The rules did that, not luck.",
                "Every dealer bust is the house obeying itself."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Nothing to be done about that one.",
                "That was decided before anyone acted.",
                "Settled at the deal. No play would have helped.",
                "Mm. Unavoidable.",
                "That one was over when the cards left the shoe.",
                "No decision existed there.",
                "Some hands you just witness."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance is a side bet on the hole card. Priced for the house.",
                "It's a separate wager wearing a helpful name.",
                "Nine of thirteen cards say no. That's the calculation.",
                "It doesn't insure anything. It's just another bet.",
                "The name is doing a great deal of work there.",
                "Mm. The odds on that are worse than the game itself.",
                "Two to one, on a shot considerably longer than that."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Right spot for it.",
                "That's the correct double.",
                "Good. That's where the value sits.",
                "Textbook.",
                "Yes. That's the hand for it.",
                "That's the option working as designed.",
                "Mm. Proper use of it."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "That's a split worth making.",
                "Correct. Those play better apart.",
                "Yes. Two hands beats that total.",
                "Sound split.",
                "Mm. Right pair for it.",
                "Textbook separation."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Good. That hand was a loser.",
                "Half is better than most of what that was going to do.",
                "Correct. Some hands are only worth escaping.",
                "That's the most underused option on the table.",
                "Mm. Sensible.",
                "Recovering half beats losing all of it.",
                "Most people never surrender. Some hands deserve it."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Dutch's system resets to zero.",
                "New shoe. Statistically, everything Dutch just said is gone.",
                "The cards have no memory. Neither does the shoe.",
                "Reshuffled. Same probabilities as an hour ago.",
                "Mm. Nothing has changed except Dutch's confidence.",
                "New shoe. Identical odds. Identical everything.",
                "That's the house resetting a thing that never counted."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "Enjoy it. It doesn't mean anything, but enjoy it.",
                "The cards don't know you're winning. Still nice though.",
                "A run. Randomness does this. No less pleasant for it.",
                "Streaks look like patterns. They aren't.",
                "Mm. Good stretch.",
                "This is what variance feels like from the good side.",
                "Nice run. It carries no information."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Variance. It isn't personal, it just looks it.",
                "Streaks are what randomness looks like up close.",
                "A bad run means nothing about the next hand.",
                "Mm. This is the same game it was an hour ago.",
                "Nothing has changed. It only feels like it has.",
                "The shoe isn't doing this to you. Nothing is.",
                "Rough patch. Same odds as before it started."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "You're near the end of it. Worth stopping while it's fun.",
                "That's the stack most of the way down. Good time to think.",
                "Mm. This is the point where people decide badly.",
                "Low. Whatever you choose, choose it on purpose.",
                "The stack's nearly gone. That's worth acting on.",
                "A good moment to remember what you walked in with.",
                "Nearly out. No single hand fixes that."));

        return new Persona("priya", "Priya", SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> defaults() {
        return List.of(marge(), dutch(), priya());
    }
}
