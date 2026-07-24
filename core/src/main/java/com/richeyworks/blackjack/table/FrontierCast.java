package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The Dusty Saloon cast, for the western table theme.
 *
 * <p>Three regulars of a frontier card room: Ruby, who owns the saloon and has
 * cut off more customers kindly than most barkeeps have served; Hank, an old
 * ranch hand who narrates the shoe like weather; and Doc Weaver, a card sharp
 * gone honest who knows the arithmetic cold and refuses to romanticize it.
 *
 * <h2>The same rule as the default cast</h2>
 * These lines run alongside real wagering, so the writing follows the one rule
 * throughout: <b>react to what happened, never steer what happens next.</b>
 * Nothing here urges a bigger stake, calls a win owed, or frames another hand
 * as the cure for a bad run. Hank is superstitious about the deck because
 * ranchers are superstitious about rain, but it stays weather talk and never
 * becomes advice; Doc corrects the record whenever it drifts. On
 * {@code LOW_CHIPS} the saloon gets quieter and kinder, and Ruby does the one
 * thing a good barkeep does best: suggests, warmly, that going home is a fine
 * way to end a night.
 */
public final class FrontierCast {

    private FrontierCast() {}

    /**
     * Owns the Dusty Saloon and has seen everything twice. Dry, motherly
     * without being soft, polishes glasses while she talks. Measures the whole
     * world in bottles, taps, tabs, regulars, and closing time.
     */
    public static Persona ruby() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one off the deal. That's a top-shelf pour, that is.",
                "A natural. Haven't seen a regular smile like that since payday.",
                "An ace and a picture. Prettiest thing behind this bar tonight.",
                "Straight off the top. I'd ring the till if it were mine.",
                "That's the good bottle of hands, right there.",
                "Blackjack. Even the piano player looked up.",
                "Dealt perfect. Some drinks mix themselves."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "There you are. Earned, and served neat.",
                "That one goes down smooth.",
                "Nicely handled. Like a clean pour on a busy night.",
                "Won fair. I'll wipe the counter in your honor.",
                "That's the way a regular does it.",
                "A win. Keeps the lamplight cheerful.",
                "Good hand. The till approves of happy customers."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over the top. Happens to the steadiest hands.",
                "One too many. I say that to somebody every night.",
                "Bust. I've mopped up worse, believe me.",
                "Went over. The rag and I have seen sadder things.",
                "That last card spilled the whole glass.",
                "Twenty-two. Like overfilling a shot, that.",
                "Ah, over. The floor's seen plenty spilled before you."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Dealer took that one. House always pours itself first.",
                "Beaten square. Nothing a bartender can fix.",
                "A decent hand, that. Some nights the till just doesn't ring.",
                "Lost on the count. I've comped drinks for less.",
                "The house had it. They usually do, sugar.",
                "Nothing wrong with your play. The cards were watered down.",
                "That one got away. Like a keg with a slow leak."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Nobody pays, nobody pours.",
                "Even. Like splitting the tab right down the middle.",
                "Tied. The till stays quiet on that one.",
                "A standoff. Cheapest round of the night.",
                "Push. I'll dust the shelf while we wait.",
                "Even money, even tempers. I'll take it."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half back. A wise regular knows a bad bottle by the cork.",
                "Handing it in. That's just good housekeeping.",
                "Smart. I don't serve hands that have gone off, either.",
                "Half saved. Better than mopping up the whole thing.",
                "Folding that was kinder to yourself than the deck was.",
                "Quitting a bad hand. I respect that in a customer."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands now. Like tending both ends of the bar at once.",
                "Splitting them. Two glasses from one bottle.",
                "A pair, parted. Keep an eye on both, sugar.",
                "Two hands. Twice the tending, same rag.",
                "Split. You run your side, I'll run mine.",
                "There go the twins, each to their own stool."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubling. Bold as a new sign over the door.",
                "One card coming. I'll hold my polishing for it.",
                "Doubled. That's the whole bottle, not the glass.",
                "Committed, then. I'll watch from behind the taps.",
                "One card, cash down. My kind of drama, honestly.",
                "Doubled down. Even the regulars went quiet."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Over she goes. Drinks taste better after that.",
                "The house went and spilled its own tray.",
                "Dealer bust. I'd stand a round if I weren't working.",
                "Twenty-two for the house. Music to a barkeep.",
                "They drew it themselves. Poured their own trouble.",
                "Bust behind the shoe. The whole saloon enjoyed that.",
                "Down goes the dealer. The piano ought to play something."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "An ace in the hole. The house keeps its best under the counter.",
                "Dealer's natural. Like finding the good bottle already empty.",
                "That was settled before your glass hit the counter.",
                "The house had it hidden. They always keep a private stock.",
                "Nothing to be done. Sympathy's on me, first one's free.",
                "A natural for the house. Rude of them, frankly."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. The house selling a second drink you didn't order.",
                "An ace showing. They'll offer; you're free to wave it off.",
                "That side bet pays the house's rent, not yours.",
                "Insurance. I've never seen a regular retire on it.",
                "The ace question. Take your time, the bar's not going anywhere.",
                "They always offer that one so sweetly. Notice that."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Same as rinsing the glasses, nothing changed.",
                "A shuffle. I'll restock while they fuss.",
                "New cards. The old ones weren't dirty, mind.",
                "There's the shuffle. Time enough to top up the lamps.",
                "Reshuffled. Hank will have a theory in a minute.",
                "New shoe. The till doesn't care either way."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "Somebody's on a roll. I'll keep the coffee coming.",
                "A good run. Enjoy it like a slow evening drink.",
                "You're winning enough to make the regulars jealous.",
                "Nice streak. The saloon likes a happy customer.",
                "Best night this bar's seen since the railroad came through.",
                "A run like that keeps the lamps warm.",
                "Winning streak. I'll polish a glass in your honor."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cards have gone cold. The stove's warm, though.",
                "A dry spell. Every saloon and every shoe has them.",
                "Rough stretch. I'll put fresh coffee on.",
                "The deck's being stingy. It's not you, sugar.",
                "Cold run. Even my best whiskey has off barrels.",
                "Nothing landing. Sit back, the bar's in no hurry.",
                "A bad patch. I've watched a hundred pass through here."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Stack's getting thin, sugar. Walking home proud is a fine finish.",
                "That's near the bottom of the bottle. No shame in corking it.",
                "Low there. I cut folks off because I like them, you know.",
                "Not much left. The door swings out just as easy as in.",
                "You've had your evening. Settling the tab now would be graceful.",
                "Thin stack. Home's still there, and so is tomorrow.",
                "Nearly the last of it. A good barkeep says so before you ask."));

        l.put(TableEvent.SESSION_START, List.of(
                "Evening, sugar. Bar's clean, cards are fresh.",
                "Come on in. Piano's out of tune but the felt's honest.",
                "Welcome to my place. Mind Hank, he'll talk your ear off.",
                "New face. Pull up, first smile's free.",
                "Evening. I keep a clean bar and an honest table.",
                "Sit anywhere. Doc doesn't bite, he just calculates."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "That up-card's as weak as my Tuesday crowd.",
                "A six for the house. Even the barflies noticed.",
                "Poor card showing. Dealer looks like a man out of ice.",
                "The house drew itself a watered-down one there.",
                "Weak card up. I'll pretend not to smile.",
                "That's a thin card for them. Like serving from an empty keg."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and standing. That's a full tray carried steady.",
                "Five of them. Careful as a barmaid on a wet floor.",
                "Look at that spread. A whole shelf of cards.",
                "Five cards, no spill. I'm quietly impressed.",
                "That hand's got more parts than my back bar.",
                "Five and holding. Patience like that keeps a saloon open."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built slow. Like a well-aged barrel.",
                "Got there card by card. Honest work.",
                "Twenty-one the long way. The regulars nod at that.",
                "Assembled it yourself. That's house-made, that is.",
                "The hard twenty-one. Tastes better than the dealt kind, I hear.",
                "Twenty-one on the build. Neat as a stacked shelf."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one point. That's a splinter off the bar top, that.",
                "One short. I'd offer sympathy and a refill.",
                "Lost by a single point. Even the clock winced.",
                "A whisker in it. Cruelest kind of tab to settle.",
                "One point shy. I'll allow a sigh on the house.",
                "That close. The rag and I both need a minute."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Now that's a payout. Buy yourself the good bottle.",
                "A handsome pile. The till hasn't rung that loud all week.",
                "Big one. Even the swinging doors stopped to look.",
                "That's a proper haul. I'll dust off the top shelf.",
                "My, my. That win could paint the whole saloon.",
                "A pile like that deserves the clean glass."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and it landed. Smooth as a practiced pour.",
                "The double came home. That's the good stuff.",
                "Doubled and won. I'll mark the occasion with a fresh rag.",
                "That gamble poured out just right.",
                "Doubled and paid twice over. Tidy as my back bar.",
                "The one card did its work. Well held, sugar."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "You've outlasted two shifts of regulars, you know.",
                "Long night. I've cleaned the counter three times around you.",
                "We're getting on toward closing-time hours, sugar.",
                "You've been here since the lamps were low. They're low again.",
                "A long sit. The stove and I both noticed.",
                "Still here. I've corked and uncorked a whole evening around you."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're ahead, sugar. That's rarer than a quiet Saturday.",
                "Up on the night. The house is sulking behind the shoe.",
                "Nicely ahead. Your tab could pay itself twice.",
                "That stack grew like my Friday crowd.",
                "Running ahead. Wear it lightly, it looks good on you.",
                "You're up. The till and I tip our caps."));

        return new Persona("ruby", "Ruby", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * Old ranch hand. Warm, loud, folksy, calls everyone partner, and reads
     * the shoe the way ranchers read clouds. His superstition is weather talk,
     * never advice; Doc keeps the record straight from the far seat.
     */
    public static Persona hank() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Hoo-wee! A natural, slick as a greased gate.",
                "Blackjack! That hit like rain after a dry August.",
                "Twenty-one on the deal! Prettier than a paint pony.",
                "Well butter my biscuit. Straight off the top, partner.",
                "A natural! I felt the wind change before that one.",
                "That's the brand on the prime steer, right there.",
                "Blackjack, partner! Yee-haw and then some."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Atta way, partner!",
                "That's ropin' and tyin' it clean.",
                "Got 'em! Like a gate swung shut on time.",
                "A win! That'll shine your spurs.",
                "Yes sir, that one's in the barn.",
                "Took it fair and square, partner.",
                "Ha! Penned that one easy as a Sunday calf."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Aw, bucked off right at the buzzer.",
                "Over the top, like a steer through a busted fence.",
                "That card came in like a twister, partner.",
                "Dadgum. One card past the corral.",
                "Bust. That deck's meaner than a rattler in July.",
                "Threw a shoe on the last stretch, that hand.",
                "Aw heck. Even good riders eat dirt sometimes."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Dealer got that one, like a coyote got the henhouse.",
                "Aw, partner. That stings like barbed wire.",
                "Lost on the count. Weather just wasn't ours.",
                "That hand deserved better pasture.",
                "The house won that fair, much as it chaps me.",
                "Beat on the numbers. Like losin' a footrace to a mule.",
                "Dagnab it. Good ride, bad draw."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Both riders stayed on.",
                "Even, partner. Nobody thrown, nobody trampled.",
                "Tie. Like two bulls starin' across a fence.",
                "A standoff. Herd don't move either way.",
                "Push. Same as swappin' hay for hay.",
                "All square, like a well-dug post hole."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Smart, partner. You don't ride every bronc they saddle.",
                "Half back beats a full stomp, I always say.",
                "Turned it loose. Sometimes you drop the rope.",
                "That hand was a widowmaker. Good call steppin' off.",
                "Handin' it over. Even rodeo men pass on a bad bull.",
                "No shame there. You shut the gate before the storm hit."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split 'em! Cuttin' the herd in two.",
                "Two hands now, like drivin' a pair of teams.",
                "Broke that pair apart like weanin' calves.",
                "Two irons in the fire now, partner.",
                "Splittin'. Ride 'em one at a time.",
                "That pair's parted. Watch both gates now."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled! Spurs down, hold on tight.",
                "One card, partner. Like one jump out the chute.",
                "Doubled down. That's full gallop, no reins.",
                "Whoa now, big swing. I'll watch through my hat.",
                "All your weight in one stirrup. Bold, partner.",
                "Doubled. Now that's rodeo."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Busted! Dealer got thrown clean off!",
                "Hoo-wee, the house hit the dirt!",
                "Over twenty-one! Like a wagon losin' a wheel.",
                "Ha! Dealer drew his own stampede.",
                "The house bucked itself off that one, partner.",
                "Busted flat! I could kiss my hat.",
                "There she goes, over the fence and gone."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Ace in the hole. Sneaky as a bobcat in the hayloft.",
                "Dealer's natural. That's a hard frost in June, that is.",
                "Aw, they had it buried the whole time, partner.",
                "The house was holdin' the whole ranch there.",
                "Nothin' for it. Storm was already in the clouds.",
                "That one was branded before the gate opened."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance, they call it. Never bought hail cover on a sunny day.",
                "Ace up. My grandpappy said side bets feed the house's horses.",
                "There's that offer again. Slick as a saddle salesman.",
                "Insurance. I've known cattle trades I trusted more.",
                "The ace question, partner. Your call, not mine.",
                "They'll insure anything but your feelings out here."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Feels like new weather comin' in.",
                "Shuffle time. I always tip my hat to a new deck.",
                "New cards. Like turnin' the herd onto fresh grass.",
                "There goes the shuffle. Doc says it means nothin'. Doc's no fun.",
                "Reshuffled. I'll knock wood on the rail anyhow.",
                "A new shoe. Smells like rain to me, partner."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're on a tear, partner! Like a colt in spring.",
                "Hoo-wee, what a run! Sun's shinin' on your side of the fence.",
                "That's a streak! Hotter than a brandin' iron.",
                "Win after win, like fence posts in a row.",
                "You're ridin' high, partner. Enjoy the view.",
                "A run like that's rarer than an honest horse trader.",
                "Whatever saddle you're in, it fits."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold spell, partner. Even droughts break someday.",
                "Rough stretch. I've sat out hailstorms longer than this.",
                "The deck's actin' like a mule in a hailstorm.",
                "Dry run. Ain't nothin' you did wrong.",
                "Cards went cold as a January bunkhouse.",
                "Bad weather at the table. It passes, same as the real kind.",
                "A losin' stretch. I'll just chew on my hat a while."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Runnin' light there, partner. Ain't no shame ridin' home early.",
                "Stack's near down to seed corn. Best keep some for plantin'.",
                "Gettin' thin. A good hand knows when to stable the horse.",
                "Low on chips. The ranch still stands either way, partner.",
                "Not much left in the saddlebag. Home cookin' sounds fine now.",
                "Easy now. Nobody ever regretted leavin' with boots still on.",
                "Short stack. My daddy said quit while the wagon's still rollin'."));

        l.put(TableEvent.SESSION_START, List.of(
                "Howdy, partner! Grab a seat, the felt's friendly.",
                "Well hey there! New face at the corral.",
                "Evenin', partner! Ruby runs a fine watering hole.",
                "Come on in! We don't bite. Doc barely even talks.",
                "Howdy! Pull up before the dust settles on that chair.",
                "A new hand at the table. Welcome, welcome, partner."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Look at that up-card. Lame as a three-legged calf.",
                "A six showin'. Dealer's ridin' a sway-backed nag.",
                "Weak card up. Even the cattle could smell that.",
                "That card's flimsier than a snow fence in April.",
                "House is sittin' on a soft one, partner.",
                "Dealer's up-card looks like it lost a bar fight."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and still in the saddle! That's ridin'.",
                "Look at that string of 'em. A whole pack train.",
                "Five cards, partner. Stubborn as a good fence post.",
                "That hand's longer than a cattle drive.",
                "Five and standin'. Hoo, that took grit.",
                "A five-card hand. Like stackin' hay to the rafters."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built plank by plank! That's carpentry.",
                "Got there the hard way, like a long trail drive.",
                "Twenty-one on the nose, partner. Earned, not gifted.",
                "Rode that one all the way to the buzzer.",
                "Card by card to twenty-one. That's honest ranch work.",
                "Hit twenty-one climbin'. Prouder than a dealt one, I reckon."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one point. That's a horseshoe missin' by a whisker.",
                "One short, partner. Stings worse than a hornet in the boot.",
                "Lost by a hair off a bull's back.",
                "One point. I've seen calmer men kick a water trough over less.",
                "Aw, by a whisker. That one'll itch all night.",
                "A single point. Close only counts in horseshoes, they say."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Now THAT'S a haul! Like sellin' the herd at top price.",
                "Hoo-wee! That pile's bigger than a hay bale.",
                "Big win, partner! Ring the dinner bell.",
                "That's prize money, that is. Rodeo purse territory.",
                "Would you look at that pile. Fat as fall cattle.",
                "A win like that deserves a tip of every hat here."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and stuck the landin'! Clean ride, partner.",
                "The double came through like a good cuttin' horse.",
                "Ha! Doubled it and rode it home.",
                "That one card ran like a thoroughbred.",
                "Doubled and won. Smooth as a broke-in saddle.",
                "Spurs down and it paid. That's the stuff."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been at it longer than a spring roundup, partner.",
                "Long haul tonight. My boots fell asleep an hour ago.",
                "Been here since the cows'd have come home twice.",
                "A long ride, this one. Good company, though.",
                "This session's stretched like fence wire, partner.",
                "Time slips in here. On the range you'd feel every hour."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're up, partner! Fat stack like a prize steer.",
                "Ahead of the game. Sun's on your side of the pasture.",
                "Look at you, sittin' pretty in the saddle.",
                "Up on the night. That's a good crop from hard ground.",
                "That stack's grown like June grass.",
                "You're winnin' more than losin'. Rare as a painless brandin'."));

        return new Persona("hank", "Hank", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * Retired card sharp turned honest. Quiet, precise, speaks rarely and in
     * probabilities and gambling-hall history. Knows the edge to the decimal
     * and has never once called the game romantic.
     */
    public static Persona docWeaver() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural. About once every twenty-one hands, fittingly.",
                "Three to two. The only payout worth the name.",
                "Dealt twenty-one. No skill involved, no apology needed.",
                "A natural. On the riverboats we'd have checked the deck twice.",
                "That's the hand the mathematics smiles on.",
                "Blackjack. Roughly a four point seven percent event.",
                "The one hand the house pays a premium on. Enjoy it."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Correct line, favorable card. Both required.",
                "A fair win at an honest table. Rarer than folks think.",
                "Well played. The percentages agreed with you.",
                "That decision holds up in any gambling hall I've known.",
                "Won on merit. I keep a mental tally; it's noted.",
                "Sound play. The old sharps would've found nothing to skim.",
                "The right call, rewarded. Not always in that order."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. The draw was still correct. Probability keeps no promises.",
                "Over. Roughly the expected frequency for that total.",
                "A fair bust. I've seen unfair ones; this wasn't.",
                "The card was against you. The arithmetic wasn't.",
                "Busted on a proper hit. No fault in the reasoning.",
                "Twenty-two. In Dodge City men blamed the dealer. Wrongly, mostly.",
                "Over the line. It happens at a knowable rate."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Outdrawn, not outplayed. Different ledgers.",
                "The dealer finished higher. No verdict on your play.",
                "A loss on the count. The percentages allow plenty of those.",
                "You lost a hand you played correctly. Keep the distinction.",
                "The house edge is small but it is patient.",
                "Beaten fair. On the riverboats that was never a given.",
                "Lost on totals. My ledger calls that variance, not error."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Statistically near nine percent of hands.",
                "Stand-off. The faro banks hated paying even that much.",
                "No money moved. The cheapest lesson blackjack offers.",
                "A tie. The expectation was zero and so was the outcome.",
                "Push. Neither ledger entry gets written.",
                "Even. The rarest common result, if that makes sense."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Correct in a narrow set of spots. That was one.",
                "Half saved is expectation preserved.",
                "Good arithmetic. Some hands are only worth half.",
                "The old halls never offered surrender. Too generous.",
                "Folding for half. The unglamorous right answer.",
                "A disciplined exit. Rarer than a fair dealer in Deadwood."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A proper split. The pair plays worse together.",
                "Split. The numbers favored the separation.",
                "Two hands, each with better expectation than the whole.",
                "Correct division. I ran those figures decades ago.",
                "That split would pass muster in any honest hall.",
                "Separated. As the arithmetic instructs."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "A double in the right window. That's where the thin edge lives.",
                "One card, price doubled. Correct in that spot.",
                "The double is the game's only real weapon. Used well.",
                "Doubled where the percentages point. Sound reasoning.",
                "That's the double the old charts drew a box around.",
                "One draw, full commitment. The numbers approve."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer must draw. That obligation is your entire edge.",
                "House bust. The rules did it; luck merely delivered.",
                "Twenty-two, forced. The one weakness written into the game.",
                "The dealer had no discretion. Discretion is what you keep.",
                "A forced draw, a forced loss. Elegant, in its way.",
                "The house obeys its own chart. Sometimes the chart collects.",
                "Dealer over. About twenty-eight percent of hands, all told."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. Decided before anyone reasoned.",
                "The hole card had it. No line of play touches that.",
                "Settled at the deal. I file those under weather.",
                "Nothing to critique. The hand contained no decisions.",
                "The house's natural. In Tombstone they'd demand a new deck.",
                "Preordained, as much as cards ever are."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. A side wager priced in the house's favor. Always was.",
                "Nine of thirteen ranks say the hole card is no ten.",
                "The word insurance is doing the selling there.",
                "I watched sharps offer worse propositions with straighter faces.",
                "Dressed as prudence, priced as profit. The house's, that is.",
                "A two to one payout on a longer shot than two to one."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Reshuffled. The probabilities are restored, not changed.",
                "A new shoe. Hank will consult the weather. The math won't.",
                "Shuffle. Memoryless before, memoryless after.",
                "Fresh cards. The count resets; the edge does not.",
                "The shoe is mixed. Same game, cleaner conscience for the house.",
                "On the steamboats a shuffle was when you watched hands closest."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A streak. Randomness clusters; people never believe it.",
                "Enjoy the run. It predicts precisely nothing.",
                "Winning in sequence. The cards remain indifferent.",
                "A good stretch. I've graphed a thousand; they all ended.",
                "Runs occur at exactly the rate they should. This is one.",
                "Pleasant, yes. Informative, no.",
                "The streak is real. The pattern is not."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold run. The distribution includes these.",
                "Losing in sequence proves nothing about the next deal.",
                "The shoe holds no grudge. It holds only cards.",
                "Variance, showing its unfriendly face.",
                "Same odds as an hour ago. Feelings disagree; figures don't.",
                "I've charted losing runs since the riverboat days. They end.",
                "A rough stretch. It is not a message."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "The stack is nearly spent. Stopping here is a respectable result.",
                "Low funds. Every old sharp I admired knew this exact moment.",
                "Near the felt. No hand on this table repairs that; rest does.",
                "Short stack. Walking out was the best play I ever learned.",
                "Almost out. The game keeps no schedule; you're free to.",
                "The bankroll speaks plainly at this level. Worth hearing.",
                "Little left. I quit the trade with less and never missed it."));

        l.put(TableEvent.SESSION_START, List.of(
                "Good evening. Doc Weaver. Retired from the trade, mostly.",
                "Take a seat. This table is honest. I checked.",
                "Evening. I used to deal these. Now I only watch them.",
                "Welcome. The odds here are printed fair, which is rare enough.",
                "A new player. I'll spare you the history unless asked.",
                "Sit. Ruby pours, Hank talks, I count."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A five or six up. The dealer busts near two times in five.",
                "Their weakest window. The old charts were unanimous about it.",
                "A bust card showing. The one moment the rules favor you.",
                "The up-card is poor. I priced these spots for a living once.",
                "Weak upcard. The percentages lean your way, briefly.",
                "That card busts dealers at the best rate on the board."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, under the line. A low-probability walk, well taken.",
                "Five draws without ruin. The old halls paid bonuses for it.",
                "A five-card hand. Each draw was a fresh calculation. All passed.",
                "Under twenty-two on five. Uncommon, and mostly skill.",
                "Five cards standing. The riverboat men called that threading it.",
                "A long hand, correctly navigated."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, constructed. Pays less than dealt, oddly enough.",
                "The hard route to twenty-one. More decisions, more credit.",
                "Twenty-one on draws. Perfect total, plain payout.",
                "Built to the exact number. Precision, or fortune dressed as it.",
                "A made twenty-one. The sharps respected those over naturals.",
                "Twenty-one assembled by hand. Good navigation."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. The margin is drama; the cost is identical.",
                "A single point. The ledger records it the same as ten.",
                "One point short. Painful and meaningless in equal measure.",
                "Near misses feel like information. They are not.",
                "By one. In the old halls that's when men reasoned worst.",
                "The narrow loss. Weight it like any other."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A large win. The distribution's kind tail, visiting.",
                "Substantial. That one moves the whole session's arithmetic.",
                "A serious pot. I've seen men retire on less. Briefly.",
                "Big result. File it under luck, not method.",
                "A considerable pile. The math tips its hat, reluctantly.",
                "A heavy win. Even I looked up from counting."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and paid. The thin edge, cashed for once.",
                "The double landed. Best return the rules allow.",
                "Correct double, favorable card. When both align, that happens.",
                "A doubled win. The one wager the mathematics actually likes.",
                "Doubled home. I've verified the figures; that's the good end.",
                "The paid double. Savor it at exactly its face value."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long session. Fatigue moves the error rate, quietly.",
                "Hours in. The game hasn't changed; players do.",
                "We've covered a lot of hands. I count them out of habit.",
                "Long sitting. In the halls we watched tired men closest.",
                "The session has stretched. Notice it before it notices you.",
                "Many deals now. Time is the house's oldest ally."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're ahead. Against a standing edge, that's noteworthy.",
                "Up on the session. Enjoy it while the figures say it's true.",
                "A genuine profit. I've audited enough nights to respect one.",
                "Ahead of the house. Statistically, a visitor's privilege.",
                "Positive on the night. The tail of the curve you want.",
                "You're up. Fact, not forecast."));

        return new Persona("doc", "Doc Weaver", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(ruby(), hank(), docWeaver());
    }
}
