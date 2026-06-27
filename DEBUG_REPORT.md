# BlackJack Pro — Debug Report

**Scope:** `/debug everything` — full sweep of the built, shipping code (`core`, `swing`, `gdx-core`, `gdx-desktop`) plus a read-through of the Android launcher.
**Branch:** `claude/gradle-foundation`
**Toolchain:** Temurin JDK 21, Gradle 8.10 (wrapper).
**Result:** 6 bugs fixed, 5 regression tests added, 4 findings reported. Full suite green — **45 tests** (was 40).

---

## Summary

| # | Bug | Module | Severity | Status |
|---|-----|--------|----------|--------|
| 1 | Splitting aces settles & pays every hand twice | `core` | **High** (real money) | Fixed + test |
| 2 | Dealer hole card is never revealed to the player | `swing`, `gdx-core` | **High** (UX) | Fixed |
| 3 | Round-complete handler skipped on dealt naturals / insurance | `swing` | Medium | Fixed |
| 4 | Insurance stake missing from the wager ledger | `core` | Medium | Fixed + test |
| 5 | Built-in plugins double-loaded when an external JAR exists | `swing` | Medium (latent) | Fixed |
| 6 | "Heart of Stone" achievement was never wired — unobtainable | `swing` | Low | Fixed |

The build itself was already green; none of these were compile or pre-existing-test failures. They were found by reading the engine and UI, then proven with targeted harnesses.

---

## Bug 1 — Splitting aces double-settles the round

**Module:** `core` · `Engine.split()`

- **Expected:** splitting a pair of aces deals one card to each ace, freezes both, then settles the two hands once.
- **Actual:** the player was paid twice and every statistic was double-counted.
- **Reproduce:** seed `103`, deal A,A vs a dealer 17, split → bankroll became **1060** with `wins=4`; correct is **1020** / `wins=2`.

**Root cause:** the split-aces branch called `advanceHand()` **twice**. But `advanceHand()` already loops internally to skip frozen split-ace hands, so the first call ran off the end of the hand list into `playDealer()` → `settle()`, and the second call invoked `settle()` again — re-paying hands whose state had not been reset.

**Fix:** collapsed the two calls into one (the loop handles skipping both frozen hands).

```java
if (rules.splitAcesOneCard && h.first().rank() == Rank.ACE) {
    h.markSplitAce();
    n.markSplitAce();
    advanceHand();   // was called twice; the loop already skips both frozen hands
}
```

**Prevention:** `SplitAceSettlementTest` searches RNG seeds for a real A,A deal, splits, and asserts each hand settles exactly once (`wins+losses+pushes == 2`) with the bankroll inside the single-settlement range. It **fails on the old code** (`expected 2 but was 4`) and passes on the fix.

---

## Bug 2 — Dealer hole card is never revealed

**Module:** `swing` · `TablePanel`, and `gdx-core` · `TableScreen` (identical bug in both)

