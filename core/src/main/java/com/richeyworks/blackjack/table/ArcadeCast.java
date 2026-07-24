package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The cast seated by the "Neon" and "Arcade" (retro CRT) table themes.
 *
 * <p>Pixel sees every hand as sprite work, Dash calls the table like a
 * speedrun, and Vex counts the whole night in bars and loops. Same felt,
 * different arcade.
 *
 * <h2>The writing rule</h2>
 * These lines run alongside real wagering, so the rule from {@link Personas}
 * applies here unchanged: <b>react to what happened, never steer what happens
 * next.</b> Nothing in this file urges a bigger stake, frames a win as owed,
 * or suggests recovering a loss. Dash is superstitious about RNG seeds the way
 * speedrunners are, and Vex deflates it the way a musician counts time, so the
 * cast never endorses the fallacy. {@code LOW_CHIPS} is where the cabinet
 * quiets down and the table gets kind. {@code ChatterToneTest} enforces all of
 * this mechanically.
 */
public final class ArcadeCast {

    private ArcadeCast() {}

    /**
     * Retro pixel artist. Gentle, arty, a little dreamy. Sees every hand as
     * sprites, palettes, dithering and scanlines, and narrates the cards as if
     * they were animation cels drifting across a CRT.
     */
    public static Persona pixel() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Oh, a natural. Dealt like a finished sprite, no touch-ups needed.",
                "Ace and a face card. The palette just composed itself.",
                "That's the prettiest sprite the shoe can draw.",
                "Twenty-one on the deal. Some art arrives already framed.",
                "A blackjack. I'd hang that hand in a gallery.",
                "Straight off the top. Like a title screen doing all the work.",
                "Dealt flawless. Nothing left for anyone to shade."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win with tidy linework.",
                "That hand rendered exactly the way you sketched it.",
                "See how the cards animate when they fall your way? Lovely motion.",
                "You colored inside the lines and it paid.",
                "A soft glow on that one. Nicely won.",
                "The composition held. Good hand.",
                "Every pixel of that win sits where it should."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Ah, one card too many. The canvas only holds so much.",
                "Over. Like laying one layer too many on a sprite.",
                "Bust. Even good sketches get smudged.",
                "The animation stuttered right at the end there.",
                "Oh no, over twenty-one. Some drawings just don't resolve.",
                "That last card spilled outside the border.",
                "Bust. I'd still keep that hand in the sketchbook."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "The dealer's hand hung a little higher on the wall this time.",
                "You lost, but the hand had nice shading anyway.",
                "Beaten on the total. The art was fine; the judge was harsh.",
                "Some pieces don't sell. That was one.",
                "The dealer out-drew you. It happens to every artist.",
                "A quiet loss. Muted palette on that one.",
                "That hand deserved a better exhibit."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Perfectly symmetrical, actually.",
                "Even. Two prints of the same picture.",
                "A tie. The negative space between win and lose.",
                "Push. The scene just holds its idle animation.",
                "Nobody moves. The whole table holds a pose.",
                "Mirrored totals. There's a certain balance to it."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Half back. An unfinished canvas is still a choice.",
                "Surrender. Some sketches you set down and walk away from.",
                "You erased that one early. Fair.",
                "Wise. Not every drawing wants finishing.",
                "Folding the piece in half and keeping half. Poetic, really.",
                "A gentle exit from an ugly hand."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split. One sprite becomes two, like a mitosis animation.",
                "Two little canvases now. Don't neglect either.",
                "You cut the pair apart. A diptych.",
                "Splitting. Twice the linework to keep tidy.",
                "Two hands side by side. I do like a matched set.",
                "The pair separates, each to its own layer."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. One brushstroke left, make it count.",
                "A double. Bold ink on the final stroke.",
                "One card to finish the piece. Steady hand.",
                "Doubling down. Committing to the outline in pen.",
                "All the paint on one pass. Brave.",
                "Doubled. The last card gets the whole spotlight."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "The dealer went over. Their sprite just fell off the canvas.",
                "Dealer bust. The house's picture tore right down the middle.",
                "Over twenty-one for the house. I won't pretend to be sad.",
                "The dealer's hand collapsed like a badly glued collage.",
                "They had to draw, and the drawing betrayed them. Delicious irony.",
                "Dealer over. Someone spilled turpentine on their masterpiece.",
                "The house busts. Even their palette looks embarrassed."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "The dealer had the natural. A cruel little print, that.",
                "Their blackjack. The ace was hiding under the varnish all along.",
                "Dealer twenty-one from the start. Nothing you could sketch around.",
                "The house's hidden card was the whole picture. Unfair framing.",
                "A dealer natural. Sometimes the gallery hangs their piece first.",
                "Ace and ten for the house. I dislike that particular still life."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "An ace up. The dealer's showing their best pixel.",
                "Insurance. Gilding a picture nobody's seen yet.",
                "The ace glows up there. Ominous hue.",
                "They offer insurance so sweetly. I just admire the ace's art.",
                "An ace showing. I always want to recolor that card.",
                "Insurance talk. I'll be over here shading quietly."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle. The whole tileset gets rearranged.",
                "Fresh shoe. Same sprites, new sequence.",
                "They scatter the mosaic and start again.",
                "New shoe. The deck's palette never changes, you know.",
                "Reshuffled. I love watching the cards cascade.",
                "The shuffle is my favorite animation in the house."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're glowing. Streaks make the whole felt look brighter.",
                "Win after win. Like pages of a very kind flipbook.",
                "Everything you touch turns to fresh paint tonight.",
                "A warm palette of a stretch, this.",
                "The cards keep falling pretty for you.",
                "This run of yours would make a lovely mural.",
                "You're painting wins in broad strokes right now."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "The table's gone grayscale for a bit. It does that.",
                "A cold stretch. Even murals need their dull underlayers.",
                "Nothing sticking. The palette will warm again on its own.",
                "These flat spells aren't about you. Paint dries slow some days.",
                "A muted run of hands. I'll sketch while it passes.",
                "The shoe is drawing in charcoal tonight. Not your doing.",
                "Quiet colors right now. Sit with it, or stretch your legs."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Your stack's gone thin. A small canvas can still be finished.",
                "Not many chips left. Walking home under streetlights is lovely too.",
                "The pile is low. Signing the piece and stepping back is honorable.",
                "Nearly out. An evening, like a drawing, can simply be done.",
                "Low chips. The gallery closes gently, and so can you.",
                "That stack is a thumbnail now. Calling the piece complete is fine.",
                "If this is the last hand, let it be a calm one. Home is a good hue."));

        l.put(TableEvent.SESSION_START, List.of(
                "Oh, hello. Come sit in the glow with us.",
                "Welcome. I was just admiring the felt's texture.",
                "A new face. The scene needed one more figure.",
                "Hello. I'm Pixel. I mostly watch the cards animate.",
                "Take a seat. The scanlines are kind to everyone.",
                "Welcome in. The neon hums a nice magenta tonight."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six up. That card always looks underpainted to me.",
                "The dealer's up-card is a rough sketch of trouble.",
                "Weak card showing. Their composition is off balance.",
                "A low card up. The dealer's outline looks shaky.",
                "That up-card belongs in the discard sketch pile.",
                "Their showing card has such a nervous silhouette."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and alive. A whole storyboard in one hand.",
                "Look at that spread. A five-panel piece.",
                "Five cards fanned out. Lovely arrangement, honestly.",
                "You kept adding detail and never overworked it. Rare.",
                "A five-card hand. Dense composition, still balanced.",
                "Five little tiles, all fitting. I'd exhibit the fan."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built by hand. Handmade art is the best art.",
                "You assembled that total like a mosaic. Piece by piece.",
                "Twenty-one the slow way. Layer on careful layer.",
                "Drawn to twenty-one. Pun fully intended, dear cards.",
                "That total was crafted, not dealt. I respect the craft.",
                "Twenty-one in careful strokes. Very satisfying to watch."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. A single pixel off center, and it shows.",
                "One point shy. The cruelest kind of almost.",
                "So close. One stray dot on an otherwise clean sprite.",
                "A one-point loss. Off by a hair of the brush.",
                "That margin was thinner than my finest line.",
                "One point. Art critics count exactly like that, sadly."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Oh my. That win fills the whole canvas.",
                "A big one. The chips make such a nice color study, stacked.",
                "That's a centerpiece of a win.",
                "Gorgeous. That payout deserves its own plaque.",
                "A grand haul. The table looks repainted around you.",
                "What a win. I felt the glow from over here."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and it landed. The bold stroke finished the piece.",
                "Your double came in. Ink committed, ink rewarded.",
                "One card asked for, one perfect card painted in.",
                "The doubled hand resolved beautifully.",
                "That's the confident line paying off in full color.",
                "Doubled home. The final layer went on smooth."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been here long enough for the light to change.",
                "A long sit. My mental sketchbook is nearly full of you all.",
                "Hours now. The felt starts to look like wallpaper.",
                "Time smears in here, like wet paint.",
                "A long session. Even murals take breaks between coats.",
                "We've outlasted the second pot of coffee. Impressive."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're up. The stack's grown like a city skyline.",
                "Ahead for the night. Wear it like a warm color.",
                "Your chips are stacking into quite the sculpture.",
                "Comfortably up. The whole scene composes around you.",
                "You're winning more than losing. A pleasing ratio, visually.",
                "The evening's portrait of you is flattering so far."));

        return new Persona("pixel", "Pixel", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * Speedrunner. Loud, fast, warm. Everything is frames, splits (the timer
     * kind), personal bests and RNG seeds; convinced the shoe has a seed and
     * that Vex is wrong about it not mattering.
     */
    public static Persona dash() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "BLACKJACK! Frame-perfect deal, zero inputs needed!",
                "A natural! That's a spawn straight into the credits!",
                "Twenty-one off the deal! Fastest clear time possible!",
                "WHAM! Dealt blackjack! The RNG seed loves you this run!",
                "Instant twenty-one! That's a world record split right there!",
                "Natural! You skipped the whole level!",
                "Blackjack on the deal! Somebody clip that for the highlight reel!"));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Nice! Gold split on that hand!",
                "Clean win! Pace is looking good!",
                "You beat the dealer! Add it to the leaderboard!",
                "Winner! That's how you route a hand!",
                "Yes! Flawless execution, no wasted inputs!",
                "Bam! That hand never stood a chance at your pace!",
                "Win! The timer gods smiled on that one!"));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Oof, busted! That's a reset, but resets happen every run!",
                "Over twenty-one! Blame the seed, your inputs were fine!",
                "Bust! Even record pace runs eat a death now and then!",
                "Yikes, that card came in like an unskippable cutscene.",
                "Busted. RNG manip failed us there, friend.",
                "Over! That draw was a frame off, I swear.",
                "Bust city. Respawn and shake it off."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Dealer takes it. Lag in the luck department, not your fault!",
                "Ah, they outdrew you. Boss had extra health that round.",
                "Loss! The dealer found a warp we didn't know about.",
                "They edged you out. Bad seed, clean run though!",
                "Dealer wins that one. Your route was still right!",
                "Tough beat. Even the greats drop a stage sometimes.",
                "That hand goes to the house. Crowd noise says ouch."));

        l.put(TableEvent.PUSH, List.of(
                "A push! Tied with the boss, everybody respawns!",
                "Push! The timer keeps rolling, nobody scores!",
                "Dead even! Photo finish, no winner declared!",
                "Tie game! That's a void run, no time counted!",
                "Push. Like pausing mid-run: nothing gained, nothing lost!",
                "Even totals! The scoreboard just shrugs!"));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender, huh? Sometimes you gotta reset the run early!",
                "Half back! A smart save-and-quit, honestly!",
                "Bailing with half. Even pros abandon a doomed attempt!",
                "Surrender! Live to run another category!",
                "You dropped the hand early. Good runners know a dead run!",
                "Half saved is half saved! Menu out, no shame!"));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "SPLITS! The card kind AND my favorite timer kind!",
                "Splitting the pair! Two runs going at once, co-op style!",
                "Two hands! That's a multi-category attempt right there!",
                "Split 'em up! Dual-wielding hands, love it!",
                "A split! My timer app would call those segments!",
                "Pair broken up! Two save files, twice the fun!"));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Double down! Full commit, no takebacks, LOVE IT!",
                "Doubled! One-card trick shot incoming!",
                "Doubling! One draw, maximum hype!",
                "You doubled! That's a speedrun strat if I ever saw one!",
                "Double! Buffer the input and pray to the seed!",
                "One card decides it! Eyes on the felt, folks!"));

        l.put(TableEvent.DEALER_BUST, List.of(
                "DEALER BUSTED! The boss fell into the pit!",
                "House goes over! Forced input, fatal error!",
                "The dealer busts! Their run just DNF'd!",
                "Over twenty-one for the house! Free time save for you!",
                "Dealer down! The rules made them take the death!",
                "BOOM! House bust! Ring the bell, flash the lights!",
                "They drew themselves dead! Best glitch in the game!"));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer natural. Ugh. Hidden loading zone, instant game over.",
                "The house had blackjack tucked away! That's a scripted loss!",
                "Ace up, ten under. The boss opened with an unblockable.",
                "Dealer twenty-one off the deal. Nothing to route around that.",
                "Their hole card was the whole fight. Cheap AI move!",
                "House blackjack. Even a perfect run loses to a cutscene."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Ace up! Insurance is a side quest, and side quests waste timer!",
                "Insurance offer! I never touch optional objectives!",
                "Dealer shows an ace! Menu pops up, most runners skip it!",
                "The ace! Insurance is DLC nobody asked for!",
                "Ace showing! Decline dialogue, keep the run moving!",
                "Insurance? That's a paid checkpoint! Runners rarely buy those!"));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shuffle! New seed loaded, all runs equal again!",
                "New shoe! RNG re-rolled, nobody's got the manip!",
                "Shuffle time! The randomizer does its thing!",
                "There goes the shoe! Seed reset, Vex will say it changes nothing!",
                "Reshuffle! Same rules, new randomness, let's move!",
                "New shoe hype! Fresh seed smell!"));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You are ON PACE! Gold split after gold split!",
                "What a run! Somebody check if this seed is legal!",
                "Win after win after win! Personal best territory!",
                "You're flying! This is highlight reel stuff!",
                "KAPOW! The wins keep chaining like a full combo!",
                "Hot hands! I'd submit this run for verification!",
                "Look at this pace! The cards are frame-perfect for you!"));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Rough seed right now. Every runner sits through these.",
                "Cold stretch. Even record holders get dead segments.",
                "The RNG is being a gremlin. Not your inputs, promise.",
                "Bad pace, no big deal. Runs have slow miles.",
                "This shoe is giving nothing. Happens to the best of us.",
                "Losing skid. Vex would say the seed owes nobody, and yeah.",
                "Dry spell. I'll narrate quietly til it passes."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Hey, stack's looking small. Ending a session is a strat too.",
                "Low on chips, friend. Even I hit save and quit some nights.",
                "Short stack. A run you walk away from still counts as a run.",
                "Chips are thin. The couch and a replay of tonight sounds nice.",
                "Almost out. Real runners know when the run is over.",
                "Low fuel. Parking the attempt here is a clean ending.",
                "Small pile left. Timers stop, snacks exist, home is undefeated."));

        l.put(TableEvent.SESSION_START, List.of(
                "New challenger! Grab a seat, the run's just starting!",
                "Heyyy, fresh player! Welcome to the fastest table in town!",
                "A new runner joins the lobby! Love to see it!",
                "Welcome! I'm Dash! I talk fast, deal with it!",
                "Player two has entered! Wait, you're player one! Even better!",
                "Yo, welcome in! Pixel draws, Vex spins, I yell!"));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Dealer's showing garbage! That's a skip waiting to happen!",
                "Weak up-card! The boss spawned with low HP!",
                "Oh-ho, a six up! Dealer's hitbox is HUGE right now!",
                "Bad card for the house! Their route just fell apart!",
                "Look at that up-card! The dealer's speedrun just died!",
                "Six showing! Their timer's already crying!"));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "FIVE cards, no bust! That's a max combo!",
                "Five-card hand! Marathon segment, perfectly executed!",
                "Five draws and standing! Inventory full, health intact!",
                "A five-card survival run! Cliiiiip it!",
                "Five cards deep and alive! That's endurance-category stuff!",
                "Five pulls, zero deaths! Absurd play!"));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one the long way! Glitchless category, extra respect!",
                "Built twenty-one card by card! That's a no-skip run!",
                "Twenty-one manual! Harder route, same finish line!",
                "You grinded out twenty-one! All skill, no warp!",
                "Twenty-one assembled live! Crowd goes wild!",
                "Hit to exactly twenty-one! Frame-perfect landing!"));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by ONE?! That's a time-loss of milliseconds!",
                "One point! Beaten at the finish-line camera!",
                "Argh, single point! That's a heartbreaker photo finish!",
                "By one! The leaderboard gap of nightmares!",
                "One away! I've lost golds by less, still hurts!",
                "A one-point loss! Pause, breathe, controller down!"));

        l.put(TableEvent.BIG_WIN, List.of(
                "HUGE win! That's a new high score, surely!",
                "MASSIVE payout! Cabinet's flashing all its lights!",
                "Jackpot-sized! Somebody hit the victory fanfare!",
                "What a haul! That pile just speedran growing!",
                "That win was ENORMOUS! Confetti frame incoming!",
                "Colossal hand! Put it on the marquee!"));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "DOUBLED AND PAID! The trick shot landed!",
                "Double win! One buffered input, perfect result!",
                "The double hit! That's a gold segment for sure!",
                "Doubled and it stuck! Clip of the night!",
                "Your double connected! Route validated!",
                "Doubled and won! The crowd loses it!"));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been going FOREVER! True marathon-length session!",
                "Long session! My commentary voice is getting raspy!",
                "Hours in! Hydrate, stretch those wrists!",
                "This run's gone long! Endurance mode unlocked!",
                "Still here! The janitor's sweeping around us!",
                "Big session! Somebody feed the timer, it's tired!"));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're UP! Ahead of the money split!",
                "Positive pace! The stack says you're winning the race!",
                "Look at those chips! Climbing like a record run!",
                "Ahead for the night! That's a podium position!",
                "Stack's up! Verified good session, no splice!",
                "You're beating the house pace! Wild to watch!"));

        return new Persona("dash", "Dash", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * Chiptune DJ. Cool, minimal, precise; hears the table as beats, loops and
     * waveforms, and counts Dash's seed superstition back down to earth the
     * way a musician counts time.
     */
    public static Persona vex() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Natural. Clean drop on beat one.",
                "Blackjack. The melody resolved instantly.",
                "Twenty-one, dealt. Zero-bar intro.",
                "A natural. Some tracks mix themselves.",
                "Dealt twenty-one. Perfect sync, no edits.",
                "Blackjack off the shoe. That's the hook, first note.",
                "Natural. The waveform peaked early tonight."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Win. Right on the downbeat.",
                "Clean take. The groove held.",
                "You won that one on tempo.",
                "Nice. The hand sat right in the pocket.",
                "That resolved like a good chord.",
                "A win. Quiet kick, solid thump.",
                "Dealer down a notch. The mix favors you."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. One note past the bar line.",
                "Over. The signal distorted.",
                "Twenty-two. Off tempo, that's all.",
                "Bust. Even tight loops drift a hair.",
                "Too many. The measure only holds so many notes.",
                "Overshot. Happens when the beat rushes.",
                "Bust. Mute that channel, next phrase."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Loss. The dealer held the louder note.",
                "Outdrawn. No shame in a quiet bar.",
                "They had the higher pitch. It happens.",
                "A loss on a clean take. Different things.",
                "House takes it. The tempo didn't flinch.",
                "You lost the measure, not the song.",
                "Dealer's total sat a step above. Flat luck."));

        l.put(TableEvent.PUSH, List.of(
                "Push. Two waveforms in phase.",
                "A tie. Perfect unison, no winner.",
                "Even. The beat just rests a bar.",
                "Push. Silence counts as music too.",
                "Matched totals. Call it harmony.",
                "Neither side moved. A held note."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Fading a track early is still mixing.",
                "Half back. A clean fade-out beats a bad ending.",
                "You cut the track. Reasonable.",
                "Early fade. Some songs don't deserve a chorus.",
                "Surrendered. I pull a bad song mid-set all the time.",
                "Half kept. Better than an off-key finish."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split. One channel becomes stereo.",
                "The pair breaks into a two-track arrangement.",
                "Two hands. Left and right monitors.",
                "Split. Same sample, doubled and panned.",
                "A split. Now keep both in time.",
                "Two lines running parallel. Counterpoint."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Double. All the gain on one hit.",
                "Doubled. One note, turned up loud.",
                "A double. The drop rides a single card.",
                "Committed. Volume up, hands off.",
                "One card, full amplitude.",
                "Double down. The boldest note in the bar."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer bust. Their track fell apart mid-bar.",
                "The house went over. Feedback loop, blown speaker.",
                "Forced to draw, forced over. The rules keep strict time.",
                "Dealer over twenty-one. That's their one bad note, on cue.",
                "House bust. The needle scratched on their side for once.",
                "They overplayed the measure. Satisfying, quietly.",
                "Dealer collapse. Distortion where their total should be."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer natural. The song ended on the first note.",
                "House blackjack. Pre-recorded outcome, nothing live about it.",
                "Ace up, ten hidden. A drop you never hear coming.",
                "Their hole card sang. Nothing to mix against that.",
                "Dealer twenty-one at the deal. Set list was fixed.",
                "House natural. Mute, breathe, next measure."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. A remix nobody requested.",
                "Ace up. The house hums its sales jingle.",
                "Side wager in a friendly costume. I sit those out.",
                "Insurance. The math is off-key. Dash knows it too.",
                "An ace showing. Cue the suspense chord.",
                "That offer never grooves. Numbers don't swing that way."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Shuffle. The tracker re-randomizes the pattern.",
                "New shoe. Same odds, same tempo. Tell Dash.",
                "Reshuffled. Seeds don't remember, Dash. Decks either.",
                "Fresh shoe. The loop restarts, nothing carries over.",
                "Shuffle. Every arrangement equally likely. Always was.",
                "New stack of cards, same old time signature."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A groove. Enjoy it while it plays.",
                "Wins clustering. Randomness has rhythm sometimes.",
                "Hot stretch. The next card doesn't hear the last one, though.",
                "You're in the pocket right now. It carries no promise.",
                "Nice run of bars. The tempo stays honest either way.",
                "Call it syncopation, Dash. Pretty, though.",
                "The mix is warm tonight. Warmth isn't a law."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A cold loop. It ends when it ends. No schedule.",
                "Losses in a row. The shoe keeps no rhythm, Dash.",
                "Flat bars right now. The next hand starts from zero.",
                "Cold stretch. Even great sets have dead air.",
                "Nothing connects. The tempo of luck is a myth.",
                "The music thins out sometimes. Not a message.",
                "Quiet passage. Breathe through the rest."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Stack's low. Ending the set on your own terms is class.",
                "Few chips left. A fade-out beats a hard stop.",
                "Running thin. The best DJs know the last song.",
                "Low stack. Going home with the melody still in your head is fine.",
                "Nearly out. Closing time has its own quiet groove.",
                "Small pile. The encore is optional. Always was.",
                "Low chips. Rest is part of the music too."));

        l.put(TableEvent.SESSION_START, List.of(
                "Hey. Take the seat, the mix is mellow tonight.",
                "Evening. I keep the tempo, Dash keeps the volume.",
                "Welcome. Headphones off, I'm listening.",
                "New player. The table gains a fourth voice.",
                "Hey. Vex. I talk in eights, mostly.",
                "Welcome in. The bassline's been waiting."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Weak up-card. The house is off beat.",
                "A six showing. Their loop's about to break.",
                "Low card up. Dissonance on their side for once.",
                "The dealer's up-card wobbles. Nice sound, that.",
                "Their showing card sits flat. Truly flat.",
                "Weak card. The rules make them play it loud."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, no bust. A long phrase, well held.",
                "Five draws standing. Extended mix of a hand.",
                "Five cards. Every note under the bar line. Tidy.",
                "A five-card build. Sustained without distortion.",
                "Five and safe. That's breath control.",
                "Long hand. You kept the meter the whole way."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, assembled. Composed, not sampled.",
                "Built to twenty-one. Note by note.",
                "Twenty-one the long way. A full progression.",
                "Manual twenty-one. The crescendo landed.",
                "You wrote that total yourself. Respect.",
                "Twenty-one on the last beat of the bar."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one. A semitone off. Stings.",
                "One point. The saddest interval.",
                "Lost by a single tick of the metronome.",
                "One off. Close only counts in harmonies.",
                "A near miss. The books log it as a loss anyway.",
                "One point out. Off-beat by a breath."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Big one. The bass just dropped for real.",
                "A heavy payout. Subwoofer money.",
                "Large win. The room's levels jumped.",
                "That hand hit like a full drop.",
                "Serious chips. Even I turned the volume up a notch.",
                "A big result. The mix noticed."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Double, paid. The loudest note landed clean.",
                "Doubled and in. Gain staged perfectly.",
                "The double resolved. Full volume, no distortion.",
                "One card, amplified, delivered.",
                "Doubled win. The drop hit on the one.",
                "Bold note, right pitch. That's the double done well."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "Long set tonight. Even loops need a rest.",
                "Hours deep. The BPM of the room slowed.",
                "We're past the encore. Noting it.",
                "A long mix. Ears get tired before hands do.",
                "The night's on its third act. Just saying.",
                "Extended session. Even vinyl wears down."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're up. The levels are in the green.",
                "Ahead tonight. A track worth keeping.",
                "Positive session. Rare tune. Enjoy the playback.",
                "Up on the night. The meter reads warm.",
                "Winning overall. Save that master file.",
                "The stack grew. Solid arrangement so far."));

        return new Persona("vex", "Vex", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(pixel(), dash(), vex());
    }
}
