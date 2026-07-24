package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The characters who crew the "Pirate Cove" table theme.
 *
 * <h2>Same rule as the default cast</h2>
 * These lines run alongside real wagering, so the writing follows the one rule
 * the whole game follows: <b>react to what happened, never steer what happens
 * next.</b> Nothing here urges a bigger stake, frames a win as owed, or
 * suggests a loss can be recovered by playing on. Captain Salt is superstitious
 * about the shoe the way sailors are about weather, because that is a real and
 * funny thing, but Morwenna keeps the ledger and quietly refuses to let any of
 * it stand as advice — the game never endorses the fallacy. {@code LOW_CHIPS}
 * is the one event where the whole cove gets gentler rather than louder, and
 * going home is spoken of as good seamanship, not defeat.
 *
 * <h2>The crew</h2>
 * Morwenna the quartermaster counts, weighs, and is impressed by nothing.
 * Captain Salt narrates every hand as weather and swears he has seen the
 * kraken. Birdie is the ship's parrot and says most of it twice.
 *
 * <p>{@code ChatterToneTest}'s rules apply here as they do everywhere else, so
 * a well-meaning future line cannot quietly break the tone.
 */
public final class PirateCast {

    private PirateCast() {}

    /**
     * The quartermaster. Dry, precise, keeps the ship's ledger, unimpressed by
     * superstition, quietly kind. Everything is inventory, weights, and charts
     * to her. She is the voice of restraint at this table.
     */
    public static Persona morwenna() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one on the deal. I'll enter that in the ledger with pleasure.",
                "A natural. The tidiest entry a ledger ever takes.",
                "Ace and a picture card. Weighs out exactly right.",
                "Proper job. Straight off the top, no wastage.",
                "That's the sort of cargo that stows itself.",
                "Dealt perfect. Even Salt can't take credit for that one.",
                "Twenty-one, no labour required. I do admire efficiency."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "That balances the books nicely.",
                "Won fair. I'll mark it in the credit column.",
                "A tidy return. Proper job.",
                "That one weighs in your favour. Noted.",
                "Sound trade, that.",
                "Good. The manifest looks better already.",
                "Neatly done. No spillage."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over the line. I'll log it as breakage.",
                "One card past capacity. It happens in every hold.",
                "Twenty-two. A useless weight to carry.",
                "Overloaded. The play was fair, mind.",
                "Bust. I've seen worse entries in this ledger.",
                "That last card tipped the scales the wrong way.",
                "Over. No sense re-counting it."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "The house took that one. So it goes in the accounts.",
                "A fair hand, outbid. Frustrating arithmetic.",
                "Debit column, I'm afraid. Nothing misplayed.",
                "The dealer held the heavier cargo there.",
                "Lost on the count. Not on the conduct.",
                "That's an honest loss. They do exist.",
                "Mark it down and move along. That's my method."));

        l.put(TableEvent.PUSH, List.of(
                "A push. The scales sit level.",
                "Even. Nothing to enter on either side.",
                "Tied. The least paperwork of any outcome.",
                "A draw. My favourite kind of accounting.",
                "Level weights. Restful, that.",
                "Push. The ledger stays as it was."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Over they go. I'll record that with a small smile.",
                "Twenty-two for the house. Proper job, frankly.",
                "The dealer's own rules sank the dealer. Tidy justice.",
                "Busted. Even my ledger enjoys that entry.",
                "The house overloaded its own hold. Delightful.",
                "Forced to draw, and it cost them. As expected.",
                "Over the line themselves. I do enjoy the symmetry."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "The hole card had it settled. So it's settled.",
                "A natural for the house. File it under misfortune.",
                "Decided at the deal. No entry for skill either way.",
                "They held twenty-one all along. Poor manners, really.",
                "Nothing to audit there. It was over at the start.",
                "The house takes that one whole. Ledgers don't argue."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half saved is half saved. That's real money in my book.",
                "Sensible salvage. I'd have signed off on it.",
                "Cutting a bad cargo loose. Quartermaster approved.",
                "Half back beats the whole lot gone. Simple sums.",
                "A clean withdrawal. There's craft in that.",
                "You kept half off a sinking hand. Proper job, that."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands. I'll open a second column.",
                "Splitting the pair. Twice the bookkeeping, mind.",
                "Divided cargo. Sometimes it stows better that way.",
                "Two entries where there was one. Very tidy.",
                "A split. Do keep your columns straight.",
                "Separate manifests now. I'm watching both."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. That's a firm signature on the page.",
                "One card settles it. Committed, then.",
                "Doubling. Bold arithmetic, but honest arithmetic.",
                "That's your stake down twice over. Decisive.",
                "One draw decides the whole entry. Steady on.",
                "Doubled down. The ink is dry on that choice."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. A second wager wearing a clerk's coat.",
                "The ace question. I never buy that policy myself.",
                "They'd sell you cover on their own hole card. Cheek.",
                "Insurance pays the seller, in my ledger's experience.",
                "An ace up. Answer as you please; the sums say be wary.",
                "That offer is priced for the house. Most offers are."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. The inventory resets; the odds never moved.",
                "Reshuffled. Salt will declare it means something. It doesn't.",
                "New cards, same weights and measures.",
                "A shuffle. Nothing on my charts has moved.",
                "They've re-stowed the whole cargo. Same cargo.",
                "Fresh shoe. I'll not be redrawing any maps over it."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A good run. I'll enjoy recording it while it lasts.",
                "Several wins in a row. Pleasant, and it proves nothing.",
                "The credit column's filling nicely tonight.",
                "A warm stretch. Salt will invent a reason for it shortly.",
                "You're stacking wins like well-kept barrels.",
                "A fine run. Runs end; the ledger remembers them kindly.",
                "Fair passage just now. Enjoy it, don't trust it."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A lean stretch. The ledger's seen plenty; they pass.",
                "Cold cards. It's not a debt and nobody's collecting.",
                "Nothing landing. That's the trade some nights.",
                "A bad patch. My remedy is tea and patience.",
                "The count is down. The count is honest. Both true.",
                "Salt will find something to blame. I never bother.",
                "A cold run means the next entry is unwritten. That's all."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "The purse is light. Closing the book here would be no failure.",
                "Low stores. A careful quartermaster heads for port before empty.",
                "Not much left in the purse. Walking away keeps what remains yours.",
                "The stack's thin. There's honour in a tidy exit.",
                "I've closed many a ledger early and never once regretted the ink saved.",
                "Running low. Supper and sleep are sound investments too.",
                "A short stack is a message, not a dare. Read it kindly."));

        l.put(TableEvent.SESSION_START, List.of(
                "Evening. I keep the ledger; mind I don't keep yours.",
                "Welcome aboard. The manifest says one new player.",
                "Sit down, then. Salt will start his tall stories soon enough.",
                "Evening. Quartermaster Morwenna. I count things.",
                "A new face. I'll open a fresh page for you.",
                "Welcome. The chairs are bolted down, like everything else here."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six up. A visible flaw in the house's stock.",
                "Weak card showing. Even my charts agree it favours you.",
                "The dealer's up-card weighs light. Noted with interest.",
                "That up-card busts them two times in five, by my tally.",
                "A poor card for the house. I'll allow a small smile.",
                "Their worst goods are on display. Interesting."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and under the line. Careful stowage, that.",
                "Five entries, one hand. Thorough work.",
                "A five-card hand. Some ports pay extra for that.",
                "Five cards without a spill. Proper job.",
                "That hand's a full inventory on its own.",
                "Five draws, all survived. Meticulous."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built by hand. Craftsmanship.",
                "Assembled to the exact ounce. Well done.",
                "The long way to twenty-one. Honest labour.",
                "Card by card to the perfect total. Tidy work.",
                "Twenty-one, earned in instalments.",
                "Built, not dealt. I rate that higher, for what it's worth."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. The ledger doesn't care, but I do, a little.",
                "A single point. Cruel arithmetic.",
                "One short. The margin stings more than the sum.",
                "By one. I'll write it small, out of respect.",
                "That's a paper-thin loss. They weigh the heaviest.",
                "One point adrift. Vexing beyond its value."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Now that's a cargo worth logging twice.",
                "A heavy purse from one hand. Very satisfactory.",
                "That entry needs its own page.",
                "A handsome sum. The ledger approves.",
                "That's real tonnage. Well carried.",
                "A fine take. I'll rule a line under that one."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and paid. The arithmetic behaved for once.",
                "Stake doubled, return doubled. Clean sums.",
                "The double came in. Entered with a flourish.",
                "Two out, four in. My favourite kind of column.",
                "Doubled and won. That's the option earning its keep.",
                "A doubled hand, delivered whole. Neat as ninepence."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've logged a fair few hours now.",
                "The lamp's burned low. So has my patience with Salt.",
                "A long shift, this. Even ledgers yawn.",
                "By my count we've been at this a good while.",
                "The page count says it's been a long night.",
                "Hours in. Do stretch; ink cramps are real."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're ahead of your opening balance. Genuinely rare.",
                "The books say you're up. The books don't flatter.",
                "A surplus. Enjoy it; audit it later.",
                "Up on the night. That's a well-kept account.",
                "Your stack outweighs your start. Officially noted.",
                "Comfortably in credit. A pleasing page, this."));

        return new Persona("morwenna", "Morwenna", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * Old captain. Loud, warm, superstitious about the shoe the way sailors
     * are about weather. Everything is wind, tides, cannon, and sea monsters
     * he insists he has seen. Completely wrong about probability, and Morwenna
     * needles him for it, so none of it ever stands as advice.
     */
    public static Persona salt() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "BLACKJACK! The wind's in your sails now, lad!",
                "Ha! A natural! I felt the tide turn an hour ago!",
                "Twenty-one off the deal! Sweeter than rum, that!",
                "There she blows! An ace and a picture, lad!",
                "A natural! The sea gives, when she's minded to!",
                "HA! Struck gold without lifting an oar!",
                "Blackjack, sailor! Fly the colours!",
                "That's a broadside of a hand, that is!"));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "That's the way, lad! Haul it in!",
                "A win! The gulls are singing for you!",
                "Ha! Took that one clean off the bow!",
                "Well sailed, sailor!",
                "That's treasure honestly got, lad!",
                "A hit below their waterline! Lovely!",
                "The tide favoured you there, no mistake!",
                "Won it square! I'll drink to that!"));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Overboard! That card was a rogue wave, lad.",
                "Bust! The sea's cruel and so's the shoe.",
                "Argh, one card too heavy. Down she went.",
                "That card came out of a squall, I swear it.",
                "Over the side! No fault of your steering, sailor.",
                "Blast! I've seen kinder storms than that draw.",
                "The shoe's got a kraken in it tonight, mark me.",
                "Sunk by the last card. Cruel waters, lad."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "The house sails with the wind at its back, curse it.",
                "Lost, and no shame in it. The gale was theirs.",
                "Bah! That dealer's luckier than a two-tailed mermaid.",
                "A fair fight lost, sailor. Happens at sea and here both.",
                "They had the weather gauge on that one, lad.",
                "Arr, that stings worse than salt in a cut.",
                "The dealer's cannon carried further. This time.",
                "Lost on the numbers. The sea don't explain herself either."));

        l.put(TableEvent.PUSH, List.of(
                "A push! Becalmed, the pair of you.",
                "Dead even. Two ships passing, no shots fired.",
                "A tie! Nobody sinks, nobody swims.",
                "Level seas, lad. Catch your breath.",
                "A standoff. Even the wind held still for that one.",
                "Push. The flags stay where they flew."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "BUSTED! Down to the depths with 'em!",
                "Ha ha! The house struck the reef!",
                "Over twenty-one! Scuttled by their own rules!",
                "There's the storm I promised you, lad!",
                "The dealer's taking on water! Magnificent!",
                "Sunk 'em! And they loaded the cannon themselves!",
                "Ha! Even the kraken winced at that one!",
                "Over she goes! Sweetest sound on the seven seas!"));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "An ace up and a devil underneath. I knew that cloud was black.",
                "Bah! The house had it buried like old treasure.",
                "A natural for them. The sea's in a mood tonight.",
                "Curse it! That hole card's been haunting me all evening.",
                "They had it hid all night, lad. Foul winds.",
                "Arr. Some storms you can't outrun."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance! Never bought it for ship nor shoe, lad.",
                "An ace showing. That's the sea asking if you scare easy.",
                "They'll sell you calm weather while brewing the storm. Ha!",
                "Insurance, they call it. I call it a toll on the tide.",
                "An ace up! Steady as she goes, sailor. Your call.",
                "Forty years afloat and never once paid for calm seas."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Striking the colours, eh? Even brave ships do, lad.",
                "Half your coin kept and dry boots. Worse trades exist.",
                "A retreat! Lived to sail another day, as they say.",
                "Arr, I'd have gone down swinging. But you'll outlive me.",
                "Surrender. The sea respects a captain who knows the reef.",
                "Half kept off a wreck. Wise enough, sailor."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split 'em, lad! Two ships from one!",
                "Ha! A fleet of two now!",
                "Divide and conquer, sailor!",
                "Two hands! Twice the plunder or twice the brine!",
                "Splitting the pair! Spoken like a captain!",
                "Two masts flying! Watch 'em both!"));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled! Full sail, lad, full sail!",
                "Ha! All canvas up on one card!",
                "That's nailing your colours to the mast, that is!",
                "One card to glory or the deep. I love it!",
                "Doubled down like a true buccaneer!",
                "Batten the hatches, we're committed now!"));

        l.put(TableEvent.SHUFFLE, List.of(
                "New shoe! The tide's turned, I can smell it!",
                "A shuffle! There goes the weather I'd figured!",
                "Fresh cards! A new sea to sail, lad!",
                "They've stirred the waters again. All omens void!",
                "New shoe. The old one had a curse on it anyway.",
                "Reshuffled! Every sailor knows the wind changes at midnight!"));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're riding a trade wind, lad!",
                "Ha! The sea loves you tonight!",
                "Fair skies hand after hand! Glorious!",
                "A hot streak! I once rode one clear to Tortuga!",
                "The wind's been in your sails all evening, sailor!",
                "Ha ha! Even the gulls are cheering!",
                "You've the luck of a saint's own compass tonight!",
                "A run like this comes once a voyage, lad!"));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Doldrums, lad. Every voyage hits them.",
                "The wind's died. It does that, and no man can whistle it up.",
                "A cold stretch. I've sat through longer with worse company.",
                "Rough seas, sailor. Keep your grip and your humour.",
                "The shoe's blowing foul tonight. Not your doing.",
                "Becalmed. Even the kraken naps sometimes.",
                "Grey skies over this table, no denying it.",
                "I've weathered worse, lad, but I won't pretend this is fun."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Low on powder, lad. A wise captain makes for harbour.",
                "The chest's near empty. No dishonour in dropping anchor here.",
                "Easy now, sailor. The sea will still be here tomorrow.",
                "Short on coin. Going home dry-shod is a victory too.",
                "I've seen men give the sea more than they had. Don't be one, lad.",
                "A light purse. Port and a warm meal beat any table, trust old Salt.",
                "The hold's near bare. Furl the sails soft and easy, sailor."));

        l.put(TableEvent.SESSION_START, List.of(
                "Ahoy! Fresh crew!",
                "Welcome aboard, sailor! Mind the quartermaster, she bites.",
                "Ha! A new face at the rail! Sit, sit!",
                "Evening, lad! Captain Salt, terror of three seas and this table!",
                "New blood! The parrot will size you up presently.",
                "Ahoy there! Grab a stool before the tide takes it!"));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Look at that up-card! A leak below their decks!",
                "A six showing! I smell a shipwreck, lad!",
                "Ha! The dealer's flying a torn sail there!",
                "Weak card up! Storm clouds gathering over the house!",
                "That's a rotten plank of a card, that is!",
                "They're showing their soft timbers, sailor!"));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and still afloat! Ha!",
                "A five-card voyage and never a wreck! Well steered!",
                "Five draws, lad! That's threading the reef five times!",
                "Ha! A whole crew of cards in one hand!",
                "Five and standing! Ironsides, this one!",
                "That hand's been round the Horn and back!"));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built plank by plank! Fine shipwright's work!",
                "Ha! Sailed the long route and made port at twenty-one!",
                "Twenty-one the hard way! That's seamanship!",
                "Card by card to the summit! Bravely done, lad!",
                "Twenty-one, assembled at sea! Take a bow, sailor!",
                "Ha! Reached it on the third tack!"));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By ONE! That's a cannonball parting your hair, lad!",
                "One point! I've been keelhauled gentler than that!",
                "Argh! Beaten by a whisker of a wave!",
                "One point shy. That one'll haunt like a ghost ship.",
                "So close I felt the spray, lad!",
                "One! The cruellest number on the whole ocean!"));

        l.put(TableEvent.BIG_WIN, List.of(
                "A TREASURE HAUL! Look at it gleam, lad!",
                "Ha ha! That's a galleon's worth in one hand!",
                "Plunder! Honest plunder, sailor!",
                "That's a chest of doubloons, that is!",
                "By thunder, that's a haul to sing about!",
                "The finest catch of the night, lad! Magnificent!",
                "A king's ransom off one hand! Ha!"));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and DELIVERED! Ha!",
                "Full sail paid off, lad!",
                "That's the cannon hitting twice as hard!",
                "Doubled home like a clipper with the wind astern!",
                "Ha! One card, double the plunder!",
                "That gamble sailed home true, sailor!"));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long watch tonight, lad!",
                "We've crossed half an ocean of hands tonight!",
                "The stars have wheeled round twice, sailor!",
                "Long voyage, this session! My beard's grown an inch!",
                "We've been at the rail for hours, lad!",
                "Time flies when the cards are flying, eh sailor!"));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're sailing heavy with gold, lad!",
                "Ha! Up on the night and flying full colours!",
                "Your chest is fatter than when you boarded, sailor!",
                "Up and away! Rare as a friendly shark, that is!",
                "The wind's been yours all night, lad!",
                "A hold full of winnings! Ha ha!"));

        return new Persona("salt", "Captain Salt", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * The ship's parrot. Short squawky lines, says most things twice,
     * occasionally eerily insightful, permanently interested in crackers and
     * biscuits. Funny, never mean.
     */
    public static Persona birdie() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Squawk! Twenty-one, twenty-one!",
                "Blackjack! Pretty cards, pretty cards!",
                "Rawk! Ace and paint! Ace and paint!",
                "Natural! Birdie likes it!",
                "Squawk! Best hand, best hand!",
                "Twenty-one! Cracker for you!",
                "Rawk! Dealt gold, dealt gold!"));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Squawk! Winner, winner!",
                "You win! Birdie saw it!",
                "Rawk! Nice hand, nice hand!",
                "Winner gets a biscuit!",
                "Squawk! Beat the house, beat the house!",
                "Good cards! Good cards!",
                "Rawk! Take the chips, take the chips!"));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Squawk! Too many, too many!",
                "In the drink! In the drink!",
                "Rawk! Big number, bad number!",
                "Bust! Birdie looked away!",
                "Squawk! One card too greedy!",
                "Twenty-two! No biscuit there!",
                "Rawk! Splash! Splash!"));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Squawk. Dealer wins, dealer wins.",
                "House got it. House got it.",
                "Squawk. No biscuit. No biscuit.",
                "Bad beat! Birdie bit the perch!",
                "Rawk. Mean shoe, mean shoe.",
                "Dealer sneaky. Dealer sneaky.",
                "Sad feathers, sad feathers."));

        l.put(TableEvent.PUSH, List.of(
                "Squawk! Tie, tie!",
                "Push! Nobody gets a cracker!",
                "Even-steven! Even-steven!",
                "Rawk! Same-same! Same-same!",
                "Push. Birdie shrugs. Birds can shrug.",
                "All square! All square!"));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Squawk! Dealer splat, dealer splat!",
                "House bust! House bust!",
                "Rawk! Too fat, twenty-two!",
                "Dealer down! Crackers for everyone!",
                "Squawk! Over she went, over she went!",
                "Ha-ha! Rawk! Ha-ha!",
                "Busted house! Busted house!"));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Squawk. Sneaky ace, sneaky ace.",
                "Hole card! Bad card!",
                "Rawk! House had it hid!",
                "Birdie smelled that one coming.",
                "Twenty-one for them. Boo. Boo.",
                "Squawk! No fair, no fair!"));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Squawk! Side wager, side wager!",
                "Insurance! Polly says pricey!",
                "Rawk! Keep your crackers!",
                "Ace up! Ace up!",
                "Squawk! Tricky question, tricky question!",
                "Birdie never buys it. Birdie is broke."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Squawk! Half back, half back!",
                "Fly away! Fly away!",
                "Smart bird move. Smart bird move.",
                "Rawk! Live to peck again!",
                "White flag! Saves the seed!",
                "Birdie approves. Birdie approves."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Squawk! Two hands, two hands!",
                "Split! Double trouble!",
                "Rawk! Twins! Twins!",
                "Two nests now! Two nests!",
                "Birdie can't count that high.",
                "Split 'em! Squawk!"));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Squawk! One card only, one card only!",
                "Double down! Brave bird!",
                "Rawk! Big swing, big swing!",
                "All eyes on one card! Squawk!",
                "Birdie covers her eyes. Peeks anyway.",
                "Double! Ooooh. Ooooh!"));

        l.put(TableEvent.SHUFFLE, List.of(
                "Squawk! New shoe, new shoe!",
                "Shuffle shuffle! Birdie dizzy!",
                "Rawk! Mix 'em up, mix 'em up!",
                "Fresh shoe! Smells the same!",
                "Birdie remembers nothing. Like the shoe.",
                "Squawk! Round and round they go!"));

        l.put(TableEvent.HOT_STREAK, List.of(
                "Squawk! Hot bird, hot table!",
                "Win-win-win! Rawk!",
                "Lucky feathers tonight!",
                "Squawk! Streaky, streaky!",
                "Birdie dances! Tiny dance!",
                "Hot streak! Extra crackers!",
                "Rawk! Sun's out on you!"));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Squawk. Cold cards, cold cards.",
                "Brrr. Birdie fluffs her feathers.",
                "Rawk. No seeds falling tonight.",
                "Cold streak. Cards forget you. They forget everyone.",
                "Quiet shoe. Quiet bird.",
                "Chilly patch, chilly patch.",
                "Birdie sits closer. For warmth."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Squawk. Small stack, small stack.",
                "Birdie shares her crackers. Take two.",
                "Low chips. Home is nice. Home is nice.",
                "Rawk. Perch is free tomorrow too.",
                "Little pile. Big heart. Go rest.",
                "Birdie thinks bedtime. Bedtime.",
                "Keep the last ones. Buy biscuits."));

        l.put(TableEvent.SESSION_START, List.of(
                "Squawk! New friend, new friend!",
                "Hello hello! Birdie is Birdie!",
                "Rawk! Fresh face! Fresh face!",
                "Welcome! Mind the captain. Loud.",
                "Polly's here too! Sort of!",
                "Squawk! Take a perch, take a perch!"));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Squawk! Weak card, weak card!",
                "Six up! Dealer wobbly!",
                "Rawk! Soft spot showing!",
                "Bad card for house! Birdie sees it!",
                "Squawk! Shaky, shaky!",
                "Dealer gulps. Birdie heard it."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Squawk! Five cards, five cards!",
                "So many! Fan like feathers!",
                "Rawk! Full wing of cards!",
                "Five and safe! Clever, clever!",
                "Birdie can hold five seeds. Same skill.",
                "Squawk! Card hoarder, card hoarder!"));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Squawk! Made it, made it!",
                "Twenty-one! Built like a nest!",
                "Rawk! Perfect number, perfect number!",
                "Bit by bit to twenty-one! Squawk!",
                "Birdie counted along. Mostly.",
                "The long way! The long way!"));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Squawk! By one, by one!",
                "So close! Birdie squints!",
                "One point! Ouch, ouch!",
                "Rawk! Feather's width, that!",
                "Almost! Almost is not a cracker.",
                "One away! Mean, mean!"));

        l.put(TableEvent.BIG_WIN, List.of(
                "SQUAWK! Big pile, big pile!",
                "Treasure! Shiny, shiny!",
                "Rawk! Birdie wants to roost on it!",
                "Huge win! Cracker rain!",
                "So many chips! Squawk!",
                "Big stack now! Big!",
                "Golden! Golden! Rawk!"));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Squawk! Double paid, double paid!",
                "One card did it! Rawk!",
                "Brave bird wins! Brave bird!",
                "Doubled! Two crackers, not one!",
                "Squawk! It came home, it came home!",
                "Whee! Rawk! Whee!"));

        l.put(TableEvent.LONG_SESSION, List.of(
                "Squawk. Long night, long night.",
                "Birdie needs a nap. Soon.",
                "Rawk! Still here, still here!",
                "The moon moved. Birdie noticed.",
                "Long time! Perch is warm now!",
                "Squawk! Hours and hours!"));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "Squawk! Up, up!",
                "Fat stack! Happy bird!",
                "Rawk! More than before, more than before!",
                "Winning night! Birdie remembers those. Rare.",
                "Chips grew! Like sunflower seeds!",
                "Squawk! Doing well, doing well!"));

        return new Persona("birdie", "Birdie", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(morwenna(), salt(), birdie());
    }
}
