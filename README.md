# Phase 10 Score Tracker

An ad-free, open source score tracker for the Phase 10 card game. Built for Android with Jetpack Compose and Material 3 Expressive.

---

## Why This Exists

Every Phase 10 score tracker app on the Play Store falls into one of a few categories: riddled with ads that break on an AdGuard network, so basic they're just a notepad with a counter, or missing obvious features like saving your regular players so you don't have to type the same six names every single game. This one does none of that. It works fully offline, has no ads, no tracking, no analytics, and no nonsense — and it actually remembers who you play with.

---

## Features

### Game Management
- **Saved player roster** — add your regular crew once, pick them from the list every game
- **Flexible game setup** — select any combination of saved players, drag to reorder them before the game starts
- **Dealer rotation** — automatically tracks who the dealer is each round, based on the player order set at game start
- **Resume game** — if the app is killed mid-game (RAM cleared, crash, whatever), your game is saved and waiting when you reopen it
- **End game early** — stop the game at any point; the current leader is declared the winner based on highest phase reached, then lowest score as tiebreaker
- **Tied winner support** — if two players finish on the same phase with the same score, both are declared winners and both get the win recorded

### Scoring
- **Cumulative scoring** — enter card values left in each player's hand at the end of every round; the app adds them up
- **Phase tracking** — each player's current phase is tracked automatically; it advances when they complete a phase
- **Smart phase completion** — if a player's score is 0 (went out) or below 50 (completed phase, few cards left), the "Phase Completed" toggle is checked automatically
- **Manual override** — the phase completion toggle can be manually checked for edge cases (e.g. a player completes their phase but foolishly holds wild cards, pushing their score above 50)
- **Card values reference** — tap the ℹ️ button on the round entry screen for a quick reminder of how much each card type is worth (single digits: 5pts, double digits: 10pts, Skip: 15pts, Wild: 25pts)
- **Correct winner logic** — highest phase reached wins; lowest score breaks ties among players on the same phase

### Screens

**Home** — start a new game, resume an in-progress game, or browse the leaderboard

**Game Setup** — pick players from your saved roster, set the phase ruleset, drag cards into play order using the ≡ handle with haptic feedback

**Active Game (Scores tab)** — live scoreboard sorted by highest phase then lowest score. Rank badges for all players. Tap any card to expand the current phase rule. Dealer badge shown inline

**Active Game (By Phase tab)** — players grouped by their current phase, with the phase rule shown as a header above each group

**Round Entry** — full-width score input per player, keyboard-aware layout so fields are never hidden. Phase completion shown as a tappable row below each score field

**Game Results** — winner announcement with animated trophy, tie support, full final standings sorted by phase then score

**Leaderboard** — lifetime stats for every saved player: games played, wins, win percentage. Sorted by win %

**Custom Rules** — create named rule sets with custom phase descriptions. Select them at game setup instead of the official rules. Official rules shown as a reference card

### Adaptive Layout
- **Foldable support** — on the Samsung Galaxy Z Fold 6 (and any wide-screen device), the Active Game screen shows Scores and By Phase side by side simultaneously
- **Seamless transition** — folding and unfolding the phone transitions between single and dual pane layouts automatically
- **Tablet ready** — same dual-pane layout activates on tablets at ≥600dp width

### Design
- **Material 3 Expressive** — built on the latest Material You design system
- **Dynamic color** — app colors are extracted from your wallpaper automatically on Android 12+
- **Themed icon** — monochrome adaptive icon layer means the app icon adopts your wallpaper palette in themed icon mode
- **Dark mode** — full dark theme support, follows system setting
- **Edge to edge** — content renders behind the status and navigation bars properly

---

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose |
| Design system | Material 3 (`material3:1.5.0-alpha19`) |
| Navigation | Compose Navigation |
| Database | Room |
| Reactive state | Kotlin Flow + StateFlow |
| Architecture | MVVM (ViewModel + Repository) |
| Drag reorder | `sh.calvin.reorderable` |
| Adaptive layout | `androidx.compose.material3.adaptive` |
| Build | AGP 9.2.1, Kotlin 2.3.10, KSP 2.3.8 |
| Min SDK | 35 (Android 15) |
| Target SDK | 37 (Android 16) |

---

## Install

[<img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="80">](http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://add/https://github.com/CrsMthw/Phase10-Tracker)

Tapping this button on your Android device will open Obtainium and automatically add the repo — it'll notify you and install new releases automatically from then on.

Or go to the Releases page and download the latest APK manually.

---

## Building

Requirements: Android Studio (latest stable), JDK 17+, Android SDK 37.

```bash
git clone https://gitea.crsmthw.com/cris/phase10tracker.git
cd phase10tracker
# Open in Android Studio and let Gradle sync
# Or build from terminal:
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Official Phase 10 Rules (reference)

| Phase | Rule |
|---|---|
| 1 | 2 sets of 3 |
| 2 | 1 set of 3 + 1 run of 4 |
| 3 | 1 set of 4 + 1 run of 4 |
| 4 | 1 run of 7 |
| 5 | 1 run of 8 |
| 6 | 1 run of 9 |
| 7 | 2 sets of 4 |
| 8 | 7 cards of 1 color |
| 9 | 1 set of 5 + 1 set of 2 |
| 10 | 1 set of 5 + 1 set of 3 |

---

## License

MIT. Do whatever you want with it.

---

*Built with Claude — because the Play Store didn't deserve another ad-infested score tracker.*
