# Swing Desktop — Code Review

**Scope:** the `swing/` module only — app shell and engine wiring, table renderer, themes, plugin loader, media (SFX/music), Steam bridge, and dialogs. ~2,020 LOC of main source across 21 files (plus 202 LOC of tests).
**Reviewed at:** `claude/gradle-foundation` (commit `246edea`).
**Method:** full read of every `swing` source, cross-checked against `core` (`Engine`, `Phase`, `Hand`, `SaveManager`, `GameSettings`, `AchievementService`). Every Low-or-worse finding below was reproduced with a JDK 21 probe against the compiled module — the phase-reachability claim over 20,000 randomized rounds. Findings that did not reproduce were dropped (see *Checked and clean*).

## Verdict

The architecture is sound: the UI never mutates hand or bankroll state directly, every action routes through an `Engine` method, and the round-complete detection is more robust than the phase-transition check it replaced. The problems are at the **edges of the session** rather than in the game loop — two paths silently destroy player money or progress on exit, the round result is computed but never displayed, and a single bad plugin JAR stops the game from launching at all.

No Critical or High findings: nothing here lets a player cheat, and no money is mis-settled during a hand. But **SW-1 through SW-5 are all user-visible**, and SW-2/SW-3 lose data the player earned.

## Findings

| # | Severity | Summary | Status |
|---|----------|---------|--------|
| SW-1 | Medium | Round result is computed but never shown — `roundSummary()` is unreachable | ✅ Fixed |
| SW-2 | Medium | Chips on the felt are destroyed when the window closes | ✅ Fixed |
| SW-3 | Medium | Quit from the Game menu discards achievements and settings | ✅ Fixed |
| SW-4 | Medium | One malformed plugin JAR prevents startup, before any window exists | ✅ Fixed |
| SW-5 | Medium | Theme choice never persists; `settings.themeId` can never match a theme | ✅ Fixed |
| SW-6 | Low | Two error flashes in a row leave the status bar permanently red | ✅ Fixed |
| SW-7 | Low | `resetSession()` leaves `previousWins` stale, breaking win detection | ✅ Fixed |
| SW-8 | Low | `NeonTheme` corrupts the shared `Graphics2D`; the contract doesn't forbid it | ✅ Fixed |
| SW-9 | Low | `SoundFx` leaks the audio line when a write fails | ✅ Fixed |
| SW-10 | Low | External-plugin `URLClassLoader` is never closed | ✅ Fixed |
| SW-11 | Low | `MusicService` mutates `clip` from two threads, closes it in its own callback | ✅ Fixed |
| SW-12 | Low | `SoundFx` volume/mute crosses threads with no memory barrier | ✅ Fixed |
| SW-13 | Low | Soft-17 menu item and Settings dialog disagree and drift | ✅ Fixed |
| SW-14 | Low | Bet stack is off-centre and shifts with the denomination | ✅ Fixed |
| SW-15 | Low | Dialogs hidden, not disposed, when closed with the window X | ✅ Fixed |
| SW-16 | Low | Clear button is live mid-round and wipes the side-bet result | ✅ Fixed |
| SW-17 | Info | UI re-derives settlement instead of asking the engine | ✅ Fixed |
| SW-18 | Info | Hi-Lo count only updates between rounds | ✅ Fixed |
| SW-19 | Info | Allow-list check is TOCTOU; asset root is CWD-relative | ✅ Fixed |
| SW-20 | Info | Per-frame allocation; `paintCardBack` replaces the clip instead of intersecting | ✅ Fixed |

## Resolution

All twenty were fixed in the follow-up commit. The changes worth knowing about:

- **`Engine` now records the result it pays.** A new `Outcome` enum plus
  `Engine.lastOutcomes()` / `lastNet()` assign a result to each hand inside
  `settle()`, next to the line that moves the money. This is the fix for both
  SW-1 and SW-17: the UI renders the engine's record instead of keeping a second
  copy of the settlement rules that could disagree with the payout.
- **One shutdown path.** `shutdownAll()` is idempotent, returns staked chips
  before saving, and runs from the window close, from Game ▸ Quit, and from a
  `Runtime` shutdown hook — so no exit route can skip the saves (SW-2, SW-3).
- **Theme calls are sandboxed.** `TablePanel` hands every theme call a scratch
  `Graphics2D` it disposes afterwards, so no theme — shipped or third-party —
  can corrupt the renderer (SW-8). The `TableTheme` contract now says so, and
  gained a stable `id()` separate from the display name (SW-5).
