# BlackJack Pro

Casino-grade single-player blackjack in Java. Pure rules engine, polished Swing desktop UI, and an early libGDX/Android port sharing the same core.

## Features

- **House rules:** 6-deck shoe (~75% penetration), 3:2 blackjack, push on tie, double on any two cards, split up to 4 hands (split aces get one card), late surrender, insurance (2:1), dealer stands on soft 17 (toggle in Options)
- **Swing desktop:** Custom-painted felt/cards/chips, procedural SFX, settings dialog, 12 achievements with toasts, bankroll + stats persistence
- **Plugins:** ServiceLoader + external JARs — built-in Neon theme, Hi-Lo counter AI, 21+3 side bet
- **Distribution:** `jpackage` native installers (MSI/DMG/DEB); optional Steamworks bridge
- **Mobile (WIP):** libGDX `TableScreen`, Android debug APK; iOS not wired yet

## Build & run

Requires **JDK 21** on PATH. Gradle wrapper is included — no separate Gradle install needed.

```bash
# Build + test (desktop modules; Android skipped unless local.properties / ANDROID_HOME set)
gradlew :core:test :swing:test :gdx-desktop:classes

# Run Swing desktop (primary)
gradlew :swing:run

# libGDX desktop preview (optional)
gradlew :gdx-desktop:run

# Native installer (optional, needs -Pjpackage)
gradlew :swing:jpackage -Pjpackage
```

**Android (optional):** Add `local.properties` with `sdk.dir=...` or set `ANDROID_HOME`, then `gradlew :android:assembleDebug`.

**Windows shortcut:** double-click `run_pro.bat` (runs `:swing:run`).

## Project status

| Area | Status |
|------|--------|
| Core engine + tests | Working — JUnit 5, CI on push/PR (Ubuntu, Windows, macOS) |
| Swing desktop | Primary polished build — themes, plugins, achievements, saves |
| libGDX + Android | Foundation landed — basic table UI, debug APK builds |
| iOS / mobile polish | Not started — animated cards, haptics, achievement port |

## Tech stack

- **Java 21** (Swing), **Java 17** (libGDX/Android)
- **Gradle** multi-module: `core`, `swing`, `gdx-core`, `gdx-desktop`, `android`
- **libGDX 1.12.1**, **JUnit 5**, **GitHub Actions**

## Layout

```
core/          Rules engine, settings, saves, achievements (no UI)
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
