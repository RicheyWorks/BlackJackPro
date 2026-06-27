# Legacy sources — reference only

The Java files in this folder are the original BlackJackPro codebase from
before the Phase 1 restructure (the audited JavaFX path plus the
self-contained Swing build that preceded the Gradle layout). They use the
unnamed package and are excluded from the Gradle build.

They are kept here for:

- **Reference** when porting features forward (e.g. multiplayer socket code,
  card-counting heat maps, audit logging) that haven't been re-implemented in
  the new architecture yet.
- **Git history continuity** — deleting them outright would make `git blame`
  on older lines harder. They will be removed in a later cleanup PR once
  every piece worth keeping has been ported to
  `src/main/java/com/richeyworks/blackjack/`.

Do not edit these files. They are not compiled and any changes here will be
lost when this folder is eventually deleted. Edit the new sources under
`src/main/java/com/richeyworks/blackjack/` instead.
