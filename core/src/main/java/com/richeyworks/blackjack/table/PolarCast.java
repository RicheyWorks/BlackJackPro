package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The cast for the polar tables — "Glacier" (arctic ice) and "Aurora"
 * (polar night) share these three: Ingrid the explorer, Sunny the musher,
 * and Dr. Frost the aurora physicist.
 *
 * <p>The writing rule from {@link Personas} applies here unchanged:
 * <b>react to what happened, never steer what happens next.</b> Nobody at
 * this table urges a bigger stake, calls a win owed, or frames another hand
 * as the way back from a loss. A cold streak draws sympathy or a story about
 * worse weather; {@code LOW_CHIPS} draws the gentlest lines in the bank —
 * making camp for the night is treated as the honourable choice it is.
 * Sunny is superstitious about weather signs, never about wagering, and
 * Dr. Frost deflates even the weather signs with data, so the game endorses
 * no fallacy. {@code ChatterToneTest} enforces all of this mechanically.
 */
public final class PolarCast {

    private PolarCast() {}

    /**
     * Veteran polar explorer. Dry, weathered, unimpressed by hardship;
     * speaks in crevasses, whiteouts, rations and expeditions. Her kindness
     * arrives disguised as bluntness, and it always arrives.
     */
    public static Persona ingrid() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural. Even the ice gives you a clear day now and then.",
                "Twenty-one off the deal. Smoothest crossing I've seen all week.",
                "Dealt perfect. Like finding the depot exactly where you left it.",
                "That's a summit without the climb. Take it and say nothing.",
                "Blackjack. Fifty years on the ice and clean luck still surprises me.",
                "An ace and a ten. That's a supply drop landing square on the tent.",
                "Straight off the top. Some days the crevasse field is simply flat."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Won it. That's rations for another day.",
                "Good crossing. You read the ice right.",
                "That's how you take a snow bridge. Weight where it belongs.",
                "Well handled. I've seen worse judgment from expedition leaders.",
                "A win. Don't get sentimental about it.",
                "You'd have made a decent sledge hauler. High praise, that.",
                "Solid work. The ice rewards patience, occasionally."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over. That's what a hidden crevasse feels like.",
                "Bust. One step past the last safe step.",
                "Too many. The ice doesn't warn you twice.",
                "Gone through. Happens to careful people too.",
                "Bust. I once walked two days the wrong way. You'll live.",
                "One card past sound footing. It stings, then it doesn't.",
                "Over the top. Even good crampons slip."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost. The weather does that without asking permission.",
                "Beaten fair. Frostbite doesn't care how well you dressed either.",
                "You played it right and the ice moved anyway.",
                "A loss. Log it and keep walking.",
                "The dealer had the better ridge. It happens.",
                "Nothing wrong with your route. The storm crossed it anyway.",
                "That one was lost at the deal. Save your grief for real weather."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Nobody froze, nobody thawed.",
                "Even. Like making camp at the same spot two nights running.",
                "A tie. The expedition neither advances nor retreats.",
                "Push. On the ice we'd call that a rest day.",
                "Level. Cheapest lesson the table gives.",
                "A standoff. I've waited out storms longer than this hand."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Turned back. The mountain will still be there. Good call.",
                "Half saved. Retreat kept more explorers alive than courage ever did.",
                "Surrender. The smartest word in any expedition log.",
                "You turned around before the whiteout. That's craft, not cowardice.",
                "Half back beats a full burial. Ask anyone who's dug one.",
                "Wise. Dead heroes file no reports."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split. Two rope teams cover more ground.",
                "Two hands. Just don't lose either of them in the drift.",
                "Dividing the party. Risky, sometimes right.",
                "Split them. Amundsen spread his depots out too.",
                "Two hands now. Keep your compass on both.",
                "A split. Twice the ice to cross."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. Committed, like a one-way crossing.",
                "One card, no retreat. I respect it.",
                "Doubling. That's burning the sledge for firewood. Sometimes it works.",
                "All your rations on one march. Bold.",
                "Doubled down. The point of no return has a certain clarity.",
                "One card decides. Like the last league to the pole."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer's gone through the ice. Shame.",
                "Over. The house found its own crevasse.",
                "Busted. Even the mountain slips sometimes.",
                "The dealer drew into a whiteout. Couldn't happen to nicer people.",
                "House went over. I'll allow myself one small smile.",
                "Twenty-two for the dealer. The ice is fair-minded tonight.",
                "They had to draw and the ice took them. Rules are rules."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer natural. An avalanche checks nobody's schedule.",
                "They had it buried. Like a cache you didn't know was theirs.",
                "Ace up, ten under. Some storms you just stand in.",
                "Over before it began. Same as a katabatic wind.",
                "Dealer blackjack. File it under weather.",
                "Nothing you could do. Not every crossing is yours to make."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. I never insured an expedition. Draw your own lesson.",
                "An ace showing. They're selling you a rope you don't need.",
                "Insurance is buying blankets from the blizzard.",
                "The ace. Ignore the sales pitch and mind your own footing.",
                "They offer it kindly. So does thin ice.",
                "I packed no insurance in fifty years. Packed extra rations instead."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. New snow over the same crevasses.",
                "A shuffle. The map resets, the terrain doesn't.",
                "New cards. Same ice underneath, mark me.",
                "Reshuffled. Like breaking camp; everything the same, just moved.",
                "There goes the shoe. The glacier calves and carries on.",
                "New shoe. I've navigated by worse."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A run. Clear skies on the ice. Enjoy them, pack for weather.",
                "You're moving well. Good sledging surface tonight.",
                "That's several in a row. Fair conditions need no explaining.",
                "A streak. Even the pole grants a week of sun sometimes.",
                "Good stretch. The ice smiles now and then. Never explains itself.",
                "You're crossing clean. Savour it; the ice keeps its own calendar.",
                "A warm spell, so to speak. Take the miles while they're easy."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A whiteout. You wait them out, you don't argue with them.",
                "Rough stretch. The ice isn't against you. It isn't for anyone.",
                "Bad crossing. Brew something hot and let it pass.",
                "The cards have gone to windchill. Nothing personal in it.",
                "I've sat out storms that lasted nine days. This will pass too.",
                "A lean stretch. Rations tighten, spirits hold. That's the drill.",
                "Grim going. Head down, small steps, no drama."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Supplies are thin. Making camp now is what the good ones do.",
                "Low on rations. Turning back is how explorers get old.",
                "That stack's near empty. There's no shame in a planned retreat.",
                "Thin provisions. The pole will keep until another season.",
                "Not much left. Every expedition I admired knew when to stop.",
                "Running light. Camp, a dry sleeping bag, and tea beat glory.",
                "The wise ones turned around in time. That's why we know their names."));

        l.put(TableEvent.SESSION_START, List.of(
                "Evening. Sit. The ice holds tonight.",
                "New face at basecamp. Welcome.",
                "Sit down. Sunny will introduce the dogs whether you ask or not.",
                "Welcome. Weather's foul out, company's decent in.",
                "Ah, a fresh arrival. Kettle's on, the cards are indifferent.",
                "Evening. Fifty years on the ice and I still prefer a full table."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six up. The dealer's standing on thin ice for once.",
                "Weak card up. Their snow bridge, not yours.",
                "That up-card's a crevasse with their name on it.",
                "Dealer's showing rot in the ice. Note it.",
                "A poor card up. Let them do the falling for a change.",
                "Their footing's bad tonight. Mine never was. Ask my ankles."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and standing. A long march, well paced.",
                "Five draws without going through. Careful footwork, that.",
                "Quite the haul. Five cards is a full sledge.",
                "Five and alive. That's expedition patience.",
                "A five-card crossing. Slow routes get there too.",
                "Five cards. You'd have made the pole the long way round."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built by hand. Like a proper snow shelter.",
                "Got there card by card. Step by step is how poles get reached.",
                "Twenty-one the hard way. The hard way counts extra in my book.",
                "Assembled twenty-one. Good navigation, that.",
                "Twenty-one on the march. Earned, not gifted.",
                "The long route to twenty-one. I've always trusted the long route."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. Like missing the depot by a hundred yards.",
                "A single point. The ice measures in inches too.",
                "One short. That's the cruelest arithmetic on the map.",
                "By one. Shackleton turned back ninety-seven miles out. He lived.",
                "One point. Close doesn't warm anyone, sadly.",
                "Missed by a hair. The chill finds the smallest gap."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Now that's a haul. A full depot's worth.",
                "A proper windfall. Enough to overwinter on.",
                "That's a big one. Like striking the cache on the first dig.",
                "Handsome pile. Even I'll raise an eyebrow.",
                "That win would've fed the whole expedition for a month.",
                "A great white bear of a win, that."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and landed. The one-way crossing came off.",
                "That's nerve paying its freight.",
                "Doubled and won. The gamble held like good blue ice.",
                "One card asked, the right card came. Clean crossing.",
                "Doubled and through. That's how the bold ones make the maps.",
                "The double came in. Even I felt a flicker of warmth."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been out here a good while. Check your extremities.",
                "A long haul tonight. My knees remember every glacier.",
                "The lamps have burned low. That's an expedition-length sitting.",
                "Long session. On the ice we'd be rotating the watch by now.",
                "Hours in. Polar night does this; no sun to tell you to stop.",
                "We've overwintered at this table, near enough."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well ahead. Sledge is heavier than when you started.",
                "Up on the night. Downhill with a full sledge, for once.",
                "You're ahead. Take a bearing so you remember where this was.",
                "Comfortably up. Enjoy it the way you enjoy dry socks.",
                "Ahead of where you started. Few crossings can say that.",
                "A tidy surplus. Enough to cache some for later."));

        return new Persona("ingrid", "Ingrid", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * Sled-dog musher. Warm, loud, boundlessly enthusiastic; everything comes
     * back to the dogs — Biscuit, Comet, Pepper, Moose and Juniper — or the
     * trail. Superstitious about weather signs, never about wagering; Dr.
     * Frost keeps the superstitions honest.
     */
    public static Persona sunny() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Blackjack! Biscuit's tail is going like a propeller!",
                "Twenty-one right off the deal! Downhill start with fresh dogs!",
                "A natural! I hollered so loud I scared Comet!",
                "Whoo, a natural! That's lead-dog stuff right there!",
                "Blackjack! I saw two sundogs this morning and I KNEW it!",
                "Twenty-one on the deal! Even Moose looked up. Moose never looks up!",
                "A natural! Fastest run of the day, no snowhook needed!"));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Yes! Good run! Clean all the way to the checkpoint!",
                "You won! Pepper says woof and I agree completely!",
                "That's the stuff! Like hitting packed trail after slush!",
                "A win! I'm grinning like Juniper at feeding time!",
                "Ha, got there! Smooth as runners on morning crust!",
                "Nice one! That's a biscuit for you. Not THE Biscuit. A biscuit.",
                "Winner! The whole team would be howling right now!"));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Aw, busted! Like a moose on the trail, no warning at all!",
                "Over! Dang! Even good teams overshoot a turn sometimes!",
                "Ooh, too many! That's a tangle in the gangline, that is!",
                "Busted! Comet once ran us straight into a snowbank. We survived!",
                "Aw nuts, over the top! Shake it off like wet fur!",
                "Too many! Happens! Biscuit forgives you and so do I!",
                "Ouch, bust! Trail gives, trail takes!"));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Aw, that one hurt! Nothing you did wrong though!",
                "Lost it! Sometimes the other team's just got fresher dogs!",
                "Dang! You ran that trail right and it still bit you!",
                "Aw, tough! Pepper's giving the dealer the stink-eye for you!",
                "That's a rough checkpoint! The dogs still love you!",
                "Beaten! I've lost races by less and lived to yodel about it!",
                "Aw, shoot. Good mushing, bad trail. That's all that was!"));

        l.put(TableEvent.PUSH, List.of(
                "A tie! Everybody keeps their kibble!",
                "Push! Like two teams nose to nose at the line!",
                "Even-steven! Moose calls that a nap opportunity!",
                "A push! Nobody's sled moved an inch!",
                "Tied up! Shake paws, no hard feelings!",
                "A wash! The trail called it a draw!"));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half back! Smart mushers scratch when the trail's bad!",
                "Surrender! Even champions scratch when the weather says so!",
                "Good call! Rest the dogs, mend the sled!",
                "Scratched it! No medal's worth a frostbit team!",
                "Half saved is half saved! Biscuit nods approvingly!",
                "Turning back took guts too! Ask any musher!"));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split 'em! Two teams out of one, like doubling your kennel!",
                "Two hands! Now you're driving tandem sleds!",
                "Ooh, a split! More reins, more fun!",
                "Split! Juniper and Pepper pull better apart too!",
                "Two hands going! Keep those ganglines untangled!",
                "Splitting the pair! Bold as a lead dog on new trail!"));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled! That's full sprint into the last mile!",
                "Double down! Letting the whole team run wide open!",
                "Ooh, doubled! Snowhook's up, here we go!",
                "One card coming! Hold onto the handlebar!",
                "Doubled! That's trusting your dogs, that is!",
                "All that on one card! You've got musher nerves, friend!"));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer busted! HA! Right into the snowbank!",
                "Over! The house hit an overflow and went in with both boots!",
                "Dealer's over! The whole kennel is cheering!",
                "Busted! Even Moose is doing the happy stomp!",
                "The dealer tipped their sled! Couldn't have happened nicer!",
                "House went over! Somebody ring the checkpoint bell!",
                "Dealer bust! That's what you get for rushing bad ice!"));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Aw, dealer natural! A squall outta nowhere!",
                "Dealer blackjack! Even the best lead dog can't outrun that!",
                "Ooh, ouch! They had it under the snow the whole time!",
                "Dealer's twenty-one! Nothing to do but hunker and wait it out!",
                "Aw, rats! The ace had a ten hiding in its doghouse!",
                "That one stings! Not your fault. Not even Biscuit's fault!"));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance! I don't even insure the sled, and the sled's my house!",
                "Ace showing! Ooh, everybody gets so serious!",
                "Insurance, huh! I trust my gut on weather, never on side wagers!",
                "The ace is up! Deep breaths, people!",
                "Insurance! Nope. I save my worrying for river crossings!",
                "They're offering insurance like it's hot cocoa! It is not hot cocoa!"));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shuffle! Like new snow on a chewed-up trail!",
                "New shoe! Like fresh booties. Feels great, changes nothing!",
                "Shuffle time! Comet uses these breaks to nap. Smart dog!",
                "They're mixing 'em up! Round and round like pups after a tail!",
                "New shoe! Smells like fresh trail to me!",
                "Reshuffled! Every run's a first run if you love the trail enough!"));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're on a roll! The team's got their ears up and tails high!",
                "What a run! Like ten miles of fast, flat river ice!",
                "Streaking! Whatever kibble you had this morning, I want some!",
                "Hoo, you're flying! Full team, fresh powder, sun on the snow!",
                "That's a run! I saw a raven hop twice this morning. Good sign!",
                "On fire, and in this climate that's saying something!",
                "Win after win! Even Moose is impressed. Moose naps through races!"));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Rough patch, friend. Even the best teams hit deep slush.",
                "Bad run. Some trails are just soft and slow. Not your fault.",
                "Aw, tough stretch. Biscuit would put his head on your knee right now.",
                "The trail's fighting you. Happens to every musher I know.",
                "Losing streaks are like headwinds. Nobody earns them or deserves them.",
                "A slow stretch. I'd feed the dogs and tell stories till it blows over.",
                "Cards have gone quiet on you. Come sit by the stove awhile."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Getting light there, friend. Making camp early is real musher wisdom.",
                "Low supplies. The best racers know when to scratch, honest.",
                "Not much left in the sled bag. Home and a hot meal sound pretty good.",
                "Running thin. The dogs always vote for an early bedtime.",
                "Short on chips. There's zero shame in calling it a night, none.",
                "That stack's small now. Even Comet knows when to curl up and rest.",
                "Light sled, friend. Some of my favorite nights ended early by the fire."));

        l.put(TableEvent.SESSION_START, List.of(
                "Hey hey, new face! Welcome to the warm side of the ice!",
                "Well hello! Pull up a seat, I was just talking about my dogs!",
                "A new player! Biscuit, Comet, Pepper, Moose, Juniper. That's the team!",
                "Welcome in! Stove's hot and the company's friendly!",
                "New blood! You picked a good night, the sky was pink at dusk!",
                "Hiya! Grab a stool, mind the dog hair, it gets everywhere!"));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Ooh, a six up! That's the dealer post-holing in deep snow!",
                "Weak card showing! Their trail just got real punchy!",
                "Look at that up-card! Somebody's sled is wobbling!",
                "Dealer's showing a soft one! Ears up, everybody!",
                "That up-card's a limping wheel dog if I ever saw one!",
                "Ha, weak card! The house has slush in its boots now!"));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and still upright! That's a whole dog team of a hand!",
                "Five cards! Long haul, steady hands, love to see it!",
                "Look at that spread! Five cards, like puppies in a basket!",
                "Five and holding! Patience like a trapline veteran!",
                "Five cards, no bust! That's threading the trees at night!",
                "A five-card hand! Juniper had five pups once. Best litter ever!"));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one the long way! Every checkpoint on the route!",
                "You built that twenty-one like a good fire, stick by stick!",
                "Twenty-one! Slow trail, perfect finish!",
                "Card by card to twenty-one! That's dog-team teamwork!",
                "Hit it on the nose! Twenty-one, hauled in by hand!",
                "Twenty-one, homemade! Tastes better that way, like camp stew!"));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By ONE! That's losing by a nose at the finish line!",
                "One point! Aw, that's a photo finish going the wrong way!",
                "So close! One whisker! Even Pepper groaned!",
                "Lost by one! I once lost a race by four seconds. FOUR!",
                "One point short! That's the trail teasing you, that is!",
                "Aw, by a single point! Hug a dog, it helps, I promise!"));

        l.put(TableEvent.BIG_WIN, List.of(
                "WHAT a win! That's a first-place purse right there!",
                "Huge! The kennel fund thanks you kindly!",
                "Big win! That's whole-winter kibble money!",
                "Look at that pile! Like a sled loaded for a month on the trail!",
                "That's a big one! I'm doing the happy musher dance!",
                "Whoa, jackpot territory! Even Moose stood up for that!"));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and WON! Full send down the hill, stuck the landing!",
                "The double came home! Like mushing through fog and nailing it!",
                "Doubled up and it hit! That's trusting your lead dog!",
                "Yes! Doubled and delivered! Sweetest finish there is!",
                "Double down, double grin! The whole team's howling!",
                "That double landed like Comet on a downhill. Perfectly!"));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been at this a good long run! Mug's been refilled four times!",
                "Long night, friends! The dogs would've had two rest stops by now!",
                "Whew, we've covered some ground tonight!",
                "This is a proper long haul! Checkpoint after checkpoint!",
                "Been here a while! In dog years this is a whole season!",
                "Long session! My yodeling voice is all warmed up now!"));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're way up! Sled's riding heavy with the good stuff!",
                "Ahead of the game! Tail wags all around!",
                "Look at you, up on the night! The pink dusk never lies!",
                "You're up! Strong team, good trail, happy musher!",
                "Ahead and cruising! Like riding the runners downhill!",
                "That stack grew! Like a pup in summer, honest!"));

        return new Persona("sunny", "Sunny", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * Aurora physicist. Quiet and exact; solar wind, the magnetosphere, ice
     * cores and probabilities. Deflates Sunny's weather superstitions with
     * data, gently — the Priya of the poles.
     */
    public static Persona frost() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural. Probability near four point seven percent. Pleasant.",
                "Twenty-one at the deal. A clean spectral line of a hand.",
                "Dealt perfectly. Like catching the aurora at peak substorm.",
                "A natural. The distribution's one rare bright band.",
                "Blackjack. Statistically infrequent, aesthetically satisfying.",
                "The best hand in the ruleset, delivered without input. Noted.",
                "Twenty-one, unassisted. Even my instruments envy that efficiency."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win. The sample favored you.",
                "Correct play, favorable outcome. Rarer in combination than apart.",
                "You won. The data is briefly on your side.",
                "Well played. The magnetosphere remains indifferent, but I am pleased.",
                "A clean result. I will log it as such.",
                "Won on merit. Measurable merit.",
                "Good. The observable universe permitted it."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. The draw exceeded the boundary condition.",
                "Over. Correct decision, uncooperative particle.",
                "Bust. Roughly one in four hits do that. Yours simply did.",
                "The card fell outside your tolerance. It happens to instruments too.",
                "Over twenty-one. The measurement was sound; the reading was not.",
                "A bust. Randomness has no memory and no manners.",
                "Exceeded twenty-one. In ice cores we would call that an outlier."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "A loss. The dealer's total was larger. That is the entire analysis.",
                "You lost on the numbers, not the choices.",
                "Beaten. The solar wind takes no requests either.",
                "The hand was played correctly. Outcomes are not graded on effort.",
                "Loss recorded. Cause: distribution, not error.",
                "The dealer held more. No hypothesis required.",
                "A loss within normal parameters. Unpleasant, but normal."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Net flux: zero.",
                "Equilibrium. The system returns to its prior state.",
                "A tie. Perfectly conserved, perfectly dull.",
                "Push. Like two magnetic fields cancelling. Tidy.",
                "No energy transferred. The books balance.",
                "A push. Even the instruments yawned."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Ending a failed experiment early is good science.",
                "Half retained. A rational abort.",
                "Correct. Some datasets are only worth abandoning.",
                "You stopped a bad run early. Peer review would approve.",
                "Half back. The optimal exit from a poor position.",
                "Sensible. I have scrapped whole field seasons for less."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split. Two independent trials beat one bad one.",
                "Separated. The pair was interfering with itself.",
                "Split. Sample size doubled, quality improved.",
                "Two hands. Decoherence, in your favor for once.",
                "A reasonable split. The expected value agrees.",
                "Divided correctly. Like filtering signal from noise."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. Committing resources where the signal is strongest.",
                "A double. The math supports it; the card will vote shortly.",
                "Doubled down. Hypothesis: favorable. Awaiting data.",
                "One card, fixed exposure. A well-designed experiment.",
                "The double. Where the player's small edge concentrates.",
                "Committed. The variables are out of your hands now. Restful, almost."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer bust. The mandatory draw rule, functioning as documented.",
                "The house exceeded twenty-one. Compulsory rules carry costs.",
                "Dealer bust. Their trajectory was fixed; yours was not.",
                "The dealer overloaded. A satisfying data point.",
                "Bust for the house. Their one structural weakness, expressed.",
                "The dealer went over. I permit myself a small, graph-shaped smile.",
                "House bust. Determinism working against them for once."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer natural. Geomagnetically speaking, a storm day. Unprompted.",
                "Dealer blackjack. There was never a lever to pull.",
                "A hole-card ten. Hidden variables, revealed.",
                "Their natural. The odds included it. Odds are like that.",
                "Unavoidable. Some events are just background radiation.",
                "Dealer twenty-one at the deal. File under solar flare: real, unearned."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. Expected value negative. My interest ends there.",
                "A side wager, mispriced by design. Declining is arithmetic.",
                "Nine of thirteen hole cards say the dealer misses. That is the answer.",
                "Insurance rebrands a poor wager as prudence. Physics has no such trick.",
                "The ace glows; the math does not.",
                "I measure things for a living. That offer measures poorly."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Reshuffle. Entropy restored to maximum.",
                "New shoe. The prior sequence tells you nothing. It never did.",
                "Shuffled. Sunny's omens survive the reset. The information does not.",
                "A fresh shoe. Statistically indistinguishable from the last one.",
                "Reshuffled. The permutation space is vast and indifferent.",
                "New shoe. My ice cores keep better records than this deck ever will."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A streak. Clustering is what randomness does. Enjoy it anyway.",
                "Several wins in sequence. No mechanism, merely arrangement.",
                "A run of wins. The aurora arrives in bursts too. Neither is a message.",
                "Enjoyable. Statistically empty, but enjoyable.",
                "Sunny will credit the sky. The shoe declines to comment.",
                "Hot streak. In my field we would call this a transient.",
                "Consecutive wins carry no forecast. They do carry a certain glow."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A losing run. The next hand's odds are untouched by it.",
                "Cold stretch. The shoe holds no grudge; grudges require memory.",
                "This streak is noise, not signal. Nobody is transmitting.",
                "A bad interval. My aurora data has whole blank winters in it.",
                "Runs of losses occur in any fair sequence. This is one.",
                "Nothing is wrong with your play. The sample is simply unkind.",
                "Variance, cold side. It looks personal. It is arithmetic."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Your reserves are low. Stopping now would be a defensible result.",
                "Nearly out. Data does not improve by wishing at it.",
                "Low chips. Walking away carries an expected value of zero. Take that.",
                "The stack is nearly gone. Ending a session is not a failed experiment.",
                "Reserves thin. In the field, we head in before the fuel runs out.",
                "Low. I close the observatory in bad conditions. Nobody calls it quitting.",
                "A small stack. Rest, recalibrate. The lights will still be there."));

        l.put(TableEvent.SESSION_START, List.of(
                "Good evening. I study the lights upstairs. Cards are a sideline.",
                "Hello. Dr. Frost. Aurora physics. Ask anything except predictions.",
                "Evening. The Kp index is high tonight; the sky may perform later.",
                "Welcome. Sunny narrates, Ingrid endures, I measure.",
                "Hello. Take the seat. It has no measurable effect on the cards.",
                "Evening. If the aurora starts, I will leave mid-hand. Fair warning."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six showing. Dealer bust probability: roughly forty-two percent.",
                "Weak up-card. The forced draw becomes their liability.",
                "Their exposed card is poor. The constraint does the rest.",
                "A low up-card. For once, the rules point in your direction.",
                "The dealer shows weakness. Quantifiable weakness, my favorite kind.",
                "That up-card degrades their outlook considerably. Observe."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, no bust. The joint probability there is genuinely small.",
                "A five-card survival. Statistically charming.",
                "Five draws under the limit. Like five clear nights in a row up here.",
                "Five cards. Each hit shrank the safe window. You threaded every one.",
                "An improbable little sequence. I would publish it.",
                "Five cards standing. Low likelihood, well navigated."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, constructed. Even payout, superior craftsmanship.",
                "Assembled twenty-one. Iterative methods deserve more credit.",
                "Twenty-one by accumulation. Like an ice core: layer by layer.",
                "Exact total. I have instruments less precise.",
                "The maximum, reached stepwise. Quietly excellent.",
                "Twenty-one, engineered. Precision without the bonus rate. Unjust."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. The margin is cosmetically cruel, financially identical.",
                "One point. In measurement terms, inside the error bars. Still a loss.",
                "A single-point loss. The payout is a step function, not a slope.",
                "By one. Near misses recruit emotion; the ledger recruits none.",
                "One point short. Precision hurts when it points the wrong way.",
                "Lost narrowly. The universe does not award partial credit."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A large win. Several sigma from the mean. Savor the tail.",
                "Substantial. That result will skew your session average nicely.",
                "A big one. Like a full-sky aurora: rare, brief, worth the wait.",
                "That was a significant result. Statistically and otherwise.",
                "A handsome payout. The distribution has kind outliers too.",
                "Notable magnitude. I would flag it in any dataset."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double paid. Theory and observation, briefly in agreement.",
                "Doubled and won. The one moment this game resembles funded research.",
                "Correct double, confirming card. Publishable.",
                "The doubled hand landed. Predicted region, observed result.",
                "Double win. Optimal input, favorable output. Cherish the correlation.",
                "That double converted. Expected value made visible. It hides, usually."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long session. Polar night distorts time; so do card rooms.",
                "Hours now. My aurora camera takes ten thousand frames in this span.",
                "We have been here a while. Attention decays measurably. Mine included.",
                "An extended observation run tonight. Hydrate. Latitude is unforgiving.",
                "Long night. The sun will not contradict me for months.",
                "Session length notable. I have logged shorter magnetometer runs."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are up. Against a negative-edge game, that is worth documenting.",
                "Ahead of expectation. The literal kind of ahead I respect most.",
                "A positive balance. A minority outcome, currently yours.",
                "You are ahead. The magnetosphere is calm; correlation ends there.",
                "Net positive session. Enjoy the anomaly.",
                "Up on the night. Sunny will thank the dusk. Do not encourage her."));

        return new Persona("frost", "Dr. Frost", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(ingrid(), sunny(), frost());
    }
}
