# BlackJack Pro — Security Audit

**Date:** 2026-06-29
**Scope:** Full source tree (`core`, `swing`, `gdx-core`, `gdx-desktop`, `android`) — ~4,400 LOC across 50 Java files, plus Gradle build config and GitHub Actions workflows.
**Type:** Security audit (vulnerabilities, untrusted-input handling, code execution, dependencies, build/CI).

## Verdict

One **High** issue (unauthenticated external plugin loading → arbitrary code execution) and a small number of Medium/Low items. The codebase is, on the whole, sober about input handling: persistence is plain-text with manual parsing (no Java/Jackson deserialization in the live path), no hardcoded secrets, `.gitignore` correctly excludes logs/saves/build output, and CI does not expose secrets to untrusted pull requests. The concerns flagged up front — unsafe Jackson deserialization and a WebSocket observer — do **not** exist in the current source: Jackson is on the classpath but never called, and the WebSocket/file-observer code survives only as stale compiled `.class` files from an older, removed architecture.

## Remediation status (2026-06-29)

The four highest-priority items have been fixed in-tree and compile-verified against JDK 21 (core + swing modules):

- **H1 — Fixed.** External plugin JARs now load only if their SHA-256 is listed in a `trusted.sha256` allow-list in the (per-user) plugins directory. Secure-by-default: no allow-list ⇒ nothing external loads. To trust a plugin, add a line `myplugin.jar=<hex-sha256>` to `<dataDir>/plugins/trusted.sha256`.
- **M1 — Fixed.** Writable state (settings, save, achievements) and the plugins directory now resolve under a fixed per-user data dir (`AppPaths.dataDir()` → `%LOCALAPPDATA%\BlackJackPro`, `~/Library/Application Support/BlackJackPro`, or `$XDG_DATA_HOME`/`~/.local/share/BlackJackPro`) instead of the working directory. Bundled read-only assets (music) still load from `resources/`. *Behavior change:* existing saves under `resources/` are not auto-migrated.
- **M2 — Fixed.** Production shoe RNG switched to `java.security.SecureRandom` (Swing + gdx entry points). Tests keep seeded `java.util.Random` for determinism.
- **L1 — Fixed.** `jackson-databind` removed from `swing/build.gradle.kts` and the bundled `lib/jackson-*.jar` copies deleted.
- **L4 — Fixed.** Stale legacy `bin/` artifacts (26 `.class` files) deleted; `bin/` was already gitignored.
- **L3 — Largely fixed.** Added least-privilege `permissions:` blocks (`contents: read` for CI, `contents: write` for release), a Gradle wrapper-validation step, and `.github/dependabot.yml` (weekly grouped updates for Actions + Gradle). SHA-pinned `actions/checkout` (`34e1148` # v4), `actions/upload-artifact` (`ea165f8` # v4), and the third-party `softprops/action-gh-release` (`3bb1273` # v2.6.2). *Remaining:* `actions/setup-java` and `gradle/actions` stay on `@v4` (their SHAs weren't resolvable at audit time — Dependabot tracks them and they can be pinned later), and `choco install wixtoolset` is still unpinned.
- **L2 — Reviewed; no code change (by design).** The flagged "edit `achievements.txt` → push to Steam" vector does not actually exist: `AchievementService.load()` restores saved state via `restore()` and never fires unlock listeners, and `SteamBridge`'s listener is attached only *after* load — so Steam achievements are granted exclusively on genuine in-session unlocks. Plain-text, editable local saves are inherent to an offline single-player game; a keyed HMAC would ship its key in the binary and be trivially bypassable, so it is not worth adding. Revisit only if a server-authoritative or wagered mode is introduced.

## Findings at a glance

| ID | Severity | Finding | Location |
|----|----------|---------|----------|
| H1 | **High** | External plugin JARs loaded and executed with no signature/allow-list/sandbox | `swing/.../plugin/PluginRegistry.java:43-60` |
| M1 | **Medium** | All file & plugin I/O uses working-directory-relative paths | `swing/.../ui/swing/BlackJackProApp.java:650,663` |
| M2 | **Medium** | Shoe shuffle uses predictable `java.util.Random`, not `SecureRandom` | `core/.../engine/Shoe.java:40`; `BlackJackProApp.java:670` |
| L1 | **Low** | `jackson-databind` 2.17.2 declared + bundled but entirely unused | `swing/build.gradle.kts:22`; `lib/jackson-*.jar` |
| L2 | **Low** | Save/achievement files have no integrity protection (Steam-achievement spoofing) | `core/.../persist/SaveManager.java`; `SteamBridge.java:75-77` |
| L3 | **Low** | Build/CI supply-chain hardening gaps (wrapper validation, action pinning) | `.github/workflows/*.yml`; `gradle/wrapper/` |
| L4 | **Low** | Stale legacy compiled artifacts in `bin/` (old networked `Deck`/`BlackJackGUI`) | `bin/*.class` |

---

## H1 — Unauthenticated external plugin loading → arbitrary code execution

**Severity: High** · `swing/src/main/java/com/richeyworks/blackjack/plugin/PluginRegistry.java:43-60`

At startup the app scans a `plugins/` directory, loads **every** `*.jar` it finds through a `URLClassLoader` whose parent is the application classloader, then instantiates and runs the discovered providers (`ServiceLoader` → `p.onLoad()`).

```java
try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
    List<URL> urls = new ArrayList<>();
    for (Path jar : stream) urls.add(jar.toUri().toURL());
    URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]),
            BlackJackPlugin.class.getClassLoader());
    for (BlackJackPlugin p : ServiceLoader.load(BlackJackPlugin.class, cl)) { ... }
```
…driven from `BlackJackProApp.launch()`:
```java
plugins.loadAll(Paths.get("plugins"));   // line 663
```

There is no code signing, no checksum/allow-list, no manifest verification, and no sandbox or restricted `ProtectionDomain`. Any JAR placed in the directory executes with the full privileges of the user running the game. Because the path is **relative** (resolved against the process working directory — see M1), an attacker who can write a file into that directory, or who can convince a user to drop in a "theme pack" / "mod," achieves silent code execution and persistence.

Plugin systems legitimately run third-party code, so some trust is inherent — the problem is the *complete absence of consent or verification* combined with auto-loading from a writable, relative location.

**Recommendation**
- Load plugins only from a fixed, user-scoped trusted directory (e.g. `%LOCALAPPDATA%/BlackJackPro/plugins`), never a CWD-relative path.
- Require explicit user opt-in per plugin (the existing `PluginManagerDialog` is a natural home for an enable/disable + "trust this plugin" toggle).
- Verify a signature or pinned SHA-256 against an allow-list before adding a JAR's URL to the classloader.
- Consider loading plugins with a restricted permission set rather than the full application classloader as parent.

## M1 — Working-directory-relative paths for all I/O

**Severity: Medium** · `swing/src/main/java/com/richeyworks/blackjack/ui/swing/BlackJackProApp.java:650,663`

```java
Path resourceRoot = Paths.get("resources");          // settings, save.txt, achievements, music
plugins.loadAll(Paths.get("plugins"));               // external JARs (see H1)
```

Every persistent file and the plugin directory are resolved against the current working directory. Consequences:
- **Enables H1**: the trust boundary of the plugin loader is "whoever controls the CWD," which for a packaged desktop app is often a user-writable install directory.
- **Tamper / portability**: save, settings, and achievements are written *inside the install/resource tree* rather than a per-user data directory — this breaks on read-only installs, leaks or collides between accounts on a shared machine, and makes state trivially locatable for tampering (see L2).

**Recommendation** Resolve all runtime data against a fixed per-user application-data directory derived from `System.getProperty("user.home")` / OS conventions, computed once at startup. Treat bundled read-only assets (cards, sounds) separately from writable state (saves, settings, plugins).

## M2 — Predictable RNG for the shoe shuffle

**Severity: Medium** *(effectively Low while the game is offline single-player; raise before any networked, wagered, or leaderboard play)* · `core/src/main/java/com/richeyworks/blackjack/engine/Shoe.java:40`

```java
Collections.shuffle(cards, rng);   // rng is a java.util.Random
```
Production wiring seeds it non-deterministically but weakly:
```java
Engine engine = new Engine(1000, new Random());   // BlackJackProApp.java:670
```

`java.util.Random` is a 48-bit linear congruential generator. Its internal state can be reconstructed from a modest run of observed outputs, after which the remaining shoe order is fully predictable. For the current offline practice game this is not meaningfully exploitable, but the project ships a Hi-Lo counter and Steam achievement hooks and is framed as "Pro" — i.e. it is heading toward competitive/online use, where a predictable shuffle is a cheating vector and undermines the integrity of counting practice.

**Recommendation** Use `java.security.SecureRandom` for the shoe (the `Shoe`/`Engine` constructors already accept an injected `Random`, so this is a one-line wiring change in production; tests can keep a seeded `Random` for determinism).

## L1 — `jackson-databind` declared and bundled but never used

**Severity: Low** · `swing/build.gradle.kts:22`, `lib/jackson-{databind,core,annotations}-2.17.2.jar`

`jackson-databind:2.17.2` is both declared as a Gradle dependency and committed as three jars under `lib/`, yet there is **no `com.fasterxml` / Jackson import anywhere in the source**. jackson-databind is the most notorious Java deserialization-gadget library; shipping it unused puts gadget-chain surface on the runtime classpath (also reachable by any loaded plugin) for zero benefit. The dual provisioning (Gradle coordinate *and* hand-dropped jars in `lib/`) is also an ambiguity about what is actually on the classpath.

The pinned 2.17.2 is several releases behind the current 2.18.x LTS / 2.22.0 line, and 2025 advisories affect that lineage (e.g. **CVE-2025-52999**, a parser-depth DoS in the jackson-core/databind line).

**Recommendation** Remove the dependency and the `lib/*.jar` copies entirely. If a future feature needs JSON, re-add a current patched release via Gradle only, and never enable polymorphic default typing on untrusted input.

## L2 — No integrity protection on save / achievement files

**Severity: Low** · `core/.../persist/SaveManager.java`, `core/.../achievement/AchievementService.java`, `swing/.../steam/SteamBridge.java:75-77`

Bankroll, stats, and achievements persist as plain, unauthenticated text in a user-writable location, so they are trivially hand-editable. For an offline single-player game this is normal and the parsing itself is safe (manual `Integer.parseInt` / `Properties`, no deserialization). The one real abuse path: `SteamBridge.wire()` forwards locally-unlocked achievements to the user's Steam profile, so an edited `achievements.txt` could push unearned Steam achievements.

**Recommendation** Low priority for offline play. If Steam integration ships, gate Steam achievement grants on server/engine-verified events rather than the local file, or add an HMAC over the save data keyed to the install.

## L3 — Build & CI supply-chain hardening gaps

**Severity: Low** · `.github/workflows/ci.yml`, `release.yml`, `gradle/wrapper/`

CI is reasonably safe — it uses `pull_request` (not `pull_request_target`) and exposes `GITHUB_TOKEN` only in `release.yml` on tag pushes, so untrusted PRs cannot exfiltrate secrets. Remaining hardening items:
- `gradle/wrapper/gradle-wrapper.jar` is committed (normal) but its checksum is never validated in CI. A tampered wrapper jar executes during every build. Add `gradle/actions/wrapper-validation` (or the standalone `gradle/wrapper-validation-action`).
- Actions are pinned to mutable major tags (`actions/checkout@v4`, `softprops/action-gh-release@v2`, etc.). Pin to full commit SHAs to defend against a moved/compromised tag.
- `release.yml` runs `choco install wixtoolset` unpinned at release time — pin the package version.

## L4 — Stale legacy compiled artifacts in `bin/`

**Severity: Low / Informational** · `bin/*.class`

`bin/` contains compiled classes from a removed JavaFX-era architecture (`Deck`, `BlackJackGUI`, and the `Deck$WebSocketObserver` / `Deck$FileObserver` / `CardParser` inner classes seen in `resources/logs/errors_log.txt`). These are **not** tracked by git (`.gitignore` excludes `bin/` and `*.class`) and are not built by Gradle, so they are not a repository or release risk today — but they are unreviewed code carrying networking/file-observer logic and should not linger in a tree that gets packaged.

**Recommendation** Delete `bin/` (and confirm the packaging/`jpackage` step never globs it). The networked legacy `Deck` is out of scope for this audit since its source is no longer present.

---

## What's already done right

- No hardcoded secrets, keys, or tokens. `GITHUB_TOKEN` is referenced via Actions secrets; the Android keystore is read from environment variables (and currently commented out).
- Persistence parsing is defensive: `SaveManager` and `AchievementService` parse line-by-line with `Integer.parseInt` guarded by try/catch; `GameSettings` uses `java.util.Properties`. No `ObjectInputStream`, no Jackson `readValue`, no default typing — none of the classic Java deserialization sinks are present.
- `SteamBridge` reflection targets fixed, known class names (`com.codedisaster.steamworks.*`), not attacker-controlled strings.
- `.gitignore` correctly excludes logs, saves, `*.class`, `build/`, `bin/`, and `local.properties`; `security_log.txt` is empty and no PII/paths/IPs were found in local logs.
- CI avoids the classic "pwn-request" pattern (no `pull_request_target`, no secrets on untrusted PRs).

---

## Prioritized fix list

1. **[High · H1] Lock down plugin loading.** Move to a fixed user-scoped trusted directory, require per-plugin opt-in, and verify a signature/SHA allow-list before loading. *Effort: M.*
2. **[Medium · M1] Resolve all I/O against a fixed per-user data dir**, not the working directory. Largely neutralizes the H1 planting vector and fixes save portability. *Effort: S–M.* (Do alongside #1.)
3. **[Medium · M2] Switch the production shoe to `SecureRandom`.** Keep seeded `Random` in tests. *Effort: S (one-line wiring).*
4. **[Low · L1] Remove the unused `jackson-databind` dependency and `lib/*.jar` copies.** *Effort: S.*
5. **[Low · L3] Add Gradle wrapper validation to CI and pin actions to commit SHAs;** pin the choco package version. *Effort: S.*
6. **[Low · L4] Delete `bin/`** and confirm packaging never includes it. *Effort: S.*
7. **[Low · L2] Defer save/achievement integrity** unless/until Steam integration ships; then verify grants server/engine-side or HMAC the save. *Effort: M.*

## Suggested verification commands

```bash
# Dependency advisories (after pruning L1)
./gradlew dependencies
# OWASP dependency-check or equivalent SCA scan
# Confirm nothing reads from a relative path post-fix:
grep -rn 'Paths.get("' --include=*.java
# Confirm SecureRandom is wired in production:
grep -rn 'new Random()\|SecureRandom' --include=*.java
```

---
*Audit limited to static review of the source tree, build files, and CI workflows at commit state of 2026-06-29. No dynamic analysis, fuzzing, or third-party dependency CVE scan was executed; run an SCA tool (e.g. OWASP dependency-check) as a follow-up.*
