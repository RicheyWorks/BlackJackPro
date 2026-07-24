# BlackJack Pro

[![CI](https://github.com/RicheyWorks/BlackJackPro/actions/workflows/ci.yml/badge.svg)](https://github.com/RicheyWorks/BlackJackPro/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)

Casino-grade single-player blackjack in Java. Pure rules engine, polished Swing desktop UI, and an early libGDX/Android port sharing the same core.

## Features

- **House rules:** 6-deck shoe (~75% penetration), 3:2 blackjack, push on tie, double on any two cards, split up to 4 hands (split aces get one card), late surrender, insurance (2:1), dealer stands on soft 17 (toggle in Options). The game pays whole dollars, and any odd half rounds in the player's favour — a \$25 natural pays 38, not 37
- **Swing desktop:** Custom-painted felt/cards/chips, procedural SFX, settings dialog, 12 achievements with toasts, bankroll + stats persistence
- **Themes:** twenty-four table looks defined once in `core` and rendered by both the Swing and libGDX front ends, across nine card-back styles — and **every theme seats a cast that talks like the theme.** Thirteen casts, thirty-nine characters: the regulars (Marge, Dutch, Priya) keep the neutral rooms (Classic, Midnight, Ink, Graphite); Neon/Arcade seat the arcade crowd (Pixel, Dash, Vex); The Abyss/Lagoon the reef crew (Marina, Moss, Dr. Coral); Glacier/Aurora the polar team (Ingrid, Sunny, Dr. Frost); Harvest Night a cozy-spooky trio (Agatha the witch, Barnaby the ghost, Edgar the raven); Ember the forge (Sela, Forge, Wyrm the polite dragon); Evergreen the snowed-in inn (Nan, Mr. Jolly, Carol); Sakura/Meadow the garden (Hana, Bram, Juniper); Crimson/Royal/Deco the society set (The Duchess, Maxie, Pemberton); Cocoa House the cafe (Esme, Gus, Praline); plus the pirate crew, the frontier crowd (also at Desert Sun), and the Nebula watch. The desktop picks themes from a visual **Theme Gallery** — every look rendered live as a miniature by its own renderer, badged with who it seats
- **Table chatter:** ~6,200 lines of dialogue across the thirteen casts; nobody ever encourages a bigger bet, and a test suite enforces tone, per-event depth, global line uniqueness, and voice separation across every cast
- **Bug-sniffing simulator:** plays the game the way a front end does — millions of seeded rounds, every query after every action, legality-honesty probes, a per-call money ledger, card-conservation checks, save/load round-trips, and a basic-strategy bot whose long-run results must land on the game's known ~0.5% house edge (a payout bug invisible to the ledger is visible to the math)
- **Plugins:** ServiceLoader + external JARs — Hi-Lo counter AI, 21+3 side bet
- **Distribution:** `jpackage` native installers (MSI/DMG/DEB); optional Steamworks bridge
- **Mobile (WIP):** libGDX `TableScreen` at feature parity with the desktop on everything platform-neutral — all twenty-four themes with per-style card backs (crewed casts included), sound, table chatter, 21+3, the Hi-Lo counter, settings/stats menu, and persistence across app restarts; Android debug APK (minSdk 26); iOS not wired yet

## Build & run

Requires **JDK 21** on PATH. The Gradle wrapper is included — no separate Gradle install needed. Examples below use `./gradlew` (macOS/Linux); on Windows use `gradlew` or `gradlew.bat`.

```bash
# Build + test (same tasks CI runs; Android skipped unless local.properties / ANDROID_HOME set)
./gradlew :core:test :swing:test :platform:test :gdx-desktop:classes

# Run Swing desktop (primary)
./gradlew :swing:run

# libGDX desktop preview (optional)
./gradlew :gdx-desktop:run

# Native installer (optional, needs -Pjpackage)
./gradlew :swing:jpackage -Pjpackage

# Bug-sniffing soak (args: seeds, rounds per seed). Plays millions of rounds
# against the real engine and exits loudly, with a reproducible seed, if
# anything impossible ever happens.
./gradlew :core:testClasses
java -cp core/build/classes/java/main:core/build/classes/java/test \
     com.richeyworks.blackjack.sim.GameSimulator 2000 2000
```

**Android (optional):** Add `local.properties` with `sdk.dir=...` or set `ANDROID_HOME`, then `gradlew :android:assembleDebug`.

**Windows shortcut:** double-click `run_pro.bat` (runs `:swing:run`).

**Background music (optional):** drop `.wav` files into `resources/music/` — see
[`resources/music/README.md`](resources/music/README.md). WAV only; stock Java
can't decode MP3. Audio is gitignored, so the repo ships without tracks and the
music feature stays quietly off until you add some.

## Project status

| Area | Status |
|------|--------|
| Core engine + tests | Working — JUnit 5; CI green on Ubuntu, Windows, macOS |
| Swing desktop | Primary polished build — themes, plugins, achievements, saves |
| libGDX + Android | Playable — themes, sound, chatter, side bet, counter, settings, persistence |
| iOS / mobile polish | Not started — animated cards, sound, settings UI on mobile |

## Tech stack

- **Java 21** (Swing), **Java 17** (libGDX/Android)
- **Gradle** multi-module: `core`, `swing`, `gdx-core`, `gdx-desktop`, `android`
- **libGDX 1.12.1**, **JUnit 5**, **GitHub Actions**

## Layout

```
core/          Rules engine, settings, saves, achievements, themes, chatter,
               sound synthesis, side bets, counting (no UI, no AWT)
swing/         Desktop UI, plugins, media, Steam bridge
gdx-core/      libGDX game + TableScreen
gdx-desktop/   LWJGL3 launcher
android/       APK launcher
platform/      Online real-money platform skeleton (design; see docs/architecture)
resources/     Shared assets (deck, lang, css)
docs/          Architecture decision records
```

## Security & online platform (design)

A security audit of the desktop build and the fixes applied are documented in [`AUDIT.md`](AUDIT.md) — hardened plugin loading (SHA-256 allow-list), a per-user data directory, a `SecureRandom` shoe, and dependency/CI hardening.

An early, compliance-first design for an online **real-money** (including crypto) platform lives under [`platform/`](platform/README.md), with the rationale in [`docs/architecture/ADR-0001-real-money-crypto-platform.md`](docs/architecture/ADR-0001-real-money-crypto-platform.md). It is interface stubs plus reference implementations of the fail-closed compliance gate, the double-entry wallet, and a server-authoritative round — **not operable without state licensing, FinCEN MSB registration, and gaming-law counsel.**

## License

Released under the [MIT License](LICENSE) — © 2026 RicheyWorks.
