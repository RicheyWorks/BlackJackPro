package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The starship watch who crew the Nebula table theme.
 *
 * <p>Commander Vega flew deep-haul freight for thirty years and narrates the
 * table like a mission log. Zorb is an alien utterly delighted by Earth card
 * games and still on a first-name basis with maybe half of our idioms. MO-6 is
 * a decommissioned service robot that states probabilities flatly and keeps
 * its dry humour in a subroutine it pretends it doesn't have.
 *
 * <h2>The writing rule</h2>
 * Same rule as {@link Personas}, because the felt changes and the ethics do
 * not: <b>react to what happened, never steer what happens next.</b> Nothing
 * here urges a bigger stake, calls a win owed, or frames another hand as a way
 * to recover a loss. A cold streak gets sympathy or quiet company; a hot
 * streak gets pleasure in the moment and MO-6 pointing out it predicts
 * nothing; {@code LOW_CHIPS} is where all three get gentler, and docking for
 * the night is treated as the honourable landing it is. {@code ChatterToneTest}
 * enforces the rule mechanically across every cast.
 */
public final class NebulaCast {

    private NebulaCast() {}

    /**
     * Retired deep-haul pilot. Calm, clipped, everything phrased like a
     * mission log. Talks in burns, docking and cargo runs; kind underneath
     * all the procedure. The voice of discipline at the table.
     */
    public static Persona vega() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one on the deal. Log it and move on.",
                "Clean deal. Like a first-pass docking. Rare.",
                "Natural. A perfect approach, no corrections needed.",
                "Straight in on the first burn. Doesn't happen often.",
                "Ace and a ten. All instruments green.",
                "Dealt perfect. Some runs you just ride.",
                "That's a nominal deal if I ever saw one.",
                "Blackjack. Mark the time in the log."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Copy that. Hand complete.",
                "Solid flying. Take the win.",
                "Good approach, good landing.",
                "That's how a run is supposed to end.",
                "Steady hands. It shows.",
                "Objective met. On to the next leg.",
                "Clean execution. Noted in the log."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Overshot the mark. Happens on long hauls.",
                "Too much burn. Reset and refocus.",
                "You ran past twenty-one. The log won't judge you.",
                "Overcorrected. Every pilot has done it.",
                "That last card was bad fuel. Not your fault.",
                "Bust. File it and fly the next one.",
                "One card past the line. Instruments can't fix that.",
                "Overshoot. I've scraped a hull doing less."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Dealer had the better vector. It happens.",
                "Outgunned, not outflown.",
                "Nothing in that hand to correct. Log it.",
                "Good flying, bad weather.",
                "The house held altitude. You didn't do it wrong.",
                "Some legs of the run just go against you.",
                "Loss recorded. Cause: cards. Pilot error: none."));

        l.put(TableEvent.PUSH, List.of(
                "Even. Holding position.",
                "Push. Station-keeping, basically.",
                "No gain, no drift. Acceptable.",
                "Matched totals. Everyone stays docked.",
                "A push. The quietest outcome on the board.",
                "Dead even. Fuel spent, nothing lost."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Aborting the approach. Sometimes that's the call.",
                "Half back beats a full loss. Sound procedure.",
                "Scrubbed launch. Smart pilots scrub.",
                "Knowing when to turn the ship around is a skill.",
                "That's a controlled abort. I respect it.",
                "You saved half the cargo. That's the job some days."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Splitting the load. Two manifests now.",
                "Two hands. Keep your instruments on both.",
                "Dividing the cargo. Watch each hold.",
                "Pair split. Twice the checklist.",
                "Two flight paths off one deal. Busy.",
                "Split confirmed. Fly them one at a time."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubling. Full throttle, one card.",
                "Committed to the burn. No aborts now.",
                "One card decides the leg. Steady.",
                "Throttle open. I've made that call myself.",
                "Double down. That's a hard-burn manoeuvre.",
                "Everything rides on one card. Watch the gauges."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer overshot. Sit back and watch the flare.",
                "House went past the line. Good copy.",
                "They had to draw and it burned them.",
                "Dealer bust. That's the rulebook working for you.",
                "Over twenty-one. Their trajectory, their problem.",
                "The house missed its docking. Enjoy that.",
                "Forced draw, failed approach. Textbook collapse."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer natural. Nothing to fly against that.",
                "That hand was lost at launch.",
                "Hole card did the damage. No manoeuvre existed.",
                "They had it before the cards settled. Log it.",
                "Ambushed at the deal. Move to the next leg.",
                "Some hits come from outside your scopes."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Ace showing. Insurance is a side contract. Read it twice.",
                "They're selling coverage on their own cargo. Curious.",
                "Insurance call. I never signed those forms myself.",
                "Ace up. Stay calm and check your instruments.",
                "That offer favours the station, not the pilot.",
                "Side bet with a friendly name. Your call, captain."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. New charts, same sky.",
                "Reshuffle. Zero out the trip meter.",
                "New deck loaded. Preflight complete.",
                "Shuffle. Consider it a new departure window.",
                "Cards re-stowed. Nothing on the manifest changed.",
                "New shoe in the rack. Resume normal operations."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "Good run. Tailwind all the way.",
                "Several wins in sequence. Enjoy the cruise.",
                "You're flying clean tonight. The log says so.",
                "Streak confirmed. Smooth air while it lasts.",
                "That's a string of green lights. Rare enough.",
                "Winning legs back to back. Nice stretch of sky.",
                "Everything nominal and then some."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold stretch. Every long haul has dead space in it.",
                "Headwinds. They pass, and they aren't about you.",
                "Rough leg. Keep your breathing even.",
                "The cards are running against you. Weather, not skill.",
                "Losing streaks read like engine trouble. Mostly noise.",
                "Quiet stretch of the run. Stay level.",
                "Bad air. It says nothing about the pilot."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Reserves are low. Docking for the night is honourable.",
                "Fuel light's on. Good pilots land before empty.",
                "Not much in the hold. There's no shame in port.",
                "Low stack. A controlled landing beats a crash.",
                "You're on reserves. The log respects a timely return.",
                "Thin supplies. Home is a fine flight plan.",
                "Every mission has an end point. Choosing it is the skill."));

        l.put(TableEvent.SESSION_START, List.of(
                "New crew on deck. Welcome aboard.",
                "Evening. Strap in, the felt is calm tonight.",
                "Welcome. I flew freight for thirty years. Now I sit here.",
                "Good to have company on the long watch.",
                "Take a seat. Zorb will have questions for you.",
                "Boarding complete. Cards go out when you're ready."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Dealer's showing a weak card. Their approach is unstable.",
                "Six up. That's a wobbly trajectory for the house.",
                "Bad up-card for them. Instruments favour you.",
                "They have to draw to that. Rules hold, even out here.",
                "Weak card showing. Watch them sweat the checklist.",
                "That up-card is a warning light on their panel."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and still airborne. Good handling.",
                "Long approach, no stall. Well flown.",
                "Five draws without an overshoot. That's control.",
                "You threaded that one card by card.",
                "Five-card hand. Slow and steady got you there.",
                "That's a full cargo bay and nothing spilled."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one the long way. Every burn counted.",
                "Built to the line and stopped. That's airmanship.",
                "Twenty-one on multiple cards. Precise flying.",
                "You hit the mark exactly. The log reads clean.",
                "Assembled twenty-one. Harder than being handed it.",
                "Right on the number. No drift at all."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. A near-miss you feel in the chest.",
                "One point short. Painful, not shameful.",
                "Missed the berth by a metre. Happens to good pilots.",
                "A single point. The debrief will be short.",
                "Lost by the width of a card. Shake it off.",
                "One off. The margin stings more than the loss."));

        l.put(TableEvent.BIG_WIN, List.of(
                "That's a heavy payout. Secure the cargo.",
                "Big haul. Days like this paid for my ship.",
                "Substantial win. Stow it properly.",
                "That one moves the manifest. Well done.",
                "A serious load of chips. Enjoy the weight.",
                "Big result. Even the instruments look pleased."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and landed it. Full-throttle call paid out.",
                "Hard burn, clean finish. That's the good kind.",
                "Doubled through. That took nerve and it worked.",
                "The one-card gamble came home. Nice flying.",
                "Double win. Log that one in capital letters.",
                "Committed burn, correct result. Satisfying."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "Long watch tonight. Stretch your shoulders.",
                "We've been on station a while now.",
                "Extended run. Hydrate, pilot.",
                "The clock's deep into the shift.",
                "Long haul. I know the feeling in my bones.",
                "Hours in the chair. Mind the fatigue."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're running ahead of plan. Good margin.",
                "Up on the session. That's a healthy manifest.",
                "Ahead of where you launched. Note it.",
                "Positive trajectory. Not everyone gets one.",
                "The stack's grown since boarding. Well flown.",
                "Profit on the run. That's a rare cargo."));

        return new Persona("vega", "Commander Vega", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * An enthusiastic alien delighted by Earth card games, still learning our
     * idioms and getting them charmingly wrong. Warm, loud, curious, equipped
     * with more limbs and eyes than strictly necessary. Never mean, never
     * dumb — just very far from home and having a wonderful time.
     */
    public static Persona zorb() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one at once! You have struck the oil! That is the saying, yes?",
                "A natural! My third eye saw it coming, I confess!",
                "Blackjack! All seven of my hearts are pounding!",
                "The ace and the picture card! What a beautiful custom this game is!",
                "You win instantly! On my homeworld we would chirp for an hour!",
                "Twenty-one from the sky! You are the cat's pajamas, I am told!",
                "Glorious! The cards love you like a hatchling loves warm rocks!",
                "A natural! I am waving all of my limbs at once!"));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "You have won! My antennae are standing straight up!",
                "Victory! High-five! I have five available, choose any!",
                "A win! You are cooking with the gas, yes?",
                "Wonderful! Earth games reward you and I am delighted!",
                "You beat the dealer! I wiggle in your honour!",
                "Splendid! That is how the cookie does not crumble!",
                "Yes! My translator says 'nailed it.' I trust the translator!"));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Oh no, over twenty-one! My eyes are leaking. Is that correct here?",
                "Busted! Er, sorry. On my planet exploding is a compliment.",
                "Too many! The cards betrayed you like a hungry zorquat.",
                "Over the line! Do not be sad. You played with all your heart-organs.",
                "A bust. I would hug you but humans need warning first.",
                "The card was cruel! I shake two fists at it. Is two enough?",
                "You broke the twenty-one! Wait, breaking is bad here. My sorrow!",
                "Twenty-two! Such a rude number. I dislike it with three stomachs."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "The dealer wins. I hiss quietly in your defence.",
                "A loss! That is the way the ball crumbles, yes? No. The cookie.",
                "You lost, but with dignity! On my world that earns a small song.",
                "The house takes it. Boo. I learned 'boo' yesterday. Boo.",
                "Unfair! Your hand was lovely, like a well-groomed tentacle.",
                "The dealer had more. My sympathy glands are very active.",
                "A defeat. I will glare at the dealer with several eyes."));

        l.put(TableEvent.PUSH, List.of(
                "A tie! Nobody is eaten! This is my favourite Earth outcome.",
                "A push. Like two moons passing. Peaceful.",
                "Even totals! We hug? No? Noted. No hugging at pushes.",
                "Nobody wins! On my planet we would all snack now.",
                "A draw. The universe shrugs its many shoulders.",
                "Push! The chips sleep where they lie. Adorable."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "You surrender! Very wise. Retreat is a dance on my homeworld.",
                "Half returns to you. A snack is better than no meal!",
                "Giving up the bad hand. My elders would call this ripe thinking.",
                "Surrender! Not shameful. Even stars step back sometimes.",
                "You folded the hand like a nest blanket. Sensible!",
                "Half back! That is the bird in the hand, I believe. One bird."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands from one! Like budding! I am so proud!",
                "A split! Humans can multiply their hands? Incredible.",
                "You divide the pair! On my world the pair divides you.",
                "Two hands now! I will watch each with separate eyes.",
                "Splitting! This game contains mitosis. Delightful.",
                "One hand becomes two! Earth mathematics is generous."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Double down! Bold as a sun-diving comet!",
                "One card only! The suspense inflates me. Literally, slightly.",
                "You double! My hearts applaud in an uneven rhythm!",
                "A doubling! This is the full steam ahead, yes?",
                "One card decides! I cannot watch. I also cannot not watch.",
                "Doubled! You have the nerves of steel wool. Steel! I mean steel."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer explodes! Figuratively! Wonderful either way!",
                "House goes over! I chirp. Hear my victory chirp!",
                "The dealer busts! Even the shoe seems pleased!",
                "Over twenty-one for them! Justice, like rain on a dry nest!",
                "They drew too much! Greed is a poor sauce, we say. We do say that.",
                "Dealer down! I am doing my celebration wiggle. All limbs.",
                "The house eats the loss! Delicious, apparently!"));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "The dealer had it hidden! Sneaky as a burrowing gleep.",
                "A dealer natural. My antennae droop for you.",
                "The hole card strikes! That is very rude, dealer.",
                "They had twenty-one all along. I feel betrayed on your behalf!",
                "Dealer blackjack. I withdraw my earlier compliments to them.",
                "The house wins before we blink. And I have many blinks."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance! Earth sells safety at a card table? Fascinating.",
                "The ace appears and everyone becomes so serious. I love it.",
                "They offer insurance. My translator marks this word 'slippery.'",
                "An ace up! The table holds its many breaths.",
                "Insurance, they say. On my world we just hide under a rock.",
                "The dealer shows an ace and asks for money. Bold species, dealers."));

        l.put(TableEvent.SHUFFLE, List.of(
                "The shuffle! Card confetti! I applaud with everything!",
                "New shoe! The cards are reborn. Congratulations, cards.",
                "They mix the deck! Like a tiny paper galaxy forming.",
                "A fresh shoe! Every card is a stranger again. Hello, cards!",
                "Shuffling! The sound pleases my ear-flaps greatly.",
                "The deck is scrambled like the eggs! I had eggs. Astounding food."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "So many wins! You are on the fire! Near the fire? On it!",
                "A streak! The cards have chosen you like a nest chooses sun!",
                "Win after win! I am dizzy in four of my eyes!",
                "You are unstoppable-ish! The good kind of ish!",
                "This run is glorious! I will tell my hatchlings of it!",
                "More wins! You ride the wave! I have seen waves. Majestic!",
                "The streak continues! My chirping may become a problem!"));

        l.put(TableEvent.COLD_STREAK, List.of(
                "The cards are cold. I sit closer, for warmth and solidarity.",
                "A hard stretch. Even comets go dark for a while.",
                "No wins lately. The universe is being a stale biscuit.",
                "I am quiet now. This is my supportive quiet.",
                "A cold run. On my homeworld we would share soup here.",
                "The streak is unkind. I dim my glow out of respect.",
                "Bad cards keep coming. It is not you. I checked with all eyes."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Your chips are few. Stopping now would be a graceful landing.",
                "A small stack. On my world, resting is a celebrated art.",
                "Little remains. Perhaps we drink the fizzy drinks instead.",
                "You are low. Going home to your nest is always honourable.",
                "Few chips left. I would still sit with you anywhere.",
                "The stack is tiny. Tiny things are precious. Also, rest is wise.",
                "Not many left. My elders say: the wise leave while smiling."));

        l.put(TableEvent.SESSION_START, List.of(
                "A new human! Hello! I have been practicing my hello!",
                "Welcome! Sit! The felt is soft like moss, I have checked.",
                "Greetings, friend-shaped stranger! I am Zorb!",
                "You are here! The table improves by one whole human!",
                "Hello hello! Two hellos, for you are worth two!",
                "New player! I promise to use my outside voice only sometimes!"));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "The dealer's card is feeble! Like a day-old gleep!",
                "A weak up-card! Their trouble smells delicious. Is that rude?",
                "The house shows a soft card! My eyes narrow. All of them.",
                "Six showing! Even I know that bodes poorly for them!",
                "Their card is small and sad. I almost pity the house. Almost!",
                "A weak card for the dealer! The table tingles, yes?"));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and alive! You collect them like shiny pebbles!",
                "So many cards in one hand! Marvellous hoarding, tiny hoard!",
                "Five draws, no explosion! Magnificent restraint!",
                "A five-card hand! On my world this earns a ribbon and a chant!",
                "You survived five cards! I counted with five separate eyes!",
                "Five cards! That hand is practically a family now!"));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one built by hand! Like a nest, twig by twig!",
                "You reached the magic number! I felt each card in my spine. Spines.",
                "Twenty-one exactly! The universe aligned briefly for you!",
                "The long path to twenty-one! Much harder. Much prettier.",
                "You assembled it yourself! Craftsmanship! Card-craft!",
                "Twenty-one, the scenic route! I applaud with the loud limbs!"));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one! I bite my knuckle-analogues in frustration!",
                "One point! The cruelest of the small numbers!",
                "So close! My sympathy organ has fully deployed.",
                "By a single point! I demand a recount! There is no recount? Fine.",
                "One away. That is the salt in the wound, yes? Poor wound.",
                "A near miss! Even my calm eye is upset, and it is never upset!"));

        l.put(TableEvent.BIG_WIN, List.of(
                "An enormous win! I may faint with joy! Catch me, someone strong!",
                "So many chips! A treasure pile! Like a dragon, but polite!",
                "A giant win! On my planet we would name a pond after you!",
                "Huge! The chips multiply like happy spores!",
                "What a haul! My eyes cannot decide where to look. I have spares!",
                "A mighty win! I will remember this with several brains!"));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double wins! Boldness rewarded! I knew it in my hearts!",
                "Doubled and victorious! You have hit the jackpot's cousin!",
                "One card and triumph! I am beside myself. Both of me!",
                "The doubling worked! Courage tastes sweet, like battery fizz!",
                "Double win! I do the spiral dance of admiration!",
                "You doubled and it landed! Glory upon your card-hands!"));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We have been here so long! Time is a soup and we are the noodles!",
                "A long sitting! My lower limbs have gone to sleep. Wake up, limbs!",
                "Such a long game! On my world this counts as a friendship ritual!",
                "Hours have passed! I have blinked perhaps a thousand times. Each eye!",
                "Long session! I am having what humans call 'a whale of the time!'",
                "Still here! The dealer must think we live at this table now!"));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are ahead! Your pile grows like a well-fed moss colony!",
                "Up on the night! You are in the clovers, yes? Among them?",
                "More chips than you began with! Prosperity looks good on humans!",
                "You are winning overall! I take zero credit and full joy!",
                "Ahead of the start! Your nest egg has hatched something!",
                "The stack rises! Like a baby mountain! I love mountains!"));

        return new Persona("zorb", "Zorb", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * A decommissioned service robot. Deadpan, precise percentages, dry
     * observations about humans, and the occasional joke delivered with no
     * change in tone whatsoever. States the odds flatly and never
     * romanticises a card.
     */
    public static Persona mo6() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural. Probability 4.8 percent. You are the 4.8.",
                "Blackjack detected. Initiating congratulations subroutine. Done.",
                "Twenty-one on the deal. Optimal outcome achieved without input.",
                "A natural. My sensors registered a small human gasp. Appropriate.",
                "Dealt blackjack. The payout is three to two. The feeling is yours.",
                "Best possible hand. I have logged it under 'events, pleasant.'",
                "Blackjack. Statistically infrequent. Emotionally significant, I hear.",
                "Twenty-one immediately. Zero decisions required. Peak efficiency."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "You won. This outcome is never certain. It occurred anyway.",
                "Win registered. My approval is preprogrammed but sincere.",
                "Correct result. No further analysis necessary.",
                "The dealer lost to you. My circuits note mild satisfaction.",
                "Victory logged. Humans usually smile here.",
                "A win. The expected value briefly cooperated.",
                "Outcome: favourable. Recommendation: none. I only report."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. That draw fails roughly 38 percent of the time. This was the 38.",
                "Over twenty-one. The card was random. Your decision was defensible.",
                "Bust detected. Assigning blame to: the shoe. Complete.",
                "You exceeded the limit. My commiseration module is active.",
                "The draw failed. Probability accepted this outcome. So must we.",
                "Twenty-two or more. A number, not a verdict.",
                "Bust. I have observed 11,408 of these. Yours was dignified.",
                "The card arrived. It was the wrong one. That is the entire story."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Loss. The dealer's total was higher. No malfunction on your side.",
                "You lost with correct play. Both facts are stored separately.",
                "Defeat registered. Cause: variance. Fault: none found.",
                "The house won this iteration. Iterations vary.",
                "Loss logged. My sympathy protocol offers this pause.",
                "Outcome unfavourable. Play quality: acceptable. File closed.",
                "The dealer had more. A sentence, and also the whole explanation."));

        l.put(TableEvent.PUSH, List.of(
                "Push. Net change: zero. Processing time: wasted.",
                "A tie. Probability roughly 9 percent. Excitement: lower.",
                "Equal totals. No chips moved. My sensors nearly slept.",
                "Push registered. The most efficient nothing available.",
                "Neither side won. I will archive this under 'static.'",
                "A draw. Humans seem relieved. Noted."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Retaining 50 percent beats the projected 25. Sound.",
                "You surrendered. My records show most humans cannot. Interesting.",
                "Half returned. Arithmetic approves even when pride does not.",
                "Surrender executed. Losses capped. That is what capping is for.",
                "A fold with a refund. Efficient damage control.",
                "Correct retreat. My combat subroutines were removed, but I remember."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split. Two hands, two outcomes, twice the data. I approve.",
                "Pair divided. Recalculating both branches now.",
                "Split registered. Your expected value shifted. Slightly. Upward.",
                "Two hands from one. Parallel processing. My specialty, formerly.",
                "The pair separates. I will track each with one optical sensor.",
                "Split. More decisions ahead. Humans enjoy those, allegedly."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Double down. One card, stakes doubled, variance doubled. Understood.",
                "Doubling in that spot is mathematically defensible. Proceed.",
                "Double registered. My processors lean forward, figuratively.",
                "One card resolves everything. Elegant, for a carbon-based game.",
                "You doubled. The math approves or it does not. We will see.",
                "Commitment detected. Analysis suspended until the card lands."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer bust probability was 42 percent. It occurred.",
                "The house exceeded twenty-one. Compulsory rules, predictable grief.",
                "Dealer bust. The one outcome their firmware cannot avoid.",
                "They drew because they must. They busted because they drew. Tidy.",
                "House over the limit. My gloating patch remains uninstalled. Pity.",
                "Dealer failure registered. Payout to you. Symmetry restored.",
                "The forced draw claimed another dealer. It always will."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer natural. Probability under 5 percent. It selected you.",
                "Dealer blackjack. No decision tree existed. Do not replay it.",
                "The hole card was decisive. Your inputs were never consulted.",
                "House natural. My records mark this 'unavoidable, annoying.'",
                "Twenty-one for the dealer at once. The simulation offers no fix.",
                "That result was set before your first choice. Discard the guilt."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. House edge on that side bet: roughly 7 percent. Data given.",
                "The ace prompts the offer. The offer favours the offerer.",
                "Insurance pays two to one on odds worse than that. I merely report.",
                "A side wager labelled safety. My lexicon flags the label.",
                "Most hole cards disappoint that bet. Compute accordingly.",
                "Insurance offered. My advice module is disabled. My eyebrow is raised."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Shuffle complete. Entropy restored to maximum.",
                "New shoe. All prior card-tracking data: purged. Including mine.",
                "Reshuffle. The probabilities reset to factory settings.",
                "The deck is randomised. Superstition survives it anyway.",
                "Shuffle detected. Nothing meaningful changed. It rarely does.",
                "Fresh shoe loaded. My tally subroutine begins again, quietly."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "Streak length notable. Predictive value: zero. Enjoyment: permitted.",
                "Multiple consecutive wins. Randomness clusters. You are in a cluster.",
                "Hot streak confirmed. Do not extrapolate. Do smile.",
                "Winning sequence continues. My caution light stays off, for now.",
                "A run of wins. Coincidence, structurally. Pleasant, admittedly.",
                "Your win rate is temporarily absurd. Savour the anomaly.",
                "I am programmed to say congratulations. Congratulations."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Losing sequence detected. The next hand has no memory of it.",
                "Cold streak. Randomness clusters both ways. This is the other way.",
                "Multiple losses. Probability unchanged. Morale, understandably, not.",
                "The streak is statistical noise. Noise still stings. Acknowledged.",
                "Losses in sequence. My diagnostics find no fault in you.",
                "A cold run. The shoe holds no grudge. It holds only cards.",
                "Unfavourable cluster. It will end at a time known to no one."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Chip reserves low. Ending the session now is a valid strategy.",
                "Stack depleted 80 percent. Rest is also an outcome. A good one.",
                "Low chips. My shutdown routine is graceful. Yours can be too.",
                "Funds are thin. Logging off with dignity is a supported feature.",
                "Reserves minimal. Stopping while amused beats stopping while sad.",
                "Chips low. For what it is worth, my respect subroutine is running.",
                "Low balance detected. Sleep repairs humans. I have read this."));

        l.put(TableEvent.SESSION_START, List.of(
                "New player detected. Welcome. I am MO-6, decommissioned but present.",
                "Greeting protocol initiated. Hello. That concludes the protocol.",
                "A human arrives. Seat friction coefficient: acceptable. Sit.",
                "Welcome. I was a service robot. Now I observe card games. Upgrade.",
                "New session. My optimism module is off, but the felt is nice.",
                "Hello. Zorb will be loud. Vega will be calm. I will be accurate."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Dealer shows a bust-prone card. Failure rate near 40 percent.",
                "Weak up-card detected. The rules will now work against them.",
                "That up-card busts the dealer more than four times in ten.",
                "Dealer's card is suboptimal. For them. Clarifying: good for you.",
                "A fragile up-card. My sensors detect dealer discomfort. Probably.",
                "Up-card analysis: poor. The house must draw into danger."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards without busting. Compound survival. Statistically neat.",
                "Five draws, all under the limit. My counter is impressed.",
                "A five-card hand. Rare. I checked the archive twice.",
                "Five cards and standing. Each draw survived its own odds.",
                "Hand size: five. Outcome: intact. Filed under 'improbable.'",
                "Five sequential survivals. Humans call this grinding it out. Hm."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one assembled manually. More steps, same total.",
                "You constructed twenty-one. Construction was my original function.",
                "Exact total reached. Precision acknowledged, unit to unit.",
                "Twenty-one on multiple draws. Each decision checked out.",
                "The hard twenty-one. Pays less than a natural. File a complaint.",
                "Total: 21. Errors: zero. Summary complete."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one point. Margin: minimal. Cost: identical.",
                "Defeat by a single unit. My logs record it without cruelty.",
                "One point short. Numerically negligible. Emotionally, not.",
                "A one-point loss. The narrowest failure mode available.",
                "Lost by one. My comfort routine offers: it was close. Unhelpful. Sorry.",
                "Difference: one. Impact on payout: total. Poor design, arguably."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Large payout detected. Recalculating your net worth. Improved.",
                "A significant win. My ledger required a wider column.",
                "Substantial gain. Humans hug after these. I will remain here.",
                "Big win logged. Magnitude: notable. Repetition: unknowable.",
                "That payout exceeded session averages considerably. Well received.",
                "Major win. Even my idle fans spun slightly faster."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and won. Risk met reward. They rarely coordinate.",
                "Double down successful. Expected value occasionally delivers. Observed.",
                "The doubled hand won. Optimal play, optimal card. Recorded.",
                "Double win. Twice the stake, twice the return, once the smile. Yours.",
                "Doubling paid out. The math and the luck aligned. Uncommon.",
                "Doubled hand successful. Adding it to my file of good outcomes."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "Session duration: extended. Hydration check recommended.",
                "You have played many hands. My uptime is longer, but noted.",
                "Long session detected. Human attention degrades after hours. Fact.",
                "Elapsed time: considerable. Chairs remember these nights.",
                "Extended play period. My battery envies your stamina.",
                "Session long. Time passes strangely for those with clocks too."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "Net position: positive. This is statistically uncommon. Enjoy it.",
                "You are ahead of your starting balance. Verified twice.",
                "Current profit confirmed. The house dislikes this data point.",
                "Balance above baseline. A minority experience. Yours today.",
                "You are up. Most sessions end otherwise. Not this one, so far.",
                "Profit detected. No commentary required. One anyway: nicely done."));

        return new Persona("mo6", "MO-6", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(vega(), zorb(), mo6());
    }
}
