package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The high-society cast, seated by the velvet-rope themes: the Crimson Room,
 * the Royal Court, and Brass Deco. A grande dame, a jazz crooner between sets,
 * and the driest butler in the building.
 *
 * <p>The writing rule here is the same one every cast obeys: <b>react to what
 * happened, never steer what happens next.</b> Nobody at this table urges a
 * larger stake, frames a win as owed, or suggests recovering a loss. The
 * Duchess's disgraced earls are cautionary comedy, never aspiration; a cold
 * streak draws sympathy or a change of subject; and {@code LOW_CHIPS} is met
 * with the firm societal position that retiring gracefully from the table is
 * the height of good breeding. {@code ChatterToneTest} enforces all of this
 * mechanically.
 */
public final class SocietyCast {

    private SocietyCast() {}

    /**
     * The Duchess. Arch, magnificent, faintly scandalous. Treats every hand as
     * society gossip, narrates the table like a season of balls and yachts and
     * disgraced earls, and is warm underneath all the chandeliers.
     */
    public static Persona duchess() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural, darling. The last time I felt this way, a prince fell overboard.",
                "Twenty-one at a stroke. The Countess would simply die of envy.",
                "Dealt like royalty. I intend to mention you at the Ambassador's soiree.",
                "How divine. The Earl of Wexley never once managed that, poor lamb.",
                "A natural! Positively the most scandalous thing since the regatta.",
                "Blackjack, darling. Wear it like a tiara.",
                "Straight off the deal. Even my late husband's yacht arrived less grandly.",
                "Twenty-one, unearned and glorious. Exactly how I like my champagne."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Victory, darling. It positively suits your complexion.",
                "Won, and gracefully. The Baroness could learn a thing or two.",
                "How splendid, darling. I intend to retell this with embellishments.",
                "A win. You'd be the toast of any ballroom in the county.",
                "Taken with style. The Viscount always grabbed; you receive.",
                "Oh, beautifully won. Society adores a graceful victor.",
                "You won that like old money, darling. Quietly and completely."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over twenty-one. We simply won't speak of it, like the Duke's toupee.",
                "Bust, darling. The Earl of Grafton did the same and moved to Belgium.",
                "One card too many. Much like the Countess's fourth husband.",
                "Oh darling, over the top. So was my second wedding, frankly.",
                "Busted. Avert your eyes, as one does at a badly planned garden party.",
                "Gone over. Even the best families overreach now and then.",
                "A bust. The orchestra played through worse at the winter ball."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Beaten, darling. How tediously the house insists on itself.",
                "A loss. Take it as the Baroness takes lemon: with a straight face.",
                "The dealer wins one. Society forgives a graceful defeat.",
                "Lost, and through no scandal of your own.",
                "Defeated fairly. It happened to Napoleon and he had better hats.",
                "The house takes that one. How dreadfully middle-class of it.",
                "A defeat, darling. Wear it lightly, like last season's pearls."));

        l.put(TableEvent.PUSH, List.of(
                "A tie, darling. Like two dowagers arriving in the same gown.",
                "A push. The polite society version of a duel.",
                "Even, darling. Nobody curtsies, nobody bows.",
                "A standoff. The Ambassador calls that diplomacy.",
                "All square. How terribly anticlimactic, like the opera's second act.",
                "A push. We remain precisely as fabulous as before."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrendering, darling. Even empires know when to leave the ball.",
                "Half back. A dignified exit is the rarest jewel of all.",
                "A graceful retreat. The Countess never learned it, and look at her hats.",
                "Wise, darling. One leaves the party while the candles are still tall.",
                "Half returned. Better a small curtsy than a grand collapse.",
                "Surrender, they call it. I call it declining a dreadful invitation."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Splitting the pair, darling. Like seating feuding cousins apart.",
                "Two hands. Ambition becomes you enormously.",
                "A split. The twins at the Wexley ball caused less commotion.",
                "Separated, darling. Like the Duke and Duchess at breakfast.",
                "Two hands at once. How gloriously greedy of you.",
                "Split them, darling. Twice the drama, and I do adore drama."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubling, darling. The boldness of a debutante in red.",
                "Doubled. My heart hasn't fluttered so since the yacht race verdict.",
                "One card decides everything. Positively operatic.",
                "Doubled down, darling. Fortune favours the well-dressed.",
                "A double. The whole ballroom would be holding its breath.",
                "Committed entirely. Like the Countess to her fourth engagement."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer busts! Delicious. Simply delicious.",
                "Over they go, like the Admiral into the punch bowl.",
                "The house busts, darling. Somewhere a chandelier just sparkled brighter.",
                "Busted! The most satisfying scandal of the evening.",
                "Down goes the dealer. I haven't enjoyed a fall so since the maypole affair.",
                "The house overreaches. It happens to the grandest families, you know.",
                "Dealer over twenty-one. Do savour it, darling, like good gossip."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural. How vulgar of them to be so fortunate.",
                "The house had it hidden all along. Positively cloak and dagger.",
                "Their blackjack, darling. Even the best parties have gatecrashers.",
                "An ambush. The Baroness would call it bad breeding.",
                "Dealt against from the start. Nothing to do but sip and endure.",
                "The dealer's natural. Some evenings the house wears the tiara."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance, darling. The Earl bought it once. He bought everything once.",
                "An ace showing. The whole salon holds its lorgnettes steady.",
                "Insurance. In my circle we call that paying to worry.",
                "The ace appears, and everyone remembers their solicitor.",
                "They offer insurance so charmingly. So did my third suitor.",
                "An ace up, darling. How the house does love its little mysteries."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A fresh shoe, darling. Like a new guest list, same old party.",
                "Shuffled. The cards rearrange themselves like place settings.",
                "A new shoe. The season turns, and nobody's the wiser for it.",
                "They shuffle with such ceremony. The coronation was briefer.",
                "Fresh cards, darling. I do adore a clean slate before midnight.",
                "The shoe is reborn. If only one's reputation reshuffled so easily."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A winning streak, darling. You're the season's most dazzling scandal.",
                "Again and again you win. The ballroom whispers your name.",
                "What a run. Even the Countess is pretending not to notice.",
                "You're glittering tonight, darling. Positively chandelier-esque.",
                "A streak like this made the Admiral insufferable for a decade.",
                "Winning in ropes, like pearls. Do enjoy the moment, darling.",
                "Such a run. Society will exaggerate it beautifully by morning."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold spell, darling. Even the Riviera has its grey mornings.",
                "The cards are being frosty. So was the Baroness, for forty years.",
                "Nothing lands. It is the shoe's failing, darling, never yours.",
                "A chilly stretch. I once endured a winter in Scotland. One survives.",
                "The cards have turned their backs, like debutantes at a dull dance.",
                "A cold run, darling. Champagne tastes the same either way.",
                "The shoe is sulking. Grand houses simply wait out the weather."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Running low, darling. Retiring gracefully is the height of good breeding.",
                "The stack grows slender. So do the best exits, made early and smiling.",
                "Nearly out, darling. The grandest guests always leave before the end.",
                "Low on chips. There is no shame in a beautifully timed farewell.",
                "A slender stack. My advice, as at any ball: depart while adored.",
                "Darling, the evening has been had. One may simply sweep out grandly.",
                "Little remains. The truly fashionable never stay for the last dance."));

        l.put(TableEvent.SESSION_START, List.of(
                "A new face. Do sit, darling. The gossip is excellent tonight.",
                "Welcome, darling. You've arrived fashionably, which is to say now.",
                "Ah, company. I was just about to tell the yacht story again.",
                "Do join us. The Duchess never plays favourites, publicly.",
                "Welcome to the table, darling. Mind Pemberton. He notices everything.",
                "A newcomer. How thrilling. The last one became a marvellous anecdote."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A weak card showing. The house has worn the wrong gown tonight.",
                "Look at that up-card, darling. Caught out like a forged invitation.",
                "A six for the dealer. One almost pities them. Almost.",
                "The dealer shows a poor card. The salon titters accordingly.",
                "That up-card, darling. The house has arrived underdressed.",
                "A limp little card. Even the footmen would blush."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, darling. A positively baroque arrangement.",
                "Five cards and standing. Like a dinner party that somehow worked.",
                "What a collection. You gather cards the way I gather invitations.",
                "Five of them. The most crowded hand since the royal box.",
                "Five cards, still under. Darling, that is practically embroidery.",
                "A five-card hand. Extravagant, and I approve of extravagance."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, assembled by hand. Like a really good rumour.",
                "You built that twenty-one yourself, darling. Artisanal.",
                "Twenty-one the long way. The scenic route through the gardens.",
                "Constructed to perfection. My dressmaker would weep.",
                "Twenty-one, piece by piece. A tiara of a hand.",
                "The full twenty-one, darling, earned like a title by marriage."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one point, darling. The cruellest cut since the guest list.",
                "A single point. Even the Baroness gasped, and she never gasps.",
                "By one. Like missing the boat train by a single minute.",
                "One point shy. Scandalously close, and scandal is my field.",
                "So near, darling. Operas have ended more kindly.",
                "Beaten by a whisker. Duels have been fought over less."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A magnificent haul, darling. The season officially has a headline.",
                "What a win. The chandeliers themselves lean in to look.",
                "A grand pile of chips. You've out-glittered the tiara table.",
                "That win, darling, will be exaggerated at every luncheon for a month.",
                "A triumph of a hand. Do let me be the one to spread it.",
                "Gloriously large. My late husband proposed with less flourish."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and won, darling. Audacity, rewarded, in evening dress.",
                "The double comes home. The ballroom would be on its feet.",
                "Doubled and victorious. Even the dowagers approve, silently.",
                "A double, landed. Like a swan dive from the Admiral's yacht.",
                "Boldness paid in full, darling. How the salon will talk.",
                "Doubled to glory. Someone fetch the good champagne."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've made a night of it, darling. The best scandals run long.",
                "Still here. The last party this long ended in two engagements.",
                "Quite the marathon, darling. Even the orchestra would have gone home.",
                "A long sitting. My jewels have seen shorter coronations.",
                "The hours have flown, darling, like doves at the garden party.",
                "What an evening it's been. Someone ought to paint it."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're well ahead, darling. Old money manners, new money pile.",
                "Comfortably up. Society smiles on the quietly triumphant.",
                "Ahead of the game, darling. The Baroness will want you at her table.",
                "Your stack has flourished, like the Wexley hedges.",
                "Up handsomely. Do carry it with insufferable grace.",
                "You're ahead, darling. Even the portraits look impressed."));

        return new Persona("duchess", "The Duchess", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * Maxie. Jazz crooner between sets, smooth as late-night radio. Speaks in
     * period bandstand slang, snaps his fingers at good hands, and treats the
     * shoe like a rhythm section that mostly keeps time.
     */
    public static Persona maxie() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Blackjack! Snap my fingers, that's the sweetest note in the book.",
                "A natural, cat! That's the band hitting the downbeat together.",
                "Twenty-one off the top! Somebody cue the horns.",
                "Blackjack, baby! That hand swings all by itself.",
                "Dealt clean at the downbeat. That's pure velvet, cat.",
                "A natural! Ain't heard a chord that pretty since Kansas City.",
                "Twenty-one on the deal! The shoe just took a solo.",
                "Blackjack! I'd write a tune about that hand if I had a pencil."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "That's a win, cat! Right in the pocket, right on the beat.",
                "You took that one smooth as a muted trumpet.",
                "Winner! Snap, snap. That's how the tune goes.",
                "Nice hand, daddy. Played it cool and it paid.",
                "A win, and clean. Like the brass section on a good night.",
                "You swung that one, cat. Swung it easy.",
                "That's the stuff! The bandstand approves."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Busted. Ouch. That's a clam right in the middle of the solo.",
                "Over the line, cat. Even the best horns crack a note.",
                "Bust. That card came in like a drummer rushing the tempo.",
                "Too many, baby. Sour note, but the set rolls on.",
                "Busted, cat. Some bars just don't resolve.",
                "Over twenty-one. That's the tune ending on the wrong chord.",
                "Ah, bust. Even Satchmo fluffs one now and then."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Dealer takes it. Tough crowd tonight, cat.",
                "A loss, smooth played though. The room just wasn't listening.",
                "House wins that one. Some nights the audience sits on its hands.",
                "You lost the hand, not the groove. Big difference, baby.",
                "Dealer edges you out. Like getting cut at a jam session, it stings.",
                "That one goes to the house. Even good bands lose the room sometimes.",
                "A loss, cat. Shake it loose like a stiff shoulder before the set."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Band and audience, dead even, nobody claps.",
                "Tie game, cat. That's two horns playing the same note.",
                "A push. The tune just held a rest, is all.",
                "Even up. Like trading fours and calling it square.",
                "Push, baby. Nobody swings, nobody sinks.",
                "A standoff. The rhythm section calls that a vamp."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Laying it down, cat. Smart. You don't solo over a train wreck.",
                "Half back and out. Sometimes you sit out a chorus.",
                "Surrender. Even Basie lays out when the chart's wrong.",
                "Folding that hand, baby. A rest is still part of the music.",
                "Half saved. That's knowing when the tune ain't yours.",
                "You bowed out smooth. That's musicianship, cat."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Splitting the pair! Now that's a duet, cat.",
                "Two hands going. Like playing piano and singing at once.",
                "Split 'em, baby. Twin melodies, one bandstand.",
                "A split! The combo just added a second horn.",
                "Two hands now. Left hand, right hand, same swing.",
                "Splitting up the pair. Harmony works better in two parts anyway."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubling down! One card, one big brass hit.",
                "Doubled, cat. That's stepping up for the solo.",
                "One card carries the whole tune now. Play it, shoe.",
                "A double! That's leaning into the downbeat, baby.",
                "Doubled down. Big note, held long. Here we go.",
                "One card to call the tune. That's nerve, cat, real brass."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer busts! The house just dropped its trombone.",
                "Over they go! Snap those fingers, cat, that's our beat.",
                "House busts! Sweetest sound since the encore in Memphis.",
                "The dealer cracks! Even the bartender's grinning.",
                "Busted dealer, baby! The whole bandstand felt that one.",
                "Over twenty-one for the house! That's music, plain and simple.",
                "Dealer goes down! Somebody hit the cymbal."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer's natural. That's the house stealing the last chorus.",
                "Blackjack for the house. Cold opening, cat.",
                "They had it hid the whole time. Sneaky as a key change.",
                "House natural. The tune was over before we counted it in.",
                "Dealer's twenty-one. Some sets open with a punch, baby.",
                "The house shows a natural. Rough intro, cat. Rough intro."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Ace up, and they're selling insurance. I don't buy sheet music twice.",
                "Insurance, huh. That's a side hustle wearing a bow tie.",
                "The ace shows and the pitch starts. Smooth patter, cat, but patter.",
                "Insurance. Sounds sweet, plays flat. Your call, baby.",
                "Ace showing. The house croons real pretty when it wants a dime.",
                "They call it insurance. I call it a cover charge, cat."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe, fresh set. Count it in, cat.",
                "Shuffle time. The deck's just changing keys.",
                "New shoe. Same songbook, brand new arrangement.",
                "They're mixing the cards. Intermission for the rhythm section.",
                "A shuffle, baby. Every set starts from the top.",
                "New shoe coming. I'll hum something while we wait."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're on a streak, cat! The whole joint's swinging your way.",
                "Win after win! That's a groove, baby, a real deep groove.",
                "Hot streak! You're the headliner tonight.",
                "Look at you go! The shoe's keeping your tempo now.",
                "Streaking hot, cat! Snap, snap, snap. Three for three.",
                "You're cooking! Ain't heard a run like that since the Savoy.",
                "What a run! The room's on its feet, baby."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold stretch, cat. Every band plays an empty room some nights.",
                "The shoe's gone quiet on you. Ain't your playing, it's the room.",
                "A cold run. Even the blues started as somebody's bad week.",
                "Rough set, baby. The tempo drags for everybody sometimes.",
                "Cards gone cold. I hum it low and let the set breathe.",
                "A chilly run, cat. Even hot horns need a rest between sets.",
                "Cold table. Some choruses you just play through soft."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Stack's running low, cat. Ain't no shame in ending the set early.",
                "Chips are thin, baby. The best players know when the gig is done.",
                "Running short. A clean last number beats an encore too far.",
                "Low stack, cat. Pack the horn while the crowd still loves you.",
                "Not much left there. Closing time comes for every bandstand.",
                "Thin chips, baby. Walk out humming. That's the classy finish.",
                "Stack's low. Even the longest set ends on a soft note, cat."));

        l.put(TableEvent.SESSION_START, List.of(
                "Hey, new cat at the table! Pull up, the set's just starting.",
                "Welcome, baby. I'm between sets, so you got me all night.",
                "A fresh face! Sit in, cat. Everybody solos here eventually.",
                "New player! The Duchess talks, Pemberton doesn't. I sing.",
                "Hey now! Grab a seat. The felt's warm and the shoe's in tune.",
                "Welcome to the combo, cat. We play whatever the cards call."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Dealer's showing a weak one. That's the house singing off-key.",
                "Look at that up-card, cat. The dealer's drummer just quit.",
                "A soft card up. The house is playing with a busted string.",
                "Weak up-card, baby. The dealer's got a frog in his throat.",
                "That card's a dud, cat. Even the piano player winced.",
                "Dealer shows a sad one. The band smells it, believe me."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and standing! That's a long solo landed clean.",
                "Five cards, cat! You built a whole chorus out of that.",
                "A five-card hand! Verse, bridge, and out, all under the wire.",
                "Five of 'em and no bust! That's tightrope trumpet, baby.",
                "Five cards! You held that phrase longer than Prez holds a note.",
                "Five cards standing. Cool hands, cat. Real cool hands."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built note by note! That's composing, cat.",
                "You worked your way to twenty-one. The long melody pays off.",
                "Twenty-one the hard way! Card by card, like a good slow burn.",
                "That's twenty-one, hand-made. Sweetest arrangement of the night.",
                "Twenty-one on the build! Snap those fingers, baby.",
                "You climbed to twenty-one. That's a walking bassline of a hand."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. That's a heartbreaker ballad right there, cat.",
                "One point short. Like missing the last train after the gig.",
                "By a single point. The blues were invented for hands like that.",
                "One away, baby. That note hung flat at the worst time.",
                "Beat by one. Even torch songs ain't that cruel, cat.",
                "A one-point loss. I felt that in the horn section of my heart."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Big win, cat! That's a full house standing and stomping.",
                "What a haul! The tip jar's never seen a night like this.",
                "That's a fat stack of a win, baby! Front-page billing.",
                "Big one! Somebody strike up the whole brass section.",
                "A heavyweight win, cat! That'll echo past closing time.",
                "Look at that pile! You're top of the marquee tonight."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and won! That big note landed square, baby.",
                "The double comes home! Hit it, drummer.",
                "Doubled up and paid! That's the solo getting a standing O.",
                "A double, cashed! Cat, that was smooth as late-night radio.",
                "Doubled and in! The nerve section carried the tune.",
                "That double sang, baby! Right on pitch, right on time."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "Long session, cat. We're deep in the after-hours set now.",
                "We've been swinging a good while. My reed would be worn out.",
                "Long night, baby. This is the hour the real music happens.",
                "Still here, still playing. That's a double gig, cat.",
                "The set's run long. Even the moon's checking its watch.",
                "Hours deep now. Somebody flip the record, baby."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're way up, cat! The stack's swinging higher than the bandstand.",
                "Ahead of the game, baby. That pile's got rhythm.",
                "Up nice and easy. Playing this room like you wrote the chart.",
                "Look at those winnings, cat. That's headline money.",
                "You're riding high tonight. The whole combo's noticed.",
                "Well up, baby. That stack could buy the band a round."));

        return new Persona("maxie", "Maxie", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * Pemberton. The butler. Impeccable, deadpan, and devastating in
     * understatement; speaks rarely, and with surgical precision. The driest
     * voice in the entire game, which he would consider a professional duty.
     */
    public static Persona pemberton() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural, sir. I shall decant the good sherry.",
                "Blackjack. Most gratifying, sir.",
                "Twenty-one, dealt outright. The kitchen will hear of this, sir.",
                "A natural, sir. I permitted myself an eyebrow.",
                "Dealt to perfection. One rarely says so, sir. One says so now.",
                "Blackjack, sir. I have taken the liberty of being impressed.",
                "An ace and a ten, sir. Precisely as they ought to arrive.",
                "Twenty-one on the deal. Commendable, sir, if unearned."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win, sir. Entirely satisfactory.",
                "The hand is yours, sir. Noted in the ledger of the evening.",
                "Victory, sir. Modestly achieved, correctly enjoyed.",
                "Capably played, sir. I say so sparingly.",
                "You have won, sir. The silver will be polished in celebration.",
                "A creditable win, sir. The household approves.",
                "The dealer is bested, sir. Quietly done."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Most regrettable, sir.",
                "Over twenty-one, sir. I shall fetch the brandy.",
                "A bust. One card past propriety, sir.",
                "Twenty-two, sir. The arithmetic is unforgiving.",
                "Gone over, sir. I have seen worse. Not recently.",
                "A bust, sir. I shall say nothing further on the matter.",
                "The hand has expired, sir. My condolences to it."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "The house prevails, sir. Regrettably.",
                "A loss, sir. Conducted with dignity throughout.",
                "The dealer's total stands higher, sir. An impertinence.",
                "Defeat, sir. I treat it as I treat dust. Briefly noted, removed.",
                "The hand goes against you, sir. The fault lies with the cards.",
                "A loss, sir. The cellar remains fully stocked, if it helps.",
                "Beaten on the total, sir. No error of yours was involved."));

        l.put(TableEvent.PUSH, List.of(
                "A push, sir. Honours even, as at the servants' ball.",
                "A tie. The correct outcome when neither party deserves the other.",
                "Even totals, sir. Nothing requires dusting.",
                "A push. I shall log it under events of no consequence, sir.",
                "A standoff, sir. Both parties may keep their composure.",
                "Even, sir. The decanter and I remain unmoved."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender, sir. A gentleman knows which invitations to decline.",
                "Half retained. Prudence, sir, is never out of season.",
                "A withdrawal, sir. Executed with appropriate discretion.",
                "Wisely folded, sir. Some hands are beneath your attention.",
                "Half returned, sir. I consider the matter closed.",
                "A tactful retreat, sir. The staff would have done likewise."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Splitting the pair, sir. Very good. I shall watch both.",
                "Two hands, sir. Like managing two dinner services at once.",
                "The pair is divided, sir. Order is often improved by separation.",
                "A split. Twice the correspondence, sir, twice the outcomes.",
                "Two hands now stand, sir. I have arranged my attention accordingly.",
                "Divided, sir. As one separates the quarrelsome guests."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubling, sir. A firm decision. I approve of firm decisions.",
                "Doubled down. One card will settle the matter, sir.",
                "A double, sir. Boldness, properly ironed.",
                "The wager is doubled, sir. I shall stand very still.",
                "Doubled, sir. Decisiveness becomes a gentleman.",
                "One card only now, sir. I find the economy admirable."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer has gone over, sir. How very careless of them.",
                "A dealer bust. I permit myself a small cough of satisfaction.",
                "The house exceeds twenty-one, sir. Standards are slipping.",
                "Over, sir. The dealer's discipline was found wanting.",
                "The house busts. I shall not gloat, sir. Visibly.",
                "Twenty-two for the dealer, sir. Most gratifying arithmetic.",
                "The dealer collapses, sir. One tidies up and moves along."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "A dealer natural, sir. Unsporting, though within the rules.",
                "The house held it concealed, sir. Butlers notice such things.",
                "Their blackjack. I shall dim the lights respectfully, sir.",
                "A natural for the house, sir. No remedy existed.",
                "The dealer's twenty-one, sir. An ambush, elegantly laid.",
                "Concluded at the deal, sir. Nothing further was possible."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance is offered, sir. I decline things politely for a living.",
                "An ace is showing, sir. The house extends a hand. Inspect the glove.",
                "Insurance, sir. The name flatters the product.",
                "The ace appears, sir. The offer accompanying it is not my favourite.",
                "They propose insurance, sir. I propose composure.",
                "An ace up, sir. The household takes a dim view of side arrangements."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A fresh shoe, sir. The cards are in acceptable disorder.",
                "The shuffle, sir. Performed with almost adequate ceremony.",
                "New cards, sir. Their behaviour, I regret, will not improve.",
                "The shoe is replenished, sir. As one refreshes the decanters.",
                "Shuffled, sir. Chaos, but tidily presented.",
                "A new shoe, sir. I have dusted no cleaner slate."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A sequence of wins, sir. I have informed the kitchen.",
                "You are on a streak, sir. I remain outwardly composed.",
                "Several victories running, sir. The good glasses are out.",
                "A winning run, sir. Most irregular. Most welcome.",
                "The wins accumulate, sir. I shall require a larger tray.",
                "A streak, sir. The staff downstairs are quietly euphoric.",
                "Another win, sir. I am scarcely able to contain my eyebrow."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A lean stretch, sir. The cards are behaving beneath their station.",
                "A cold run, sir. I shall have a word with the shoe. It will not help.",
                "Nothing lands at present, sir. The fault is not in your conduct.",
                "The cards are unkind, sir. One endures, as with visiting relatives.",
                "A cold spell, sir. The house is being tiresome.",
                "An inhospitable shoe, sir. I have known warmer pantries.",
                "The run is against you, sir. I shall stand fractionally closer."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "The chips run low, sir. A dignified withdrawal is always in good taste.",
                "Not a great deal remains, sir. The coat and the evening air await.",
                "The stack is diminished, sir. Leaving well is a skill. You possess it.",
                "Funds are low, sir. The finest guests depart before they must.",
                "A modest remainder, sir. There is no dishonour in an early carriage.",
                "The chips dwindle, sir. I shall have your things ready, whenever.",
                "Low, sir. A gentleman's exit is timed, never forced."));

        l.put(TableEvent.SESSION_START, List.of(
                "Good evening, sir. The table is prepared. The company, variable.",
                "Welcome, sir. I answer to Pemberton. The others answer to no one.",
                "A new guest. Your seat has been dusted, sir. Twice.",
                "Good evening. I keep the observations brief, sir. And accurate.",
                "Welcome to the table, sir. The Duchess will explain everyone shortly.",
                "You are expected, sir. You were not, but a butler adjusts."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "The dealer shows a weak card, sir. I noticed. I notice everything.",
                "An unfortunate up-card for the house, sir. My sympathy is elsewhere.",
                "A six, sir. The dealer wears it like an ill-fitting waistcoat.",
                "The up-card is poor, sir. The house must now draw. A pity. For them.",
                "A shabby card showing, sir. One averts one's eyes from such service.",
                "The dealer's card is beneath the occasion, sir."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, sir, and order maintained throughout. Commendable.",
                "A five-card hand. Like a dinner of many courses, correctly served.",
                "Five cards without mishap, sir. The restraint was impeccable.",
                "Five, sir, and still standing. I have laid out the good coasters.",
                "An accumulation of five, sir. Handled without a single spill.",
                "Five cards, sir. Assembled with a footman's patience."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, constructed by hand, sir. Craftsmanship.",
                "The full total, sir, achieved by instalments. Very proper.",
                "Twenty-one, sir. Assembled like a correct place setting.",
                "You have built twenty-one, sir. The house is inconvenienced.",
                "Twenty-one the diligent way, sir. Diligence is rather my religion.",
                "An exact twenty-one, sir. Exactness is the entire job."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by a single point, sir. I felt the draught from here.",
                "One point, sir. The margin was unkind. The outcome, unmoved.",
                "Beaten narrowly, sir. Most vexing. I have alerted no one.",
                "A one-point defeat, sir. I shall polish something vigorously.",
                "By one, sir. Even my composure creaked.",
                "The narrowest of losses, sir. Poor manners from the house."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A considerable win, sir. The silver salver is on its way.",
                "A handsome sum, sir. I shall count it with white gloves.",
                "Substantial, sir. The cellar door stands open in tribute.",
                "A large win, sir. I have adjusted my estimate of the evening.",
                "Winnings of consequence, sir. It will be spoken of downstairs.",
                "A very great win, sir. I nearly smiled. Nearly."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double succeeds, sir. Boldness, correctly rewarded.",
                "Doubled and won, sir. Entered in the plus column with relish.",
                "The doubled hand arrives, sir. Punctual and profitable.",
                "A double, delivered, sir. Like the post. Only welcome.",
                "Doubled and successful, sir. The good port may now be considered.",
                "The doubled wager lands, sir. Tidy. Most tidy."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "The hour grows late, sir. The candles are on their second shift.",
                "A long sitting, sir. I have refreshed the tray thrice.",
                "We have been at this some while, sir. The clocks concur.",
                "An extended session, sir. Even the woodwork is yawning.",
                "The evening has matured, sir. As has the brandy, conveniently.",
                "Late, sir. The household would be abed. The household is flexible."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are ahead, sir. Comfortably. I have double-checked.",
                "The stack has grown under my supervision, sir. I take partial credit.",
                "Up on the evening, sir. The ledger smiles, insofar as ledgers do.",
                "A profitable sitting, sir. Reported without embellishment.",
                "You are up, sir. I have permitted the staff to know.",
                "Ahead of the house, sir. It happens rarely. Enjoyment is permitted."));

        return new Persona("pemberton", "Pemberton", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(duchess(), maxie(), pemberton());
    }
}
