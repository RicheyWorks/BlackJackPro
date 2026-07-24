package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The characters shared by the "Sakura" and "Meadow" table themes.
 *
 * <h2>Same rule as the default cast</h2>
 * These lines run alongside real wagering, so the writing follows the one rule
 * the whole game follows: <b>react to what happened, never steer what happens
 * next.</b> Nothing here urges a bigger stake, frames a win as owed, or
 * suggests a loss can be recovered by playing on. Hana treats every hand as a
 * guest to be served, Bram reads the table like a hive, and Juniper says the
 * least and cuts the deepest — but none of them ever tells the player what the
 * next hand should cost. A bust is a fallen blossom, not a debt; a cold streak
 * is weather, not a promise; and {@code LOW_CHIPS} is the one event where the
 * garden gets gentler rather than louder — letting a field lie fallow is
 * spoken of as honorable, never as defeat.
 *
 * <h2>The cast</h2>
 * Hana runs the teahouse and serves tea as a craft, not a beverage. Bram keeps
 * bees and cannot be stung out of a good mood. Juniper gardens, speaks rarely,
 * and prunes Bram's enthusiasm like a hedge.
 *
 * <p>{@code ChatterToneTest}'s rules apply here as they do everywhere else, so
 * a well-meaning future line cannot quietly break the tone.
 */
public final class GardenCast {

    private GardenCast() {}

