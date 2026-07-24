# Changelog

## 0.3.0 (unreleased)

The theme release — plus the tooling that proves the game under it.

### Themes & front end
- **24 table looks** (was 7), defined once in `core`, rendered identically by
  the Swing and libGDX front ends, across **nine card-back styles** (four new:
  diamonds, dots, waves, starburst).
- **Every theme seats a matching cast.** Thirteen casts, thirty-nine
  characters, ~6,200 lines of dialogue — pirates at Pirate Cove, a saloon
  crowd out west, a starship watch on Nebula, a witch/ghost/raven trio for
  Harvest Night, and so on. The regulars keep the neutral rooms. Nobody ever
  urges a bigger bet; a test suite enforces tone, per-event depth, global line
  uniqueness, and voice separation across every cast.
- **Theme Gallery** on desktop: every look rendered live as a miniature by its
  own renderer, badged with who it seats; click to apply, Escape to close.
- **Felt decoration**: each theme's card-back motif restated across the felt
  at large scale and low alpha under a soft vignette, on both platforms. The
  mobile build previously cleared to a flat colour — it now draws the full
  gradient, motif, vignette, and per-style card backs.
- **Themed window chrome**: control bar, status bar, and action buttons derive
  from the active palette instead of staying casino-green under every theme;
  felt captions choose readable ink for light felts (Glacier, Meadow).
- **Removed the Hint button** from both front ends. It's casino blackjack;
  the basic-strategy brain survives in `core` for the simulator's oracle.

### Testing & tooling
- The bug-sniffing simulator now checks **card conservation** (a seventh copy
  of any card from a six-deck shoe fails the run), **save/load round-trips**,
  rotates through **all 13 chatter casts**, and fields a **basic-strategy
  bot** whose long-run results must land on the game's known ~0.5% house edge
  — a payout bug the ledger can't see is visible to the mathematics.
  Verified over a 4,000,000-round soak: zero findings.
- 286 tests across `core`, `swing`, `platform`, and `gdx-core`.

### Packaging
- **Fixed `:swing:jpackage`, which had never worked** — the Badass Runtime
  plugin was applied in a way Gradle silently ignores. It now produces MSI
  (Windows, needs WiX), DMG (macOS), or DEB installers with a bundled jlink
  runtime, verified end to end on Linux.

### Deploy audit
- Full playability audit: the built distribution was launched on a display
  server and played by scripted input — bet, deal, hit, stand, payout, theme
  switch, quit, save verification. See `DEPLOY_AUDIT.md`.

## 0.2.x and earlier

Engine + Swing foundation, libGDX/Android port, plugin system (themes, AI,
side bets), security audit and hardening (`AUDIT.md`), platform module design
(`platform/`, ADR-0001), and the review/debug passes recorded in
`CODE_REVIEW.md`, `SWING_REVIEW.md`, `PLATFORM_REVIEW.md`, and
`DEBUG_REPORT.md`.
