# Core Engine — Code Review

**Scope:** the `core/` module only — rules engine, settlement, shoe/RNG, persistence, achievements, and the basic-strategy advisor. ~1,016 LOC across 15 files.
**Reviewed at:** `main` (commit `ba41eea`).
**Method:** full read of every `core` source; game correctness reasoned against standard US rules (dealer peek, dealer stands on soft 17), settlement branches cross-checked against the JUnit suite and confirmed with a JDK 21 probe where noted.

## Verdict

The engine is in good shape. The money-handling path — the part that actually matters — is **correct and well-tested**, with no High or Critical issues. Every finding below is **Low** or **Informational**: a cosmetic stat floor, the limits of using `int` for money, and light input-validation gaps on hand-editable save files. None affects fair play in the shipping desktop game.

## What's solid (verified)

- **Settlement is correct across every branch.** Natural blackjack pays 3:2, insurance pays 2:1 and breaks even against a dealer natural, pushes return the stake, late surrender forfeits exactly half, and split aces draw one card without the old double-pay. These are exercised by `EngineTest`, `SettlementTest`, `SplitAceSettlementTest`, and the new `RoundActionsTest`, and the ledger invariant `bankroll == start − totalWagered + totalReturned` holds.
- **Dealer peek is implemented correctly** for both an Ace up-card (via the insurance phase) and a ten up-card (via `afterInsuranceCheck`). A dealer natural is always resolved *before* the player acts, so the player never loses double/split money into a dealer blackjack.
- **Persistence is injection-safe by design.** `SaveManager` and `AchievementService` use flat text plus `Integer.parseInt`/`Boolean.parseBoolean` — no Java serialization, so there is no deserialization-gadget surface. `AppPaths` anchors writable state to a per-user directory (the AUDIT hardening).
- **Small types are defensively coded.** `Card` is immutable with correct `equals`/`hashCode`; `Achievement` clamps `goal` and restored progress; `Shoe` cut-card math reshuffles at the intended penetration.
- **Clean boundaries.** `core` has no AWT/Swing/libGDX dependencies, so it is unit-testable in isolation and reusable by every front end.

## Findings

### Correctness

**CR-1 · Low · `peakBankroll` floored at 1000.**
`SessionStats.peakBankroll` is initialized to `1000` (`SessionStats.java:14`) and `reset()` restores it to `1000` (`:27`) rather than to the current bankroll. `Engine` only lifts it via `Math.max(peak, startingBankroll)` (`Engine.java:30`). A player who starts below \$1,000 (or resets stats) shows a peak of \$1,000 they never held. Verified: `new Engine(500, …)` reports `peakBankroll = 1000`. Fix: default to `0` and seed the peak from the actual starting/current bankroll.

**CR-2 · Info · The strategy advisor never recommends surrender.**
`BasicStrategy.Action` includes `R`, and the doc comment lists it, but `recommend()` never returns it (`BasicStrategy.java`). With late surrender enabled as a house rule, the hint feature will never suggest the correct surrenders (hard 16 vs 9/10/A, hard 15 vs 10). Documented as a "no surrender chart," so this is a known simplification — worth closing if the advisor is meant to be optimal.

**CR-3 · Info · Soft-double advice degrades on 3+ card hands.**
`recommend()` returns `D` ("double, else hit") for soft 13–19 in the doubling range, but the caller can only double a two-card hand. For a multi-card soft total (e.g. A-3-4 = soft 18 vs 5) the fallback is Hit, where Stand is usually correct. Minor advice imperfection, not an engine bug.

### Robustness & money handling

**CR-4 · Low · `int` money: overflow ceiling and 3:2 flooring.**
Bets and bankroll are `int` throughout. `blackjackPayout` uses multiply-before-divide (good), but integer division floors odd bets — a \$25 blackjack pays 37 not 37.5 (verified), and a \$1 blackjack pays 1. Also `bet*2`/`bet*3` and accumulating `totalWagered` overflow above ~1.07 B / ~715 M, reachable only via an inflated bankroll (see CR-5), not normal play. Fine for chip-denominated desktop play; a high-limit or real-money build should move to `long` and pay fractional chips explicitly.

**CR-5 · Low · Loaded values are not validated.**
`SaveManager.load` feeds the file's `bankroll` straight into `Engine.setBankroll` (`:54`, `Engine.java:45`) with no bounds check, and `GameSettings` doesn't clamp `sfxVolume`/`musicVolume` to `[0,1]`. On the offline desktop this is only self-cheating, but it's worth stating the trust boundary explicitly: the save file is user-writable and unauthenticated. Clamp on load (e.g. `bankroll >= 0`, volumes to `[0,1]`).

**CR-6 · Low · Saves are not written atomically.**
`SaveManager.save` and `AchievementService.save` call `Files.writeString` directly (`SaveManager.java:38`, `AchievementService.java:100`). A crash or full disk mid-write can leave a truncated file. Loads tolerate missing keys, so the blast radius is small, but a temp-file-plus-`ATOMIC_MOVE` write would make saves crash-safe.

**CR-7 · Low · One bad line aborts the whole achievement load.**
`AchievementService.load` wraps the read loop in a single `try` and catches `NumberFormatException` *outside* the loop (`:83`, `:87`), so a single malformed line stops all remaining achievements from loading. `SaveManager` handles this better with a per-line `continue`. Move the parse guard inside the loop.

### Design & concurrency

**CR-8 · Info · Mutable, non-thread-safe state; rules are openly mutable.**
`Engine`, `Hand`, `SessionStats`, and `BlackjackRules` are mutable with no synchronization. That's fine under the Swing EDT (single-threaded) and is documented, but `BlackjackRules` exposes all fields publicly (`decks`, payouts, `maxSplits`) so any caller can change a payout mid-session with no guard. Acceptable for a single-player desktop game; a served/multiplayer context would need the rules frozen per round.

**CR-9 · Info · Shoe fairness rides entirely on the injected RNG.**
`Shoe` shuffles with whatever `Random` the `Engine` is constructed with (`Shoe.java:40`). Production must inject `SecureRandom` (the AUDIT says it does); a plain `java.util.Random` has a 48-bit seed and cannot reach all 312! shoe orderings, which would be predictable. Also, the defensive empty-shoe reshuffle in `deal()` (`:49`) would splice a fresh shoe into a live hand — practically unreachable at 75% penetration, but a hard error might be safer than silently continuing.

## Recommended order

1. **CR-1** — one-line stat fix, user-visible.
2. **CR-5 / CR-7** — cheap input-validation hardening on the load paths.
3. **CR-6** — atomic-write saves.
4. **CR-2 / CR-3** — advisor completeness, if the hint is meant to be optimal.
5. **CR-4 / CR-8 / CR-9** — revisit only if a high-limit, served, or real-money build is on the table (the `platform/` module already assumes a different money model).

*No changes were made to the codebase as part of this review.*
