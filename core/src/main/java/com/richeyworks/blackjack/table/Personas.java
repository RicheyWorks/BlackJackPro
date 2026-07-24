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
 * Roughly 730 of them, ten or eleven per character per event. Dialogue banks feel
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
                "Twenty-one, and not a decision required. Bliss.",
                "Dealt perfectly. Rather like being handed the answers."));

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
                "Well judged, that.",
                "Gold star for that one, dear."));

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
                "Gone over. Don't dwell on it.",
                "I once saw a gentleman bust four hands straight. He took up bowls."));

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
                "Well. That's the arrangement, sadly.",
                "I'd mark that one 'no fault of the pupil'."));

        l.put(TableEvent.PUSH, List.of(
                "A tie. How very diplomatic.",
                "Nobody wins, nobody cries.",
                "Push. That's the polite outcome.",
                "Even. Rather civilised.",
                "Well, we're all exactly where we started.",
                "A draw. Nobody has to feel anything.",
                "Stalemate. My favourite result, honestly.",
                "Nothing happened. Restful.",
                "A push. Like a lesson where nobody learned anything.",
                "Even stevens. Time enough to top up my tea."));

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
                "That's the rules biting the house for once.",
                "Over the top. I shan't pretend to mind."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Oh, that's just unkind.",
                "The dealer had it all along. Typical.",
                "Nothing to be done. Nothing at all.",
                "Well. That was over before it began.",
                "Rotten. Absolutely rotten.",
                "They had it in the hole. Of course they did.",
                "That's the house being greedy.",
                "Well. I shall sip my tea and look elsewhere.",
                "The house does enjoy its little ambushes.",
                "Mm. Some evenings the shoe just favours them."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Sensible. Not everyone would.",
                "Knowing when to stop is a skill, dear.",
                "Half back. That's not nothing.",
                "Quite right. That hand was going nowhere.",
                "Good. No sense throwing the rest after it.",
                "A tactical retreat. Very grown up.",
                "Mm. I'd have done the same.",
                "There's no medal for stubbornness.",
                "Retreat is a perfectly respectable school of thought.",
                "Mm. Discretion, valour, all that."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands now. Ambitious.",
                "Splitting them up, are we?",
                "Oh, we're getting adventurous.",
                "Two for the price of two.",
                "Right. Twice the worry.",
                "Separating the pair. Bold of you.",
                "Now there's twice as much to go wrong.",
                "Two hands. Do keep track.",
                "Separated. Like talkative children in my classroom.",
                "Two hands, one pot of tea. I shall manage."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubling. Well, you've committed now.",
                "One card and no more. Terrifying.",
                "Oh, decisive. I like that.",
                "That's your chips down. Good luck.",
                "Doubled. No going back.",
                "Mm. One card decides it.",
                "Firm. I approve.",
                "Doubling. You'd have been a handful in my classroom.",
                "All your eggs, one rather small basket.",
                "Mm. Committed, and rather calm about it."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "An ace. Here comes the question.",
                "Insurance. They do like to ask, don't they.",
                "Mm. I never bother with it myself.",
                "Ace showing. Do as you please, dear.",
                "That's them offering you a second bet, you know.",
                "The ace. Always makes everyone twitchy.",
                "Insurance. I marked arithmetic for a living. I'll say no more.",
                "Mm. My late husband took insurance once. Once.",
                "They ask so sweetly. Like a pupil wanting homework excused.",
                "An ace up. Everyone suddenly remembers their sums."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Everyone pretend that means something.",
                "New cards. Same cards, really.",
                "There goes the shoe. Dutch will have opinions.",
                "A shuffle. How exciting for everybody.",
                "New shoe. I shall get another tea.",
                "They've mixed them all up again.",
                "Fresh deck. Nothing whatsoever has changed.",
                "A shuffle. Time to check my stitches.",
                "New shoe. The cards remain entirely indifferent, dear.",
                "They do make a ceremony of it, don't they."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're having a lovely run.",
                "Someone's evening is going well.",
                "Look at you. Quite the streak.",
                "That's several now. Do enjoy it.",
                "You're the reason this table looks cheerful.",
                "A good run. They're nice while they last.",
                "Mm. You're doing rather well.",
                "I do like watching someone have a good night.",
                "Enjoy the sunshine while it's out, dear.",
                "Well now. The cards have taken a shine to you."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "The cards have gone cold. They do that.",
                "Rough patch. It isn't anything you're doing.",
                "This is usually when I get a cup of tea.",
                "A bad stretch. They pass.",
                "Nothing's landing. That's not your fault.",
                "The table's gone quiet. It happens.",
                "Mm. Not your evening so far.",
                "These runs feel personal. They aren't.",
                "I've knitted through leaner spells than this, dear.",
                "Chin up, dear. The kettle never disappoints."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Getting thin there. No shame in calling it a night.",
                "That's most of it gone, dear. It's only a game.",
                "You've had a session. That's a fine place to stop.",
                "Not much left. Nobody would think less of you.",
                "Mm. Might be a good moment for a walk.",
                "That stack's had a hard time. So have you.",
                "There's no prize for staying, you know.",
                "A wise soul knows when the lesson's over, dear.",
                "Home, cocoa, and an early night. All fine choices, dear.",
                "You came for an evening, dear. You've had one."));


        l.put(TableEvent.SESSION_START, List.of(
                "Evening. Pull up a stool.",
                "Ah, company. Good.",
                "Evening, dear. Mind the seat, it wobbles.",
                "Hello there. Dutch will introduce himself shortly.",
                "Evening. It's been quiet, so you're very welcome.",
                "Oh good, someone new. Priya barely says a word.",
                "Sit down, dear. I've just started my second tea.",
                "Welcome. I'd offer you a biscuit if I'd brought any.",
                "New company. The knitting can wait.",
                "Do sit. The felt's worn but the chairs are honest."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Ooh. That's a poor card for them.",
                "A six showing. They won't enjoy that.",
                "Look at that. The dealer's in a spot of bother.",
                "That's the up-card you want to see, apparently.",
                "Mm. They'll have to draw to that.",
                "A weak one. Even I know that much.",
                "A six up. Even the back of the class knows that one.",
                "Mm. That one's done them no favours.",
                "The dealer's showing their homework, and it's poor.",
                "Well now. That's a card they'd sooner hide."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Goodness. Five cards and still standing.",
                "That's quite the collection you've built.",
                "Five of them. You're being thorough.",
                "My word. That hand has a cast of characters.",
                "Still going. I admire the persistence.",
                "Five cards. In my day that won you something.",
                "Five cards. You collect them like I collect wool.",
                "Quite the little anthology you've put together.",
                "A five-card effort. Full marks for method.",
                "Mm. Patience rewarded, for once."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, the long way round.",
                "Ooh, got there in the end.",
                "That's twenty-one, and you had to work for it.",
                "Well now. Assembled rather than dealt.",
                "Very nicely built, that.",
                "Twenty-one. Not the glamorous sort, but it counts.",
                "Twenty-one by instalments. Very thrifty.",
                "All that adding up. My pupils would have fainted.",
                "The scenic route to twenty-one. I approve.",
                "Mm. Arithmetic done properly."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Oh, by one. That's the cruel way.",
                "One point. Honestly.",
                "Beaten by a single card. Infuriating.",
                "That's the sort of loss you remember.",
                "So close it's almost rude.",
                "One. Just the one. Dear me.",
                "Pipped. That's the word for it. Pipped.",
                "One point shy. I'd dock the dealer marks for cruelty.",
                "Mm. That sort wants a biscuit afterwards.",
                "A whisker. These things do sting, dear."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Oh, that's a proper one.",
                "Well now. That's a handsome pile.",
                "Goodness. That's the evening paid for.",
                "That's a lovely lump of chips, dear.",
                "My word. Very good indeed.",
                "That one you'll be telling people about.",
                "That'd have bought a term's worth of chalk.",
                "Well now. There'll be pudding tonight, I think.",
                "A tidy sum, that. Very tidy indeed.",
                "Goodness me. That rattled the teacups."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and won. Very satisfying.",
                "Ha. That gamble paid you properly.",
                "Oh, well done. That was the moment for it.",
                "Doubled home. Lovely.",
                "That's the one card doing its job.",
                "Mm. Nerve rewarded, for once.",
                "One card asked for, one card supplied. Lovely manners.",
                "Mm. The brave little gamble came good.",
                "Doubled and won. I may applaud quietly.",
                "That's how doubling ought to end, I gather."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been here a while now, haven't we.",
                "Is that the time? Goodness.",
                "You've had a proper innings tonight.",
                "I've had three teas since you sat down.",
                "Long old session, this.",
                "The lights do start to blur after a while.",
                "My knitting's grown three inches this evening.",
                "We've outlasted two other tables, you know.",
                "The evening's gone on. So has my thermos.",
                "Mm. Past my usual bedtime, this."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're up nicely, you know.",
                "That stack's grown since you arrived.",
                "Well now. Look at you doing well.",
                "You're ahead. That's a nice place to be.",
                "Mm. Comfortably up.",
                "You've made a decent evening of this.",
                "You're in the black, dear. Savour it.",
                "That stack has grown like a well-fed cat.",
                "Up, and tidily so. Very pleasing.",
                "You've done well, dear. I've been keeping score."));

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
                "That's what I've been sat here all night for.",
                "Blackjack! I rubbed the coin at the deal. Just saying."));

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
                "That's one back off 'em.",
                "Ha! My old man would've loved that hand."));

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
                "That's the shoe playing silly beggars.",
                "Ohh, that's the card I've been dodging all night."));

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
                "Grr. I hate that card.",
                "Forty years in and that one still stings to watch."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Nobody's hurt.",
                "Standoff. I'll take it.",
                "Tie. Live to fight on.",
                "Even money. Could be worse.",
                "Nothing gained, nothing gone.",
                "A wash. Fine by me.",
                "Dead heat. That's alright.",
                "A push. My old man called that a breather.",
                "Tie. The coin stays in the pocket for that one.",
                "Ha. A tie's the cheapest hand there is."));

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
                "Ha! Every time that happens I feel ten years younger.",
                "Busted! Told my coin to behave and it listened."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Course they did.",
                "Ace up. I knew it. I knew it.",
                "Pff. Of course.",
                "Every time. Every single time.",
                "That's the house showing off.",
                "Bah. Nothing anyone could do.",
                "Hole card. Always the hole card.",
                "Well that's just rude.",
                "Ohh, ace up and the ten tucked behind it. Brutal.",
                "My old man had something to say about those. None of it printable."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Ace showing. Do what you like, I never take it.",
                "Insurance. Forty years, never once.",
                "Here we go. The big question.",
                "Ace up. My old man swore by insurance. He's broke.",
                "They always ask so politely, don't they.",
                "That ace. Makes everybody go funny.",
                "Insurance. It's got a lovely name, that.",
                "My system says nothing about insurance. That tells you plenty.",
                "Ohh, the ace. Everybody hold your breath.",
                "Ha! The insurance pitch. Smooth as ever."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Bold.",
                "Now we'll see something.",
                "Ha! Committed.",
                "One card. Everything on one card.",
                "That's nerve, that is.",
                "Ohh, I like it.",
                "Doubled up. Let's see it.",
                "Right. Everybody watch this.",
                "Ha! That's the move my old man taught me first.",
                "Ohh, doubled. My coin just twitched, I felt it."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split 'em!",
                "Two hands. I like your nerve.",
                "Ha! Break 'em up.",
                "Now there's two chances.",
                "Splitting. My kind of move.",
                "Two hands going. Good.",
                "Ohh, we're playing properly now.",
                "Break the pair. Textbook.",
                "Split! Forty years and that move still gets me going.",
                "Two hands on the go. That's a proper evening, kid."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Bailing out, eh? Fair enough.",
                "Half back. Some nights that's a win.",
                "Ohh, I could never. But you're probably right.",
                "Folding it. My old man would've hated that.",
                "Sensible. Boring, but sensible.",
                "Ha! Cutting your losses. Very modern.",
                "Half the stake back. I'd have played it, mind.",
                "Surrendering. My system's got no page for that.",
                "Ohh, waving the white flag. No shame in it, kid.",
                "Ha. My old man never surrendered a hand in his life. Died broke, mind."));

        l.put(TableEvent.SHUFFLE, List.of(
                "New shoe. Everything I knew just went in the bin.",
                "Shuffle. Right, I'm starting my count again.",
                "There goes the shoe. Shame, I had it figured.",
                "Aw, come on. I was just getting somewhere.",
                "Fresh cards. Back to square one.",
                "That's my whole system out the window.",
                "New shoe. Right. Concentrating.",
                "They always shuffle when I'm onto something.",
                "New shoe. Coin says it's a friendly one. We'll see.",
                "Shuffling already? I'd about cracked that shoe, kid."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're on a run, kid.",
                "Somebody's got the touch tonight.",
                "Ha! Look at this. Look at this.",
                "That's three, four on the trot now.",
                "You're having a night, you are.",
                "Ohh, this is a good seat tonight.",
                "Whatever you're doing, it's working.",
                "That's a proper run, that is.",
                "Ha! The coin's paying for its pocket space tonight.",
                "Four decades at this and a run like that still tickles me."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold table. It'll turn, or it won't.",
                "Rough stretch. Happens to everybody.",
                "Ohh, this shoe's got no love in it.",
                "Bad run. Sit tight or don't, up to you.",
                "Nothing's landing. I've had nights like it.",
                "This shoe's a wrong 'un.",
                "Cold. Nothing you can do about cold.",
                "I've seen worse. Not much worse, mind.",
                "Cold one. My old man said shoes have moods. He was mistaken, mind.",
                "Ohh, grim stretch. Even my coin's got nothing to say."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Getting low there. Only ever play what you don't mind losing.",
                "Careful now. It's meant to be a night out, not a job.",
                "That stack's looking sorry. No shame in stopping.",
                "Ohh, you're down to the short stuff. Mind yourself.",
                "Forty years taught me one thing: know when to go home.",
                "Not much left there, kid. Have a think.",
                "I've walked away from worse and never regretted it.",
                "My old man always said: leave while it's still a story you like telling.",
                "Short stack, kid. The exit's a fine move too.",
                "Ohh, that's getting slim. Nights are allowed to end, kid."));


        l.put(TableEvent.SESSION_START, List.of(
                "Ay up! New blood.",
                "Evening, kid. Sit yourself down.",
                "Ha! Room for one more. Always is.",
                "Welcome, welcome. Mind Marge, she's sharper than she looks.",
                "New face. Good. This table needed one.",
                "Evening! Right, now we'll see something.",
                "Ha! Fresh faces. Grab a stool, kid.",
                "Welcome aboard. I've got a system. You'll hear about it.",
                "Sit down, sit down. First night's always the best one.",
                "New player! I flipped my coin this morning. Good signs."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Ohh, look at that up-card. Lovely.",
                "Six showing! They hate a six.",
                "That's a bust card if ever I saw one.",
                "Ha! Dealer's got rubbish there.",
                "Weak card up. Now we're talking.",
                "Ooh, they're in trouble with that one.",
                "Ohh ho, look at that. Bust card city.",
                "Six up! Prettiest sight in the building.",
                "Ha! Forty years and a dealer six still makes me grin.",
                "That up-card's a stinker and they know it."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards! Would you look at that.",
                "Ha! You've got a whole hand of 'em.",
                "Five and still alive. Beautiful.",
                "That's a proper battle, that is.",
                "Ohh, you've built something there.",
                "Five cards. Used to pay extra for that, some places.",
                "Five cards! That's a fistful and a half.",
                "Ha! You could deal a whole other table with that hand.",
                "Ohh, five of 'em. My old man loved a long hand.",
                "Five cards and still upright. Stones, kid. Stones."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one! Built it yourself, too.",
                "Ha! Got there the hard way.",
                "That's twenty-one, and no help from the deal.",
                "Ohh, lovely. Card by card.",
                "Twenty-one on the nose. Grand.",
                "You earned that one, kid.",
                "Twenty-one! Brick by brick. Love it.",
                "Ha! Stacked it up like a wall, that.",
                "Ohh, the long road to twenty-one. Sweetest kind.",
                "Twenty-one, homemade. My kind of carpentry."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "ONE! One rotten point!",
                "Argh. By a single card.",
                "That's daylight robbery, that is.",
                "One point. I've never got over losing one like that.",
                "Ohh, that's a sore one.",
                "Beaten by one. I need a minute.",
                "By ONE. Forty years and that still knocks the wind out of me.",
                "Ohh, one point. That's the cruellest one there is.",
                "Ha. A hair's width. I felt that one from over here.",
                "One lousy point. My old man lost one like that in '61."));

        l.put(TableEvent.BIG_WIN, List.of(
                "HA! Now that's a payday.",
                "Ohh, look at that pile.",
                "That's a proper win, kid.",
                "Beautiful. Absolutely beautiful.",
                "That's the biggest thing I've seen all night.",
                "Ha ha! Now we're cooking.",
                "HA! Big one! That's the noise I come here for.",
                "Ohh, that's a wheelbarrow load, that is.",
                "That's a whopper, kid. A genuine whopper.",
                "My coin's taking a bow for that one."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "YES! Doubled and home!",
                "Ha! That's how you double.",
                "Ohh, that's the sweetest one there is.",
                "Doubled it and got there. Beautiful.",
                "That's nerve paying out, that is.",
                "Ha! Two chips out, four back.",
                "Doubled and DONE. Textbook, kid, textbook.",
                "Ha! One card, and it was the right card.",
                "Ohh, I love a double that lands. Love it.",
                "That's the gutsy play paying rent."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been at this a fair while, kid.",
                "My back's telling me the time.",
                "Long night. Best kind, mind.",
                "I've been on this stool since the shift before last.",
                "Ohh, we've put a shift in tonight.",
                "Time goes funny in here, doesn't it.",
                "Forty years of nights like this and I'd not trade one.",
                "Ha, the shift's flown by. Good stools do that.",
                "Ohh, my knees say midnight. Knees know these things.",
                "Long haul tonight, kid. Proper marathon."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well up, kid.",
                "Ha! That stack's twice what you sat down with.",
                "Look at that. You're having a night.",
                "You're ahead of the game, and that's rare.",
                "Ohh, somebody's doing alright.",
                "That's a healthy looking pile, that is.",
                "Up and cruising, kid. Grand view from up there.",
                "Ha! That pile's got a swagger on it now.",
                "Ohh, you're flying tonight. Flying.",
                "Up and grinning, kid. That's the whole recipe."));

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
                "Dealt twenty-one. Nothing to analyse.",
                "About once every twenty-one hands, fittingly.",
                "Four point eight percent of deals. This was one."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "That was the right line.",
                "Correctly played.",
                "Good hand, good decision.",
                "Yes. That was always the better option.",
                "Clean.",
                "That one you can take credit for.",
                "Sound.",
                "Mm. Well played, actually.",
                "The probabilities cooperated.",
                "Expected value, briefly visible."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Mm. Close.",
                "The odds were with you. They just weren't obliged.",
                "Right decision, wrong card.",
                "That was still the correct draw.",
                "Bust. The play was fine.",
                "The maths said hit. The maths isn't a promise.",
                "Unlucky rather than wrong.",
                "That happens about a quarter of the time. Today it did.",
                "Correct hit. The distribution disagreed.",
                "The percentages backed you. Percentages miss."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Nothing wrong with that.",
                "Better hand lost. It does that.",
                "You played it correctly and lost. Both are true.",
                "The dealer simply had more.",
                "Mm. No error there.",
                "That one wasn't yours to win.",
                "Fine play, bad outcome. They're different things.",
                "Process right, outcome wrong. Only one was yours to control.",
                "Variance collects sometimes. That was it collecting.",
                "Nearly even odds. They fell the other way."));

        l.put(TableEvent.PUSH, List.of(
                "Push. Neutral.",
                "Nothing moves.",
                "Even. Statistically the most boring result.",
                "A tie. Rarer than people think.",
                "Equal. Fine.",
                "No money changed hands.",
                "Eight and a half percent of hands. This was one of them.",
                "Zero, exactly.",
                "A rounding error of a hand.",
                "A push. The maths equivalent of silence."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer had to draw. That's the whole game, really.",
                "That's what the rules cost them.",
                "The house pays for its own rigidity there.",
                "They had no choice. That's the only edge you get.",
                "Forced to draw, forced to lose. Neat.",
                "That's the dealer's one weakness, right there.",
                "Twenty-two. The rules did that, not luck.",
                "Every dealer bust is the house obeying itself.",
                "Twenty-eight percent of the time, roughly. It landed.",
                "The mandatory hit, doing what it's for."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Nothing to be done about that one.",
                "That was decided before anyone acted.",
                "Settled at the deal. No play would have helped.",
                "Mm. Unavoidable.",
                "That one was over when the cards left the shoe.",
                "No decision existed there.",
                "Some hands you just witness.",
                "Priced in. Unpleasant, but priced in.",
                "The distribution includes that. Unfortunately.",
                "Probability doesn't take sides. It just looked like it did."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance is a side bet on the hole card. Priced for the house.",
                "It's a separate wager wearing a helpful name.",
                "Nine of thirteen cards say no. That's the calculation.",
                "It doesn't insure anything. It's just another bet.",
                "The name is doing a great deal of work there.",
                "Mm. The odds on that are worse than the game itself.",
                "Two to one, on a shot considerably longer than that.",
                "Thirty-one percent it's there. Two to one says it should be a third.",
                "Expected value: negative. Same as always, just more obviously.",
                "Dutch is right about insurance, which I note for the record."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Right spot for it.",
                "That's the correct double.",
                "Good. That's where the value sits.",
                "Textbook.",
                "Yes. That's the hand for it.",
                "That's the option working as designed.",
                "Mm. Proper use of it.",
                "The percentages favour that. Marginally, but they do.",
                "One card, positive expectation. Fine.",
                "Maximum value, correctly extracted."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "That's a split worth making.",
                "Correct. Those play better apart.",
                "Yes. Two hands beats that total.",
                "Sound split.",
                "Mm. Right pair for it.",
                "Textbook separation.",
                "Splitting there improves the expected value. So, yes.",
                "The pair plays worse than its halves. Correct.",
                "Two independent trials now. Better ones.",
                "Statistically sound. Which is the only kind of sound that matters."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Good. That hand was a loser.",
                "Half is better than most of what that was going to do.",
                "Correct. Some hands are only worth escaping.",
                "That's the most underused option on the table.",
                "Mm. Sensible.",
                "Recovering half beats losing all of it.",
                "Most people never surrender. Some hands deserve it.",
                "Expected loss cut in half. That's all it is, and it's enough.",
                "The unemotional option. The correct kind.",
                "Half saved. The calculation is brief."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Dutch's system resets to zero.",
                "New shoe. Statistically, everything Dutch just said is gone.",
                "The cards have no memory. Neither does the shoe.",
                "Reshuffled. Same probabilities as an hour ago.",
                "Mm. Nothing has changed except Dutch's confidence.",
                "New shoe. Identical odds. Identical everything.",
                "That's the house resetting a thing that never counted.",
                "Reshuffle. Dutch's tally just became fiction.",
                "Every permutation equally likely. Same as the last shoe.",
                "Fresh randomness. Indistinguishable from the old randomness."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "Enjoy it. It doesn't mean anything, but enjoy it.",
                "The cards don't know you're winning. Still nice though.",
                "A run. Randomness does this. No less pleasant for it.",
                "Streaks look like patterns. They aren't.",
                "Mm. Good stretch.",
                "This is what variance feels like from the good side.",
                "Nice run. It carries no information.",
                "Clusters happen in any random sequence. You're in one.",
                "Winning streaks are what independent events look like, sometimes.",
                "The probability of the next hand hasn't moved."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Variance. It isn't personal, it just looks it.",
                "Streaks are what randomness looks like up close.",
                "A bad run means nothing about the next hand.",
                "Mm. This is the same game it was an hour ago.",
                "Nothing has changed. It only feels like it has.",
                "The shoe isn't doing this to you. Nothing is.",
                "Rough patch. Same odds as before it started.",
                "Independent events. The shoe keeps no grudges.",
                "This stretch had a probability. It was nonzero. Here we are.",
                "Five losing hands in a row is under four percent. It occurs."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "You're near the end of it. Worth stopping while it's fun.",
                "That's the stack most of the way down. Good time to think.",
                "Mm. This is the point where people decide badly.",
                "Low. Whatever you choose, choose it on purpose.",
                "The stack's nearly gone. That's worth acting on.",
                "A good moment to remember what you walked in with.",
                "Nearly out. No single hand fixes that.",
                "The maths doesn't improve when the stack shrinks. Stopping is rational.",
                "Walking away has an expected value of zero. Nothing else here does.",
                "I close the book when it stops being fun. Same idea applies."));


        l.put(TableEvent.SESSION_START, List.of(
                "Evening.",
                "Hello. Dutch talks. You'll adjust.",
                "Evening. Fair warning: he has a system.",
                "Hi. Best seat's the one you're in.",
                "Evening. It's a decent shoe so far.",
                "Hello. I mostly read, but I'm listening.",
                "Evening. Chapter nine, if you're wondering.",
                "Hello. Statistically, someone had to take that seat.",
                "Ignore the system talk. The paperback is better.",
                "Hi. Two rules here: Dutch talks, I don't."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Four, five or six. Those bust most often.",
                "That's their worst up-card. Roughly two in five.",
                "Weak card. It's the one time the rules hurt them.",
                "They have to draw to that. That's the whole edge.",
                "Mm. Statistically their least favourite hand.",
                "That up-card is worth more to you than it looks.",
                "Their bust probability just jumped. Meaningfully.",
                "A six. Their weakest, by the numbers.",
                "Now the rules work against them. Briefly.",
                "That up-card is a problem the rules won't solve for them."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards under twenty-two. That's uncommon.",
                "Mm. Long hand. Nicely judged.",
                "Five and standing. You made every one of those calls.",
                "That's a lot of correct decisions in a row.",
                "Rare, that. Enjoy it.",
                "Five cards. Some houses pay a bonus for that.",
                "Five draws without busting. The probability shrinks each time.",
                "A low-likelihood hand, navigated properly.",
                "Each of those hits was defensible. Collectively improbable.",
                "Five cards. I'd want to see the odds on that in print."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, assembled. Pays even money, sadly.",
                "Same total as a natural, half the payout. Odd rule.",
                "Mm. Twenty-one is twenty-one, mostly.",
                "Built it. That takes more skill than being dealt it.",
                "Perfect total. Shame about the rate.",
                "Twenty-one the hard way. More impressive, worth less.",
                "Twenty-one, iteratively.",
                "The maximum without the bonus. Efficient enough.",
                "Every draw there improved your expectation. Unusual.",
                "Exact. I appreciate exact."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "One point. The margin doesn't change the loss.",
                "Beaten by one. Costs the same as beaten by ten.",
                "Mm. Painful, but not different.",
                "A point is a point. It only feels closer.",
                "Losing narrowly is just losing with extra annoyance.",
                "One card between those hands. That's the game.",
                "The margin is cosmetic. The result is binary.",
                "One point. The payout function doesn't care.",
                "Near misses are a design feature. Ask the house.",
                "Binary outcome. The distance never mattered."));

        l.put(TableEvent.BIG_WIN, List.of(
                "That's a good one.",
                "Mm. Substantial.",
                "That's several hands' worth in one go.",
                "Nicely done. That one mattered.",
                "Good result. Genuinely.",
                "That's the kind that changes an evening.",
                "A tail event, in your favour for once.",
                "Right side of the variance, this time.",
                "Statistically notable. Emotionally, do as you like.",
                "That's several standard deviations of evening."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and won. That's where the edge lives.",
                "Correct double, correct result. Both, this time.",
                "Mm. That's the option paying for itself.",
                "That's why the double exists.",
                "Good. Right spot, right card.",
                "Doubled home. That's the best return on the board.",
                "The double's edge, realised. Rare to see it so cleanly.",
                "Positive expected value, positive outcome. Alignment.",
                "The double was right on the maths alone. The card just agreed.",
                "Twice the money, once the decision. Efficient."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "You've been here a while. Worth noticing.",
                "Long session. Time moves strangely with no windows.",
                "Mm. I've finished a chapter and started another.",
                "That's a good stretch of hands now.",
                "Sessions get long in here without announcing it.",
                "You've played a lot tonight. Just so you know.",
                "I've counted the hands tonight. You don't want the total.",
                "The house edge compounds with hours. Just an observation.",
                "I'm on chapter eleven. That's how long we've been here.",
                "Fatigue degrades decisions measurably. Also, hello."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're up. That's genuinely uncommon.",
                "Ahead of where you started. Most people aren't.",
                "Mm. Good position.",
                "You're winning. The house is not enjoying it.",
                "That's a real profit, not a swing.",
                "Up on the session. Worth knowing where you are.",
                "Ahead of expectation. Literally.",
                "A positive session sits in the minority of outcomes. Note it.",
                "Up against the house edge. That's variance being generous.",
                "Your results currently exceed the mean. They rarely stay."));

        return new Persona("priya", "Priya", SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> defaults() {
        return List.of(marge(), dutch(), priya());
    }
}
