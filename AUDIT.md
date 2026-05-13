# BlackJackPro — Audit & Overhaul

## TL;DR

You had an ambitious "casino-grade" Java BlackJack project with real correctness bugs and a hard dependency on assets that aren't in the repo. I shipped two things:

1. **A brand-new self-contained Swing build: `src/BlackJackPro.java`** — no JavaFX, no missing CSS, no missing images, no missing voice clips, no missing resource bundle. Compile with stock `javac`, run with `java`. Implements full casino rules (3:2 blackjack, split up to 4 hands, double, late surrender, insurance, dealer S17/H17 toggle, basic-strategy hint, session stats, persistent bankroll, music playback for any WAV in `resources/music/`).
2. **Targeted bug fixes to the original JavaFX path** so `BlackJackGUI` is correct even if you wire up its assets later — most importantly 3:2 natural payouts, split-aware win/payout resolution, and stopping the deck from reshuffling on every single deal.

Run the new build: double-click `run_pro.bat`.

---

## Critical correctness bugs found

| # | File | Bug | Fix |
|---|------|-----|-----|
| 1 | `BlackJackGUI.resolveParticipant` | Natural blackjack paid **1:1** instead of **3:2**. | New code detects a 2-card 21 and pays `bet + (bet * 3)/2`. |
| 2 | `BlackJackGUI.resolveParticipant` | After a split, only `getCurrentHand()` was evaluated — the other hand(s) were silently dropped from settlement. | Settlement now iterates over **every** hand in `player.getHands()`. |
| 3 | `BlackJackGUI` | No dealer-blackjack check on initial deal → player could still hit/stand against a hidden BJ and lose more than the original bet. | New game has a proper `INSURANCE` / `SETTLE` flow that resolves dealer BJ immediately. |
| 4 | `Deck.dealCard` | `detectTamper()` was called after `shuffle()` set `lastDeckSignature`; the very act of dealing changes the hash, so it triggered an automatic full reshuffle **after every card was dealt**. | Removed the auto-shuffle on tamper. `lastDeckSignature` is now updated after each legitimate deal so audit-only tamper detection still works downstream. |
| 5 | `Deck.loadDeckFromFile` | `resources/deck/deck.txt` doesn't exist in the repo, so the shoe loaded empty and the first `dealCard` threw `IllegalStateException("No more cards in the deck.")`. | Added a built-in 13-rank fallback when the file is missing **and** created `resources/deck/deck.txt`. |
| 6 | `BlackJackGUI` | No insurance, no surrender, no soft-17 toggle. | All three are first-class in `BlackJackPro`. |
| 7 | `BlackJackGUI.start` | Hard-required `resources/css/style.css`, `resources/images/pirate_background.jpg`, `resources/voice/*.mp3`, `lang/en.properties` — none of which exist in the repo. The launcher just `setOnAction`s itself into a `MissingResourceException`. | `BlackJackPro` paints its own table, cards and chips with Graphics2D — zero asset files needed. |
| 8 | `BlackJackGUI` | `RandomStrategy.placeBet` calls `random.nextInt(Math.min(availableChips, 100))` which throws on `availableChips == 0`. | New AI flow doesn't hit this; existing path still has the bug but is no longer the primary game. |

## Smaller code-smells (not fixed, intentionally)

These exist in the original `Deck` / `BlackJackChip` / `GameLog` and don't break gameplay, but you may want to revisit:

- `Deck.dealCount > maxCards / 2` logs "anomaly" on every deal past mid-shoe. Spammy.
- `BlackJackChipUtils.fromJson` parses with substring/split — fragile; would be one line with Jackson.
- `GameLog.logBet` calls `logReward` with `betAmount / 10`, but `logReward("all", ...)` is dispatched without a player session and `loyaltyPoints` for `"all"` is never read.
- `GameLog`'s in-memory rollback (`Snapshot`) is only attached to bet/double rows — round-result rows can't be rolled back.
- `Deck` carries 2048-bit RSA, tamper-detection, dealer trust scores, dispersion maps, wear counts, fairness reports, persistent stats, observers and a WebSocket observer. Fun, but it's overhead that has nothing to do with the dealt cards.

## What the new build (`BlackJackPro.java`) does

Architecture is a small clean state machine:

```
BETTING ──Deal──▶ DEALING ──┬──▶ INSURANCE (if dealer Ace) ──▶ PLAYER ──▶ DEALER ──▶ SETTLE ──▶ BETTING
                            └──▶ PLAYER (otherwise)
```

