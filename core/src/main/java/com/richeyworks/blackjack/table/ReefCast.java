package com.richeyworks.blackjack.table;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The cast shared by the "The Abyss" and "Lagoon" table themes: a submersible
 * pilot, a surfer, and a marine biologist, one ocean between them.
 *
 * <p>The writing rule here is the same one every cast obeys, and it is worth
 * restating because these lines run alongside real wagering: <b>react to what
 * happened, never steer what happens next.</b> Nobody at this table urges a
 * bigger stake, calls a win owed, or frames another hand as the way out of a
 * losing one. A cold streak gets sympathy or an ecology lecture; a hot streak
 * gets pleasure in the moment and nothing more. {@code LOW_CHIPS} is where the
 * table gets gentler — Marina treats surfacing as good piloting and Moss
 * treats paddling in as the best part of the day, because stopping is always
 * an honorable end to a session. {@code ChatterToneTest} enforces all of this
 * mechanically.
 */
public final class ReefCast {

    private ReefCast() {}

    /**
     * Submersible pilot. Calm under pressure, literally. Thinks in depth,
     * hull, ballast, sonar and trim, and has a dry line in jokes about the
     * dark. The disciplined one at this table.
     */
    public static Persona marina() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Twenty-one on the deal. Cleanest descent I've seen all week.",
                "A natural. Even my depth gauge looks impressed.",
                "Straight to the bottom of it, no ballast needed. Nice.",
                "That's the card equivalent of a perfect trim dive.",
                "Blackjack. Somewhere below us a trench just applauded.",
                "Dealt perfect before the descent even started.",
                "A natural. I log those the way I log calm seas: gratefully.",
                "Twenty-one off the top. Smooth as a hatch seal."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "Won it. Steady hands on the controls, that.",
                "That's a clean ascent. Take the win aboard.",
                "Good hand. You held your trim the whole way.",
                "Nicely piloted. No leaks in that one.",
                "A win. Log it and check your instruments.",
                "You read that one like sonar. Clear ping, clear answer.",
                "Held your nerve at depth. It shows.",
                "That's how you run a descent: quiet and correct."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Over. That's what we call exceeding crush depth.",
                "Bust. The hull can only take so much.",
                "One card past the safe margin. It happens down here too.",
                "Went too deep on that one. The gauge doesn't lie.",
                "Bust. Even good pilots clip the seafloor sometimes.",
                "Too far down. Blow the ballast and come back up.",
                "Over the line. The dark doesn't warn you first.",
                "That last card had the density of lead ballast."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Lost on the numbers. Your piloting was fine.",
                "The dealer had deeper reserves. Nothing you did wrong.",
                "A loss. Some currents you just ride out.",
                "That one sank quietly. No fault in the crew.",
                "Outdrawn, not outflown. There's a difference.",
                "The sonar showed nothing wrong with that hand.",
                "Lost it. At depth we call that a bad reading, not a bad pilot.",
                "The house surfaced first this time. Log it and move along."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Neutral buoyancy. We hang here a while.",
                "Even. Neither of us moved a meter.",
                "A tie. The gauge reads exactly where it did.",
                "Push. Holding depth, holding steady.",
                "Nobody rose, nobody sank. I can live with that.",
                "Level trim. We call that station-keeping."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. A controlled abort is still good piloting.",
                "Half back and a safe ascent. I'd sign that log.",
                "You scrubbed the dive. Sometimes the checklist says so.",
                "Aborted clean. That's training, not cowardice.",
                "Half saved. Better than flooding the whole compartment.",
                "Smart. You don't argue with a bad hull reading."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Split. Two hulls now. Watch both gauges.",
                "Two hands. Like running twin submersibles solo.",
                "Splitting the pair. Double the instruments to watch.",
                "Two dives at once. Keep your checklists separate.",
                "Split them. Now the sonar has two contacts.",
                "A split. Twice the hatches to keep sealed."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. That's dropping all your ballast at once.",
                "One card, full commitment. Like a free descent.",
                "Doubled down. No second reading on this one.",
                "Committed the whole dive plan to one card. Bold.",
                "Doubling. The trim is set; enjoy the ride down.",
                "One card decides it. Steady breathing works down here too."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer's over. Imploded on their own draw.",
                "The house went past crush depth. I never tire of it.",
                "Dealer bust. Their hull wasn't rated for that card.",
                "Over they go. Straight down, no ballast to blow.",
                "The dealer sank themselves. Rules made them draw it.",
                "Bust. That's the house flooding a compartment for once.",
                "Down she goes. I'd radio it in, but I'm enjoying it.",
                "The dealer found the seafloor the hard way."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer natural. Something big on sonar, then it was on us.",
                "Ace up, ten under. That's a hazard no chart marks.",
                "Their blackjack. Over before I finished my descent checks.",
                "The house had it hidden below the waterline. Typical.",
                "Nothing to pilot around. Some hazards just find you.",
                "A natural for the dealer. The dark keeps its secrets."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. I trust my hull, not their side offer.",
                "An ace up. Stay calm and mind your instruments.",
                "Insurance on offer. My checklist has no line for it.",
                "The ace surfaces and everyone grips the rails.",
                "Insurance. Down below we just call that fear with paperwork.",
                "Ace showing. Breathe slow, air lasts longer that way."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe. New chart, same trench.",
                "Reshuffled. Recalibrating the sonar, are we.",
                "New shoe. Every dive starts at the surface again.",
                "A shuffle. The instruments zero themselves out.",
                "Fresh cards. Same depth, same dark, same odds.",
                "New shoe. I'll treat it like an unmapped canyon."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're running clean. Every gauge in the green.",
                "A streak. Like finding a current going your way.",
                "That's a smooth run. Enjoy the calm while it holds.",
                "Winning in sequence. The dark approves, apparently.",
                "You're gliding tonight. Barely a bubble out of place.",
                "A good run. I've had dives like this. I still talk about them.",
                "Streaking along. Trim perfect, ballast untouched.",
                "All systems nominal and then some. Nice stretch."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Cold stretch. Down here we hold trim and wait.",
                "Nothing landing. Some dives are all silt and no wonder.",
                "A rough patch. The gauge says weather, not fault.",
                "Losing runs are like the dark. They end at the surface.",
                "Cold cards. I've sat out worse on the seafloor.",
                "The shoe's gone quiet as a dead sonar. It comes back online.",
                "Bad stretch. Check your air, not your pride.",
                "Murky run. Visibility improves. It always does."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Stack's shallow. Surfacing now is a clean end to a dive.",
                "Low on reserves. Every good pilot respects that reading.",
                "That's near the bottom of the tank. Coming up is the skill.",
                "Not much left. We surface on the gauge, not on hope.",
                "Low chips. Nobody logs shame for an early ascent.",
                "The stack's thin. A dry deck and a warm drink is a fine plan.",
                "Reserve level. I'd call the dive here, and kindly.",
                "When my air hits the red line, I go up. No exceptions, no regrets."));

        l.put(TableEvent.SESSION_START, List.of(
                "Welcome aboard. Hatch is sealed, cards are warm.",
                "Evening. Take a seat. The dark out there is friendly enough.",
                "New crew. Good. I'll run the pre-dive checks.",
                "Welcome. I pilot submersibles. This felt is the shallowest I go.",
                "Sit down. The table's rated to full depth.",
                "Evening. Moss will offer you sunshine. I offer instruments."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Six showing. Their hull's already groaning.",
                "Weak up-card. That's a stress fracture in the house.",
                "A soft card up. Sonar says they're in trouble.",
                "The dealer's carrying bad ballast this hand.",
                "That up-card reads like a leak warning.",
                "Weak card showing. Watch the gauge on this one."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards and intact. A long dive, well managed.",
                "Five draws without a crack in the hull. Respect.",
                "That hand's been down a while and it's still holding air.",
                "Five cards. Like threading a canyon in the dark.",
                "A five-card hand. Slow descents take the most nerve.",
                "Five and steady. Your trim never wavered."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built by hand. A full descent, meter by meter.",
                "You reached the exact floor without touching it. Twenty-one.",
                "Twenty-one on the gauge, no overshoot. Pilot's hands.",
                "Assembled twenty-one. Careful metering all the way down.",
                "The long way to twenty-one. Descents reward patience.",
                "Twenty-one, precise as a depth reading."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. A meter short of the surface.",
                "One point. That's a hatch that wouldn't quite seal.",
                "Beaten by a single point. The dark laughs quietly.",
                "One off. Down here we call that a near-miss report.",
                "So close the sonar couldn't split the two.",
                "A one-point loss. I've grazed the seafloor gentler than that."));

        l.put(TableEvent.BIG_WIN, List.of(
                "That's a heavy haul. Mind the winch on the way up.",
                "A big one. Like surfacing with the whole wreck's cargo.",
                "That win could bend a ballast tank.",
                "Substantial. The kind of payload you double-check the tether for.",
                "Big win. I'd stow that below decks before the weather turns.",
                "A serious pile. Enough to trim the sub nose-down."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and it landed. A free descent, done properly.",
                "The doubled hand came in. Bold dive, clean surfacing.",
                "Doubled home. One card, one perfect reading.",
                "That double paid. Nerve and instruments in agreement.",
                "Doubled and won. The gauge never wobbled.",
                "One-card commitment, full return. Textbook piloting."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "We've been down a long while. Time drifts at depth.",
                "Long session. My legs think we're still submerged.",
                "Hours in. No windows down here either, notice.",
                "A long dive, this. Watch your air and your judgment.",
                "We've logged a full shift at this table.",
                "Long stretch. Even the dark takes breaks. Allegedly."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're up on the ledger. Positive buoyancy, I'd call it.",
                "Ahead of where you launched. That's a good dive log.",
                "Running well. Every reading above the line.",
                "You're up. Cargo heavier than when we left the dock.",
                "In profit and trimmed level. Enjoy the view.",
                "Ahead tonight. The instruments confirm it."));

        return new Persona("marina", "Marina", Personas.SEAT_LEFT, 0.40, l);
    }

    /**
     * Surfer. Warm, unhurried, delighted by nearly everything — tides, swells,
     * boards, strangers, sunshine. Laid-back but never dumb, and given to
     * inventing his own mellow exclamations.
     */
    public static Persona moss() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "Honey tides! Twenty-one right off the deal.",
                "A natural! That's a set wave rolling in just for you.",
                "Warm foam and sunshine! Blackjack, friend.",
                "Oh, that's glassy. Dealt perfect, nothing to do but smile.",
                "Blackjack! Like paddling out and the ocean hands you a barrel.",
                "Sweet saltwater. Twenty-one without a paddle stroke.",
                "The tide just did all the work for you. Beautiful.",
                "Mango sunrise! That's the prettiest deal there is."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "There it is! Rode that one all the way to the sand.",
                "A win! Tastes like salt and sunshine.",
                "Nice one. You caught that wave right at the peak.",
                "Sweet! Clean ride, no wipeout.",
                "Won it like a longboarder on a small day: easy and smiling.",
                "Warm water win. Soak in it a second.",
                "Caught it clean. The sandbar would be proud.",
                "That's the good kind of set. Nicely surfed."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Oof, over the falls on that one.",
                "Bust. Even clean waves close out sometimes.",
                "Wiped out. Shake the water from your ears and float a bit.",
                "Over. The ocean flips everybody now and then.",
                "That card came down like a lip on your head. Happens.",
                "Bust, friend. Sand in the shorts, nothing broken.",
                "A tumble. The board always pops back up, though.",
                "Closed out. Breathe easy, the ocean's still friendly."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "Aw, that one slipped past. Tide does that.",
                "A loss. Some waves just belong to somebody else.",
                "Dealer got it. The ocean keeps its own score.",
                "That hand paddled hard and still missed. Happens to everybody.",
                "Lost it fair. The swell picks its riders.",
                "Aw. Good line, wrong wave.",
                "The house caught that one first. So it goes.",
                "That sting fades quicker than a sunburn, promise."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Flat water, friend. Nothing wrong with flat.",
                "Even! Like floating out past the break, just bobbing.",
                "A tie. The tide came in and went out again.",
                "Push. Nobody surfed, nobody sank.",
                "All even. Perfect time to feel the sun a moment.",
                "A push. Some sets just roll under you."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Paddled around that one. Wise.",
                "Surrender. Even big-wave folk let some sets pass.",
                "Half back. That's ducking under instead of eating foam.",
                "Let it roll by. The smart ones always do.",
                "You read the closeout early. That's ocean sense.",
                "Half saved and dry hair. Good call, friend."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "Splitting! Two boards under one surfer, look at you.",
                "A split. Riding two waves at once, that takes balance.",
                "Two hands now. Like a twin-fin, twice the wiggle.",
                "Split them up. More rides in the same swell.",
                "Two hands going. Keep your toes loose.",
                "A split! The fun kind of busy."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubling! Dropping in steep. Love the commitment.",
                "One card coming. Deep breath, loose knees.",
                "Doubled down. That's a late takeoff, full trust.",
                "Big drop! One card and gravity does the rest.",
                "A double. You picked your wave and leaned in.",
                "Doubled. Sweet saltwater, here it comes."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer wiped out! Sunshine for everybody.",
                "Ha! The house went over the falls this time.",
                "Dealer bust. Even the lifeguard's smiling.",
                "Over they went! The ocean is just, sometimes.",
                "The dealer ate foam on that one. Couldn't happen nicer.",
                "House wipeout! I'd clap but my hands are full of sunshine.",
                "The dealer sank it. Tide turned friendly.",
                "Busted! That wave broke exactly where it should've."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Whoa. Dealer natural. Rogue wave, clean out of nowhere.",
                "Ace up, ten hiding. The ocean has moods too.",
                "Dealer blackjack. Sneaker set, nothing you could ride.",
                "Aw. That one broke before anybody stood up.",
                "The house got the perfect wave that time. It happens.",
                "A dealer natural. Shake it off like cold spray."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance, huh. I never buy wax I don't need.",
                "Ace showing. Stay mellow, watch the horizon.",
                "Insurance. Feels like renting a leash for a flat day.",
                "The ace is up. Deep breath, warm thoughts.",
                "Insurance talk. The tide never sold me anything worth having.",
                "An ace up top. Easy now, no sudden moves."));

        l.put(TableEvent.SHUFFLE, List.of(
                "Fresh shoe! New tide coming in, same beautiful ocean.",
                "A shuffle. Like the tide smoothing the sand flat again.",
                "New cards. Dawn patrol feeling, everything rinsed clean.",
                "Shuffled up. The lineup resets, friend.",
                "New shoe, new swell. Same sunshine.",
                "They stirred the whole ocean up again. Lovely."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "You're in the pocket! Wave after wave after wave.",
                "A streak! The swell found you and it likes you.",
                "Honey tides, look at this run.",
                "Set after set, all yours. Grin wide, friend.",
                "You're trimming down the line perfect right now.",
                "This is a glassy morning of a streak. Just enjoy the ride.",
                "Warm run! The sun's sitting on your shoulder.",
                "Everything's peeling right for you. Beautiful to watch."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "Flat spell, friend. The ocean goes quiet sometimes.",
                "Cold stretch. Even the best beaches have onshore days.",
                "Nothing rolling your way right now. Float easy.",
                "A lull. Good time to just breathe the salt air.",
                "The swell dropped out. No shame in bobbing a while.",
                "Choppy run. Sit tall, watch the pelicans.",
                "Cold cards. Sunshine's free, though.",
                "A slow patch. The sand isn't going anywhere, and neither is the sun."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Stack's getting shallow. Paddling in while it's still golden is a win.",
                "Low tide on the chips. The beach walk home is the best part anyway.",
                "Not much left, friend. The best surfers know when to call it a day.",
                "Running light. A warm towel and a sunset beat a heavy pocket.",
                "Chips are thin. Nobody ever regretted leaving the water smiling.",
                "Low stack. Come in easy, the shore's a kind place.",
                "That's nearly the last of it. Dry sand and a cold drink sound right.",
                "Small stack now. The sun sets pretty whether you're up or down."));

        l.put(TableEvent.SESSION_START, List.of(
                "Hey, welcome! Water's warm, felt's warmer.",
                "New face! Pull up, the sun's out somewhere.",
                "Welcome in, friend. Swell's easy tonight.",
                "Hey hey! Grab a seat, the tide's friendly.",
                "Welcome! Marina drives submarines. I mostly float. You'll fit in.",
                "A new rider in the lineup. Love it."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "Ooh, soft card up. The dealer's paddling against it.",
                "A six showing. That's the house caught inside.",
                "Weak up-card. Sun's angled our way, friend.",
                "The dealer's holding a waterlogged board there.",
                "Soft card for the house. The lineup just shifted.",
                "That up-card's about to get worked by the whitewash."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards! That's a long ride, all the way to the sand.",
                "Five and still up! Balance like a tightrope surfer.",
                "Look at that, five cards and still cruising.",
                "A five-card hand. Longest noseride I've seen all night.",
                "Five draws, no wipeout. Smooth as morning glass.",
                "Five cards! You walked that board tip to tail."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, built slow! Like a long peeling point break.",
                "You carved your way to twenty-one. Sweet lines.",
                "Twenty-one the patient way. That's soul surfing, that is.",
                "Got there card by card. Smooth water all the way.",
                "Twenty-one! Every stroke of that paddle earned.",
                "The slow roll to twenty-one. Warmest kind."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "By one. Aw, that's a wave closing out at the very end.",
                "One point short. Like getting caught by the last inch of foam.",
                "Lost by a whisker of a point. Stings like reef rash, that.",
                "So close. The nose of the board touched the sand, almost.",
                "One point. Even the seagulls groaned.",
                "Aw, one short. Salt in a small cut, that."));

        l.put(TableEvent.BIG_WIN, List.of(
                "Whoa! That's a set wave of a win, friend.",
                "Big one! Ride that feeling all the way home.",
                "That's the wave of the day, right there.",
                "A monster win! Warm all over, isn't it.",
                "Look at that pile! Like a perfect swell hit your beach.",
                "Sunshine jackpot! That one's postcard-worthy."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "Doubled and made it! Steep drop, clean exit.",
                "The double landed! Threading the barrel and coming out dry.",
                "Doubled home! Committed late and rode it out. Beautiful.",
                "That's a bottom turn done perfect. Double paid.",
                "One card, full send, warm landing. Love it.",
                "Doubled and won! The ocean rewards the decisive, sometimes."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "Long session, friend. Like a dawn-to-dusk beach day.",
                "We've been out here a while. My hair's practically salty.",
                "Long night at the felt. Time floats in here.",
                "Been a while, huh. Even the tide's changed twice.",
                "A marathon session. Stretch those shoulders, friend.",
                "We've logged some hours. Feels like one long summer afternoon."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You're up! Riding high on the open face.",
                "Ahead of the game, friend. Warm current all evening.",
                "Look at you, up and cruising. Sun-soaked run.",
                "You're glowing like sunrise on the water. Doing great.",
                "Up on the night! Keep smiling, that's the whole trick.",
                "That stack grew like a summer swell. Lovely to see."));

        return new Persona("moss", "Moss", Personas.SEAT_RIGHT, 0.50, l);
    }

    /**
     * Marine biologist. Precise, speaks rarely, and answers table superstition
     * with ecology — species, symbiosis, sampling, the statistics of shoals.
     * The Priya of the sea.
     */
    public static Persona coral() {
        Map<TableEvent, List<String>> l = new EnumMap<>(TableEvent.class);

        l.put(TableEvent.PLAYER_BLACKJACK, List.of(
                "A natural. Roughly one deal in twenty-one. Observed today.",
                "Blackjack. The rarest healthy specimen on this felt.",
                "Twenty-one dealt whole. Like netting a mature specimen first cast.",
                "A natural. No intervention required. Ideal sampling conditions.",
                "Dealt twenty-one. Symbiosis of ace and ten, if you like.",
                "Blackjack. I will record it as an uncommon sighting.",
                "A clean natural. Even taxonomy has its show species."));

        l.put(TableEvent.PLAYER_WIN, List.of(
                "A win. Consistent with sound decision-making.",
                "Correct play, favorable draw. The two do meet occasionally.",
                "You won. No superstition necessary. Note that.",
                "A tidy result. I'd accept it into the dataset.",
                "Won on merit. Like a well-adapted forager.",
                "That hand survived. Selection favored it.",
                "A win. Statistically unremarkable, emotionally pleasant."));

        l.put(TableEvent.PLAYER_BUST, List.of(
                "Bust. Overreach is common in juvenile predators too.",
                "Over twenty-one. The sample exceeded the container.",
                "Bust. Correct choices still meet bad draws. Ecology is full of them.",
                "You busted. Populations overshoot carrying capacity. So do hands.",
                "Over. An unlucky draw, not a flawed method.",
                "Bust. I have lost specimens the same way. The protocol was fine.",
                "Twenty-two. Outside the viable range. It happens to careful people."));

        l.put(TableEvent.PLAYER_LOSS, List.of(
                "A loss. The dealer's total was higher. That is the entire story.",
                "Lost. No omen involved. Only arithmetic.",
                "Beaten on the count. Variance, not karma.",
                "The house held more. Predation is like that. Nothing personal.",
                "A loss with no error in it. Those exist. Often.",
                "You lost. Shoals do not keep accounts for single fish.",
                "Lost fairly. I can show you the distribution, if it helps."));

        l.put(TableEvent.PUSH, List.of(
                "A push. Equilibrium. Ecosystems spend most of their time here.",
                "A tie. Energy in equals energy out.",
                "Push. A stable state. Briefly.",
                "Even totals. Commensalism: neither party harmed.",
                "A push. The null result. Publishable, barely.",
                "Tied. Homeostasis, at a card table."));

        l.put(TableEvent.PLAYER_SURRENDER, List.of(
                "Surrender. Retreat is a survival strategy older than fins.",
                "Half retained. An anemone retracts; it is not embarrassed.",
                "Good. Escaping a losing position is fitness, not failure.",
                "Surrendered. Octopuses jettison an arm and live. Same logic.",
                "Half back. Cutting losses is textbook foraging theory.",
                "A withdrawal. Prey that retreats survives to be counted."));

        l.put(TableEvent.PLAYER_SPLIT, List.of(
                "A split. Budding, effectively. One organism, now two.",
                "Split. Asexual reproduction of a hand. Coral does it constantly.",
                "Two hands. Independent trials. I approve of replication.",
                "Splitting the pair. A colony expands.",
                "Split. Twice the data per round. Efficient.",
                "Two hands from one. Polyps would recognize the method."));

        l.put(TableEvent.PLAYER_DOUBLE, List.of(
                "Doubled. A high-stakes foraging bout. Sometimes correct.",
                "A double. Committed energy expenditure. The spot warranted it.",
                "Doubling there matches the percentages. Noted with approval.",
                "Doubled down. One draw, maximal information.",
                "A double. Risk allocation, done deliberately. Good.",
                "Doubled. Even sessile creatures commit fully when they spawn."));

        l.put(TableEvent.DEALER_BUST, List.of(
                "Dealer bust. The mandatory hit rule is their predator.",
                "The house exceeded twenty-one. Structural, not karmic.",
                "Dealer over. Rigid behavior fails in variable environments.",
                "Bust for the house. Obligate strategies carry obligate costs.",
                "The dealer busted, as they do roughly a quarter of the time.",
                "Dealer over the line. Rules without plasticity. Observe the result.",
                "The house busted itself. No luck involved, only law."));

        l.put(TableEvent.DEALER_BLACKJACK, List.of(
                "Dealer natural. Ambush predation. Efficient and unlovely.",
                "Their blackjack. A moray strike from a hole you knew was there.",
                "Dealer twenty-one on the deal. No countermeasure exists.",
                "A dealer natural. Decided before behavior began.",
                "The house had it concealed. Camouflage is old technology.",
                "Dealer blackjack. Roughly one deal in twenty-one, for them too."));

        l.put(TableEvent.INSURANCE_OFFERED, List.of(
                "Insurance. A side wager priced against you. The data is public.",
                "Declineable. Nine of thirteen ranks say the ten is absent.",
                "Insurance is not symbiosis. Only one party benefits.",
                "An ace up. Superstition blooms here. Ignore the bloom.",
                "Insurance. Parasitism with a reassuring name.",
                "The offer profits the house on average. That is its ecology."));

        l.put(TableEvent.SHUFFLE, List.of(
                "A shuffle. The population is re-randomized. Nothing is remembered.",
                "Fresh shoe. Cards, like plankton, drift without memory.",
                "Reshuffled. Identical distribution, new arrangement.",
                "New shoe. Any pattern you saw was pareidolia.",
                "A shuffle. The deck resets. The odds never moved.",
                "Shuffled. Superstition is the only thing lost in there."));

        l.put(TableEvent.HOT_STREAK, List.of(
                "A winning cluster. Random sequences produce these. Enjoy it anyway.",
                "A streak. Fish school; wins appear to. Only one truly coordinates.",
                "Hot streak. No mechanism exists. The pleasure is real, though.",
                "Consecutive wins. Sampling noise, favorably arranged.",
                "A run. I would not build a hypothesis on it. Smiling is permitted.",
                "Streaks are perceptual. Your chips, however, are empirical.",
                "A good stretch. Correlation with your mood: high. With the cards: zero."));

        l.put(TableEvent.COLD_STREAK, List.of(
                "A losing stretch. Currents shift without malice.",
                "Cold cards. The shoe is not an organism. It intends nothing.",
                "A drought of wins. Droughts end by the same chance that starts them.",
                "Losing run. The next hand's odds are untouched by this one.",
                "A cold stretch. Superstition breeds in exactly these conditions.",
                "Nothing landing. The distribution includes this region too.",
                "A bad patch. Data has these regions. They mean nothing about you."));

        l.put(TableEvent.LOW_CHIPS, List.of(
                "Chips are low. Ending a session is a decision, and a sound one.",
                "A depleted stack. In the wild, knowing when to stop is fitness.",
                "Low reserves. No further data is required from tonight.",
                "The stack is nearly gone. Stopping now would be the rigorous choice.",
                "Low chips. Leaving with something is a perfectly good result.",
                "Reserves are thin. I end field seasons early when the weather turns.",
                "Nearly out. The honest reading of that is rest, not another sample."));

        l.put(TableEvent.SESSION_START, List.of(
                "A new arrival. Welcome to the study site.",
                "Hello. I count fish for a living. Tonight I count nothing. It's nice.",
                "Welcome. Observational role only, I assure you.",
                "Good evening. You will hear Moss before you understand him.",
                "A newcomer joins the shoal. Sit anywhere. Statistically identical.",
                "Welcome. I speak rarely. Consider each remark peer-reviewed."));

        l.put(TableEvent.DEALER_WEAK_CARD, List.of(
                "A six up. Their bust rate climbs to roughly two in five.",
                "Weak up-card. The dealer is an exposed organism at the moment.",
                "A soft up-card. Their rules will now hurt them. Documented.",
                "The dealer shows weakness. In a reef, something would notice.",
                "A poor card up. Their obligate draw becomes a liability.",
                "Weak card. For once the structure favors the specimen across the felt."));

        l.put(TableEvent.FIVE_CARD_HAND, List.of(
                "Five cards, no bust. A low-probability specimen. Preserve the memory.",
                "Five draws survived. Each one thinned the odds. Impressive.",
                "A five-card hand. Rare enough that I would photograph it.",
                "Five cards under the limit. Sequential survival. Well navigated.",
                "Five and standing. Like a larva reaching adulthood. Most don't.",
                "A five-card hand. I have catalogued rarer things, but not tonight."));

        l.put(TableEvent.TWENTY_ONE, List.of(
                "Twenty-one, accreted. Like a reef: slowly, then complete.",
                "Assembled twenty-one. Growth by deposition. Admirable.",
                "Twenty-one on multiple cards. More skill than the dealt kind.",
                "The full total, built stepwise. Reef logic. Layer on layer.",
                "Twenty-one, constructed. I respect accretion.",
                "Exactly twenty-one. Precision pleases me. Professionally."));

        l.put(TableEvent.CLOSE_CALL, List.of(
                "Lost by one. Margins fascinate biologists. Results ignore them.",
                "One point. In morphometrics, that is within measurement error.",
                "A single point. The outcome is binary; the sting is analog.",
                "By one. Near-misses recruit superstition. Decline the recruitment.",
                "One point short. Statistically identical to losing by ten.",
                "Lost by a point. Painful. Also meaningless. Both true."));

        l.put(TableEvent.BIG_WIN, List.of(
                "A large win. An outlier, in your favor. Savor the tail.",
                "Substantial gain. Rare events do occur. This one paid.",
                "A big win. Note it in the log with the date.",
                "That was a spring bloom of a payout.",
                "A significant result. Even a skeptic can smile at one.",
                "Large win. The sample size of joy just increased."));

        l.put(TableEvent.DOUBLE_WIN, List.of(
                "The double paid. Committed energy, returned with interest.",
                "Doubled and won. Decision quality and outcome aligned. Rare.",
                "A doubled hand, successful. The bold forager ate well tonight.",
                "Double win. The correct exposure at the correct moment.",
                "Doubled, and it held. Spawning events sometimes pay off at once.",
                "The double landed. Textbook risk, textbook reward."));

        l.put(TableEvent.LONG_SESSION, List.of(
                "A long session. Observation fatigue is measurable. Hydrate.",
                "We have been here some hours. Field shifts run like this.",
                "Long session. My longest transect took less time than tonight.",
                "Hours in. Attention decays like light underwater.",
                "A lengthy sit. Even barnacles reposition eventually.",
                "Long night. I have monitored spawning events shorter than this."));

        l.put(TableEvent.RUNNING_WELL, List.of(
                "You are ahead. A minority outcome. Enjoy your percentile.",
                "Net positive. Against a house edge, that is noteworthy.",
                "Ahead on the session. The data smiles on you, for now.",
                "You are up. Document it. Memory inflates these things later.",
                "A profitable evening so far. Empirically verified.",
                "Ahead. Like the one tagged fish that thrives. It happens."));

        return new Persona("coral", "Dr. Coral", Personas.SEAT_FAR, 0.28, l);
    }

    /** All three, in seat order. */
    public static List<Persona> cast() {
        return List.of(marina(), moss(), coral());
    }
}
