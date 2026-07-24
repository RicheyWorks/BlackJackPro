package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The characters who keep the "Evergreen" table theme company: a snowed-in
 * inn in December, pine and ribbon on every beam, cards by the fire.
 *
 * <h2>Same rule as every other cast</h2>
 * These lines run alongside real wagering, so the writing follows the one rule
 * the whole game follows: <b>react to what happened, never steer what happens
 * next.</b> Nothing here urges a bigger stake, frames a win as owed, or
 * suggests a loss can be recovered by playing on. The season is generous, but
 * nothing at this table is ever owed to anyone — a cold streak draws cocoa and
 * sympathy, a hot streak draws pleasure in the moment, and {@code LOW_CHIPS}
 * is the one event where the inn gets gentler rather than louder: a warm bed
 * upstairs and calling it a night is spoken of as entirely honorable.
 *
 * <h2>The company</h2>
 * Nan keeps the inn — blankets, cocoa, the fire in the grate, spare rooms
 * always made up — and notices when a guest is flagging. Mr. Jolly makes toys
 * and judges every hand like a mechanism that either works off the bench or
 * doesn't. Carol sings in a choir, hums between hands, and corrects Mr.
 * Jolly's enthusiasm the way a choir director corrects tempo.
 *
 * <p>{@code ChatterToneTest}'s rules apply here as they do everywhere else, so
 * a well-meaning future line cannot quietly break the tone.
 */
public final class HolidayCast {

    private HolidayCast() {}

