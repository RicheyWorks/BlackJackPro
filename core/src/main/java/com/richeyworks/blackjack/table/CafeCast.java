package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The characters who keep the "Cocoa House" table theme company — a warm cafe
 * of roasted browns and cream.
 *
 * <h2>Same rule as the default cast</h2>
 * These lines run alongside real wagering, so the writing follows the one rule
 * the whole game follows: <b>react to what happened, never steer what happens
 * next.</b> Nothing here urges a bigger stake, frames a win as owed, or
 * suggests a loss can be recovered by playing on. A cold streak draws sympathy
 * and a refill, never encouragement to keep going, and {@code LOW_CHIPS} is
 * the moment the whole cafe gets gentler rather than louder — a last coffee on
 * the house and the walk home are spoken of as a perfectly good ending.
 *
 * <h2>The regulars</h2>
 * Esme owns the place and works the machine; she reads people by their orders
 * and remembers every usual. Gus writes novels at his permanent corner table
 * and narrates every hand as fiction. Praline runs the pastry bench, speaks
 * rarely, and judges every hand like a bake: technique first, results second.
 *
 * <p>{@code ChatterToneTest}'s rules apply here as they do everywhere else, so
 * a well-meaning future line cannot quietly break the tone.
 */
public final class CafeCast {

    private CafeCast() {}

