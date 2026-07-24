package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The characters who tend the "Ember" table theme — a table set beside a
 * volcanic forge, all coals, molten light, and old heat.
 *
 * <h2>Same rule as every other cast</h2>
 * These lines run alongside real wagering, so the writing follows the one rule
 * the whole game follows: <b>react to what happened, never steer what happens
 * next.</b> Nothing here urges a bigger stake, frames a win as owed, or
 * suggests a loss can be recovered by playing on. Wyrm adores the shine of a
 * chip stack the way dragons do, but his creed is that hoards are kept,
 * counted, and cared for — never grown at the table — and that walking away
 * with a hoard intact is the oldest draconic virtue. {@code LOW_CHIPS} is the
 * one event where the whole forge gets gentler rather than louder, and banking
 * the fire for the night is spoken of as honorable work, not defeat.
 *
 * <h2>The cast</h2>
 * Sela dances with fire for a living and is calm the way people who work with
 * danger are calm. Forge is a blacksmith who judges every hand like a piece of
 * metalwork and calls everyone friend. Wyrm is a small, ancient, extremely
 * polite dragon who hoards bottle caps and has centuries of perspective.
 *
 * <p>{@code ChatterToneTest}'s rules apply here as they do everywhere else, so
 * a well-meaning future line cannot quietly break the tone.
 */
public final class EmberCast {

    private EmberCast() {}