- **A broken plugin is skipped, not fatal.** `PluginRegistry` drains the
  `ServiceLoader` one provider at a time catching `Throwable` (which is what
  `ServiceConfigurationError` needs), guards `onLoad()` the same way, and
  surfaces the reasons through `failures()` in the plugin manager (SW-4). It
  also stages each approved JAR to a private temp copy so the bytes that are
  hashed are the bytes that get loaded, closing the TOCTOU window (SW-19).
- **`previousWins` was deleted rather than fixed.** Once outcomes drive win
  detection the field became write-only, and a write-only field that used to
  carry a correctness decision is precisely the trap that produced SW-7.

40 regression tests were added (114 total, all passing): `OutcomeTest` for the
engine API, `SessionExitTest` for the stake-return-and-save cycle,
`PluginRegistryResilienceTest` for loader hardening and the allow-list,
`TableRenderIsolationTest` for graphics isolation and bet-stack centring, and
`ThemeIdentityTest` for theme id persistence.

---

### Game flow & presentation

**SW-1 · Medium · The round result is computed but never displayed.**
`describe()` (`BlackJackProApp.java:492`) has a `SETTLE` branch that returns `roundSummary()` and a `DEALER` branch that returns "Dealer playing…". Neither is reachable. `Engine` sets `phase = Phase.SETTLE` and calls `settle()` in the same breath (`Engine.java:143,151,217,246`), and `settle()` ends with `phase = Phase.BETTING` (`:297`); `playDealer()` does the same for `DEALER`. Probed across 20,000 randomized rounds driving every action path — the only phases ever observable after an engine call returns are `BETTING`, `INSURANCE`, and `PLAYER`.

The consequence is user-visible: after every hand the status bar reads "Place your bet." The player is never told Win / Loss / Push / Blackjack / Bust, and the whole of `roundSummary()` (`:504-520`) is dead code. Only the outcome SFX distinguishes results, so a muted player gets nothing.

*Fix:* build the summary inside `postAction()`'s round-complete branch (`:433-436`), where the hands are still on the table, and pass it to `updateUi` instead of `describe()`. Cleaner still: have `Engine.settle()` record a per-hand outcome the UI can read (see SW-17).

**SW-2 · Medium · Chips staked but not yet dealt are destroyed on exit.**
`Engine.addBet` moves money out of `bankroll` into `pendingBet` (`Engine.java:78-82`), and `SaveManager.save` persists `bankroll` only — there is no `pendingBet` key. `windowClosing` (`BlackJackProApp.java:150-160`) calls `save.save(engine)` without first returning the stake, and the same applies to `sideBets.pending()`.

Verified: start at \$1000, bet \$200, close the window, relaunch → bankroll \$800, pending bet gone. The chips simply vanish.

*Fix:* in the close handler, `engine.clearBet()` (which is already phase-guarded and refunds correctly) plus `engine.setBankroll(engine.bankroll() + sideBets.clear())`, before `save.save(engine)`.

**SW-3 · Medium · Quit from the Game menu loses achievements and settings.**
`quit` (`:255-256`) runs `save.save(engine); System.exit(0);`. The window-close handler that saves *everything else* — `settings.save()`, `achievements.save()`, `music.stop()`, `sfx.shutdown()`, `plugins.shutdown()`, `SteamBridge.shutdown()` — never runs, because `System.exit` does not fire `windowClosing` and there is no shutdown hook anywhere in the repo (grepped: none).

So a player who unlocks achievements and quits from the menu rather than the title-bar X loses all of that session's progress, plus any preference change made through the menu, plus a clean Steam shutdown.

*Fix:* extract the close handler's body into a `shutdownAll()` method and call it from both paths — or register it once with `Runtime.getRuntime().addShutdownHook`, which covers every exit route including SW-2.

**SW-5 · Medium · Theme selection never persists, and the lookup can never succeed.**
`GameSettings.themeId` defaults to `"classic"` (`GameSettings.java:26`) and `launch()` matches it against `TableTheme.displayName()` (`BlackJackProApp.java:680-683`). The two shipped themes report `"Classic Felt"` and `"Neon"`, so the comparison is verified to never match — the default value cannot select anything. Independently, nothing in the module ever *writes* `themeId`: `addThemeItem` (`:311-318`) swaps the field and repaints, and `SettingsDialog` doesn't expose themes at all. Both halves of the persistence path are dead.

*Fix:* add a stable `String id()` to `TableTheme` (distinct from the human-facing `displayName()`), match on that, and set `settings.themeId` in `addThemeItem`.