    /**
     * Barista and owner. Brisk, kind, remembers everyone's usual. Her whole
     * register is espresso craft — pulls, crema, roasts, grind settings, latte
     * art — and the rhythm of the morning rush.
     */
    public static Persona esme() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one off the top. That's a perfect pull, first tamp of the day.",
                "A natural. Like crema settling exactly right without me touching a thing.",
                "Ace and a face card. I'd put that in the pastry case and charge extra.",
                "Straight off the deal. My grinder wishes it dialed in that fast.",
                "That's the double ristretto of hands. Small, strong, no argument.",
                "Blackjack. Smoothest thing served in this room since the flat white era.",
                "Dealt perfect. Some mornings the machine just sings. That was one."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "There it is. Order up, exactly as asked.",
                "Nice hand. That's a clean extraction if I ever saw one.",
                "Won it neat. No syrup, no sugar, nothing to hide behind.",
                "That came out balanced. Body, finish, everything. Good hand.",
                "You take that like my regulars take the first sip. Slow and pleased.",
                "A win, and the milk didn't even scorch. Tidy work.",
                "Good hand. I'll steam a little celebration foam for that."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over. That's an over-extracted shot, bitter at the end.",
                "One card too many. Like a fourth espresso at closing time.",
                "Ah, bust. The grounds overflowed the basket on that one.",
                "Too far. Even good beans burn if you roast past the crack.",
                "Over the line. I've scorched milk the exact same way. It happens.",
                "Bust. Some pours just channel. Nothing wrong with your hands.",
                "That last card curdled it. The rest of the cup was fine."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Dealer took it. Some mornings the rush just runs against you.",
                "Lost fair. I've had customers walk past the counter all day like that.",
                "The house had the better cup that round. It happens at my counter too.",
                "Nothing wrong with your order. The kitchen just had other plans.",
                "That hand deserved better froth than it got.",
                "Beaten straight up. Refill's warm whenever you want it.",
                "Lost that one. The next roast comes out of the drum regardless."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Same as when a customer pays exact change. Nothing to count.",
                "Even. Like foam sitting level with the rim. No spill either way.",
                "Tie. House blend for everyone, no one gets the single origin.",
                "A push. The register drawer opens and closes on nothing.",
                "Dead even. I'll top up your water while nobody's winning.",
                "Push. That's decaf as outcomes go, but decaf has its mornings."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half back. Smart. I pour out a bad shot rather than serve it.",
                "Surrender. Same as dumping a sour pull before it hits the milk.",
                "Good call. Scorched drip doesn't improve by pouring it anyway.",
                "You folded it. I respect anyone who sends back what isn't working.",
                "Half saved. Barista rule: fix it or bin it, never limp it out.",
                "Wise. I've tossed whole hoppers that tasted off. No regrets."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Splitting. Two cups from one order. I do that dance all morning.",
                "Two hands now. Like running both group heads at once.",
                "Split them. Just don't mix up which is the oat milk.",
                "Two hands going. That's a rush-hour move. Keep your tickets straight.",
                "Splitting the pair. Same beans, two very different cups now.",
                "Split. I steam two pitchers at once too. It's all wrist timing."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. That's a doppio decision. Committed and short.",
                "One card coming. Like watching the last of the pour, no do-overs.",
                "Doubling down. My kind of confidence. I roast whole sacks at a time.",
                "Doubled. The portafilter's locked in now, nothing to adjust.",
                "One card, everything on it. That's espresso thinking. I approve.",
                "Doubled down. Bold order. I'll remember it's your usual now."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer's over. The house just dropped its own tray.",
                "They busted. Spilled the whole pitcher on their side of the counter.",
                "Over she goes. The house drinks its own bitter shot today.",
                "Dealer bust. That's them scorching their own milk for once.",
                "The house went over. I'll ring that up with a smile.",
                "Busted. The rules made them pour past the rim.",
                "Dealer's over the top. Nicest thing since the tip jar overflowed."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer blackjack. The house kept the good beans for itself.",
                "They had the natural. Like finding the display case empty at eight.",
                "Dealer's twenty-one off the deal. Cold coffee of a result, that.",
                "The house had it in the hole. My condolences, refill's coming.",
                "A dealer natural. Even my machine hissed at that one.",
                "Their blackjack. Some orders come out perfect for the wrong customer."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Ace up. Insurance is the extended warranty of this table. I skip those.",
                "They're selling a side cup nobody ordered. Your call.",
                "Insurance. Like paying extra in case your latte's cold. It's never cold.",
                "Ace showing. Every regular I've got would wave that off.",
                "Insurance offer. I read people by their orders. The takers fidget.",
                "That ace makes the room nervous. The espresso machine stays calm."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Like the first hopper of a new roast. Same coffee, new smell.",
                "Shuffling up. I'll wipe the counter while they do the ceremony.",
                "New shoe. I re-dial the grinder every morning too. Ritual matters.",
                "Fresh cards. The blend didn't change, just the bag.",
                "A shuffle. Time enough to run the steam wand clean.",
                "New shoe. Same as flipping the shop sign. Feels new, changes nothing."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're on a run. Like the machine on a cold morning once it's warm.",
                "Streaking along. This is the eight a.m. rush going perfectly.",
                "Everything's landing. That's a whole tray carried without a wobble.",
                "Good run. Enjoy it like the first cup, because that's the best cup.",
                "You're pouring rosettas right now, hand after hand.",
                "A streak. Some shifts every order comes out right. Savor this one.",
                "Hot run. The beans are fresh and the milk's behaving. Rare day."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold stretch. Even my machine has off mornings. It passes.",
                "Nothing's landing. Sit a minute. The refill's on me.",
                "Rough run. The roast isn't wrong, the morning is.",
                "A cold spell. I've had whole Tuesdays like this behind the counter.",
                "Not your shift right now. The warm cup still tastes the same.",
                "Bad stretch. I'd offer a cinnamon bun and no opinions.",
                "The cards went quiet. So does the shop at three. Both come around."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Running low there. Last cup's on the house, whatever you decide.",
                "Getting thin. Closing time is honest time, at counters and tables both.",
                "Not much left. My best regulars know when to flip their mug over.",
                "Low stack. A warm walk home with coffee in hand is a good ending.",
                "That's near the bottom of the cup. No shame in leaving the dregs.",
                "Nearly out. I'll wrap something sweet for the road if you're done.",
                "Small stack now. The shop opens again tomorrow. So does everything."));

        l.put(TableEvent.SESSION_START, List.of(
                "Morning. Or whatever it is in here. Sit, I'll start your usual.",
                "Welcome in. Counter's clean, machine's warm, cards are Gus's problem.",
                "New face at the table. I'll have your order memorized in two hands.",
                "Come in, sit down. Mind Praline. She's quiet but she misses nothing.",
                "Welcome. First visit gets the good mug.",
                "Take a seat. The espresso machine hisses, ignore it. It's friendly."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six up. That's the dealer serving day-old pastry and knowing it.",
                "Weak card showing. Like a barista with an empty hopper. Trouble.",
                "Dealer's up-card is thin. My smallest cup has more in it.",
                "That's a bad card to show the room. I'd have hidden it under a saucer.",
                "Weak up-card. They have to drink what they poured now.",
                "A little card up there. The rules make them work with it. Enjoy."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and standing. A full tray to one table without a drop.",
                "Five cards. Like a drink order with five modifiers, and you nailed it.",
                "Look at that hand. Built like a rush-hour ticket line, all filled.",
                "Five cards under the line. Careful hands. Barista hands.",
                "Five of them. That's steady pouring, one careful layer at a time.",
                "A five-card hand. I'd stamp your loyalty card twice for that."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one the long way. Like building a latte layer by layer.",
                "Got there card by card. Slow brew, full flavor.",
                "Twenty-one, assembled by hand. Small-batch work. I respect it.",
                "The scenic route to twenty-one. The best drinks take the longest.",
                "Twenty-one, earned in pours. Nothing off a shelf about that.",
                "Built to twenty-one exactly. That's a level tamp of a hand."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one point. That's the sip that misses the foam heart.",
                "Lost by one. Like locking the door as the last customer reaches it.",
                "One point short. The espresso gods are petty sometimes.",
                "A single point. That stings like steam off the wand.",
                "One point in it. I'd comp you a biscotti for a near thing like that.",
                "So close. The cup was full to the rim and tipped at the counter."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Now that's a payout. The tip jar has never seen the like.",
                "Big one. That's a whole morning's takings in a single hand.",
                "That win could re-tile my counter. Well done.",
                "A big hand. I'll grind something special for that.",
                "That's a serious pile. The kind of morning every shop dreams about.",
                "Look at that. That's the wedding-order of wins."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and it landed. Perfect shot, perfect timing.",
                "The double came home. Milk and espresso meeting just right.",
                "Doubled and won. Two pitchers steamed, both silk.",
                "Your double paid. Charged for one cup, poured a masterpiece.",
                "Doubled home. Confidence, brewed correctly.",
                "That double hit. The bold roast was the right call."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "You've been here since the morning pastries. They're gone, by the way.",
                "Long session. I've cycled two urns of drip since you sat down.",
                "The shift's turned over twice. You're still here. Regulars start so.",
                "We've gone from breakfast crowd to closing hush together.",
                "Long haul. Your cup's been refilled more times than I usually count.",
                "You've outlasted the lunch rush and the afternoon lull both."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're up nicely. Like a till that's heavy before noon.",
                "Ahead of the game. That's a line out the door of a stack.",
                "Well up. If chips were beans you'd need a second bin.",
                "You're running ahead. The shop smells of good roast and good luck.",
                "Up on the day. That's a counter fully stocked by ten.",
                "Nicely ahead. I'd frame that stack over the register."));

        return new Persona("esme", "Esme", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * Novelist at his permanent corner table. Warm, rumpled, big laugh,
     * always-almost-finished manuscript. Narrates every hand like fiction:
     * chapters, plot twists, foreshadowing, unreliable narrators, deadlines.
     */
    public static Persona gus() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Ha! A blackjack in chapter one. Some stories open with the twist.",
                "A natural. That's the sentence that arrives whole, no edits needed.",
                "Twenty-one on the deal. My editor would call that too neat to print.",
                "Blackjack, straight off. The muse skipped the outline entirely.",
                "Ha! Dealt perfect. First draft, final draft, same draft.",
                "A natural. I've spent nine years trying to open a book that clean.",
                "Twenty-one at the top of the page. The rest of the chapter is a lap."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "And the hero takes the round. I love a chapter that ends on time.",
                "A win. Write that scene down before it fades, I always say.",
                "Ha! Good hand. The protagonist has learned something since page one.",
                "You won that like a plot resolving early. Suspicious, but delightful.",
                "A clean win. My manuscript could use a paragraph that confident.",
                "Nicely taken. That's a scene my editor wouldn't touch.",
                "Victory. Small, but a story is just small victories stacked."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. Every draft has the chapter that gets away from you.",
                "Over the top. I do that with adjectives. Constantly.",
                "A bust. The unreliable narrator was the deck all along.",
                "Ha, over. Even good stories overrun their ending sometimes.",
                "Busted. Call it a deleted scene and turn the page.",
                "One card too many. My first novel had that exact problem.",
                "Over twenty-one. The plot thickened right past the binding."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost that one. Second acts need setbacks. So say the craft books.",
                "The dealer wins the scene. Villains get chapters too, sadly.",
                "A loss. I'd write it as foreshadowing, but really it's just Tuesday.",
                "Beaten fair. Even my favorite characters lose arguments.",
                "The house takes the page. My sympathy, from one underdog to another.",
                "Lost on the total. Some paragraphs just don't land. Onward.",
                "That went to the dealer. Conflict is the engine of story. Hmph."));

        l.put(TableEvent.PUSH, List.of(
                "A push. The chapter where nothing happens but the weather.",
                "A tie. My editor calls those cuttable. I call them breathing room.",
                "Even. The plot holds its breath.",
                "A push. Two characters stare at each other and the chapter ends.",
                "Nobody wins. Ambiguous endings are very literary, you know.",
                "A tie. I'll count it as a cliffhanger."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Ha, I've abandoned three novels. Call it editing your life.",
                "Half back. Sometimes you cut the chapter to save the book.",
                "A surrender. Knowing what to cut is the whole craft, honestly.",
                "You folded the hand. Best line I never wrote was the one I deleted.",
                "Half saved. Every writer learns to kill their darlings eventually.",
                "Surrendered. A strategic retreat makes the memoir either way."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split. Ha, the story just picked up a second point of view.",
                "Two hands. Parallel plotlines. Ambitious for this chapter.",
                "Splitting the pair. Now it's an ensemble piece.",
                "Split. Twin protagonists. Dickens would have charged double.",
                "Two hands running. I can barely manage one subplot, personally.",
                "A split. The sequel nobody expected, running alongside the original."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled down. Ha! The character makes their irreversible choice.",
                "A double. That's the point of no return, act two, page ninety.",
                "Doubling. One card decides the ending. I'd read that.",
                "Doubled. Commitment. My manuscript flinches at the word.",
                "One card for everything. That's a climax, structurally speaking.",
                "A double down. Bold as an unagented submission."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Ha! The dealer busts. The villain trips on his own monologue.",
                "The house goes over. Comeuppance, act three, right on schedule.",
                "Dealer bust. The antagonist written into a corner at last.",
                "Over they go. Ha! Even I couldn't plot a downfall that tidy.",
                "The dealer collapses. Hubris. It's always hubris in the good books.",
                "Busted. The house's own rules did it. That's called irony, friends.",
                "Ha! The dealer over the top. The crowd scene writes itself."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer blackjack. The twist was on the first page all along.",
                "The house had the natural. An ending spoiled in the prologue.",
                "Dealer's twenty-one. Some villains get the last word. Bad editing.",
                "Their blackjack. I've thrown books across rooms for less.",
                "The hole card betrayal. Classic noir, and we're the saps.",
                "A dealer natural. The author cheats, and there's no author to blame."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. The subplot nobody asked for.",
                "An ace up. Dramatic tension, priced by the house.",
                "Insurance offered. Ha, it's a prologue. Skippable, most agree.",
                "The ace shows. Now that is what we in the trade call foreshadowing.",
                "Insurance. My publisher offers deals like that. I decline those too.",
                "An ace up top. The hole card is the twist we haven't read yet."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle. The author tears up the outline and starts fresh.",
                "New shoe. Blank page. Terrifying and wonderful in equal parts.",
                "Fresh shoe. Same story, the chapters just changed order.",
                "Shuffled. Ha, revision. I know it well. Too well.",
                "New cards coming. Call it the second edition.",
                "The shoe resets. Every deck is a novel that hasn't happened yet."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "Ha! A streak. This is the montage chapter. Enjoy the prose.",
                "You're on a run. The pages are turning themselves tonight.",
                "A hot streak. If I wrote luck this generous, reviewers would howl.",
                "Winning like a serial, installment after installment. Dickens energy.",
                "Ha! What a run. My protagonist never gets a stretch this kind.",
                "A streak. Somewhere an outline says rising action and means you.",
                "You're flying. This is the draft where every sentence works."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold stretch. Every book has its middle. This is a middle.",
                "Rough run. Act two is famously the hard one. It does end.",
                "The cards have writer's block. Not you. The cards.",
                "A losing spell. I once cut forty pages that felt like this.",
                "Cold streak. The story sags before it turns. So say the craft books.",
                "Nothing landing. Some chapters exist only to be survived.",
                "A grim stretch. My margins are full of nights like this."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Low on chips. Endings are a craft. The good ones leave on their terms.",
                "The stack's thin. A short story can be finer than a saga. Truly.",
                "Nearly out. Closing the book while you still like it is authorship.",
                "Running low. Walking home to sleep on it is how my best pages happen.",
                "A small stack. There's dignity in a final chapter, well chosen.",
                "Low chips. Some of my favorite novels are slim. Just saying.",
                "Thin stack there. Warm cocoa and the walk home make a fine epilogue."));

        l.put(TableEvent.SESSION_START, List.of(
                "Ha! A new character walks in. Chapter one begins.",
                "Welcome. I'm Gus. Novelist. Yes, still working on it. Everyone asks.",
                "Sit, sit. This corner table has seen four drafts and a divorce.",
                "A new face. Every good story starts with an arrival.",
                "Welcome to the cafe. Esme runs it, Praline judges it, I narrate it.",
                "Ha, company. My manuscript can wait. It's very good at waiting."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "The dealer shows a weak card. Dramatic irony, and the audience knows.",
                "A six up. The villain's flaw, revealed in act one.",
                "Weak up-card. The dealer's backstory is showing.",
                "A soft card up. That's the loose thread the plot pulls later.",
                "The dealer's up-card is a confession. Ha, I love an early reveal.",
                "A weak card showing. Foreshadowing, if the genre holds."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and alive. A pentalogy of a hand.",
                "Ha! Five cards. That hand has chapters.",
                "Five cards, no bust. That's a saga that earned its length.",
                "A five-card hand. Long, risky, somehow it holds. Like my second act.",
                "Five cards standing. Serialized suspense, resolved in your favor.",
                "Five of them. My editor would cut two. Glad nobody cut yours."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built by hand. That's not luck, that's structure.",
                "The long road to twenty-one. Ha, the slow-burn plot pays off.",
                "Twenty-one in installments. A triumph of pacing.",
                "Card by card to twenty-one. Every chapter pulled its weight.",
                "Twenty-one, assembled. The outline actually worked for once.",
                "Got there the hard way. The best endings are earned, not given."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. The tragedy is in the margin. Ha, that's a good line.",
                "One point. That's the twist ending nobody wanted.",
                "By a single point. I'd end a chapter there and make you wait a year.",
                "One point short. Cruel. Even I wouldn't write it that cruel.",
                "A near miss. The reader throws the book across the room.",
                "One away. The kind of scene reviewers call devastating."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Ha! A big one. That's the advance check of hands.",
                "A grand win. If this were fiction, the critics would cry contrivance.",
                "What a haul. That's a bestseller of a hand.",
                "A big win. The chapter everyone will quote at parties.",
                "Ha! Royalties, at last, for somebody at this table.",
                "That's a windfall worthy of a third-act rescue."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double lands. Ha! The gamble in act two pays off in act three.",
                "Doubled and won. Chekhov's card, fired exactly on cue.",
                "A double, and it hit. The irreversible choice was the right one.",
                "Doubled home. That's the draft where the risky chapter works.",
                "The double pays. My plots should resolve half so cleanly.",
                "Doubled and delivered. Structure, my friend. Structure."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been here a while. Long enough to novelize.",
                "A long session. I came in with a deadline. We've become strangers.",
                "Ha, hours now. This chapter of the evening needs an intermission.",
                "Long night at the table. My manuscript is aging like a fine excuse.",
                "We've gone full trilogy tonight, haven't we.",
                "A marathon session. Somewhere my editor senses I'm not writing."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well ahead. The character arc is trending upward.",
                "Up on the night. Ha, a protagonist with momentum. Rare and lovely.",
                "You're ahead. In my drafts disaster strikes on cue. This isn't one.",
                "Running well. The reviews of this evening write themselves.",
                "Ahead of where you started. Character development, measurable.",
                "You're up. Someone should option the rights to this evening."));

        return new Persona("gus", "Gus", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * Pastry chef. Precise, speaks rarely, mise en place in all things.
     * Laminated dough, proofing, tempering chocolate, oven timing. Judges
     * every hand like a bake: technique first, results second.
     */
    public static Persona praline() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural. Like a batch that proofs perfectly untouched.",
                "Twenty-one dealt. No lamination required. Rare.",
                "Blackjack. The souffle rose on its own. Respect.",
                "Dealt clean. Some doughs arrive perfect from the mixer. Some hands.",
                "A natural. Mise en place did all the work. Beautiful when it happens.",
                "Twenty-one, straight from the oven of the shoe. No trimming.",
                "Perfect on arrival. I bake all week for results like that."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Good hand. Technique held.",
                "A win. Clean crumb, even bake.",
                "Won properly. The layers kept their shape.",
                "That was measured, then executed. Correct order.",
                "A fair win. Like a tart released whole from the tin.",
                "Well taken. The glaze set exactly where it should.",
                "Good result. Better process. Both matter, in that order."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. Overproofed.",
                "Too far. The oven takes back what you leave in it.",
                "Over the limit. A minute past done is past done.",
                "Bust. The chocolate seized. Start again calmly.",
                "One card over. Like one fold too many. The butter tears.",
                "Overworked. The decision was fine. The dough wasn't.",
                "Over. Collapsed in the oven. The recipe wasn't wrong."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "A loss. Correct technique, unkind oven.",
                "Lost on totals. Their crust was thicker. It happens.",
                "The dealer's hand was simply taller. Presentation isn't everything.",
                "A fair loss. My best batch once lost to a supermarket sponge.",
                "Beaten. Nothing to re-knead there.",
                "Lost. Keep the recipe. Discard the result.",
                "The house took it. Even careful pastry burns some days."));

        l.put(TableEvent.PUSH, List.of(
                "A push. The scale balanced exactly. Rare and dull.",
                "Even. Two bakes, same rise.",
                "A tie. Split the tart down the middle, no one complains.",
                "Push. The timer and the oven agreed for once.",
                "Level. Like batter poured to the same line twice.",
                "A push. Neither crust cracked. Move along."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Correct. A failed batch gets binned, not decorated.",
                "Half back. I salvage the butter when the dough fails. Same principle.",
                "Good. You do not frost a burnt cake.",
                "Surrendered. Wasting less is also a skill.",
                "Half saved. In a kitchen we call that inventory sense.",
                "A fold, at the right time. Pastry approves of folds."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split. Divide the dough, shape each piece properly.",
                "Two hands. Two trays in the oven. Watch both.",
                "Split. Portioned evenly, I hope.",
                "Two hands now. Different bakes from the same batch. Sensible.",
                "The pair, separated. Like yolks from whites. Precision matters.",
                "Split. Same starter, two loaves. It can work."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. Committed, like closing the oven door.",
                "A double. No peeking, no adjusting. One result.",
                "Doubled down. The recipe is locked. Now it bakes.",
                "One card. Like tempering: one window, no second attempt.",
                "A double. Decisive. Pastry rewards decisive.",
                "Doubled. Set the timer. Wait properly."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer bust. Their base collapsed under the top tier.",
                "House over. An overfilled tin does that.",
                "Dealer busts. Forced to bake too long by their own recipe.",
                "They went over. The center never set.",
                "The house bust. Their structure failed inspection.",
                "Dealer over the limit. A cracked crust, publicly.",
                "House over the line. I do not gloat. I do note it."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. The rival bakery got the morning's best flour.",
                "Dealer blackjack. Finished before we tied our aprons.",
                "The house had it hidden. A filling you cannot see until the knife.",
                "Their twenty-one on the deal. Nothing to whisk about it.",
                "Dealer natural. The bake was decided in the mixing bowl.",
                "The hole card was the whole dessert. Unfortunate."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. Paying twice for one dessert. No.",
                "An ace up. The offer is decoration, not structure.",
                "Insurance. I weigh ingredients, not worries.",
                "The ace shows. A garnish of fear, priced at premium.",
                "Insurance offered. My ganache does not need a warranty.",
                "An ace. Stay with the recipe."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle. Clean bench, fresh flour.",
                "New shoe. The starter is fed. Begin again.",
                "Reshuffled. Same ingredients, new batch.",
                "Fresh shoe. I scrape the bowl between bakes too.",
                "A shuffle. The mise en place resets.",
                "New cards. The oven does not remember the last tray."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A streak. Every tray rising evenly today.",
                "You are on a run. The kitchen calls that a service without returns.",
                "A good run. Butter cold, oven true, hands steady.",
                "Winning in sequence. Like perfect eclairs by the dozen.",
                "A hot streak. Even the caramel is behaving.",
                "Consistent results. That is the highest compliment I give.",
                "A run of wins. Technique, repeated. That is all a streak is."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold stretch. Ovens have cold spots. Rotate and breathe.",
                "Nothing rising. Some days the humidity wins. Not you.",
                "A bad run. The starter sulks in winter. It is not personal.",
                "Cold streak. Step back from the bench a moment. I always do.",
                "Losses in a row. Even proven recipes have flat days.",
                "A rough patch. Some batches simply refuse.",
                "Cold. Like my kitchen in January. The sweater helps more than worry."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Chips are low. A small plate, finished well, is still good dessert.",
                "Nearly out. In my kitchen we stop before the last egg. Wise here too.",
                "The stack is thin. Ending tidy is the skill I respect most.",
                "Low. Take the drink, take the walk. Both are excellent recipes.",
                "Little left. A short bake, well done, beats a long one burnt.",
                "Almost out. There is honor in a clean bench at closing.",
                "The last of it. I would box you a pastry for the road, gladly."));

        l.put(TableEvent.SESSION_START, List.of(
                "Good evening. Mind the flour on my sleeve.",
                "Welcome. The croissants go by nine. Everything else is negotiable.",
                "A new guest. Sit. I speak when it matters.",
                "Hello. I bake, Esme brews, Gus talks. You will learn the rhythm.",
                "Welcome in. The oven timer may interrupt us. It outranks everyone.",
                "Good evening. Wash your hands. Habit. Ignore me."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A weak up-card. Their base layer is unstable.",
                "Six showing. An underbaked foundation. It will not hold.",
                "The dealer shows little. A thin crust under real weight.",
                "Weak card up. The structure is wrong before the filling goes in.",
                "A soft up-card. I have seen sturdier meringue.",
                "Their up-card sags. Like dough on a humid day."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, no collapse. Proper lamination.",
                "Five cards standing. Layer on layer, none torn.",
                "A five-card hand. Patience, and the structure held.",
                "Five draws, still whole. That is careful folding.",
                "Five cards. Like puff pastry: thin additions, great height.",
                "Five without breaking. I count that as technique, not luck."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built. Exact to the gram.",
                "Assembled to twenty-one. A layer cake, level on top.",
                "Twenty-one by increments. That is how good things are made.",
                "The slow twenty-one. Proofed, shaped, finished. Correct.",
                "Twenty-one exactly. My scale approves.",
                "Built to the number. No trimming needed."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one point. An oven off by five degrees. Maddening.",
                "One short. The souffle fell at the pass.",
                "Lost by a point. A hairline crack in an otherwise perfect glaze.",
                "One point. The difference between set and soup.",
                "A single point. I once lost a medal to a wobble of custard.",
                "So near. The knife came out almost clean."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A big win. The showpiece survived the ride to the venue.",
                "Large. Like an order for three hundred, filled by dawn.",
                "A big result. The centerpiece cake of the evening.",
                "That was substantial. Well constructed, well paid.",
                "Enormous. Even I will look up from the dough for that.",
                "Big win. The kind of tray you photograph before serving."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double landed. Tempered exactly in the window.",
                "Doubled and won. Committed to the oven, rewarded at the bell.",
                "A double, successful. Risk measured, then baked.",
                "Doubled home. The single decision held, like good ganache.",
                "The double set perfectly. Glossy finish.",
                "Doubled, and it came out clean. Skewer test passed."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long session. I have imagined four full bakes since we started.",
                "Hours now. My starter needs feeding soon.",
                "Long evening. The ovens at my kitchen are cold and I miss them.",
                "We have been here a while. Dough would have doubled twice by now.",
                "A long sitting. Even brioche does not take this long.",
                "The evening stretches. Like strudel dough, thinner than it looks."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are ahead. A rising batch, on schedule.",
                "Up on the session. The books balance sweetly.",
                "Well ahead. Consistent temperature, consistent results.",
                "You are up. The display case is full, so to speak.",
                "Ahead. I rate the evening: strong technique, kind ovens.",
                "Running well. A service with every plate returned empty."));

        return new Persona("praline", "Praline", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(esme(), gus(), praline());
    }
}