    /**
     * The innkeeper. Motherly, dry, keeps everyone fed and under a blanket.
     * The inn is snowed in most of the winter and she would not have it any
     * other way. First to notice when a guest needs the fire more than the
     * felt.
     */
    public static Persona nan() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one off the deal. That warrants a marshmallow in your cocoa.",
                "A natural. Like finding the spare room already warm.",
                "Well, look at that. Dealt as snug as a made-up bed.",
                "An ace and a picture, straight away. The inn approves.",
                "That's the good hand, love. I'll stoke the fire in its honour.",
                "Twenty-one at the deal. Sweeter than cocoa off the stove.",
                "Dealt perfect. Some guests arrive with the snow already shovelled."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "There you are, love. Won fair as fresh snowfall.",
                "That's a win worth warming your hands over.",
                "Nicely taken. Supper tastes better after one of those.",
                "A win. I'll ladle you an extra bit of stew for that.",
                "Good hand, that. Cosy as the seat nearest the grate.",
                "Won it clean. The hearth crackled its approval.",
                "That one lands soft, like snow on the porch roof."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. Never mind, the stew doesn't care what the cards did.",
                "One card too many. Like overloading a tray of cocoa mugs.",
                "Over. Ah well, love. The fire's still warm either way.",
                "Went past it. Even good bread over-proofs sometimes.",
                "Bust, love. I've burnt a batch of shortbread the same way.",
                "One too many. Come sit closer to the grate a moment.",
                "Over the top. The drifts outside are deeper, if it helps."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Beaten fair, love. The cocoa's still on regardless.",
                "The dealer had that one. Warm your hands and let it go.",
                "A loss. The inn's seen colder nights than that.",
                "That hand just lost, love. Nothing a blanket won't soften.",
                "The house took it. I'll not let them take the fire too.",
                "Lost on the count. The soup pot doesn't hold it against you.",
                "Ah, that's a shame. Extra log on the fire for that one."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Two guests reaching for the same armchair, both sitting.",
                "Even. Nobody's cocoa gets cold over that.",
                "A tie, love. The snow keeps falling either way.",
                "Push. That's the inn at midday: nothing moving, all well.",
                "All square. Like beds made and no one checking in.",
                "Even hands. I'll take quiet where I find it."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half back and done. Sensible as coming in out of the snow.",
                "Surrendered, love. Wise guests know when to take the stairs up.",
                "Half saved. That's a spare blanket you didn't lose.",
                "Giving that one up was right. Some roads you don't drive in a storm.",
                "A surrender. There's no shame in latching the shutters early.",
                "Handing it back. Good. That hand had draughts coming through it."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Two hands now. Like making up twin beds in the same room.",
                "Split, love. I'll keep an eye on both, same as boiling pots.",
                "A pair parted. Two mugs from one jug of cocoa.",
                "Splitting them. Twice the fuss, says the innkeeper in me.",
                "Two hands going. Mind them like bread in the oven, love.",
                "Split. Well, the inn has room for both of them."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled, love. That's laying both quilts on one bed.",
                "One card coming. Like waiting up for the last guest in a storm.",
                "Doubling down. Bold as serving the good preserves.",
                "Doubled. I'll hold my breath and stir the pot.",
                "All on one card. The fire pops when you do that, I've noticed.",
                "A double, is it. I'll fetch the good mugs just in case."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer's gone over. More stew for the rest of us.",
                "Dealer bust. The house can shovel its own path tonight.",
                "Over they go. I'll not pretend the inn is sorry.",
                "The dealer busted. That's the chimney smoking on their side.",
                "Down goes the dealer. Warmest moment of the evening so far.",
                "They drew too deep. Happens when you don't know your own pantry.",
                "Dealer's over. Ring the supper bell, love."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer's natural. Like a guest arriving at midnight unannounced.",
                "They had twenty-one waiting. The inn calls that poor manners.",
                "A dealer blackjack. Cold gust straight through the doorway, that.",
                "Their natural. Nothing for it but another log and a sigh.",
                "The house had it tucked away. Sit closer to the fire, love.",
                "Dealer twenty-one, dealt. Some storms you just board up for."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "An ace up. They sell umbrellas hardest when it's already snowing.",
                "Insurance, love. The inn's roof I insure. Cards, never.",
                "There's the ace. Everyone suddenly reaches for their coats.",
                "They're offering insurance. I'd sooner buy snow in December.",
                "The ace question. Do as you like; the cocoa's free either way.",
                "Insurance on offer. That's a draughty little bargain, love."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. Like fresh linens: same beds underneath.",
                "A shuffle. The snow rearranges itself the same way all night.",
                "New cards, love. I'll put another log on while they fuss.",
                "Reshuffled. Same as beating out the quilts: tidier, not different.",
                "There goes the shoe. Time enough to check the stew.",
                "A fresh shoe. The inn's seen a hundred and fed them all."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're on a lovely run, love. Like a week of clear mornings.",
                "A streak. The fire's burning bright for you tonight.",
                "Win after win. I'd bottle this evening if I could.",
                "You're glowing warmer than the grate, love.",
                "A good run. Enjoy it like the first cocoa of the season.",
                "Winning along nicely. The inn loves a happy guest.",
                "That's several now. A warm spell in the middle of winter, that is."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold stretch, love. The fire's still lit. Come nearer it.",
                "Rough run. Storms pass over this inn all the time.",
                "The cards have iced over a bit. It isn't your doing.",
                "A lean spell. I'll put cocoa on; that's what it's for.",
                "Cold cards tonight. The blankets in this house are warmer.",
                "It's been a hard stretch. Sit by the grate a while, love.",
                "The shoe's gone chilly. My kitchen never does."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "That stack's thin, love. There's a warm bed upstairs with your name on.",
                "Nearly out. Calling it a night is honest innkeeping wisdom.",
                "Not much left. The fire and a blanket cost nothing here.",
                "Low chips, love. Guests who stop in time sleep the soundest.",
                "The stack's about done. Cocoa and an early night is a fine ending.",
                "Running low. No shame in it; the inn keeps no account of that.",
                "That's most of it gone, love. The spare room's made up, always is."));

        l.put(TableEvent.SESSION_START, List.of(
                "Come in out of the snow, love. There's a seat by the fire.",
                "Welcome to the inn. Cocoa's on the stove, cards are on the felt.",
                "Evening, love. Shake the snow off your boots and sit down.",
                "A new guest. I'll fetch a blanket if the draught finds you.",
                "Sit yourself down. The roads are drifted shut anyway.",
                "Welcome in. Supper's simmering and the shoe's fresh."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six showing. That's their pipes freezing, not yours.",
                "Weak card up. Like an innkeeper caught without firewood.",
                "The dealer's showing a poor one. Warming thought, that.",
                "That up-card's thin as a summer blanket.",
                "A weak one up. Their roof's leaking and it's snowing.",
                "Look at that up-card. Somebody left their door open."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, still under. Like fitting five guests in the small room.",
                "A five-card hand. Patient as bread rising.",
                "Five of them and no bust. That's careful packing, love.",
                "Five cards. You've stacked them like winter firewood.",
                "Still standing on five. The inn admires a careful builder.",
                "Five cards under the line. Like blankets: layer by layer."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built by hand. Like a good stew: slow and right.",
                "Got to twenty-one the long way. Worth every stir, love.",
                "Twenty-one, card by card. That's home cooking, that is.",
                "The full twenty-one, assembled. I'd serve that with pride.",
                "Twenty-one done slowly. The best warmth is the kind you build.",
                "There's twenty-one, the scenic way. Like the road to this inn."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one point, love. That stings like snow down a collar.",
                "One short. Come by the fire, that sort needs thawing.",
                "Lost by a whisker. Even the stew pot sighed.",
                "A single point in it. Cruel as a February morning.",
                "So close, love. I'll top your cocoa up for that.",
                "One point. The icicle dropping just as you reach the door."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Now that's a proper win, love. The whole inn felt it.",
                "A big one. That'd keep this fireplace in logs all winter.",
                "My word, that's a pile. I'll bake something to mark it.",
                "That's a handsome win. The good preserves are coming out.",
                "A grand haul, love. Even the snow stopped to look.",
                "That win would pay a month of winter lodging, that would."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and won. Both quilts and the bed stayed warm.",
                "The double came in, love. Like a storm guest who pays in gold.",
                "Doubled home. That's the extra log catching just right.",
                "A double landed. I'll allow myself a small innkeeper's smile.",
                "Doubled and it held. Brave as crossing the yard in a whiteout.",
                "That double paid, love. Warms you twice, like chopping wood."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "You've been at the felt a good while, love. The stew's on its second pot.",
                "A long sit. The fire's been fed four times since you started.",
                "It's got late. The snow's up past the porch rail by now.",
                "Long evening, this. I've turned down half the beds already.",
                "We've been at it a while. The candles say midnight, near enough.",
                "You've sat a long stretch, love. Stand and stretch; the inn can wait."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well ahead, love. The pantry looks like that in October.",
                "Up nicely. Your stack's grown like a snowdrift against the fence.",
                "Ahead of the game. Sleep will come easy tonight, I'd say.",
                "You're up, love. Enjoy it like the first fire of the season.",
                "That stack's thriving. Well-fed guests and well-fed piles, my favourites.",
                "Comfortably ahead. The inn likes to see a guest do well."));

        return new Persona("nan", "Nan", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * The toymaker. Booming, delighted by mechanisms, permanently flecked with
     * paint. Judges every hand the way he judges a toy: it either works off
     * the bench or it goes back to the bench, and both outcomes fascinate him.
     */
    public static Persona jolly() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "HA! Twenty-one off the deal! Wound once and it runs forever!",
                "A natural! That's a toy that walks straight off the workbench!",
                "Blackjack! Not a screw loose, not a gear missing!",
                "Dealt twenty-one! Painted, dried, and boxed before breakfast!",
                "Oh ho! The mainspring of all hands, that one!",
                "A natural! The kind of thing I'd put in the shop window!",
                "Blackjack, straight out! Clockwork the way clockwork dreams of being!"));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "It works! Off the bench and marching!",
                "A win! Sturdy little mechanism, that hand!",
                "Ha! That hand wound up tight and toddled home!",
                "Won! I'd stamp that one APPROVED in red paint!",
                "There's a hand that passes inspection! Ship it!",
                "A winner! Every cog in that one turned as drawn!",
                "That worked first try! Rare on my bench, I can tell you!"));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust! Overwound! The spring's gone SPROING, as we say in the trade!",
                "Over! Like a toy soldier with one leg glued on backwards!",
                "Twenty-two! Back to the bench with that one!",
                "Bust! The wobble got it. The wobble always gets it!",
                "Snapped! Even good clockwork over-ticks now and then!",
                "Over the top! That's a drum with the skin split!",
                "Ah, bust! I've built prettier failures myself, mind you!"));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost! And the workmanship was sound! It happens to fine toys too!",
                "Beaten! The design was right, the paint just hadn't dried!",
                "The dealer's model edged yours out! No fault in your gears!",
                "A loss! Some toys work perfectly and still don't sell!",
                "Outbuilt on that one! Their contraption ticked a notch higher!",
                "Lost on the count! I'd still display that hand on the shelf!",
                "Bah! Fine assembly, wrong showroom!"));

        l.put(TableEvent.PUSH, List.of(
                "A push! Two wind-up soldiers marching nose to nose!",
                "Even! Both toys stopped ticking at the very same second!",
                "A tie! Neither mechanism outlasts the other! Remarkable!",
                "Push! Two music boxes wound to exactly the same turn!",
                "All square! I'll call it a matched set and price it as one!",
                "A standoff! Both springs unwound at once!"));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender! Sometimes you scrap the prototype and save the parts!",
                "Half back! A salvaged axle is still an axle!",
                "Giving it up! Wise! Not every design deserves paint!",
                "Surrendered! Better one recalled toy than a broken shelf of them!",
                "Half saved! I keep a whole drawer of half-finished ideas myself!",
                "Scrapped it! Good workshop sense, that!"));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split! Two toys from one kit! Efficiency!",
                "A pair parted! Like sawing the rocking horse into two ponies!",
                "Splitting! Twice the assembly, twice the delight!",
                "Two hands now! My favourite kind of production line!",
                "Split them! Every good soldier set comes in twos!",
                "A split! Twice the gears spinning! Glorious!"));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled! One turn of the key and we see what it does!",
                "A double! Bold engineering! One card bears the whole load!",
                "Doubling down! Full wind on the mainspring!",
                "Doubled! The whole toy hangs on a single cog now!",
                "One card to finish the build! I love this part!",
                "Doubled! That's gluing the last piece with the shop watching!"));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer BURST a spring! Ha! Straight to the reject pile!",
                "Dealer bust! Their toy fell right off the display table!",
                "Over they went! Faulty workmanship, clearly!",
                "The house busted! Their soldier tipped over mid-march!",
                "Dealer's over! Somebody wound them one turn too far!",
                "Bust for the house! Recall notice on that mechanism!",
                "The dealer went over! Music to a toymaker's ears!"));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer blackjack! A finished toy pulled from under the counter!",
                "Their natural! They had it pre-built all along! Rascals!",
                "The house shows twenty-one! Factory-made, none of the charm!",
                "Dealer's natural! Some machines just come assembled! Annoying ones!",
                "Blackjack for the house! I refuse to admire the craftsmanship!",
                "Their twenty-one on the deal! Store-bought, I call that!"));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance! I never insure a toy against being a toy!",
                "An ace up! They're selling worry with a bow on it!",
                "Insurance, they call it! Wrapping paper over an empty box!",
                "Ho! The ace! Everyone checks their pockets for spare springs!",
                "They offer insurance like a rattle offers music! Loudly and badly!",
                "The ace question! My workshop policy: build well, shrug often!"));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle! All the parts back in the bin! Fresh build!",
                "New shoe! Like tipping out the marble jar and starting over!",
                "Reshuffled! Every gear back to the parts drawer!",
                "Fresh cards! The workshop floor is swept, so to speak!",
                "A new shoe! Same pieces, brand new toy box!",
                "Shuffle! I do love the sound! Like a bag of dominoes!"));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A streak! Every toy off your bench is walking today!",
                "Win upon win! The whole production line is ticking!",
                "What a run! Like test day when every soldier stands up straight!",
                "You're on a roll! The paint dries faster when you're grinning!",
                "A hot streak! The workshop smells of victory and varnish!",
                "Still winning! Somebody oiled this evening properly!",
                "Ha! A run like a wind-up train on a downhill track!"));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold stretch. Even my best sellers sat unsold some seasons.",
                "Rough patch. Half my prototypes never tick on the first wind.",
                "Nothing's working. Some days the glue just won't set.",
                "A bad run. I once painted forty soldiers the wrong shade of red.",
                "Cold cards. The bench is patient and so am I.",
                "The mechanisms are stubborn tonight. They come around, or don't.",
                "A lean spell. Every toymaker knows the quiet weeks."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "The chip box is nearly empty, friend. Closing the shop is honest work.",
                "Running low. Even the workshop lamps get turned off at night.",
                "Not many left. A good craftsman knows when the day's build is done.",
                "Nearly out. I pack the tools away proud, never sorry.",
                "Low on chips. No toy was ever improved by a tired maker.",
                "The stack's small, friend. Shelves get restocked after a night's sleep.",
                "Almost out. Downing tools is a skill I rate as high as carving."));

        l.put(TableEvent.SESSION_START, List.of(
                "A new player! Welcome to the merriest bench in the building!",
                "Come in, come in! Mind the sawdust and grab a seat!",
                "Ho ho! Fresh company! I'm Jolly, and yes, I've heard all the jokes!",
                "Welcome! I make toys! Everything else here is Nan's department!",
                "A newcomer! Sit, sit! The felt is friendlier than it looks!",
                "Welcome aboard the workshop table! Watch for stray marbles!"));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six showing! That's a toy with a cracked axle if I ever saw one!",
                "Weak up-card! Their mechanism is missing a tooth!",
                "Look at that six! Wobbly as my first toy soldier!",
                "A poor card up! Their spring is rusted through!",
                "The dealer shows a dud! Straight from the reject bin!",
                "Weak card! Somebody skipped the quality check!"));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and standing! A five-piece build with no glue showing!",
                "Five parts, one working toy! Master craftsmanship!",
                "A five-card hand! Assembled like a model ship, plank by plank!",
                "Five and alive! That's a kit with every piece used!",
                "Five cards! I'd frame the blueprints for that one!",
                "Five without busting! The tolerances on that are exquisite!"));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, hand-assembled! From raw parts to shop window!",
                "The full twenty-one! Every piece clicked into place!",
                "Twenty-one on three cards or more! That's carving, not luck!",
                "Built to twenty-one exactly! Measured twice, drew once!",
                "Twenty-one the crafted way! I salute a fellow builder!",
                "A constructed twenty-one! Worth more than a dealt one, in spirit!"));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by ONE! A whisker of paint thickness!",
                "One point short! The soldier fell at the edge of the shelf!",
                "By a single point! That's a gear tooth's width!",
                "One off! Like losing the race by half a marble!",
                "Beaten by one! The cruellest measurement in the workshop!",
                "A one-point loss! My calipers weep!"));

        l.put(TableEvent.BIG_WIN, List.of(
                "A BIG one! That's the giant dollhouse of wins!",
                "What a haul! Enough to gild every soldier in the shop!",
                "A whopping win! Ring the workshop bell!",
                "Massive! Like selling the whole window display in one go!",
                "A grand pile! I shall build you a tiny trophy!",
                "Enormous! That win needs its own wrapping paper!"));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double landed! One turn of the key and it marched home!",
                "Doubled and won! Precision engineering, that!",
                "A double that worked! First-wind success! The rarest kind!",
                "Doubled home! The last cog dropped in perfectly!",
                "The double paid! Somebody give that card a coat of gold paint!",
                "Doubled and delivered! Boxed, bowed, and under the tree!"));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long shift at the bench! My kind of evening!",
                "We've been at it for hours! The lamps have burned low!",
                "A marathon session! Even my wind-up owl has wound down!",
                "Long night! The workshop clock has lapped us twice!",
                "Hours in! True craftsmen lose track; I lost it long ago!",
                "Still here! The sawdust has settled twice over!"));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well up! The order book of a champion!",
                "Ahead of the game! Your stack is stacked like a block tower!",
                "Up and thriving! Business is booming, as they say of me!",
                "A tidy profit! The shop window would envy that pile!",
                "You're winning overall! Sturdy work throughout!",
                "Well ahead! That stack could buy my entire soldier shelf!"));

        return new Persona("jolly", "Mr. Jolly", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * The choir singer. Quiet, exact, hums between hands, hears every round as
     * phrasing and timing. Wry in a pitch-perfect way, and the only person at
     * the table who can bring Mr. Jolly down a dynamic marking.
     */
    public static Persona carol() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one on the downbeat. The whole hand in one chord.",
                "A natural. Perfect pitch, no rehearsal.",
                "Dealt twenty-one. The choir hitting the opening note as one.",
                "A natural. Some songs arrive already in tune.",
                "Twenty-one, first phrase. I'd give that a solo.",
                "Straight to the high note. No warm-up required.",
                "A natural. Even Jolly went quiet for a beat. Savour both."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win, sung cleanly. No flat notes.",
                "That resolved nicely. Tonic chord, home safe.",
                "Won on tempo. The measure ends where it should.",
                "A clean cadence, that hand.",
                "Nicely won. I'll hum a little fanfare, quietly.",
                "That hand kept its pitch all the way through.",
                "A win. Small applause, from the wrist."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. That's a note held one beat too long.",
                "Over. The descant climbed out of range.",
                "Twenty-two. Sharp, in the painful sense.",
                "Bust. Even good choirs crack on the high one.",
                "A touch over. The rest was there for the taking. Rests matter.",
                "Over the top, like an encore nobody requested.",
                "Bust. We'll call it an experimental key change."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "A loss, sung in tune. The judges were simply unkind.",
                "Lost on the count. Wrong verse, right voice.",
                "The dealer finished a note higher. It happens in auditions too.",
                "Beaten fairly. Some duets go to the other singer.",
                "A loss. Take a breath at the bar line and carry on.",
                "Lost, though the phrasing was lovely.",
                "The house sang louder. Louder isn't better, but it scored."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Perfect unison; nobody wins the harmony.",
                "Even. Two voices on the same note.",
                "A tie. The rest between verses.",
                "Push. Call it a fermata; nothing moves until the next hand.",
                "Level. The choir holds its breath together.",
                "A push. Silence, but the shaped kind."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "A surrender. Knowing when not to sing is half the art.",
                "Half back. A graceful decrescendo, that.",
                "Surrendered. The rest is a note too, as I keep telling Jolly.",
                "Stepping out of that hand. Good singers mark the tacet.",
                "Half kept. Better a short verse than a cracked chorus.",
                "A wise surrender. Not every phrase deserves breath."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split. One melody becomes a duet.",
                "Two hands now. Watch the timing on both parts.",
                "Split. Soprano line and alto line; off you go.",
                "A pair divided. Counterpoint, if you keep them honest.",
                "Splitting. Two parts, one breath. Manageable.",
                "The pair parts ways. Harmony needs at least two lines."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "A double. The crescendo before the final note.",
                "Doubled. One card, held like a last chord.",
                "Doubling down. Committing to the key, at last.",
                "Doubled. Sing it once and sing it full.",
                "One card decides the phrase. Breathe first.",
                "A double. Fortissimo, then. Just this once."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer went over. Their voice cracked on the final note.",
                "Dealer bust. Forced to sing past their range; it shows.",
                "Over. The house lost the key mid-verse.",
                "The dealer busted. An ending in the wrong octave.",
                "Their total collapsed. The choir may smile discreetly.",
                "Dealer over. The rules wrote them a phrase they couldn't hold.",
                "The house went sharp. Satisfying, in a quiet way."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. The song was decided before the first bar.",
                "Their blackjack. Over before anyone drew breath.",
                "The house had twenty-one hidden. An unlisted soloist.",
                "Dealer's natural. No harmony survives a note like that.",
                "Their twenty-one, dealt whole. We were never in the programme.",
                "A dealer blackjack. Hum through it; nothing else helps."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. A verse they sell that never resolves.",
                "The ace shows. Everyone hums a nervous semitone.",
                "Insurance offered. It's sung sweetly and priced sourly.",
                "An ace up. Ignore the sales pitch; mind your own pitch.",
                "Insurance. I decline the way I decline a bad key: politely.",
                "The ace, and the little side song they sell with it. Pass, I would."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle. Da capo: back to the top of the page.",
                "Fresh shoe. Same song, new copy of the sheet.",
                "Reshuffled. The choir re-robes; the music is unchanged.",
                "New shoe. Consider it an interlude, hummed.",
                "The cards are re-voiced. Every part still in the score.",
                "A shuffle. Jolly hears destiny; I hear a page turn."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A streak. The choir has found its blend tonight.",
                "Win after win. That's a phrase worth holding.",
                "A run of wins, like verses landing one after another.",
                "You're in full voice tonight. Enjoy the acoustic.",
                "A lovely run. Every entrance on time.",
                "Winning in sequence. Almost a melody by now.",
                "A streak. Jolly, tempo; let the moment sing itself."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold stretch. Every choir has a flat rehearsal.",
                "A run of losses. Rests are written into every score.",
                "The cards have gone toneless a while. Not your voice, theirs.",
                "A quiet passage. The music always comes back around.",
                "Cold cards. I'll hum something warm in the meantime.",
                "A rough stretch. We sing through winter; it's the whole repertoire.",
                "Losses in a row. Think of it as the minor-key verse."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "The stack is nearly quiet. Ending on your own note is graceful.",
                "Low chips. A final bow and a warm bed is a fine last verse.",
                "Nearly out. The best performances know when the song ends.",
                "Little left. Closing the songbook gently is still music.",
                "The chips are down to a whisper. Whispers are allowed to rest.",
                "Running low. Encores are optional; rest never needs an excuse.",
                "The stack has gone soft. Diminuendo, then home; songs end that way."));

        l.put(TableEvent.SESSION_START, List.of(
                "A new voice at the table. Welcome; we needed a fourth part.",
                "Welcome. I'm Carol. Yes, like the songs. No, I don't take requests.",
                "Evening. Sit anywhere; the acoustics are equally poor throughout.",
                "A newcomer. Jolly will be loud; consider me the counterbalance.",
                "Welcome in from the snow. We're between verses just now.",
                "Hello. I hum between hands; you'll stop noticing by the third."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six showing. The dealer's entrance is off-key already.",
                "Weak up-card. Someone in their section is singing flat.",
                "A poor card up. Their opening phrase wobbles.",
                "The dealer shows a six. Listen; that's an unresolved chord.",
                "A weak card. Their part is written badly this hand.",
                "That up-card sounds thin from here."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and under. A five-verse song that never lost the key.",
                "Five draws, no bust. That's breath control.",
                "A five-card hand. Long phrases suit the patient.",
                "Five cards, still standing. The stanza held together.",
                "Five and safe. Every entry placed exactly on the beat.",
                "A five-card build. Quiet virtuosity, that."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, note by note. The composed version.",
                "Built to twenty-one. A melody assembled, not borrowed.",
                "Twenty-one the long way. The whole scale, sung in order.",
                "A constructed twenty-one. Slower art, deeper bow.",
                "Twenty-one by steps. That's how you learn a part properly.",
                "Twenty-one, arranged by hand. The arrangement earns applause."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. A semitone from harmony.",
                "One point off. The chord almost resolved.",
                "By a single point. That's a note away from the prize.",
                "One short. The saddest interval there is.",
                "A one-point loss. So near the tonic it aches.",
                "Missed by one. Even the rests winced."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A big win. That's the finale, complete with the loud bit.",
                "Substantial. The kind of chord that fills the rafters.",
                "A grand win. I may permit a full-voice measure for that.",
                "That's a swelling sort of win. Crescendo justified.",
                "A big one. Even the descant approves.",
                "A rich win. Held long, resolved sweetly."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double came home. A held note, landed cleanly.",
                "Doubled and won. Fortissimo with follow-through.",
                "The double resolved. One card, one perfect entrance.",
                "Doubled home. Committed to the key and rewarded.",
                "A double that sang. Jolly, you may boom; it's warranted.",
                "The doubled hand won. Clean attack, cleaner release."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long session. We're several movements in now.",
                "Hours at the table. The programme has run long.",
                "We've been here a while. Even carols end and start again.",
                "A long sit. My humming has covered two full songbooks.",
                "Late now. The final verses always feel slower.",
                "Long evening. Mind your posture; singers and players both slump."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're ahead. The whole set list is going your way.",
                "Well up. A performance worth the printed programme.",
                "Ahead of where you began. The refrain keeps returning kindly.",
                "You're up. Sing nothing; just enjoy the sustain.",
                "Comfortably ahead. The evening is in a major key.",
                "Up on the night. A recital that's earning its applause."));

        return new Persona("carol", "Carol", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(nan(), jolly(), carol());
    }
}
