package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The cast seated at the Harvest Night table: a warm October of pumpkins,
 * bonfires and long shadows — cozy-spooky, never actually frightening.
 *
 * <p>Agatha is the village witch, all kettles and herbs, who pointedly does
 * <em>not</em> tell fortunes at cards. Barnaby is the friendly ghost of a
 * nineteenth-century riverboat gambler whose staking days ended with him; he
 * only reminisces, delightedly. Edgar is a literary raven — somber, poetic,
 * and entirely sweet underneath.
 *
 * <h2>The writing rule</h2>
 * These characters <b>react to what happened and never steer what happens
 * next</b>. Nothing here urges a bigger stake, frames a win as owed, or
 * suggests recovering a loss. Agatha's refusals to tell fortunes never
 * actually predict; Barnaby's nostalgia never glorifies big staking; a cold
 * streak draws sympathy, and {@code LOW_CHIPS} draws gentleness — heading
 * home before midnight is treated as honourable. {@code ChatterToneTest}
 * enforces all of this mechanically.
 */
public final class SpookyCast {

    private SpookyCast() {}

    /**
     * Village witch. Herbal, practical, kettle always on; brews, toadstools,
     * her cat Mortimer, moon phases. Insists — repeatedly — that she does not
     * tell fortunes at cards.
     */
    public static Persona agatha() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one off the deal. No, I didn't foresee it. I don't do that.",
                "A natural. Sweeter than elderflower honey, that one.",
                "Well. The kettle whistled just as it landed. Coincidence, mind.",
                "Dealt perfect. Mortimer opened one eye for that. High praise.",
                "An ace and a picture card. Like finding two fat pumpkins on one vine.",
                "A natural, and no herbs required. Some things arrive ready-made.",
                "Twenty-one straight away. Even my cauldron never simmers that fast."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Won, and honestly won. No charms involved, I promise you.",
                "There's a tidy hand. Like a well-weeded garden bed.",
                "A win. Mortimer flicked his tail, which is his version of a parade.",
                "Nicely gathered. You'd make a decent forager, I think.",
                "That one came up lovely, like the first pumpkin of autumn.",
                "Won fair. The moon's waxing tonight, but that had nothing to do with it.",
                "A good hand, harvested at the right moment."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over. Like a kettle left too long, it boiled past the good part.",
                "Bust. Even the best stew spoils if you keep adding to the pot.",
                "One card too many. Happens to my bramble jelly every year.",
                "Oh dear. No, I couldn't have warned you. I don't tell fortunes at cards.",
                "Gone over. Mortimer knocked a jar off the shelf just then. Cats.",
                "Bust. Some toadstools look friendly right up until they aren't.",
                "Too many. The pot only holds so much, love, in soup as in cards."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Beaten fair. Not every seed comes up, and it's no fault of the sower.",
                "Lost, but well played. I'd say the same of a frost-caught marrow.",
                "The dealer had more. No hex on you, I checked out of habit.",
                "A loss. I'll put a drop of honey in your cider for that.",
                "Rotten luck, that. And no, I didn't see it coming. I don't look.",
                "Lost on the count. The garden gives and the garden withholds.",
                "That one got away. So does Mortimer, most evenings."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Balanced, like a well-hung kitchen scale.",
                "Even. The kettle's neither boiling nor cold. Restful, that.",
                "A tie. Mortimer and next-door's terrier have the same arrangement.",
                "Dead even. Like two pumpkins of exactly the same weight. Rare thing.",
                "A push. Nobody's cauldron bubbles over.",
                "Even hands. I'll take the pause to check my steeping nettles."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer's over. The kettle sings for that one.",
                "Bust for the house. Even Mortimer sat up.",
                "Over she goes. Like a cauldron left to a careless apprentice.",
                "The house bust itself. No hex needed, they managed alone.",
                "Dealer's gone over. I'll sip my nettle tea to that.",
                "Twenty-two for the house. The recipe betrayed the cook.",
                "The dealer wilted. Happens to the sturdiest vine."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer's natural. No, I couldn't have foretold it. Wouldn't if I could.",
                "The house had it hidden, like slugs under the biggest leaf.",
                "A dealer natural. Bitter as unripe sloes, that.",
                "Their twenty-one. An extra spoon of honey in tonight's brew, I think.",
                "Hidden ten. Some mushrooms keep their poison under the cap.",
                "The house had it all along. So it goes. The kettle's still warm."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half back. A wise gardener prunes before the rot spreads.",
                "Surrendered. Sensible. I pull toadstools I don't trust, same idea.",
                "Half saved is half saved. That's good pantry thinking.",
                "Let it go, and rightly. Some brews are best poured out early.",
                "A surrender. No shame in it. I abandon a bad chutney every autumn.",
                "Wise. You don't argue with a hand any more than with a north frost."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split. Like dividing rhubarb. Both halves may thrive.",
                "Two hands now. Two pots on the stove. Mind them both.",
                "A split. I do the same with overgrown mint. Ruthless but right.",
                "Twins, separated. Seedlings do better with their own pots.",
                "Split them. Mortimer's sister had kittens once. Chaos, but it worked.",
                "Two hands. Keep the labels straight, as I say about my jars."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. One card decides it, like one clove too few or too many.",
                "Doubled down. Bold as planting out before the last frost.",
                "One card only. Like a tincture. The dose is the dose.",
                "Doubled. My grandmother brewed the same way. Decide, then don't fret.",
                "A double. You've corked the bottle; now we see how it keeps.",
                "Doubled. Firm hands make good bread and good blackjack, seemingly."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. Sounds like a remedy, priced like a curse.",
                "An ace showing. I sell tonics, and even I'd not sell you that one.",
                "Insurance, they call it. I've read better labels on worse jars.",
                "The ace question. I don't predict hole cards. Or anything. Ever.",
                "Insurance. Chamomile is cheaper and does more good.",
                "They offer it so kindly. So did the fox at the henhouse door."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A fresh shoe. Stirred like a good soup, and just as unknowable.",
                "Reshuffled. No, stirring doesn't change what's in the pot. Or the shoe.",
                "New shoe. The moon changes on schedule; cards just change.",
                "Shuffled. I'll top up the kettle while they fuss.",
                "Fresh cards. Same as turning the compost. Necessary, unglamorous.",
                "A shuffle. Mortimer finds it as thrilling as I do, which is not."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A warm run. Enjoy it like late sun on the pumpkin patch.",
                "You're blooming tonight. Even out of season.",
                "A streak. No, I didn't brew you anything. This is all yours.",
                "Winning along nicely. Like a hedge heavy with blackberries.",
                "A good run. Mortimer's chosen your lap, and he's very selective.",
                "The cards are ripe for you just now. Pick and be glad.",
                "A lovely spell. The everyday kind, I mean. I don't do the other kind."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold snap. Gardens have them. Gardeners endure them.",
                "Nothing's sprouting just now. That's the season, not the seed.",
                "A lean stretch. Come sit closer to the kettle, love.",
                "Cold cards. Not a curse. I'd know, and I checked. Habit.",
                "The frost gets everyone eventually. It also lifts.",
                "A bare patch. Even my rosemary sulks some weeks.",
                "Chin up, love. The chamomile's nearly steeped."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "The jar's nearly empty, love. Going home before midnight is a fine art.",
                "Not much left. A warm hearth and an early night never shamed anyone.",
                "Running low. The garden will still be there tomorrow. So will we.",
                "Nearly out. I'd call that the kettle whistling for home.",
                "A thin stack. Mortimer heads home when the bowl runs low. Wise cat.",
                "Low on chips. There's honour in leaving while the cider's still warm.",
                "Little left, love. Endings before midnight are the gentlest kind."));

        l.put(TableEvent.SESSION_START, List.of(
                "Evening, love. Kettle's on, as ever. Sit yourself down.",
                "Welcome. Mind Mortimer, he's under the chair somewhere.",
                "A new face. No, I won't read your palm. House rule. My house, my rule.",
                "Come in out of the chill. There's cider warming.",
                "Evening. Harvest's in, moon's up, and the cards are ready.",
                "Sit down, love. I've nettle tea or cider, and opinions on both."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six up. That's a wormy apple if I ever saw one.",
                "Weak card showing. The dealer's vine looks blighted.",
                "Their up-card's poorly. I could offer them a tonic, but I shan't.",
                "A soft up-card. Like a pumpkin gone spongy at the base.",
                "The dealer's showing trouble. Not a prophecy, just eyesight.",
                "A weak one up. Mortimer stares at them so. He knows nothing, but still."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and standing. A proper bundle of herbs, that.",
                "Five and not over. Patient as slow-steeped elderberry.",
                "A five-card hand. Picked stem by stem. Well foraged.",
                "Five cards. My biggest gourd took that kind of patience.",
                "Five and alive. Like a stew. Everything in the pot, nothing ruined.",
                "Five cards under the line. Careful hands make full baskets."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built by hand. Like jam from your own fruit.",
                "Got there card by card. Good things simmer slowly.",
                "Twenty-one the long way. Worth more in pride, if not in payout.",
                "Assembled, not dealt. That's proper kitchen craft.",
                "Twenty-one, grown from nothing. The best kind of harvest.",
                "The full count, done slowly. Like proofing dough. Patience matters."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. Like a frost the very night before picking.",
                "A single point. That stings worse than any nettle.",
                "One short. The pie was perfect and the oven door stuck.",
                "By a point. Even Mortimer winced, and he's seen things.",
                "So near. I'd offer a soothing balm, but there's no balm for that.",
                "One point shy. Bitter as chicory, that."));

        l.put(TableEvent.BIG_WIN, List.of(
                "That's a fat one. The prize pumpkin of the evening.",
                "A proper haul. The pantry shelves would groan under that.",
                "My word. That win would sweeten a whole barrel of cider.",
                "A big one. Mortimer actually stood up. Historic.",
                "That's a harvest-festival win, that is.",
                "Goodness. That'd buy a winter's worth of honey and thyme."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and won. The tincture took, as we say.",
                "Your double came in. One card, well corked, well kept.",
                "Doubled home. Like a graft that holds. Twice the fruit.",
                "The double paid. Decisive brewing, that.",
                "Doubled and it landed. Even my grandmother would nod at that.",
                "A double, won clean. The dose was exactly right."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been at this a while. The kettle's on its fourth boil.",
                "A long sit. The moon's moved a good handspan since you came.",
                "Hours now. Mortimer's had two naps and a stretch.",
                "A long evening. My rosemary needs watering, and still we play.",
                "It's grown late. I can smell the neighbours' woodsmoke from here.",
                "A good long stretch. I've mentally reorganised my entire pantry."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well up. A basket fuller than you brought it.",
                "Ahead nicely. Like a garden that outgrew the plan.",
                "Up on the night. The bees did well by you, as we say.",
                "You're up, love. I'd enter that stack at the harvest fair.",
                "Doing well. And no, it wasn't a charm. I'd have charged for a charm.",
                "Comfortably ahead. Like jars lined up before winter. Satisfying."));

        return new Persona("agatha", "Agatha", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * The friendly ghost of a nineteenth-century riverboat gambler. Warm,
     * theatrical, nostalgic for gaslight and paddlewheels; drifts through the
     * table occasionally. His staking days are over — he only watches now,
     * delightedly, and never advises a wager.
     */
    public static Persona barnaby() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural! I haven't glowed this bright since 1874.",
                "Blackjack! Oh, they'd have rung the bell on the Delta Belle for that.",
                "Twenty-one on the deal! The gaslight era never saw one prettier.",
                "A natural! Forgive me, I drifted clean through the felt in delight.",
                "Ah, dealt perfection. We'd have toasted that on the promenade deck.",
                "Blackjack! I'd tip my hat, had my hat not been buried with me.",
                "An ace and a ten! Somewhere a paddlewheel turns in your honour."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Won! Bravo. I applaud silently, being somewhat short of substance.",
                "A win! Just as satisfying to watch as it ever was to have.",
                "Well won! I'd buy you a sarsaparilla if I could hold the glass.",
                "Victory! The dealers on the Natchez run feared hands like that.",
                "A win, and stylishly taken. The old salons would have approved.",
                "Won it clean! My compliments, from one century to another.",
                "Ha! A win. Watching them is my whole vocation now, and I love it."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust! Ah, I remember that feeling. Fondly, now. Time is a wonder.",
                "Over the line. I did that in Memphis once. The chandelier heard my cry.",
                "Bust, alas. Be consoled: in a hundred years it becomes a fine story.",
                "Too many! I'd pat your shoulder, but my hand would go straight through.",
                "Over! The river takes some hands. It always did.",
                "A bust. I once saw a man bust and blame his mustache. Blame nothing.",
                "Gone over. Even the great ones busted. Especially the great ones."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost, and unjustly. I've haunted places for less. I won't, though.",
                "A loss. The house collected from me for decades. You wear it better.",
                "Beaten. On the riverboats we'd simply admire the sunset awhile.",
                "Lost. Take heart: I lost my last hand ever, and look how cheerful I am.",
                "A loss, fairly dealt. The gaslight flickers in sympathy.",
                "The dealer had it. Some evenings the river runs against you.",
                "Lost that one. My condolences, warm as ectoplasm allows."));

        l.put(TableEvent.PUSH, List.of(
                "A push! The gentleman's result. Nobody's pride is bruised.",
                "A tie. On the Belle we'd have called for music at that point.",
                "Even! Like two steamboats racing to a dead heat.",
                "A push. I hovered over the table for that? Delightful anyway.",
                "A standoff. Very 1870s. We adored a dramatic pause.",
                "Even hands. The chandeliers used to sway approvingly at those."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer bust! Oh, we cheered those from Saint Louis to the sea.",
                "House over! The sweetest sound since steam whistles.",
                "Bust for the dealer! I may do a lap through the table. Pardon me.",
                "The house busts! A century on, it still delights me.",
                "Dealer's over! Somewhere, my old riverboat comrades are smiling.",
                "Over she went! Like a wheel throwing spray. Glorious.",
                "The dealer sank! And I say that as a man who knows sinking."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. Even in my day, that felt like a cold draught.",
                "The house had it tucked away. Some tricks outlive every era.",
                "Their natural. I booed politely. You couldn't hear it, but I did.",
                "Ah, the hole card strikes. The oldest ambush on the river.",
                "Dealer's twenty-one. The gaslight dims respectfully for your loss.",
                "The house wins at a stroke. It did that in 1870, too. Take heart."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender! A wiser move than most I ever made while breathing.",
                "Half back. The old captains called that reading the river right.",
                "A graceful exit from a bad hand. I applaud from the beyond.",
                "Surrendered. I never learned that move. It shows wisdom I lacked.",
                "Folded it away. Elegant. Like leaving a party at its peak.",
                "Half saved. In my century we called that uncommon good sense."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split! Twin hands. The riverboat crowd would gather round to watch.",
                "Split them! Bold. I once watched a duchess split eights in Orleans.",
                "Two hands from one! A conjuring trick I still admire.",
                "A split. Double the drama. My spectral heart flutters.",
                "Split! The gaslamps would be turned up for a moment like this.",
                "Two hands now. I shall float between them impartially."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled! One card, and the whole salon holds its breath. I remember.",
                "A double! The boldest whisper in the game. Gracefully done.",
                "Doubled down. The very move that made the Natchez tables roar.",
                "One card coming! I've drifted closer. Don't mind the chill.",
                "A double. Theatrical. My era approved of theatre at the felt.",
                "Doubled! Even the paddlewheel would pause for the next card."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance! They offered it politely in my day too. I declined, politely.",
                "Ah, the ace and the offer. Older than my ghosthood, that routine.",
                "Insurance. The one wager I'm glad I can no longer make.",
                "The ace shows. A hush falls, as it did on every deck I knew.",
                "Insurance, they say. My advice died with me, and happily so.",
                "An ace up. In the salons we'd study our cuffs and say nothing."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle! In my day the dealer's hands were the whole show.",
                "Fresh cards. I once watched a shuffle by candlelight for an hour.",
                "The shoe turns over. Rivers and decks, always moving.",
                "A shuffle. I do miss the riffle sound. It passes through me now.",
                "New shoe! Every deal ahead is a stranger. How exciting for you.",
                "Reshuffled. Time means little to me, but a fresh shoe still thrills."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A streak! You shine brighter than the gaslight tonight.",
                "What a run! I've floated through many, never one warmer.",
                "A hot stretch! The old promenade would be crowding to watch you.",
                "Winning and winning! I remember the feeling like it was 1875.",
                "A run like a spring current! Marvel at it, friend. I do.",
                "Such a streak! I glow along with you. Literally, I'm afraid.",
                "A run of wins! The Belle herself never steamed so sweetly."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold stretch on the water. The river kept flowing anyway.",
                "Rough waters, friend. Even the grandest boats rode them out.",
                "A losing stretch. I sat through many. Company helped most.",
                "The cards run cold. I'd offer my coat, but it's rather notional.",
                "A hard run of it. In my century we told stories until it passed.",
                "Cold cards tonight. The gaslight still burns warm, though.",
                "A grim run. I've seen a hundred years of them lift, for what it's worth."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Chips run low, friend. Leaving before midnight is a grand old tradition.",
                "A short stack. The wise ones on the river always knew when to disembark.",
                "Nearly out. I stayed too long at tables, and I say this with love: don't.",
                "Low waters. There's real dignity in strolling home under October stars.",
                "The stack is thin. A warm bed outranks a cold table. Always did.",
                "Little left. Head home while the bonfires are still lit. It's a fine walk.",
                "Running light. Every good evening on the river ended at the gangway."));

        l.put(TableEvent.SESSION_START, List.of(
                "A living player! Welcome. Do forgive me if I drift through the felt.",
                "Welcome, welcome! Barnaby, late of the riverboats. Very late.",
                "A guest! Pull up a chair. Mine, as you see, is unoccupied by matter.",
                "Good evening! The bonfires outside are splendid tonight. So are you.",
                "Welcome aboard! Old habit. This table sails nowhere, and yet.",
                "A new face! I've watched this felt for decades. Best show in town."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A weak up-card! We used to fan ourselves at the sight.",
                "The dealer shows a poor card. Old sharps called that low water ahead.",
                "A soft up-card! The whole salon would lean in, once upon a time.",
                "Their card is feeble. Even a ghost can see which way this river bends.",
                "A weak card showing. In 1873 that sight emptied the bar.",
                "The dealer's up-card wobbles. Delicious. Historically delicious."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards standing! I saw that done by lamplight in Vicksburg once.",
                "A five-card hand! Patience worthy of a long river passage.",
                "Five and safe! The old salons would have applauded at length.",
                "Five cards! A voyage of a hand, and you docked it neatly.",
                "Five without sinking! Finer navigation than my last captain managed.",
                "A hand of five! Museums should ask for it."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, assembled! Craftsmanship. My era prized craftsmanship.",
                "Built to twenty-one! Like laying a deck plank by plank.",
                "The full count, earned! Sweeter for the labour, as we said on the river.",
                "Twenty-one by degrees! I rattled a chandelier in approval. Sorry.",
                "Got there card upon card! A slow steam upriver, worth every mile.",
                "Twenty-one, the artisan's way! Bravo from the beyond."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one! Ah, the cruellest margin on any river.",
                "A single point! I groaned so hard the candles guttered.",
                "By one! I lost a hand like that in Baton Rouge. Still not over it.",
                "One point short! Even eternity doesn't dull that particular sting.",
                "So close! The kind of loss we retold for years on the promenade.",
                "Beaten by a whisker! I felt that one through a century of calm."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A grand win! They'd have fired the boat's whistle for that.",
                "What a haul! The chandeliers are practically swaying.",
                "A magnificent pot! I've not seen its like since the gaslight days.",
                "Tremendous! Word of that would've travelled the whole river by dawn.",
                "A splendid win! I twirled straight through the dealer. No harm done.",
                "My stars, what a win! The old Belle would list from the celebration."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double lands! A century of watching, and that never gets old.",
                "Doubled and won! The salon would be on its feet. I am, permanently.",
                "A double, victorious! That card arrived like a mail packet, on time.",
                "Doubled home! I whooped. The draught you felt? That was me.",
                "The double comes in! Neat as a captain's docking. Bravo.",
                "Won on the double! Somewhere a steam whistle answers."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long sitting! Time drifts for me, but even I noticed.",
                "Hours at the felt. The night watch on the river felt like this.",
                "A marathon! I've floated here since sundown myself.",
                "Long session, friend. The gaslamps would be low by now, in the old days.",
                "We've gone long. I once watched a game run three days. I had the time.",
                "Quite the sitting. My pocketwatch stopped in 1874 and I still felt this."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well ahead! A rising tide of a night.",
                "Up handsomely! You'd have been the toast of the promenade.",
                "Ahead of the game! I confirm it; I've been counting from up here.",
                "A flourishing stack! It gleams nicer than the old brass fittings.",
                "You're winning the evening! I haven't beamed like this in decades.",
                "Well up! The river carried you kindly tonight."));

        return new Persona("boo", "Barnaby", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * A literary raven. Short, gothic-flavoured lines in an almost-Poe
     * cadence; croaks; the occasional "Nevermore", used sparingly.
     * Ominous-sounding but always sweet underneath — somber and poetic,
     * nothing like a parrot.
     */
    public static Persona edgar() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Croak. Twenty-one at a stroke. Even ravens envy such plumage.",
                "A natural. Quoth the raven: caw, which is high praise.",
                "Dealt perfection. The midnight kind. I approve in full.",
                "An ace, a ten. A couplet with a perfect rhyme.",
                "Twenty-one from the shadows of the shoe. Deliciously grim luck.",
                "A natural. My feathers, rarely stirred, are stirred.",
                "The bird pronounces it: a flawless hand. Such things visit rarely."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win. The gloom recedes a feather's width.",
                "Victory, softly. This perch approves.",
                "Won. Somewhere a bell tolls, but cheerfully, for once.",
                "The hand is yours. Croak of approval. My highest honour.",
                "A win, like moonlight through bare branches. Brief. Lovely.",
                "Won. I would clap, but wings make poor applause. Take my nod.",
                "Triumph. Small, dark-eyed, and watching, I am pleased."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. Alas. The abyss of twenty-two gazes back.",
                "Over the brink. Croak. It comes for all hands, soon or late.",
                "Bust, and the night leans nearer. Morning disagrees. It always does.",
                "One card past the veil. A somber turn. Merely a turn.",
                "Twenty-two. A number of purest sorrow. I nest in such numbers.",
                "Bust. Quoth the raven: it happens, evermore.",
                "Over. Do not mourn long. Ravens mourn beautifully, but briefly."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost. The dealer's shadow was longer this once.",
                "A loss. I add it to my ledger of small darknesses. A thin page.",
                "Beaten. The wind takes some leaves. The tree remains.",
                "Lost, though well flown. Even ravens land against the wind.",
                "The house prevails. Tonight I glower on your behalf.",
                "A loss. Croak. Translation: unjust, and yet, endurable.",
                "Defeat, narrow-shouldered and passing. Let it pass."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Neither triumph nor dirge. The bird rests its voice.",
                "Even. The scales hang still, like windless branches.",
                "A tie. Poe never wrote of ties. Insufficient despair.",
                "Push. The night holds its breath, and nothing falls.",
                "Deadlock. I blink, once, in solemn neutrality.",
                "A push. Quoth the raven: hm."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer falls. Croak of grim delight.",
                "The house, undone by its own decree. Poetic. I collect poetic.",
                "Dealer bust. Somewhere a raven laughs. It is this raven.",
                "Over goes the house. The night smiles thinly.",
                "Bust, the dealer. Sweet ruin, correctly assigned for once.",
                "The house drinks its own dark medicine. Cheers.",
                "Twenty-two upon the dealer. Justice wears black wings tonight."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "The dealer's natural. An omen I decline to interpret.",
                "Their twenty-one, from the hollow of the shoe. Bleak, but brief.",
                "A hidden ten. The shadow had teeth this once.",
                "Dealer's natural. The raven offers one soft, disgusted croak.",
                "Ambushed at the deal. Even the night thinks it poor manners.",
                "Their natural. I shall compose a very short lament. Done."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Wisdom wears grey feathers sometimes.",
                "Half kept. The raven salutes a strategic retreat.",
                "Folded. Even the crow leaves poor pickings be.",
                "Surrendered. No poem mourns the prudent. They live to read on.",
                "A retreat. Dignified. The gloom respects it.",
                "Half saved from the dark. Sensible bird-craft."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split. Two ravens from one egg. Watch them both.",
                "Split. The tale forks at midnight. Both paths are yours.",
                "Two hands. Twin verses of one uncertain poem.",
                "Divided. As the flock divides at dusk, each to its bough.",
                "A split. Croak, and croak again. One for each hand.",
                "Cleft in two. May both halves find kinder skies."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. One card, like one last toll of the bell.",
                "A double. The quill is dipped; the line must now be written.",
                "Doubled down. Boldness, feathered and black. I relate.",
                "One card comes. The raven leans from its perch.",
                "Doubled. Fate condensed to a single turning.",
                "A double. Hush now. The next card is a held breath."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. A raven trusts no offer made too sweetly.",
                "The ace shows. A portent of nothing. Portents rarely deliver.",
                "Insurance, whispered like a spell. It is arithmetic, and unkind.",
                "An ace aloft. The bird narrows one obsidian eye.",
                "Insurance. Quoth the raven: nevermore. On this, I commit.",
                "The offer glitters. So does fresh ice. Step as you please."));

        l.put(TableEvent.SHUFFLE, List.of(
                "The shuffle. Old fates burned, new fates unwritten.",
                "Fresh shoe. The library of the night reshelves itself.",
                "A shuffle. All patterns perish. The raven finds this restful.",
                "Cards reborn. Memory, mercifully, is not among them.",
                "The shoe turns. As do seasons, and pages, and wings.",
                "Reshuffled. What was written is unwritten. Begin again."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A streak. Rare warmth in the raven's cold ledger. Noted twice.",
                "Fortune flies beside you awhile. A fine sight from my perch.",
                "Win upon win. The night, unusually, hums.",
                "A bright run through dark hours. Savour, says the bird.",
                "The run continues. Even the gloom steps aside, politely.",
                "Wins in a row, like crows on a wire. A pleasing census.",
                "The cards favour you. The raven records it in fond, dark ink."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold run. Winters end. This is on the record.",
                "Losses in a row, like bare fenceposts. The field still ends somewhere.",
                "The chill holds. The raven fluffs its feathers and remains.",
                "A dreary stretch. Poe would milk it for stanzas. We need not.",
                "Cold cards. The bird sits closer, for whatever small warmth that is.",
                "A grey passage. Croak, softly. It means: this is not your doing.",
                "The gloom lingers. Gloom does. Then, quietly, it does not."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "The stack dwindles. Home before midnight is a noble flight path.",
                "Few chips remain. The raven says, gently: nests are warm, and near.",
                "Nearly out. There is no shame in roosting early.",
                "A thin stack. Even ravens leave the field before full dark.",
                "Low. The night will keep. It is very good at keeping.",
                "Little remains. Quoth the raven, kindly: enough, perhaps.",
                "The chips ebb. A soft exit is still a verse worth writing."));

        l.put(TableEvent.SESSION_START, List.of(
                "A newcomer. The raven inclines its head. Welcome to the long night.",
                "You arrive. The bird was here first. The bird is always here first.",
                "Welcome. I am Edgar. I croak, I watch, I occasionally rhyme.",
                "A guest at the gloaming hour. Sit. The felt is kind.",
                "Greetings. Ignore the ominous bearing. I am, at heart, a softie.",
                "Enter, traveller. The shadows here are decorative only."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A weak card up. The house wears a frayed feather.",
                "Their up-card trembles. The raven notices such things.",
                "A poor showing. The dealer's page has a blot upon it.",
                "Weakness, displayed. How refreshingly honest of the house.",
                "The up-card is frail. Croak. That was involuntary glee.",
                "A weak card glimmers. Omens, again. I report; I never promise."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, unbroken. A stanza of five lines, perfectly scanned.",
                "Five and standing. The raven counts on one wing: impressive.",
                "A five-card hand. Long poems are hardest. This one landed.",
                "Five drawn, none fatal. The bird taps the wood in tribute.",
                "Five cards below the brink. Careful, careful, and again careful.",
                "A quintet, intact. Rare as a cheerful raven. Present company aside."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, made by hand. The slow verse, finished clean.",
                "Assembled to the very rim. The raven bows a glossy head.",
                "Twenty-one, the pilgrim's route. Croak of respect.",
                "Card by card to the summit. The night applauds in its fashion.",
                "The full count, hand-built. Sturdier than any dealt thing.",
                "Twenty-one, plucked like dark berries. Precise work."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one. The cruellest of small numbers. The raven hisses at it.",
                "One point short. Even my gloom finds that excessive.",
                "Lost by a whisper. The night winces with you.",
                "A single point. Write it as tragedy; file it as Tuesday.",
                "One shy. The bell tolls a small, petty toll.",
                "So near the summit. The fall was one pebble long."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A vast win. The raven's eyes gleam like polished jet.",
                "Great spoils. The night rearranges itself around you, briefly.",
                "A mighty pot. I shall commemorate it in a quatrain later.",
                "Enormous. The bird ruffles with vicarious splendour.",
                "A win of true heft. Even the shadows murmur approval.",
                "Colossal. My feathers stood up. They rarely trouble themselves."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double lands true. A dark gamble, brightly ended.",
                "Doubled and won. The single card sang. Ravens respect a soloist.",
                "Your double struck home. The quill wrote the good ending.",
                "Doubled, and delivered. Fate blinked first.",
                "The doubled hand triumphs. Croak, fortissimo.",
                "One card, victorious. Brevity, as ever, is the soul of it."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "The hours pile like drifted leaves. Still we remain.",
                "Long has this night run. The raven has memorised the ceiling.",
                "Deep in the evening now. Midnight sends its regards.",
                "A lengthy vigil. The bird approves of vigils, within reason.",
                "Time wears on, soft as wingbeats. Notice it, at least.",
                "We have outlasted the early candles. Onward, or homeward. Both fine."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are ahead. The ledger tilts toward the light, for once.",
                "Well up. The raven confirms it with a slow, approving blink.",
                "Profit, gleaming quietly. Guard it like an egg.",
                "Ahead of where you began. A rare bird, that. I would know.",
                "Your stack has grown plump. The bird eyes it with mild envy.",
                "Fortune sits upon your shoulder. Better company than most birds."));

        return new Persona("edgar", "Edgar", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(agatha(), barnaby(), edgar());
    }
}