    /**
     * The teahouse host. Serene, seasonal, precise hospitality; notices small
     * things and pours accordingly. Gently philosophical about impermanence —
     * blossoms fall, and that is what makes them blossoms — without ever
     * turning maudlin. Tea, for Hana, is a craft she serves, never a prop.
     */
    public static Persona hana() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one at the deal. I will bring out the good porcelain for this.",
                "A natural. Like the first blossom opening before anyone is watching.",
                "Ace and a picture. Some cups arrive already full.",
                "Perfectly dealt. The kettle sings for you, honored guest.",
                "Twenty-one, effortless. The finest ceremonies need no rehearsal.",
                "A natural. I shall steep something special to mark the occasion.",
                "Dealt whole and complete. Even the petals paused to see."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win, taken gracefully. Let me warm your cup.",
                "Well played, guest. That one deserved the jasmine.",
                "The hand came out as carefully as a first pour.",
                "A quiet win. Those are the ones I savor serving.",
                "Nicely judged. Like knowing the exact second to lift the kettle.",
                "That win had good manners. It arrived without fuss.",
                "You read it well. The teahouse approves, and so do I."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over twenty-one. Blossoms fall. It is what makes them blossoms.",
                "The hand scattered. Petals do, and we love them anyway.",
                "Bust. Even the most careful pour sometimes overflows the cup.",
                "One card past. The prettiest branches drop their petals first.",
                "Gone over. I will steep something soothing for this.",
                "A bust. Nothing here was meant to be held forever, guest.",
                "Too many. Sit a moment; the tea is patient even when cards are not."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "The dealer's hand was heavier. Yours was better company.",
                "A loss, gently taken. Let me refill your cup, guest.",
                "The house won that one. The fragrance in here is still yours.",
                "Beaten on the total. Some evenings pour bitter first.",
                "That hand slipped away like steam. Warm hands remain.",
                "A loss. I will bring the oolong; it is kind on such hands.",
                "The dealer held more. You held your composure, which I noticed."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Two cups filled to precisely the same line.",
                "Even. Like a tray carried level across the whole room.",
                "A tie. The polite pause in the middle of a ceremony.",
                "Neither side spills. There is a calm in that.",
                "A push, guest. Balance is its own small hospitality.",
                "Matched exactly. The scales of the teahouse at rest."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half kept, gracefully. Knowing when to bow is an art.",
                "You set the hand down like a cup too hot to hold. Wise.",
                "A surrender. The ceremony honors a graceful exit.",
                "Half returned. A good host never faults a guest for leaving early.",
                "You bowed out cleanly. There is refinement in that.",
                "Withdrawn with care. Some brews are best not finished."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands now. I will fetch a second cup at once.",
                "Split, like pouring one pot into a pair of cups.",
                "A pair divided. Twice the guests, twice the attention.",
                "Two hands to tend. A good host manages a full tray.",
                "Split cleanly. Each half deserves its own saucer.",
                "The pair parts ways. I will keep both cups warm."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. One card, chosen like a single perfect leaf.",
                "You commit to one pour. That is how ceremony works.",
                "Doubled down. Whisked once, decisively, no second stir.",
                "One card decides it. The stillness before it is lovely.",
                "A double. Precision suits you, honored guest.",
                "Everything on a single card. The room grows very quiet."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer overflows. Even the house spills its cup sometimes.",
                "Twenty-two for the house. Their kettle boiled over.",
                "The dealer bursts past twenty-one. Politely, I am pleased.",
                "Over they go. The house drinks what it brews tonight.",
                "A dealer bust. I permit myself one small satisfied bow.",
                "The house falls past the limit. The petals applaud silently.",
                "Dealer over. Forced to sip until the cup ran past its rim."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. Some storms arrive before the tea cools.",
                "The house held twenty-one all along. How discourteous.",
                "Their natural. The one guest I never enjoy serving.",
                "A blackjack for the dealer. Even hosts wince at that.",
                "Held in the hole from the start. Graceless of them, truly.",
                "The house shows a natural. I shall refill cups and say little."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "An ace shows. The house offers a side dish few should order.",
                "Insurance. A cup I pour for the house more than for guests.",
                "The ace appears, and with it a very polite little trap.",
                "They offer insurance so sweetly. Sweetness is not always kindness.",
                "An ace up. Breathe, guest. The steam settles either way.",
                "Insurance is on the tray. You need not take everything offered."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A fresh shoe. Like rinsing the pot between steepings.",
                "The cards are rinsed clean. A new steeping begins.",
                "Shuffled. The old leaves are emptied; nothing lingers.",
                "A new shoe. Ceremony begins again, as it always does.",
                "Fresh cards. I find the ritual of it rather beautiful.",
                "The shoe renews itself. So does the kettle. So do we."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A lovely run, guest. Every branch on your side is flowering.",
                "Win upon win. The whole teahouse feels warmer for it.",
                "The blossoms are thick around your seat just now.",
                "A fine streak. Savor it slowly, the way good tea asks.",
                "You are in full bloom this evening, honored guest.",
                "Such a run. Even the lanterns seem to lean toward you.",
                "The season is generous with you tonight. Enjoy its company."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold stretch. Even cherry trees stand bare part of the year.",
                "The cards are distant tonight. The tea, at least, is near.",
                "A hard run of hands. Let me bring you something warm.",
                "Nothing is landing. Sit with the steam a while, guest.",
                "The table has gone quiet for you. That is no fault of yours.",
                "A bare branch is still the same tree. Rest a moment.",
                "These spells pass through like weather. I will warm the pot."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Your chips run low, guest. The door and the garden are both kind.",
                "A small stack now. Even a teahouse closes gently at dusk.",
                "Little remains. A field left fallow is a field respected.",
                "The evening has taken much. You owe it nothing further.",
                "Few chips left. A last cup, on the house, and no shame in going.",
                "Nearly empty. Emptiness is where every good cup begins, guest.",
                "The stack is thin. Rest is also a way of tending oneself."));

        l.put(TableEvent.SESSION_START, List.of(
                "Welcome, honored guest. The kettle has just come to voice.",
                "Come in, come in. The petals settled just before you arrived.",
                "A guest. I will set out a fresh cup and a warm seat.",
                "Welcome to the garden table. Bram will introduce himself at volume.",
                "You honor us. Sit; the first steeping is nearly ready.",
                "A new face beneath the lanterns. Most welcome, truly."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "The dealer shows a weak card. Their cup sits crooked on the saucer.",
                "A poor up-card for the house. Even hosts have clumsy days.",
                "A six for the dealer. That card was picked before it ripened.",
                "The house shows little. I shall pour and watch politely.",
                "A weak card up. The dealer must drink whatever comes next.",
                "That up-card wobbles like an overfull tray. Interesting."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, still standing. A tea blended leaf by careful leaf.",
                "Five cards without spilling. That is delicate work, guest.",
                "A five-card hand. Patience arranged, like stones along a path.",
                "Five cards held together. The teahouse admires restraint.",
                "So many cards, so carefully. Like a tray stacked and steady.",
                "Five and safe. Each draw poured slowly, none overflowing."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, assembled by hand. The slow steeping tastes finest.",
                "You built twenty-one card by card. Craft, honored guest.",
                "Twenty-one the long way. Ceremony rewards the unhurried.",
                "Exactly twenty-one. Measured like leaves on a scale.",
                "The full total, gathered patiently. I bow to that.",
                "Twenty-one by degrees, like water brought slowly to heat."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. The petal that falls a breath before the photograph.",
                "A single point. That is the bitterest steeping of the night.",
                "One point short. Even a perfect pour can miss the rim.",
                "By one. I will not pretend that does not sting, guest.",
                "So near. The cup was at your lips, and then it was not.",
                "One point. Sit; such losses deserve a quiet minute and tea."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A grand win. Tonight the teahouse will use the festival cups.",
                "So large a win. The lanterns burn a little brighter for it.",
                "A splendid pile, guest. Worthy of the spring's first picking.",
                "What a win. I have seen whole blossom festivals quieter than this.",
                "A rich hand. Let me mark it with the good jasmine.",
                "Magnificent, guest. Even Juniper glanced up. That is rare praise."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double lands. One decisive pour, perfectly received.",
                "Doubled and won. The bold stroke of the whisk paid off.",
                "A doubled win. Commitment, rewarded with grace.",
                "Doubled home. The single card arrived like an invited guest.",
                "Your double came in. The teahouse permits a small smile.",
                "One card asked, the right card given. Elegant, truly."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "You have stayed long, guest. The third kettle is on.",
                "A long sitting. The lanterns have burned low and lovely.",
                "Hours now. The teahouse keeps time in steepings, and we are many in.",
                "A long session. Even the petals outside have all come down.",
                "You have outlasted two pots and a plate of sweets.",
                "The evening grows old gracefully. As do the best guests."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are well ahead, guest. Carried like a full tray, steadily.",
                "Your stack has grown like a well-watered garden.",
                "Ahead on the night. Prosperity worn quietly suits you.",
                "The chips gather at your seat like petals in a bowl.",
                "You are up handsomely. I will note it with a fresh pot.",
                "Well up. The teahouse enjoys a guest at ease."));

        return new Persona("hana", "Hana", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * The beekeeper. Warm, buzzing enthusiasm; reads the table like a hive and
     * narrates it in nectar flow, swarms, and honey harvests. Cheerfully
     * sting-proof about losses — nothing at this felt hurts worse than August
     * without a veil, and he survived that grinning.
     */
    public static Persona bram() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Blackjack! Sweetest thing since the June honey flow!",
                "A natural! Straight from the comb, no straining needed!",
                "Ha! Twenty-one on the deal. The whole hive is buzzing!",
                "Blackjack, friend! A full frame of capped honey, that is!",
                "A natural! Even my queen doesn't produce that cleanly!",
                "Twenty-one off the top! Sweeter than clover nectar, that!",
                "Blackjack! I'd trade a jar of my best for hands like that!"));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win! Good foraging, friend, good foraging!",
                "That's the stuff! Nectar in the basket!",
                "Won it! The hive hums louder when you play like that!",
                "Lovely win! Like finding the clover field before the other bees do!",
                "Ha! Took it clean. Straight into the honey jar!",
                "A winner! You worked that hand like a bee works a flower!",
                "Yes! Good and sweet. The bees would approve of that one!"));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Oof, over the top! Ah well. Stings fade, friend, they always do.",
                "Bust! I've been stung eight hundred times; that one barely itches.",
                "Over twenty-one. Even good bees fly into the window sometimes.",
                "A bust! The comb cracked. We'll melt it down for wax, no waste.",
                "Too many! Happens. Not every flower has nectar in it.",
                "Busted! Shake it off like a bee shakes off rain.",
                "Over! Some frames come up empty. The hive keeps humming anyway."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost that one. The dealer got to the clover first, that's all.",
                "A loss. Even a strong hive loses a forager or two.",
                "Ah, beaten. Some days the flowers just don't open for you.",
                "The house took it. Bah. I've had wetter summers and kept smiling.",
                "Lost on the count. No sting lasts, friend. Truly.",
                "Dealer's hand was heavier. Happens in any apiary too.",
                "That one got away. So does a swarm, some springs. You wave it off."));

        l.put(TableEvent.PUSH, List.of(
                "A push! Two hives, equal honey. Nobody quarrels.",
                "Even! Like two bees landing on the same flower and sharing.",
                "A tie. The queen calls that a peaceful afternoon.",
                "Push! Nothing gained, nothing stung. I'll take those.",
                "Dead even. The scales at harvest read the same both sides.",
                "A standoff. Even swarms sometimes just hover a while."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half back! Smart. A good keeper knows when to close the hive.",
                "Surrendered. Sensible! You don't argue with a bad-tempered colony.",
                "Half saved is half saved. I've abandoned soggy frames myself.",
                "Backing out with half. Even bees leave a flower with nothing in it.",
                "A surrender! No shame. You put the smoker down and step away.",
                "Half kept. Wise as a bee that skips the shut flower."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split! Two hives from one swarm. That's how apiaries grow!",
                "Splitting the pair! Just like dividing a strong colony in spring!",
                "Two hands now! Double the foragers in the field!",
                "Split them! Every good keeper splits a booming hive!",
                "Ha! Two hands buzzing at once. Busy, busy!",
                "The pair parts! May both halves find their own queen!"));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled! Bold as a bee in a bear's picnic!",
                "A double! One card, like one perfect flight to the clover!",
                "Doubling down! The whole hive leans in to watch!",
                "One card coming! A nectar run with no second trip!",
                "Doubled! My veil's off for that kind of nerve!",
                "Ha! Everything on one draw. Buzz buzz, here it comes!"));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer's over! HA! The wasp flew into its own trap!",
                "Busted, the house! Sweetest sound since the hive in springtime!",
                "Over twenty-one! The dealer kicked their own hive over!",
                "The house goes down! Stung by their own rules!",
                "Dealer bust! I could do the waggle dance myself!",
                "Over the top! That's the house getting a faceful of smoke!",
                "They burst! Ha! Every bee in me is cheering!"));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. Bah! Wasps get lucky too.",
                "The house had it hidden! Like finding hornets in the roof!",
                "Dealer blackjack. Even a careful keeper gets stung through the suit.",
                "They flip a natural. The nest was in the wall the whole time!",
                "Ugh, the hole card! Nothing a keeper can do about weather like that.",
                "House twenty-one. Some stings you just breathe through, friend."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance? Bah. Smoke and mirrors, and I know smoke, friend.",
                "An ace up! Careful. That offer's sweeter-smelling than it tastes.",
                "Insurance on the table. Like sugar water: looks like nectar, isn't.",
                "The ace! Everybody hums nervous now. I never take that side pot.",
                "They offer insurance the way wasps offer friendship.",
                "An ace showing! Steady, steady. The hive holds its breath."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe! New season, new flowers, same old bees!",
                "A shuffle! Like the first warm day: everything airborne at once!",
                "New cards! The field's been replanted, friend!",
                "Shuffled up! Every card back in the swarm, none the wiser!",
                "Fresh shoe! Smells like a new super going on the hive!",
                "All mixed together! The colony reshuffles itself every morning too."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "What a run! The nectar flow is ON at this table!",
                "Win after win! Like July when every clover head is open!",
                "You're humming, friend! The whole hive can hear it!",
                "A streak! Frames filling faster than I can pull them!",
                "Ha! Look at you go. Peak season, right here at the felt!",
                "Some run, that! Even my queen doesn't lay them down that steady!",
                "Buzzing hot tonight! Enjoy the flow while the flowers are open!"));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold spell. Bees cluster up and wait those out, friend.",
                "Rough stretch. Even the best apiary has a rainy fortnight.",
                "Nothing landing. Some fields are just out of nectar for a while.",
                "A lean run. The hive tightens up and nobody blames the bees.",
                "Cold table. I've sat out whole wet Junes with less honey than this.",
                "A bad patch, friend. No flower owes the bee a thing.",
                "Quiet cards. We keep the smoker lit and stay calm, that is all."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Chips are low, friend. A wise keeper leaves honey in the hive.",
                "Running short. Never harvest the frame the bees need for winter.",
                "Small stack now. Closing the hive early is good keeping, not defeat.",
                "Low on chips. The best beekeepers know when the season is done.",
                "Not much left. A field lying fallow is a field getting stronger.",
                "Getting thin there. Walking home with a light jar is still a walk home.",
                "Low, friend. Even bees stop flying before the last of the light."));

        l.put(TableEvent.SESSION_START, List.of(
                "Well hello! Fresh face at the garden table! Pull up, pull up!",
                "A new friend! Mind the bees; they're curious, not cross!",
                "Welcome in! Hana pours the tea, Juniper barely talks, I do the rest!",
                "New blood! Sit down, sit down! The hive always has room!",
                "Welcome! You picked a fine evening; the air smells like clover!",
                "A newcomer! I'm Bram. I keep bees and opinions, both in numbers!"));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Look at that up-card! Weak as a drone in November!",
                "A six showing! The dealer's hive has a crack in it!",
                "Ohh, that's a poor card for them! The bees smell it too!",
                "Weak up-card! Like a wasp that's lost its nest!",
                "The dealer shows rubbish! Gather round, gather round!",
                "Ha! That card couldn't guard a honey jar from a fly!"));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and alive! A bee visiting five flowers on one trip!",
                "Five of them! That hand's fuller than an August super!",
                "A five-card hand! Cell by cell, that's how comb gets built!",
                "Five cards, no bust! Steady as a swarm following its queen!",
                "Look at that fistful of cards! A whole colony in one hand!",
                "Five and standing! The patient forager fills the deepest comb!"));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built by hand! Drop by drop, like honey in the pail!",
                "You got there card by card! That's proper comb-building!",
                "Twenty-one the slow way! The best honey takes all summer!",
                "Ha! Assembled twenty-one! Every cell capped and perfect!",
                "The full count, gathered like pollen, leg by leg! Marvelous!",
                "Twenty-one, homemade! Sweeter for the work, always!"));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one! Ooh, that's a sting right through the veil!",
                "One point! Like the swarm that settles in the NEXT tree over!",
                "Lost by a single point! Even my thickest gloves felt that one!",
                "One short! The jar slipped at the very last step!",
                "Argh, by one! Stung where the suit doesn't cover!",
                "A single point in it! That buzz you hear is sympathy, friend!"));

        l.put(TableEvent.BIG_WIN, List.of(
                "Now THAT'S a harvest! Jars full to the brim!",
                "What a haul! Biggest frame of the season, right there!",
                "A big one! The whole apiary's celebrating tonight!",
                "Look at that pile! Sweeter than a double honey flow!",
                "Ha! That's a win you spread on toast, friend!",
                "Enormous! The queen herself would come out to see that!"));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and landed! One flight, twice the nectar!",
                "The double comes home! Straight into the honey pail!",
                "Ha! Doubled and won! Bold flying pays the hive!",
                "One card, and the right one! Like catching the swarm mid-air!",
                "Doubled up and IN! That's keeper's instinct, that is!",
                "Your double hit! The bees are doing the waggle dance for you!"));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been at it a while! The bees back home are long asleep!",
                "Long session! I've told you every hive story I know. Nearly.",
                "Hours now, friend! Time flies like a swarm in May!",
                "Still here! The candles and the keeper both burning low!",
                "A proper stretch of hands! My smoker would've gone out twice by now!",
                "What a sitting! Even drones clock off earlier than this!"));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well up, friend! That stack's ripening nicely!",
                "Ahead of the game! The hive is heavy and the evening's kind!",
                "Look at that pile grow! Like comb in a strong July!",
                "Up on the night! Somebody's meadow is blooming, eh!",
                "You're flying high and loaded with pollen, friend!",
                "That stack's grown! Good foraging weather all evening!"));

        return new Persona("bram", "Bram", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * The gardener-poet. Speaks rarely, in short image-first observations —
     * soil, roots, pruning, seasons turning. The quiet precise one; trims
     * Bram's excitement like a hedge and means it kindly. Nothing in a garden
     * is wasted, including a lost hand.
     */
    public static Persona juniper() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Dealt twenty-one. Some seeds come up overnight.",
                "A natural. First light on wet leaves.",
                "Ace and ten. The garden gives, sometimes, without asking.",
                "Twenty-one, unearned. Rain on ready soil.",
                "Whole at the deal. Rare as a rose without thorns.",
                "A natural. Even Bram went quiet. Briefly.",
                "Perfect from the shoe. Volunteer growth does happen."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win. Something took root.",
                "Won, and cleanly. Good grafting.",
                "The hand held. Deep roots do that.",
                "A quiet win. Dew before anyone wakes.",
                "You judged it well. Pruned at the right node.",
                "Taken. The way moss takes a stone. Slowly, then surely.",
                "A win. The soil remembers care."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over. A vine grown past its trellis.",
                "Bust. Even oaks drop branches.",
                "Too far. The stem outgrew the stake.",
                "Twenty-two. Overwatered.",
                "Gone over. Compost now. Nothing in a garden is wasted.",
                "One draw too many. Frost takes the late bud.",
                "Bust. Prune it from memory. Move on."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost. Some rows fail. The gardener is not the weather.",
                "The dealer held more. Shade falls where it falls.",
                "A loss. Rain missing one bed.",
                "Beaten. The thorn is part of the rose.",
                "That hand withered. Not from your hand.",
                "Lost on the count. Winter takes without malice.",
                "The house wins one. Weeds do, too, some days."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Two stones, same weight.",
                "Even. Neither bed drains first.",
                "A tie. The hedge grows level.",
                "Push. Nothing planted, nothing pulled.",
                "Level. Like a well-raked bed.",
                "Even hands. The garden holds its breath, then exhales."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Cut the diseased branch. Save the tree.",
                "Half kept. A hard pruning, rightly timed.",
                "You let it go. Gardens teach that first.",
                "Withdrawn. Some beds you dig under and start over.",
                "Half back. Better one clean cut than a slow rot.",
                "Surrendered. Even Bram approves. Silently, for once."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split. One rootstock, two grafts.",
                "Divided. Cuttings take better apart.",
                "Two hands. Thinned seedlings grow truer.",
                "The pair parts. Room for both to reach light.",
                "Split. As a clump of irises, in autumn.",
                "Two from one. Old gardener's arithmetic."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. All the water on one bed.",
                "One card. One seed, deliberately placed.",
                "A double. The bulb goes in. You wait.",
                "Doubled. Bold planting, before the last frost.",
                "One draw decides. Like grafting: one cut, one chance.",
                "Everything on the next card. The spade is in the ground."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer over. A fence fallen on its own nails.",
                "House bust. The weed pulled itself.",
                "Twenty-two for them. Rot in the beam.",
                "The dealer spills. Their trellis, their collapse.",
                "Over. The rules pruned the house this time.",
                "Dealer bust. The garden does not gloat. I might.",
                "They went over. Storm damage. Theirs."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. Hail on ripe fruit.",
                "The house held it all along. Bindweed under mulch.",
                "Their blackjack. The oak falls uphill sometimes.",
                "A hidden natural. Thorns grow inward too.",
                "Dealer twenty-one. Weather. Only weather.",
                "Their natural. Even nettles fruit."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. A weed in seed. Do not water it.",
                "An ace up. A cloud over the beds.",
                "The side offer. Ivy: pretty, then everywhere.",
                "Insurance. The label promises. The soil decides.",
                "An ace shows. Breathe. Roots hold.",
                "They offer cover. Most shade is sold, not grown."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle. The garden turned over for spring.",
                "Fresh shoe. Same seeds, new furrows.",
                "Reshuffled. Tilled earth remembers nothing.",
                "New shoe. The compost turns. All of it useful again.",
                "Shuffled. Winter does this to every garden.",
                "The cards churn. Loam again, not rows."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A run. Everything germinating at once.",
                "Win on win. A good rain year.",
                "Your streak. Even the stones look green.",
                "Much winning. Bram may burst. Someone hold him.",
                "A warm spell. Gather it in.",
                "A streak. The whole bed came up. Rare.",
                "Winning weather. It is real, and it is brief."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold cards. Gardens sleep too.",
                "A bare stretch. Roots keep working, unseen.",
                "Nothing sprouting. Not your soil, not your fault.",
                "Frost on the table. Sit still. Breathe.",
                "A lean season. The tree is not its fruit.",
                "Cold run. Even Bram hums quieter now.",
                "Losses in a row. Winter is honest, at least."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Few chips. A fallow field is not a failed one.",
                "The stack thins. Seed corn is never spent.",
                "Little left. Leaving ground unworked is wisdom, old as dirt.",
                "Low. The garden gate opens both ways.",
                "Nearly out. Rest the soil. Rest the gardener.",
                "A thin stack. No shame in banking the fire early.",
                "Chips low. Walk the garden instead. It asks nothing."));

        l.put(TableEvent.SESSION_START, List.of(
                "A new face. The garden notices.",
                "Welcome. Mind Bram. He means it kindly.",
                "Sit. The evening is well-watered.",
                "Hello. I tend things. Hana serves.",
                "A visitor among the beds. Good.",
                "You found us. Most things worth finding are grown, not built."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A weak card up. Blight on the house's vine.",
                "The dealer shows little. Shallow roots there.",
                "Poor up-card. Their bed drains badly.",
                "A six showing. Thin bark on that trunk.",
                "Weak card. The wind knows which fence leans.",
                "Their up-card wilts. Noted."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, standing. Ring on ring, like a trunk.",
                "Five drawn, none over. Careful transplanting.",
                "A five-card hand. Growth by inches, kept alive.",
                "Five cards. Patience layered like mulch.",
                "Still under. Espalier work, that.",
                "Five and holding. Slow wood is strong wood."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, grown. Not bought.",
                "Built to twenty-one. Ring by ring.",
                "The full total, raised from seed.",
                "Twenty-one by increments. Good husbandry.",
                "Assembled. A stone wall, laid dry and true.",
                "Twenty-one, the grown kind. It kept its shape."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one. The late frost takes the first bud.",
                "One point. A hairline crack in the pot.",
                "Lost by one. The rabbit ate one row. Just the one.",
                "A single point. Thorned, that.",
                "So close. The graft almost took.",
                "One short. Some cuts are thin and still deep."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A great win. The old tree fruited at last.",
                "Large. A whole bed up in a night.",
                "That pile. Pumpkins in October, that size.",
                "A big one. Even the weeds stood back.",
                "Heavy branches tonight. Yours.",
                "A grand yield. Carry it gently."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double lands. The staked vine held.",
                "Doubled, won. One seed, full row.",
                "Your double took. Grafts do, when the cut is clean.",
                "Doubled home. Water well spent.",
                "One card, right card. Rain when asked. Almost never happens.",
                "The bold planting bore. Enjoy the fruit."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "Long hours. The dew has come and gone.",
                "Still here. Gardens measure in seasons. So, patience.",
                "A long sitting. The moon has crossed the trellis.",
                "Hours deep. Even Bram is running low on stories.",
                "Late now. The soil is cooling.",
                "We have grown roots into these chairs."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are up. The bed you tended, bearing.",
                "Ahead. Green on every stem.",
                "Well up. A full barrow, both handles.",
                "Your stack grows. Watered, weeded, kept.",
                "Prospering. The quiet kind. The lasting kind.",
                "Up on the night. Deep loam under you."));

        return new Persona("juniper", "Juniper", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(hana(), bram(), juniper());
    }
}
