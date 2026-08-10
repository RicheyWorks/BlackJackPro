# Desktop release checklist (v0.3.0)

BlackJack Pro ships as an **offline, play-money** desktop game. This is the
path to a tagged GitHub Release with native installers — not the real-money
platform (see `docs/architecture/ADR-0001-real-money-crypto-platform.md`).

## Pre-tag

- [ ] `version` in root `build.gradle.kts` is a non-SNAPSHOT (currently `0.3.0`)
- [ ] CI green on `main` (Ubuntu / Windows / macOS)
- [ ] `./gradlew :core:test :swing:test :platform:test :gdx-core:test` locally
- [ ] Optional soak: `GameSimulator` for a few million rounds
- [ ] No personal editor files (`*.geany`) or player-state under `swing/resources/`
- [ ] Changelog / release notes drafted

## Tag & publish

```bash
git tag -a v0.3.0 -m "BlackJack Pro 0.3.0"
git push origin v0.3.0
```

GitHub Actions `release.yml` builds MSI / DMG / DEB and attaches them to the
release for the tag.

## Smoke after assets land

| OS | Installer | Check |
|----|-----------|--------|
| Linux | `blackjackpro_0.3.0_amd64.deb` | Install → launch → bet → deal → stand → quit → save reloads |
| Windows | `BlackJackPro-*.msi` | Same; WiX must have been available in CI |
| macOS | `BlackJackPro-*.dmg` | Same |

Optional: drop `.wav` tracks into `resources/music/` before packaging so the
installer ships music (gitignored by default).

## Not in this release

- Android store APK (debug path only; signing commented)
- iOS
- Real-money / crypto online play
- Steamworks (optional bridge only)

## Rollback

Desktop is a self-contained installer with per-user saves under the OS data
dir. Reinstall the previous tag; `SaveManager` ignores unknown keys and clamps
negatives. No server migrations.