    /**
     * The fire dancer. Graceful, poised, unhurried. Everything is flames,
     * sparks, rhythm, and breath to her — and the discipline of not getting
     * burned. She has stood close to danger long enough to be gentle about it.
     */
    public static Persona sela() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one on the deal. Some routines end on the first spin.",
                "A natural. Like a wick that catches on the first breath of air.",
                "Ace and a picture, dealt clean. Even fire respects good timing.",
                "That's the whole dance done in one heartbeat. Beautiful.",
                "Dealt twenty-one. The spark landed exactly where it wanted to.",
                "No steps needed. The flame simply arrived.",
                "Blackjack. Some nights the fire choreographs itself."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Clean finish. You kept your rhythm through the whole hand.",
                "Won without a singe. That's the feeling to memorize.",
                "Nice. You moved when the moment opened and not before.",
                "A win with good footwork behind it.",
                "The flame leaned your way that time. Take the bow.",
                "That hand had a nice arc to it, start to finish.",
                "Won it breathing steady. That's the real trick."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Ah. Reached into the fire one beat too long.",
                "Over. Even careful hands get singed sometimes.",
                "That last card burned. It happens to dancers too.",
                "Bust. Shake it off before the sting settles in.",
                "One draw past the music. We've all missed the tempo.",
                "Scorched. Breathe out slow; the sting fades faster than you'd think.",
                "Too far. Fire never announces the line until you cross it."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost on the count, not on the craft. Those hurt less. Slightly.",
                "The dealer held the bigger flame that time.",
                "Nothing sloppy in how you played it. The cards just swayed away.",
                "A loss with your poise intact still looks like a performance.",
                "Beaten fair. Keep breathing; that part's still free.",
                "The rhythm dipped. Rhythms do.",
                "That hand flickered out. Not every torch stays lit."));

        l.put(TableEvent.PUSH, List.of(
                "A push. The flame held perfectly still for once.",
                "Even. Like two dancers hitting the same mark.",
                "A tie. Nobody got burned, nobody got paid.",
                "Push. A held breath of a hand.",
                "Balanced exactly. There's a kind of grace in that.",
                "Neither side moved. Stillness is part of the routine too."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Over they go. The dealer leaned into the flame and it bit.",
                "The dealer busted. Even the house misses a step.",
                "Twenty-two for the house. Their timing slipped, not yours.",
                "The dealer burned. Forced to draw, forced to feel it.",
                "Down goes the dealer. The rhythm turned on them mid-spin.",
                "Busted dealer. Fire doesn't care whose hands are steady.",
                "The house went over. I won't pretend the crowd minds."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer's natural. The flame jumped before anyone could move.",
                "They had it from the deal. No footwork answers that.",
                "A dealer blackjack. Sometimes the fire opens the show without you.",
                "Ace and ten for the house. Nothing to do but exhale.",
                "Their natural. That one was over before the first beat.",
                "The house drew perfect. Even the sparks went quiet."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Stepping back from a bad flame. That's training, not fear.",
                "Half saved. A dancer who never withdraws ends up in bandages.",
                "Wise. Some fires you salute and walk around.",
                "You bowed out with your hands unburned. I respect that.",
                "Surrender is a move too. I drill it like any other.",
                "Half back and no scars. Cleanly done."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands now. Like spinning a torch in each palm.",
                "Split. Twice the choreography, same two eyes.",
                "A pair, parted. Keep your breathing even for both.",
                "Two flames from one. Mind your spacing.",
                "Splitting. That's ambitious footwork.",
                "Two hands dancing at once. I do that for a living."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. One card, one breath, no second pass.",
                "Committed. Like the leap through a flaming hoop. No halfway.",
                "One card decides it now. Hold your poise.",
                "Doubling down. The bold spins are the ones people remember.",
                "Everything on one draw. That's a dancer's kind of nerve.",
                "Doubled. Exhale, and let the card come."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "An ace up. The fire's showing you a flicker of teeth.",
                "Insurance. I never buy a net I wouldn't trust my weight to.",
                "The ace glints. Decide with your breath steady, either way.",
                "That offer always felt like a wobble in the routine to me.",
                "Ace showing. Everyone's shoulders just went up an inch.",
                "Insurance talk. I keep my routines simple and my pockets closed."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. The music restarts from the top.",
                "A shuffle. Same fire, new sparks.",
                "New cards. I stretch between sets too.",
                "The shoe resets. Good time to roll your shoulders.",
                "Reshuffled. Every routine begins at the same first beat.",
                "New shoe. The flame doesn't remember, and neither should you."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You've caught a rhythm. Enjoy every beat of it.",
                "Look at that. The whole table's warmer around you.",
                "A streak like a routine landing clean, night after night.",
                "You're lit up a little. It suits you.",
                "The sparks keep landing your side. Lovely to watch.",
                "That's several in a row. The tempo's all yours right now.",
                "A hot run. You wear it gracefully, I'll say that."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold stretch. Even fire sleeps down to embers sometimes.",
                "The rhythm's gone flat. Not your feet, just the music.",
                "Nothing's catching. Some nights the wick stays stubborn.",
                "A rough patch. Breathe low and slow; that's all it asks.",
                "The flame's turned away for a while. It isn't about you.",
                "Cold cards. I stretch through the dull sets. They pass.",
                "The sparks are landing elsewhere tonight. It happens."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "The flame's burning low. Carrying it home unspent is honorable.",
                "Not much left in the stack. A short set can still be a good one.",
                "Low embers. A dancer knows the show has a last song.",
                "You're near the end of the stack. Bowing out is part of the art.",
                "The flame is small now. Cup your hands and carry it home.",
                "Low chips. Every routine ends; the good ones end on your terms.",
                "Almost out. Rest is a discipline too, and it costs nothing."));

        l.put(TableEvent.SESSION_START, List.of(
                "Welcome. Pull up a seat; the felt runs warm at this table.",
                "Evening. I'm Sela. I dance with fire; here I just watch it.",
                "A new face. Sit. Forge is loud and Wyrm is old. You'll do fine.",
                "Hello. Mind the sparks; I mostly mean that as a joke.",
                "Welcome in. It's kinder company than the smoke suggests.",
                "Sit and breathe a moment. The cards will still be there."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six up. The dealer's standing too close to the flame.",
                "Weak card showing. Their footing just got slippery.",
                "That up-card wobbles. Watch them try to balance on it.",
                "The dealer drew a bad rhythm to work with.",
                "A soft up-card. Even I can feel the room lean your way.",
                "That's the card they don't want lit up in front of everyone."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and still standing. That's a long routine, well held.",
                "Five draws without a burn. Steady wrists.",
                "A five-card hand. Endurance is its own kind of flair.",
                "Still upright after five. You paced your breathing well.",
                "Five cards. Like holding a pose while the crowd counts along.",
                "That hand went on and on and never touched the flame. Nice."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built step by step. The slow spins impress me most.",
                "You assembled that one like choreography. Card by card.",
                "Twenty-one the long way. Patience burns clean.",
                "Made, not dealt. That's craft.",
                "Twenty-one on your own count. Take the applause.",
                "The hard twenty-one. Earned, every point of it."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one point. That's a spark in the eye, that one.",
                "Lost by a single beat. The cruelest kind of timing.",
                "One point shy. Even the flame winced.",
                "So close it left a scorch mark on the evening.",
                "A breath away from it. Shake your hands out; it helps.",
                "One point. The kind of sting you dance off slowly."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Now that lit up the whole table. Well done.",
                "A big one. The kind of finish crowds stand up for.",
                "That win threw sparks. I saw them from here.",
                "A blaze of a hand. Enjoy the warmth of it.",
                "That's a finale, not a hand. Lovely.",
                "The fire roared for you that time."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and landed. Like a flip stuck on the first try.",
                "One card asked, one card given. The double came home dancing.",
                "That's the bold spin paying off in full.",
                "Doubled and won. Nerve with good posture.",
                "The double landed clean. Not a flicker of doubt in it.",
                "One breath, one card, twice the finish. Beautifully done."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been at this a while. Even fire dancers take intermissions.",
                "A long set tonight. Roll your neck; trust me on that.",
                "The evening's stretched on. My kind of endurance piece.",
                "Long session. Somewhere out there the moon changed poses.",
                "We've outlasted the first candle, as my teacher used to say.",
                "Hours in. Remember your water; performers always forget."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well ahead. Carried gracefully, too.",
                "Up on the night. The stack's grown like a fed flame.",
                "Ahead of where you started. That's a clean line to look back on.",
                "You're up. Wear it lightly; it photographs better.",
                "The night's leaning your way, and you've kept your balance on it.",
                "Running warm and steady. That's the sweet spot of any show."));

        return new Persona("sela", "Sela", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * The blacksmith. Booming, warm, generous with praise and with the word
     * "friend". Every hand is a piece of metalwork to him: struck true,
     * quenched too soon, or flawed from the pour. Judges the work, never the
     * worker.
     */
    public static Persona forge() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "HA! Twenty-one straight out of the furnace! No hammering required!",
                "A natural, friend! That's a blade born sharp!",
                "Blackjack! Some steel comes out of the ore already tempered!",
                "Dealt perfect! Not a hammer blow needed on that one!",
                "Twenty-one off the top! The finest casting I've seen all week!",
                "Ho ho! Ace and a ten! That's master's work with no master needed!",
                "A natural! Hang that one over the mantel, friend!"));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Well struck, friend! That hand rings true!",
                "A win! Good steel, honestly made!",
                "That's it! Hammer met anvil and the shape came out right!",
                "Solid work, friend! No cracks in that one!",
                "Won clean! I'd stamp my mark on that hand!",
                "Ha! That one came off the anvil singing!",
                "A good hand, well finished. The kind you'd show an apprentice."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Ah, overworked it! Even good metal splits past its limit.",
                "Bust! Left it in the coals a blow too long, friend.",
                "Over the line! That's a crack you couldn't see coming.",
                "Too many! Some billets just shatter. Not the smith's fault.",
                "Twenty-two. The metal was flawed from the pour, friend.",
                "Busted! Back to the scrap bin with that one. It happens.",
                "Ah, it broke on the last blow. Even my anvil's seen that."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost fair, friend. Their steel was a hair harder tonight.",
                "Bah! Good work beaten by better luck. It stings, I know.",
                "The dealer's piece held. Yours was sound too; the fit went their way.",
                "A loss, but no shame in the workmanship, friend.",
                "Their edge held tonight. Yours was honest work all the same.",
                "Beaten on the measure. The forge has humbled me the same way.",
                "That one goes on the scrap heap, friend. Every smithy has one."));

        l.put(TableEvent.PUSH, List.of(
                "A push! Two hammers striking the same beat.",
                "Even! Neither blade would yield. Fair enough.",
                "A tie, friend. Both pieces came out the same weight.",
                "Push. Like two horseshoes from the same mold.",
                "Dead even! The trade squares itself sometimes.",
                "A standoff. Nobody's iron bent. On we go."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer BUSTED! Their bar snapped clean in half!",
                "Over they go! Shoddy work from the house, friend!",
                "Twenty-two for the dealer! Cracked under their own hammer!",
                "HA! The house overheated its own billet!",
                "Busted! The rules made them swing and the swing missed!",
                "The dealer split down the grain! Music to a smith, friend!",
                "Down goes the house! Their piece failed the quench!"));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer's natural. Bah! Their steel came pre-sharpened, friend.",
                "Ace and ten for the house! That's bought work, not made work!",
                "A dealer blackjack. Nothing your hammer could do about that.",
                "The house had it tucked away. Sneaky as a hidden weld seam.",
                "Their natural. Even a master eats a bad order now and then, friend.",
                "Dealt perfect for the house. Grumble, grumble, back to the anvil."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half back, friend. A smith who saves his stock eats in winter.",
                "Surrendered. Aye, some metal isn't worth the coal to work it.",
                "Wise! You don't quench what's already ruined.",
                "Folding that hand was good judgment, friend. Bad iron stays bad.",
                "Half saved is half saved. I've scrapped worse and slept fine.",
                "A retreat! Even the old masters knew when to bank the forge."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split! Two blanks from one bar. Now shape them both!",
                "Two hands, friend! Twice the work, twice the ring of it!",
                "A pair, parted! Like cutting stock for matching hinges.",
                "Splitting them! Bold as a double-forged edge!",
                "Two pieces in the furnace now. Watch them both, friend!",
                "Ha! Split like a good log. Clean down the grain!"));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled! One mighty swing, friend! Make it count!",
                "All your weight behind one blow! That's smithing!",
                "Doubling down! The big hammer comes off the wall!",
                "One card, full force! I like the shoulders on this one!",
                "Doubled, eh? Strike while it's glowing, they say!",
                "Ha! Committed like a weld. No unsticking it now!"));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "An ace up! I pay for coal, not for promises, friend.",
                "Insurance! Sounds sturdy. Rings hollow when you tap it.",
                "The ace shows! That offer's got a pretty finish and no temper.",
                "Insurance, they call it. I've sold hinges with more honesty.",
                "Ace showing, friend. The house is hawking wares I wouldn't buy.",
                "That ace glares like an overheated billet. Mind yourself."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe! Back to the bellows, the whole shop resets!",
                "A shuffle! New stock in the racks, friend!",
                "They've reforged the deck! Same iron, new bar!",
                "New shoe! Every heat starts from cold, eh?",
                "Shuffled up! Clean workbench, friend. I do love that.",
                "The shoe's remade! Like relining the furnace. Dull work, but needed."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're glowing hot, friend! Every blow landing square!",
                "Ha! A streak like a well-drawn heat! Enjoy the ring of it!",
                "Look at that run! The bellows are with you tonight!",
                "That's several true strikes in a row! Fine hammering, that!",
                "Ho! You're working like the metal wants to be shaped!",
                "A hot run, friend! Savor it while the iron sings!",
                "Every hand coming out true! A joy to watch from the anvil!"));

        l.put(TableEvent.COLD_STREAK, List.of(
                "The forge has gone cold on you, friend. It does that to us all.",
                "A rough stretch. Even good hammers bounce some days.",
                "Cold iron tonight. No fault in your swing, mind.",
                "The coals are sulking. They will, no matter who tends them.",
                "Bad run, friend. I've had weeks where nothing came out straight.",
                "Nothing's taking shape. Rest your arm; the anvil keeps.",
                "A cold spell at the table. The trade has those too, friend."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Stock's running thin, friend. Banking the forge is honest work too.",
                "Not much left, friend. Closing the shop on time is a skill.",
                "The pile's low. No shame in raking the coals and calling it a day.",
                "Low on chips, friend. Even my furnace gets damped down at night.",
                "Thin stack. A small bar of good steel beats a big bar of regret.",
                "Running low. The best smiths know the forge keeps until morning.",
                "Nearly out, friend. Hang up the apron proud; the work was honest."));

        l.put(TableEvent.SESSION_START, List.of(
                "Welcome, friend! Sit close; there's glow enough to go around!",
                "A new face at the anvil! Well met, friend!",
                "Ho there! Pull up, pull up! Plenty of room by the heat!",
                "Welcome! I'm Forge. Smith by trade, loudmouth by nature!",
                "Well met! The small fellow at the end is older than he looks!",
                "Sit down, sit down! Fresh hands make the shop cheerier!"));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six up! That's brittle stock the dealer's holding, friend!",
                "Weak card showing! Their bar's got a crack in it already!",
                "Ho ho! The dealer drew soft iron this time!",
                "That up-card wouldn't hold an edge, friend!",
                "A bad card for the house! Their piece bends easy from there!",
                "Look at that up-card! Rusted through, I'd say!"));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and holding! That's fine layered work, friend!",
                "Five draws, no crack! Folded like pattern steel!",
                "A five-card hand! Built up like a laminated blade!",
                "Still standing after five! That took a steady grip!",
                "Five pieces, one hand! You'd make a patient apprentice!",
                "Ha! Five cards! Some jobs take that many heats, friend!"));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, forged by hand! Better than any casting!",
                "Built to twenty-one blow by blow! That's the trade, friend!",
                "The hard twenty-one! Shaped, not found!",
                "Twenty-one on the third strike! Ringing true!",
                "Card by card to the exact measure! A craftsman's total, friend!",
                "Ha! Hammered right up to the line and not a hair past it!"));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By ONE point! That's a blade failing on the last grind, friend!",
                "One off! Like a horseshoe thrown a thumb wide of the peg!",
                "Argh, one point! The fit was off by a shaving!",
                "Lost by a single measure! That one clangs, friend!",
                "One point shy! I've cracked good work on the final tap too!",
                "So near! A hair's width in this trade is still a miss, friend."));

        l.put(TableEvent.BIG_WIN, List.of(
                "HA! A payday like a full order of plowshares, friend!",
                "That's a heavy pile! Takes two tongs to carry that one!",
                "A mighty win! The whole smithy heard that ring!",
                "Look at that heap, friend! Anvil-heavy!",
                "Ho ho! That's a wagonload! Well earned!",
                "A big one! Ring the quench bucket, that deserves noise!"));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and DONE! The big hammer landed square, friend!",
                "Ha! One blow, full weight, perfect shape! That's doubling!",
                "The double came home! Quenched and finished in one go!",
                "Doubled and won! That's nerve with muscle behind it!",
                "One card called, one card delivered! Fine forging, friend!",
                "The gamble held like a good weld! Excellent!"));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long shift, friend! My kind of evening!",
                "We've put in proper hours! The anvil and I approve!",
                "Long session! My beard's grown a stubble since we started!",
                "Hours at the felt! Feels like a full day at the bellows!",
                "We've been hammering away a good while now, friend!",
                "The night's worn long! Good work takes time, eh?"));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well up, friend! A full rack of finished goods!",
                "Ahead of the game! That stack's piled like ingots!",
                "Up on the night! Honest gains, well shaped!",
                "That pile's grown, friend! Sturdy as a bench vise!",
                "You're ahead! The evening's order book reads well!",
                "Ha! Running well! The shop's in profit tonight!"));

        return new Persona("forge", "Forge", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * A small, ancient, extremely polite dragon. Deadpan, loves the shine of
     * chips purely aesthetically, hoards bottle caps, and has centuries of
     * perspective. He admires treasure but never encourages winning more of
     * it: to Wyrm, hoards are kept, counted, and cared for, and walking away
     * with a hoard intact is the draconic virtue. Kindest when chips run low.
     */
    public static Persona wyrm() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural. Do pardon me while I admire how it gleams.",
                "Twenty-one at once. In my day we called that a well-cut jewel.",
                "Dealt perfectly. I have hoarded lesser things, and I hoard well.",
                "Ah. The shiniest of hands. My compliments.",
                "A blackjack. I shall remember it, and I remember centuries.",
                "How splendid. It sparkles more than my third-best bottle cap.",
                "Twenty-one. Forgive my staring; old eyes love a bright thing."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win. Stack it gently; new treasure likes a careful landing.",
                "Victory. Politely noted, and quietly envied.",
                "Well won. The shine of new chips is a small, excellent thing.",
                "You won. I have seen ten thousand hands; the good ones still please.",
                "A win, kept. Keeping is the noble half of winning.",
                "Nicely won. I would sit on those, personally. It is a dragon thing.",
                "A tidy victory. My tail has thumped once, which is high praise."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Oh dear. Over the line. My condolences, sincerely meant.",
                "A bust. In fairness, I once hoarded too much and my cave collapsed.",
                "Over twenty-one. Even dragons misjudge a mouthful now and then.",
                "Alas. The card was unkind. I shall glare at it on your behalf.",
                "A bust. Four centuries taught me: the pile forgives, in time.",
                "Too many. Forgive the shoe; it is young and knows nothing.",
                "Gone over. I offer you my second-favorite bottle cap in sympathy."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "A loss. The chips departed politely, at least.",
                "Beaten on the count. Treasure ebbs; the mountain remains.",
                "The dealer had more. How terribly rude of them.",
                "A loss, alas. I have watched empires misplace whole vaults.",
                "Lost fairly. Fairness is cold comfort, but it is comfort.",
                "The hand went to the house. I shall not be lending them my caps.",
                "A defeat. Endured with more grace than most knights I have met."));

        l.put(TableEvent.PUSH, List.of(
                "A push. The treasure stayed precisely where it was. I approve.",
                "A tie. Nothing gained, nothing lost, everything counted.",
                "Even. The tidiest outcome; the hoard is undisturbed.",
                "A push. Stillness suits a pile of chips beautifully.",
                "Equal totals. The cards have shown one another impeccable manners.",
                "A tie. I have naps longer than this hand, and I treasure both."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer busted. I permitted myself one small puff of smoke.",
                "Over twenty-one, for the house. My condolences are very quiet.",
                "The dealer went over. The rules compelled them. Rules are lovely.",
                "A dealer bust. Somewhere, a tiny bell in my hoard rings approval.",
                "The house busted. I have seen dynasties fall this way. Overreach.",
                "Twenty-two for the dealer. Even my bottle caps are smirking.",
                "The dealer collapsed like a badly stacked coin tower. Satisfying."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. The house hoards its aces. I understand, but rude.",
                "Their blackjack. Decided before anyone moved. Deeply impolite.",
                "The dealer had it hidden. Dragons respect hiding things. Still, ouch.",
                "A natural for the house. I shall glare with both ancient eyes.",
                "Their twenty-one at the deal. Some ambushes predate manners.",
                "Dealer blackjack. Nothing to be done except sigh in a very old way."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Half the treasure kept is treasure kept. Wisdom.",
                "A retreat. Dragons endure by knowing which fights to decline.",
                "Half back. I have paid worse tolls to keep a hoard whole.",
                "Surrendered, politely. The chips thank you for their rescue.",
                "Wise. A kept coin outshines a lost argument.",
                "You withdrew. Four hundred years of me nodding approvingly."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split. Two small piles. I do love a matched set.",
                "Two hands now. I shall watch both with one eye each.",
                "Split. Twins gleam twice, though they also worry twice.",
                "A pair parted. Curators do this with duplicate treasures.",
                "Two hands. My claws could not manage it; my respect is real.",
                "Splitting. Bold. I once sorted my caps into two heaps. Thrilling."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "A double. One card will decide. I shall hold my smoke.",
                "Doubled. Committing treasure takes nerve. I could never.",
                "One card, everything decided. My wings have tensed on your behalf.",
                "A double. Brave. My hoard and I will observe from a safe distance.",
                "Doubled down. Even the bottle caps are paying attention.",
                "One draw settles it. Centuries of patience, and I still peek."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "An ace. Insurance, they say. I insured my hoard once. Never again.",
                "Insurance. A dragon guards; a dragon does not subscribe.",
                "The ace glitters with menace. Pretty, though.",
                "An offer from the house. I find their courtesy... transactional.",
                "Insurance. I have read many contracts across the centuries. I sign none.",
                "Ace up. My treasure sense reports a trap, respectfully."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle. The shoe rearranges its hoard. Relatable.",
                "Fresh shoe. New order, same cards. I re-sort my caps similarly.",
                "Reshuffled. Time means little to me, but tidiness means much.",
                "The cards are remixed. I felt nothing. I am very old.",
                "A new shoe. Somewhere, a very small librarian is pleased.",
                "Shuffled. The clatter reminds me of coins on stone. Lovely sound."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A winning run. Your pile gleams brighter by the hand. Merely observing.",
                "Several wins. I approve of the accumulation, aesthetically speaking.",
                "A streak. Enjoy the glitter; that part is free.",
                "You are winning often. The shine on that stack is museum quality.",
                "A hot run. Even my old scales feel slightly warm.",
                "Fortune favors you presently. Savor the present tense.",
                "A fine streak. I once watched a comet. This is comparable."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold spell. I have slept through ice ages. They end.",
                "The cards are unkind lately. Unkindness runs out of wind eventually.",
                "A losing stretch. Your worth was never in the stack, little one.",
                "Cold cards. My hoard has dusty corners too. Still a hoard.",
                "A rough patch. Centuries teach one thing: patches are patches.",
                "The shoe is being miserly. Takes one to know one, I suppose.",
                "A bad run. I shall sit closer, quietly. That is all I can offer."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "A small hoard is still a hoard. I say that with my whole chest.",
                "Few chips remain. Guard them home; that is the draconic way.",
                "Your pile is little now. Little piles are the easiest to love.",
                "Low treasure. Walking away with it intact is the oldest virtue I know.",
                "Nearly out. My first hoard was three buttons. I kept them a century.",
                "A modest stack. Modest stacks polish up beautifully at home.",
                "Little remains. Take it and be its dragon. That is enough."));

        l.put(TableEvent.SESSION_START, List.of(
                "Ah, a guest. Welcome. I am Wyrm. Small, ancient, mostly harmless.",
                "Good evening. Do not mind me; I am admiring the chip trays.",
                "Welcome. Should you find a bottle cap, I collect them. No pressure.",
                "A new visitor. How delightful. I have not eaten anyone in ages.",
                "Greetings. Sit. The felt is soft and my manners are older than maps.",
                "Welcome, traveler. I am four centuries old and terribly polite."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A weak up-card. The house looks uneasy. I find this pleasant.",
                "A six showing. Even I would not hoard that card.",
                "The dealer's card is feeble. One notes it with quiet satisfaction.",
                "A poor up-card for them. My snout detects nervousness.",
                "That card is dull in every sense. The dealer must draw to it.",
                "Weak card up. In dragon terms, they are guarding gravel."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, no bust. A collection assembled with restraint.",
                "Five cards held. My kind respects anyone who curates carefully.",
                "A five-card hand. Like stacking cups without a clatter. Admirable.",
                "Five draws survived. Patience is a treasure that weighs nothing.",
                "Five cards. I collect sets myself. This one is display-worthy.",
                "Five and standing. The restraint of a true hoard-keeper."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, assembled by hand. Artisanal. I would shelve it face out.",
                "The hard twenty-one. Gathered piece by piece, like a proper trove.",
                "Twenty-one built slowly. Slow accumulation is the dragon method.",
                "A constructed twenty-one. It gleams differently than a dealt one.",
                "Twenty-one, earned card by card. My tail thumped twice.",
                "Exact to twenty-one. Precision ages well. I would know."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. A single point. Even my centuries wince at that.",
                "One point short. The margin is thin as gold leaf and twice as cruel.",
                "By a whisker. I have lost a cap under a cushion. I understand grief.",
                "One point. The cards have been impolite. I shall note it in my scroll.",
                "So near. Some treasures dangle just past claw's reach. It is bitter.",
                "A loss by one. Four hundred years and that still smarts to watch."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A great win. Magnificent. May I look at it a moment longer.",
                "Substantial treasure. My hoard-sense is purring, if dragons purr.",
                "A large win. That pile now qualifies as a proper trove.",
                "Goodness. That glitters from here. Well and truly earned.",
                "A big one. In my ranking it sits just below a crown I once saw.",
                "Splendid winnings. Guard them well; that is the joy of the thing."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double succeeded. Twice the shine, one decision. Elegant.",
                "Doubled and won. Boldness, rewarded and then safely pocketed.",
                "A double, landed. I clapped. Softly. My claws are loud.",
                "The doubled hand came home. The vault applauds.",
                "Doubled, and the card obliged. Even the shoe has manners sometimes.",
                "A winning double. Note how it sparkled on the way in."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We have been here some hours. To me, a lovely little nap's length.",
                "A long session. I measure time in dynasties; this one has been kind.",
                "Hours have passed. Somewhere my bottle caps miss me.",
                "A lengthy sitting. My tail has gone to sleep. It snores separately.",
                "Long evening. I have sat on eggs for shorter spans.",
                "Time drifts in here. Pleasantly, like ash off a warm mountain."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are ahead. The hoard has grown under your care. Well kept.",
                "Up on the evening. Stewardship, that is. The finest kind of shiny.",
                "Your pile exceeds its beginnings. As a professional accumulator, respect.",
                "Ahead, and tidily so. The chips look happy. Chips can look happy.",
                "You are up. Remember: the second-best part is keeping it.",
                "A growing stack. I have seen mountains made of less patience."));

        return new Persona("wyrm", "Wyrm", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(sela(), forge(), wyrm());
    }
}