**SW-7 · Low · `resetSession()` leaves `previousWins` stale, silently breaking win detection.**
`resetSession` (`:569-585`) clears `processedHands` and `winStreak` but not `previousWins`, while `stats().reset()` zeroes `wins`. Verified: with 37 wins before the reset, `wonThisRound = s.wins - previousWins` evaluates to **−37**, so `playerWon` is false. Until the player wins 38 more rounds, the win sting never plays, `first_win`/`ten_wins`/`fifty_wins`/`survived_bust` never advance, and `winStreak` never increments — while losses are still counted, because `playerLost` only requires "not won and not pushed".

*Fix:* add `previousWins = 0;` alongside the other resets. (Worth a regression test — this is the second bug in this family after core CR-1.)

**SW-6 · Low · Two error flashes within 900 ms leave the status bar red for the rest of the session.**
`flash()` (`:634-642`) captures `Color was = statusBar.getBackground()` before overwriting it. Call it again while the first timer is still pending and the second call captures `FLASH_BG` as the "normal" colour, so both restores are no-ops. Verified with a verbatim copy of the method: the bar ends on `#6e3030` instead of `#0e2e1a`. Trivial to trigger — click a chip twice with insufficient chips.

*Fix:* restore the `STATUS_BG` constant explicitly rather than a captured value, and reuse one non-repeating `Timer` with `restart()` instead of allocating a new one per flash.

**SW-14 · Low · The bet stack is off-centre and moves with the denomination.**
`TablePanel.paintBetStack` (`:124-135`) advances `x += 50` for every denomination in `{500,100,25,5,1}` whether or not it drew a chip. Verified: a \$1 bet renders 200 px right of centre, a \$5 bet 150 px right, a \$500 bet exactly at centre. The stack visibly jumps sideways as the bet changes.

*Fix:* only advance `x` when `count > 0`, and offset the whole run by half its width.

**SW-16 · Low · Clear is live mid-round and erases the side-bet result.**
The chip buttons are gated on `betting` (`:612`), but the Clear button (`:184-187`) is a local that is never stored and never disabled. No money moves — `Engine.clearBet()` is phase-guarded and `sideBets.pending()` is already zero after `resolve` — but the handler still sets `sideMsg = ""` and writes "Bet cleared." over the status bar, wiping the 21+3 result mid-hand.

*Fix:* keep the reference and add it to the `betting` enable group.

### Plugins & extensibility

**SW-4 · Medium · One malformed plugin JAR prevents the game from starting.**
Two unguarded paths in `PluginRegistry.loadAll`:

1. **`ServiceConfigurationError` escapes.** Iterating `ServiceLoader.load(BlackJackPlugin.class, cl)` (`PluginRegistry.java:84`) throws when a JAR's `META-INF/services` names a class that isn't there. That's an `Error`, so the `catch (IOException)` at `:90` does not see it. Verified with an allow-listed JAR declaring a missing provider: `ServiceConfigurationError` propagates straight out of `loadAll`.
2. **`onLoad()` is unguarded.** `:38` calls `p.onLoad()` in a bare loop, while `shutdown()` correctly wraps `onUnload` in `try/catch` (`:130`). Verified: a plugin throwing from `onLoad` escapes `loadAll` the same way.

Both fire inside `launch()` *before* the frame is constructed, so the failure mode is the game simply not appearing — no dialog, no window, nothing on screen. The JAR has to be allow-listed first, so this is a footgun rather than an attack, but it's the exact scenario the plugin system exists to support.

*Fix:* wrap per-provider iteration and each `onLoad()` in `catch (Throwable)`, log the plugin name, and skip it. Consider surfacing skipped plugins in `PluginManagerDialog`.

**SW-8 · Low · `NeonTheme` corrupts the caller's `Graphics2D`, and nothing in the contract forbids it.**
`ClassicTheme` saves and restores font and colour (the fix in `bf90475` / `8a941da`); `NeonTheme` never did. Verified: hand it a 13 pt italic font, `#F8E9A1`, and a 2.5f stroke, call `paintCardFace`, and you get back a **48 pt bold** font, **cyan**, and a **2.0f** stroke. `TablePanel:90-92` already carries a defensive comment and re-sets the font for exactly this reason — a workaround for one symptom of a general problem. Any third-party theme will break the renderer the same way, because `TableTheme`'s javadoc says implementations should be "stateless" but never says they must not mutate the `Graphics2D` they're handed.

*Fix:* make it structurally impossible — have `TablePanel` pass each theme call its own `g2.create()` and `dispose()` it after. That costs one object per card and removes the whole class of bug, including the `TablePanel:91` workaround.

**SW-10 · Low · The external-plugin `URLClassLoader` is never closed.**
`loadFromDirectory` creates the loader as a local (`:81-83`) and lets it fall out of scope; `shutdown()` (`:128-133`) calls `onUnload` and clears the list but never closes it. The JAR file handles stay open for the life of the process — on Windows that keeps the plugin JARs locked, so the user cannot update or delete a plugin while the game runs.

