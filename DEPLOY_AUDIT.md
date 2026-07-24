# Deploy & Playability Audit

**Scope:** is BlackJack Pro ready to deploy and playable, end to end — build, test, package, install, launch, play, save, quit.
**Method:** every claim below was *executed*, not inspected. The Swing build was launched from its real distribution zip on a virtual display and played by scripted mouse input: bet placed, hand dealt, hit, stand, win paid, theme switched live, gallery opened, quit through the menu, save file inspected afterward. The installer was actually produced and its metadata verified.
**Date:** 2026-07-24.

## Verdict

**Deployable and playable on desktop, with two defects found and fixed during the audit** — one of which (the installer build being broken) would have been discovered by the first person to follow the README's packaging instructions. The mobile port builds and its logic is tested, but its GL rendering path cannot be verified without real hardware and remains the one genuinely unverified surface.

## What was verified by doing it

| Check | Result |
|---|---|
| Full build, all modules | ✅ `gradle build` green, distributions produced |
| Test suite | ✅ 286 tests green (core 184, swing 37, platform 41, gdx-core 24) |
| Simulator soak | ✅ 52k rounds this audit (4M in an earlier session), full coverage, all 13 casts, house edge 0.306% over 2M book-play rounds |
| Launch from distribution zip | ✅ window renders, HUD correct, buttons enabled/disabled correctly |
| Scripted play-through | ✅ $25 bet debited → deal → hit → stand → win paid +$25 → bankroll $1,025 |
| Hole card discipline | ✅ hidden during play ("showing 9"), revealed at settlement ("Dealer (19)") |
| Hi-Lo counter HUD | ✅ updates as cards land |
| Live theme switch | ✅ Pirate Cove applied mid-session: felt, motif, cards, cast all swap; bankroll untouched |
| Theme Gallery | ✅ 24 live miniatures with crew badges, scroll, click-to-apply |
| Quit via Game menu | ✅ process exits cleanly |
| Save round-trip | ✅ `save.txt` bankroll/stats exactly match the played hand; `themeId=pirate` persisted; per-user data dir (XDG/LOCALAPPDATA), not CWD |
| Native installer | ✅ **after fix** — `blackjackpro_0.3.0_amd64.deb` (33 MB, bundled jlink runtime) built and verified with `dpkg-deb` |
| Packaged binary | ✅ the jpackage app-image launches and plays with its own runtime, no system JDK |
| Fresh-profile first run | ✅ clean data dir → new $1,000 session, no errors |
| CI workflow | ✅ pinned actions, wrapper validation, 3-OS matrix, least-privilege token, report upload |

## Defects found and fixed in this audit

**A-1 (release-blocking): `:swing:jpackage` had never worked.** The packaging
script was loaded with `apply(from = "jpackage.gradle.kts")`, but Gradle does
not honour a `plugins {}` block inside an applied script — so the Badass
Runtime plugin never loaded and the task failed on `Unresolved reference:
runtime` before doing anything. The README's installer instructions were
untestable as written. Fixed by declaring the plugin in
`swing/build.gradle.kts` and moving the configuration inline;
`jpackage.gradle.kts` is deleted. Verified by building and inspecting a real
DEB and launching its app-image. (Windows MSI additionally needs the WiX
Toolset installed; macOS DMG needs a Mac. Neither could be exercised here.)

**A-2 (minor): Escape didn't close the Theme Gallery.** Found when the
scripted play-through tried to dismiss it. Fixed with a root-pane key binding.

## Open items before you'd call it a release

1. **Version is `0.3.0-SNAPSHOT`.** Fine for development; a tagged release
   should drop `-SNAPSHOT` (the installer already strips it, so the DEB says
   0.3.0 — the jar manifests disagree with it).
2. **Repo hygiene.** `geany_run.bat` points at an old class project via a
   hardcoded personal path and should go. `BlackJack.geany` is a personal
   editor file (add to `.gitignore`). `swing/resources/save.txt`,
   `settings.properties`, and `achievements.txt` are committed player-state
   from the old CWD-relative save era; the game no longer reads them.
   `_to_delete/` should be emptied before pushing.
3. **libGDX desktop/Android rendering unverified.** `gdx-core` logic is tested
   and `gdx-desktop` compiles and packages, but GLFW needs a real display and
   GPU: it cannot be exercised in this container (it fails at `glfwInit`, as
   expected headless). One manual launch on real hardware — plus one Android
   `assembleDebug` install — is the remaining smoke test. The README already
   marks mobile as WIP.
4. **Windows/macOS installers untested** (Linux DEB proven; the tooling is now
   correct, but MSI/DMG need their native OSes — CI could grow a packaging
   job per OS if releases become regular).

## Rollback plan

Desktop is a self-contained installer with per-user saves: rolling back is
reinstalling the previous build; saves are forward-compatible (`SaveManager`
ignores unknown keys, clamps negatives). No server, no migrations.