- **Expected:** the dealer's hole card stays face-down during the player's turn and is revealed once the dealer plays / the round settles.
- **Actual:** the hole card (and the dealer's true total) was **never** shown.

**Root cause:** `hideHole = engine.phase().ordinal() < Phase.DEALER.ordinal()`. That is also true in the post-round `BETTING` view. Because the engine resolves the dealer turn and settlement synchronously, the UI only ever repaints at `BETTING` (Swing) or never observes the transient `DEALER`/`SETTLE` phases between frames (gdx). So the only rendered states are `PLAYER` and `BETTING`, both of which hid the hole.

**Fix:** hide the hole only during the player's decision phases; reveal it everywhere else (dealer play, settlement, and the post-round view, where the dealer hand is still on the table).

```java
Phase phase = engine.phase();
boolean hideHole = phase == Phase.DEALING || phase == Phase.INSURANCE || phase == Phase.PLAYER;
```

The Swing dealer caption was also guarded so it reads `Dealer` (not `Dealer (0)`) before the first deal.

**Verification:** compile-verified in both modules. Pure paint logic — worth a 10-second visual smoke test (play a hand to the end and confirm the dealer's second card flips up).

---

## Bug 3 — Round-completion handler skipped on immediate settlements

**Module:** `swing` · `BlackJackProApp`

- **Expected:** post-round processing (outcome SFX, achievement progress, win-streak) runs once per completed round.
- **Actual:** it was skipped whenever a round both started and ended inside one click — a dealt blackjack, or a dealer blackjack — and on the insurance path entirely. The blackjack fanfare never played on a dealt natural, and the "Natural Twenty-One" achievement was effectively **unobtainable** (a natural always settles on the deal).

**Root cause:** `onRoundComplete()` only ran when the captured `before` phase was non-`BETTING`. A round dealt from `BETTING` that settled immediately left `before == BETTING`, so the guard was false. The insurance handler never called it at all.

**Fix:** replaced the phase-transition guard with a `postAction()` keyed off the engine's hand counter, so it fires exactly once per round on every path (normal play, dealt naturals, dealer blackjack, and out of the insurance prompt). `postAction()` is now called by both `safe()` and `takeInsurance()`, and the round/streak counters are reset on New Session.

---

## Bug 4 — Insurance stake missing from the wager ledger

**Module:** `core` · `Engine.takeInsurance()`

- **Expected:** `bankroll == start − totalWagered + totalReturned` holds at all times, including when insurance is taken.
- **Actual:** accepting insurance deducted the stake from the bankroll but never added it to `totalWagered`, so the lifetime ledger drifted by the insurance amount.

**Fix:** record the insurance stake as a wager.

```java
bankroll    -= cost;
stats.totalWagered += cost;   // insurance is a wager; keep accounting consistent
insuranceBet = cost;
```

**Prevention:** `SettlementTest.insuranceIsReflectedInTotals` finds a dealer-Ace deal, accepts insurance, and asserts the ledger invariant. Pre-fix it fails by exactly the stake (`expected 1050, was 1000`); post-fix it passes.

---

## Bug 5 — Built-in plugins double-loaded

**Module:** `swing` · `PluginRegistry.loadFromDirectory()`

- **Expected:** built-in plugins load once; external JARs add to them.
- **Actual:** dropping any JAR into `plugins/` registered every built-in (Neon theme, Hi-Lo AI, 21+3) **twice**, because the child `ServiceLoader` also re-discovers providers from the parent classpath.

**Fix:** keep only providers actually loaded from the external class loader.

```java
for (BlackJackPlugin p : ServiceLoader.load(BlackJackPlugin.class, cl)) {
    if (p.getClass().getClassLoader() == cl) plugins.add(p);
}
```

Latent (only triggers with an external JAR present), so no default-path test; the fix is by class-loader identity.

---

## Bug 6 — "Heart of Stone" achievement was unobtainable

**Module:** `swing` · `BlackJackProApp.onRoundComplete()`

The `survived_bust_streak` achievement ("win 5 in a row") was registered but no code ever updated it. Now driven by a win-streak counter (a push keeps the streak alive, a loss resets it), reset on New Session.

---

## Findings (reported, not changed)

These are gaps or judgment calls rather than defects, left for a product decision:

- **21+3 side bet and Hi-Lo AI are dead features.** Both are loaded and 21+3 is fully unit-tested, but neither is wired into gameplay — they only surface as a *count* in the Plugin Manager dialog. There is no UI to place a side bet or have the AI advise/play. Wiring them is feature work.
- **`Animator` is dead code** — never referenced outside its own file.
- **Line-ending churn.** The working tree was full of phantom CRLF-only diffs with no `.gitattributes`. Added one (`* text=auto eol=lf`, `.bat` as CRLF, assets binary); run `git add --renormalize .` once to flatten the existing churn.
- **Stale `build/` directories** from a prior Windows build sit in the tree; they are git-ignored and harmless.

---

## Verification

- Provisioned Temurin JDK 21 (repo targets JDK 21 for `core`/`swing`; the sandbox shipped only JDK 11).
- Full Gradle build green: `:core:test :swing:test :gdx-desktop:classes` → **BUILD SUCCESSFUL**, **45 tests, 0 failures** (was 40).
- 5 new tests added: `SplitAceSettlementTest` (1) and `SettlementTest` (4 — 3:2 natural, bust, money-conservation over 300 rounds, insurance ledger).
- Each new core test was **mutation-checked**: breaking the corresponding production logic (e.g. paying blackjack 1:1, removing the insurance ledger line, restoring the double `advanceHand()`) makes the matching test fail, confirming the tests have teeth.

## Files changed

- `core/.../engine/Engine.java` — split-aces + insurance fixes
- `swing/.../ui/swing/BlackJackProApp.java` — round-complete detection, win-streak achievement
- `swing/.../ui/swing/TablePanel.java` — hole-card reveal
- `swing/.../plugin/PluginRegistry.java` — plugin de-duplication
- `gdx-core/.../gdx/TableScreen.java` — hole-card reveal
- `core/.../test/.../SplitAceSettlementTest.java`, `SettlementTest.java` — new tests
- `.gitattributes` — line-ending normalization