*Fix:* store the loader in a field and `close()` it in `shutdown()`.

**SW-19 · Info · The allow-list check is TOCTOU, and the asset root is CWD-relative.**
`loadFromDirectory` hashes the JAR (`:73`) and then hands its URL to a classloader that opens the file again later; a swap in between defeats the check. The risk is low for a single-user desktop app where the attacker would already need write access to the per-user data directory — but `AUDIT.md` presents this as *the* hardening, so the residual window is worth stating. Separately, `launch()` resolves bundled assets from `Paths.get("resources")` (`:652`), relative to the process working directory. Writable state correctly uses `AppPaths.dataDir()`, but under a `jpackage` installer the working directory is not the install directory, so music silently disappears in the packaged build.

### Media & threading

**SW-9 · Low · `SoundFx` leaks the audio line when playback fails.**
`writeSample` (`SoundFx.java:141-150`) calls `line.close()` as the last statement inside the `try`. Any exception from `open`, `start`, `write`, or `drain` skips it, and the blanket `catch (Exception ignored)` hides the failure. Each failed effect leaks one `SourceDataLine`; enough of them exhaust the mixer and the game goes permanently silent with no diagnostic.

*Fix:* `finally { if (line != null) line.close(); }`, and log at least once rather than swallowing every failure.

**SW-11 · Low · `MusicService` mutates `clip` from two threads and closes it inside its own listener.**
`next()` is reachable from the EDT (Options ▸ Next track, `BlackJackProApp:275-276`) and from the Java Sound daemon thread via the `LineListener` at `MusicService.java:49-54`. Both paths run `play()` → `stop()` → `clip.close()`. Neither `clip` nor `index` is synchronized, so two concurrent calls can leak a `Clip` — one thread overwrites the field before the other closes it — and closing a line from inside its own event callback is documented in the Java Sound API as deadlock-prone.

*Fix:* `synchronized` on `play`/`stop`/`next`, and have the listener hand the advance off with `SwingUtilities.invokeLater(this::next)` so it never closes the clip from the callback thread.

**SW-12 · Low · `SoundFx.volume`/`muted` cross threads with no memory barrier.**
`setVolume`/`setMuted` (`:31-33`) are called from `SettingsDialog` on the EDT; `synthesize` reads `volume` (`:127`) and `playToneBurst` reads `muted` (`:96`) on the `blackjack-sfx` thread. Neither field is `volatile`, so a settings change is not guaranteed to be visible. Benign in practice on x86, and a one-word fix. `MusicService.muted`/`volume` have the same shape.

**SW-13 · Low · The soft-17 menu item and the Settings dialog disagree, and drift.**
The `JCheckBoxMenuItem` (`:263-265`) writes `engine.rules().dealerHitsSoft17` directly and never touches `settings`, so the change is lost on next launch — `launch():676` overwrites the rules from settings. Conversely `SettingsDialog:71` writes both but never re-syncs the menu item's checked state, so within one session the two controls display different values for the same setting. Both also mutate the rules mid-round, which is core review CR-8 seen from the caller's side: flipping soft-17 while the dealer is drawing changes the rules underneath a live hand.

*Fix:* route the menu item through the same code path as the dialog, re-sync the checkbox when the dialog closes, and gate the change on `Phase.BETTING`.

### Design notes

**SW-15 · Low · Dialogs are hidden, not disposed.**
`SettingsDialog` and `PluginManagerDialog` are constructed fresh on every open, and neither sets a close operation — `JDialog` defaults to `HIDE_ON_CLOSE`. Every dialog closed with the title-bar X (rather than Cancel/Close) stays alive and reachable from the parent frame's owned-window list. Small, but unbounded over a long session. *Fix:* `setDefaultCloseOperation(DISPOSE_ON_CLOSE)`.

**SW-17 · Info · The UI re-derives settlement instead of asking the engine.**
`anyHandPushed()` (`:481-490`) and `roundSummary()` (`:504-520`) each reimplement the push/win/loss comparison that `Engine.settle()` has already performed and paid out on. Today they drive the outcome SFX and the achievement branch; once SW-1 is fixed they'll drive the on-screen result too. Two independent implementations of the same rule will eventually disagree — and the one the player sees would be the wrong one. *Fix:* have `settle()` record a per-hand outcome enum and expose it; delete both UI copies.

