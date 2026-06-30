# Gradle foundation + debug pass + plugin features

Branch: `claude/gradle-foundation`

## What this PR does

Builds on the Gradle multi-module migration with a full debugging sweep of the shipping code and wires up two plugin features that were previously loaded but disconnected. The engine and both UIs were audited end to end; every bug found is fixed and, where testable, pinned by a regression test. The suite goes from **40 → 56 tests**, all green on JDK 21.

See `DEBUG_REPORT.md` for the full per-bug breakdown.

## Bug fixes

| Area | Bug | Fix |
|------|-----|-----|
| `core` | **Splitting aces settled & paid every hand twice** — `split()` called `advanceHand()` twice, but it already loops past frozen hands, so `playDealer()`/`settle()` ran twice. | Single `advanceHand()`. Regression test asserts each hand settles once. |
| `swing` + `gdx` | **Dealer hole card was never revealed** — `hideHole` was true in the post-round view, and the only rendered states are PLAYER/BETTING. | Hide only during the player's decision phases. |
| `swing` | **Round-complete processing skipped on dealt naturals / insurance** — so the blackjack fanfare never fired and "Natural Twenty-One" was unobtainable. | Detect round end via the engine hand counter (`postAction()`), fired on every path. |
| `core` | **Insurance stake missing from the wager ledger.** | Record insurance in `totalWagered`; covered by a test. |
| `swing` | **Built-in plugins double-loaded** when an external JAR is present. | Keep only providers from the external class loader. |
| `swing` | **"Heart of Stone" achievement was never wired** — impossible to unlock. | Driven by a win-streak counter. |

## Features wired up

- **21+3 side bet** — place a stake during betting, resolved on the deal against the opening cards + dealer up-card (suited trips 100:1 → flush 5:1), paid into the bankroll with the outcome shown in the HUD. Money flow lives in a Swing-free `SideBetManager` (unit-tested).
- **Hi-Lo counter** — a running/true count shown in the HUD, fed each round and reset on shoe reshuffle, with an Options-menu toggle. Counting logic now has tests (it had none).

## Tests

- 16 new tests: `SplitAceSettlementTest` (1), `SettlementTest` (4), `SideBetManagerTest` (6), `HiLoCounterAiTest` (5).
- New core tests are **mutation-checked** — reverting the corresponding fix makes the matching test fail.

```
gradlew :core:test :swing:test :gdx-desktop:classes   # BUILD SUCCESSFUL, 56 tests, 0 failures
```

## Housekeeping

- Added `.gitattributes` (`* text=auto eol=lf`, `.bat` CRLF, assets binary) to stop CRLF churn; run `git add --renormalize .` once to flatten the existing working tree.
- Added `DEBUG_REPORT.md`.

## Not covered / follow-ups

- **Swing & libGDX UI changes are compile- and logic-verified but not runtime-tested** (no headless GUI harness). Worth a manual smoke test: get dealt a blackjack (fanfare + achievement), play a hand to the end (dealer hole reveals), place a 21+3 bet, and watch the count update.
- `legacy/` is untouched (deprecated, not built). Note its original settlement paid blackjack at 1:1 — fixed in the new engine.
- `Animator` is dead code; iOS target and License are still open per the README.
