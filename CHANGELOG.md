# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [4.2.2] - 2026-06-20

### Added
- **App version on the About screen** — the current version (`BuildConfig.VERSION_NAME`) is shown under
  the app name. Enabled `buildConfig = true` to expose it.

## [4.2.1] - 2026-06-20

### Added
- **Date & time on Game Detail** — opening a finished game from Game History now shows the full date *and*
  time it was played, under the winning score in the header (e.g. "20 Jun 2026, 10:25 PM"). Uses
  `finishedAt`, falling back to `startedAt`; if a game has no stored timestamp the line is omitted (no
  crash). The Game History *list* is unchanged (date only — it already had enough information).

## [4.2.0] - 2026-06-20

### Added
- **Delete a game** — Game Detail has a trash action that, after a confirm dialog and **biometric
  authentication**, removes the game from history and reverses its leaderboard effect (the winner loses
  that win; every participant's games-played drops by one).
- **Delete all history & leaderboard** — a button in About behind **two** confirm dialogs and biometric
  auth: clears all finished games and resets every player's stats. Saved players and any in-progress game
  are kept (the leaderboard then reads empty because it hides players with no games).
- **Biometric authentication** (`androidx.biometric`) for every destructive action — uses
  `BIOMETRIC_STRONG or DEVICE_CREDENTIAL`, so a competitive friend can't quietly erase records; falls back
  to proceeding only if the device has no lock set up. `MainActivity` is now a `FragmentActivity`.

### Changed
- **Deleting a player now preserves game history** — past games keep the player shown as **"Deleted
  Player"** instead of vanishing; only their leaderboard record is removed. This required dropping the
  `game_players → players` foreign key (DB migration 4→5) so deletes no longer cascade away history.
  Player deletion is now biometric-gated too.
- Leaderboard hides players with zero games played.

### Fixed
- **Opaque status bar on the history screens** — Game History, Round History, and the Round editor showed
  a white status strip above the collapsing app bar (and a standout grey collapsed bar). Top app bars now
  use their default window insets so the bar background tints the status bar in every state; applied across
  all screens for consistency.
- **"?" instead of player names** in Round History (list, editor, and the confirm dialog) — the player
  list wasn't being collected, so name lookups always hit the empty initial value.
- **Tall white gap above the keyboard** in the Round editor — fixed the IME padding to consume the
  bottom-bar inset (`consumeWindowInsets` + `imePadding`) so only the extra keyboard space is added.

## [4.1.0] - 2026-06-20

### Added
- **Round History (edit past rounds)** — from the active board, review completed rounds and edit any
  round's scores / phase-completed toggles. Saving recalculates every player's running phase and total
  from scratch (so editing an early round correctly shifts later phases), with a confirmation dialog
  summarizing the new scores. If an edit pushes a player past phase 10 the game ends → winner screen.
- **Game History + Game Detail** — a Home button opens a browsable list of finished games (winner,
  players, date, rounds, phase set, winning score); tapping one shows a read-only detail with final
  standings and a round-by-round breakdown.

### Changed
- **Stricter Submit validation** in Round Entry — Submit is enabled only when every score is filled, every
  score is a non-negative multiple of 5, and exactly one player scored 0 (the player who went out); fields
  that aren't a multiple of 5 show an inline hint. Also fixed the enabled-state not updating reliably as
  scores were typed.
- **Transparent navigation bars with fade scrims on every screen** — removed all opaque/semi-opaque nav
  bars; long lists now fade out under a transparent system nav bar, and floating action buttons sit above
  the bar (the Enter-Round FAB no longer hides behind it).

### Fixed
- **Crash opening Game History** when a finished game had lost all its players (older orphaned games) —
  results computation is now empty-safe and such games are skipped.

## [4.0.0] - 2026-06-20

A ground-up Material 3 Expressive rebuild of the entire UI, plus three long-standing gameplay bug fixes.

### Added
- **Material 3 Expressive UI** throughout — `MaterialExpressiveTheme` + expressive motion, flexible
  collapsing top app bars, `MaterialShapes` cookie/clover avatars & badges, a connected `ButtonGroup`
  Scores/By-Phase toggle, wavy per-player phase-progress indicators, and a spring-in winner hero. (Strictly
  M3 Expressive components — no custom floating pills.)
- **Haptic feedback** app-wide via a semantic vocabulary (`util/Haptics.kt`), with a **Settings → Haptic
  feedback** toggle (DataStore, default on) that silences everything when off.
- **Redesigned two-pane board** for foldables/tablets — two rounded surface cards (Scores | By Phase) with
  bottom fade scrims and expressive spring motion.

### Changed
- Dependencies bumped to the Lyra baseline (Compose BOM 2026.05.01, Material3 1.5.0-alpha21) and added
  `graphics-shapes` for `MaterialShapes`.

### Fixed
- **Winner screen sometimes never appeared after completing phase 10** — results navigation is now driven
  off persisted game state (not a one-shot flag dropped mid-transition), and a finished-but-unseen game is
  re-surfaced on next launch (`resultsSeen`, DB migration 3→4) so the winner reveal can't be lost.
- **A score of exactly 5 didn't auto-complete the phase** — the auto-complete check had a wrong
  string-length guard; any score below the threshold now completes the phase.
- **Single-pane board snapped back to "Scores"** after entering a round — the selected Scores/By-Phase view
  now persists (`rememberSaveable`).

## [3.0.3] - 2026-05-17

Last release before the overhaul (baseline). Earlier history predates this changelog.