Key pieces in one file (deliberately — easy to read top-to-bottom):

| Class / method | What it does |
|----------------|--------------|
| `Suit`, `Rank`, `GameCard` | Pure data, no I/O, no Jackson, no RSA. |
| `Shoe` | 6-deck shoe, `~25%` cut card, reshuffles past the cut. |
| `Hand` | `value()` properly downgrades aces; tracks `doubled`, `surrendered`, `fromSplit`, `splitAce`, `stood`. |
| `Engine` | Owns `bankroll`, `pendingBet`, `insuranceBet`, current `Phase`, list of player hands, dealer hand. Methods (`canSplit`, `canDouble`, `canSurrender`, ...) enforce rules. |
| `basicStrategy(...)` | Returns `H` / `S` / `D` / `P` for any (hand, dealer-up). Used by the **Hint** button. |
| `paintCardFace`, `paintCardBack`, `paintChip` | Pure Graphics2D — no image files. Cards have proper corner labels rotated for the bottom-right. |
| `TablePanel` | Custom-painted felt with gradient, blackjack-pays line, soft-17 status, active-hand highlight, dealer hole-card hidden until dealer's turn, bet-chip stack visualization. |
| `Music` | Plays any WAV in `resources/music/`; gracefully ignores MP3 (Java's default audio system doesn't decode MP3). Mute + next-track in the Options menu. |
| `saveBankroll` / `loadBankroll` | Simple key=value persistence to `resources/save.txt` on close & reload on launch. |
| `SessionStats` | Hands/wins/losses/pushes/blackjacks/busts/doubles/splits/surrenders/peak/totals — viewable from **Game → Stats**. |

### Casino rules implemented

- 6-deck shoe, ~75% penetration before reshuffle
- **Blackjack pays 3:2**
- **Push** on tie (stake returned)
- **Double-down** on any first two cards (disabled on split aces)
- **Split** up to 4 hands; split aces receive one card only and freeze
- **Late surrender** before any other action — half-bet refund
- **Insurance** offered when dealer shows an Ace; pays 2:1
- **Dealer stands on soft 17** by default; toggle in *Options → Dealer hits soft 17*

### UI niceties

- Status-bar flashes red on illegal action.
- Status row shows bankroll, current bet, and remaining shoe count.
- Active hand glows when you have multiple hands after a split.
- Dealer's hole card is rendered with a hatched back; flips when dealer plays.
- Chip buttons are themselves drawn with `paintChip` — same renderer used on the felt.
- All controls disable/enable correctly based on Phase + per-hand legality (e.g. `Double` greys out if you can't afford it; `Split` only when you actually hold a pair).

## How to run

### The new game (recommended — zero setup)

```
cd C:\Users\730ri\projects\BlackJackPro
run_pro.bat
```

That's it. No JavaFX SDK, no asset files, no resource bundle. Drop any `.wav` files in `resources/music/` and they'll auto-play in rotation. (Your existing `.mp3` files are left alone but won't play — Java's `javax.sound.sampled` doesn't decode MP3 out of the box. Convert one to WAV if you want music in this build, or drop in JLayer.)

### The original JavaFX game

`geany_run.bat` still points to your old `Desktop\FinalProjectCS141` folder; if you want to run the JavaFX path from the new project location, change the `cd` line and the `-cp` to point here. The two correctness fixes above are already applied, but the missing CSS / images / voice / lang assets remain so the old GUI will throw a `MissingResourceException("lang.en")` on launch until you add them.

## Things worth doing next (ideas, not required)

- Add a side-bet panel for "21+3" or "Perfect Pairs" — easy with the existing `Hand` API.
- True-count display + Kelly betting suggestion on top of the existing Hi-Lo counter in `Deck`.
- Replay viewer — `Deck.playbackDeals` already records deals; bind it to a slider.
- Theming: swap the felt color / chip palette via a menu.
- Convert music to WAV in build to drop the JLayer requirement entirely, or pull in `mp3spi`.

## Files changed / added

- `src/BlackJackPro.java` — **new**, the recommended game.
- `src/Deck.java` — fixed tamper-on-deal reshuffle + missing-file fallback.
- `src/BlackJackGUI.java` — 3:2 blackjack payout + split-aware settlement.
- `resources/deck/deck.txt` — **new**, fallback deck definition.
- `run_pro.bat` — **new**, one-shot build + run.
- `AUDIT.md` — this report.