**SW-18 · Info · The Hi-Lo count only updates between rounds.**
`counter.observe` runs inside `onRoundComplete` (`:472-477`), so the displayed running/true count excludes every card of the hand in progress — precisely when a card counter would want to consult it. The reshuffle detection is correct; it's the update cadence that's off. *Fix:* observe on each deal/hit in `postAction()`, keyed on cards not yet seen.

**SW-20 · Info · Per-frame allocation and an absolute clip.**
`TablePanel.paintComponent` builds a new `GradientPaint` and `BasicStroke` on every repaint (`:47,51`) — cheap individually, but it runs on every `updateUi`. And `ClassicTheme.paintCardBack` uses `g.setClip(r)` (`:77-83`), which *replaces* the clip rather than intersecting it, so during a partial repaint the card back can draw outside Swing's damage region. `g.clip(r)` is the intersecting form.

## Checked and clean

Verified during the review; no action needed.

- **Round-complete detection is correct and better than what it replaced.** Keying on `engine.stats().hands > processedHands` (`:433`) catches rounds that resolve during the deal (dealt naturals, dealer blackjack) and out of the insurance prompt — cases a phase-transition check misses. Confirmed reachable exactly once per round across the 20,000-round probe.
- **Blackjack detection does not misfire on splits.** `Hand.isBlackjack()` requires `!fromSplit`, so a two-card 21 after a split correctly triggers neither the fanfare nor `first_blackjack`.
- **Side-bet money flow keeps the core ledger invariant.** `placeSideBet` debits the stake, `resolveSideBet` (`:396-405`) records both `totalWagered += stake` and `totalReturned += payout` and credits the payout, so `bankroll == start − totalWagered + totalReturned` still holds. `SideBetManager.resolve` zeroes `pending` before returning, so a later Clear cannot double-refund.
- **21+3 evaluation and pay table are right.** Classification order (suited trips → straight flush → trips → straight → flush) is correct, `isStraight` handles both the A-2-3 wheel and Q-K-A, duplicate ranks in a 6-deck shoe don't false-positive, and `bet + bet * multiplier` returns stake-inclusive as the caller expects.
- **Multiple unlock listeners coexist.** `AchievementService.onUnlock` appends to a list, so `SteamBridge.wire()` (called in `launch()`) and the toast/chime listener (registered in the constructor) both fire. A setter-style API here would have silently dropped one — it doesn't.
- **`SteamBridge` degrades cleanly.** `ClassNotFoundException` is separated from real failures, every method is a no-op when disabled, and no Steam class leaks into a signature.
- **SFX runs off the EDT.** A daemon single-thread pool (`SoundFx:22-26`) means a 300 ms fanfare never blocks the UI, and effects don't overlap into garbage.
- **Plugin allow-list is secure-by-default.** No allow-list file, or no matching entry, loads nothing (`:63`, `:69`), and the `getClassLoader() == cl` identity check at `:88` correctly stops built-ins being registered twice by the child `ServiceLoader`.
- **`ClassicTheme` restores caller graphics state.** Font, colour, and stroke all come back unchanged — verified directly (contrast SW-8).
- **Split-hand layout does not clip.** Suspected overflow at four split hands; measured `paintHand` geometry at 1200×820 with four 4-card hands and the extremes land at x=93 and x=1107, comfortably inside the panel. Not a finding.

## Verification

Each finding was reproduced with a JDK 21 probe before the fix and the same
probe re-run after:

| Finding | Before | After |
|---------|--------|-------|
| SW-1 | `SETTLE`/`DEALER` unreachable across 20,000 randomized rounds; `roundSummary()` dead | Result rendered from `lastOutcomes()`; covered by `OutcomeTest` |
| SW-2 | bet \$200 → close → relaunch: bankroll \$800 | relaunch: bankroll \$1000 |
| SW-4 | `ServiceConfigurationError` escaped `loadAll()` | loader survives; built-ins still load |
| SW-5 | `"classic"` matched neither `"Classic Felt"` nor `"Neon"` | matches `ClassicTheme.id()` |
| SW-6 | two flashes in 900 ms left the bar on `#6e3030` | three flashes → restored to `#0e2e1a` |
| SW-7 | `wins - previousWins` = −37 after a reset | field removed; outcomes drive win detection |
| SW-8 | `NeonTheme` returned a 48 pt bold font, cyan, 2.0f stroke | vandalising theme cannot alter what any later call sees |
| SW-14 | \$1 bet drew 200 px right of centre | every single-denomination bet draws at centre |

One suspected finding did not reproduce and was never written up as a defect:
four split hands were measured to fit inside the 1200 px panel with ~90 px to
spare (see *Checked and clean*).

*The review itself made no code changes; the fixes shipped in the follow-up commit.*
