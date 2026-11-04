# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

"Virtual Pet Pompi" is an Android app (Kotlin, Jetpack Compose, package `com.rares.virtualpetpompi`) originally written as a bachelor's thesis in Java and fully rewritten in November 2026. The phone's step counter is turned into coins; coins buy food and rooms; food keeps a virtual panda alive. There is no backend and nothing leaves the device. The old Java/XML implementation is only in git history (before the "Rewrite in Kotlin and Compose" commit); its SharedPreferences and SQLite data are deliberately not migrated.

## Build and test

Gradle (wrapper 9.8.2), AGP 9.5.0 with built-in Kotlin 2.4, KSP for Room, compileSdk/targetSdk 37, minSdk 26. Versions live in `gradle/libs.versions.toml`. **Use JDK 17**: `export JAVA_HOME=~/Library/Java/JavaVirtualMachines/corretto-17.0.11/Contents/Home` (the default JDK 22 is too new for this AGP).

```bash
./gradlew assembleDebug                 # debug APK
./gradlew installDebug                  # install on the running emulator/device
./gradlew testDebugUnitTest             # JVM tests: domain rules (app/src/test)
./gradlew testDebugUnitTest --tests "com.rares.virtualpetpompi.PetRulesTest"   # one class
./gradlew connectedDebugAndroidTest     # instrumented tests on a device/emulator: repository + Room + DataStore
./gradlew lintDebug                     # must stay at 0 errors
./gradlew assembleRelease               # R8-minified, unsigned (about 4 MB)
```

Environment on the Mac: the Android SDK is at `/opt/homebrew/share/android-commandlinetools` (Homebrew cask), referenced by the gitignored `local.properties` (`sdk.dir=...`); recreate it on a fresh checkout. An AVD `pet37` (API 37, arm64) exists: `$ANDROID_HOME/emulator/emulator -avd pet37 -no-audio -gpu swiftshader_indirect`. The emulator has **no step sensor**, so debug builds include `DebugStepReceiver` (in `app/src/debug`):

```bash
adb shell am broadcast -n com.rares.virtualpetpompi/.DebugStepReceiver --el counter 10000   # first call sets the baseline
adb shell am broadcast -n com.rares.virtualpetpompi/.DebugStepReceiver --el counter 18200   # +8200 steps
```

## Architecture

Three layers under `app/src/main/java/com/rares/virtualpetpompi/`, wired by hand (no DI framework): `PetApp` holds an `AppContainer` (repository and `StepCounter`), `MainActivity` creates `PetViewModel` from it.

- `domain/`: pure Kotlin with no Android imports, fully unit tested. `PetRules` (hunger decay, losing lives, feeding), `CoinRules`, `StepRules.delta` (turns the cumulative sensor into new steps and handles reboot resets), `Difficulty`, and the static `Catalog` of foods and rooms. **All game balance numbers live here** (1 hunger point per 20 minutes, coins per step by difficulty, food prices and fullness).
- `data/`: `GameRepository` is the single writer. It combines Room (`StepDay` per calendar day, `PantryItem` counts) and a DataStore (`GameStore`: difficulty, pet hunger/lives, hunger anchor timestamp, total steps earned, spent coins, last sensor counter, owned/selected rooms, daily goal). Every mutation goes through one `Mutex`. Coins are **derived**, never stored: `CoinRules.balance(stepsEarned, difficulty, spentCoins)`. Hunger is stored with an anchor timestamp and aged lazily by `settle()`, so it also advances while the app is closed. The repository takes an injectable clock and zone for tests.
- `ui/`: Compose, single activity, four tabs in a floating glass tab bar (no Navigation library). `AppRoot` owns the permission flow, the haze blur source, and a lifecycle-scoped loop that samples the step sensor and calls `settle()` while the app is on screen. Screens are in `ui/screens`, shared pieces in `ui/components`, tokens in `ui/theme`.
- `work/`: `PetCheckWorker` (WorkManager, every 30 minutes) samples the step counter, settles hunger, and posts one "Pompi is hungry" reminder at 30% hunger (re-armed after feeding). There is no foreground service and no permanent notification: the hardware step counter keeps counting while the app is not running.

Mini-game ("Catch", from the thesis's future work): `domain/MiniGame.kt` has `CatchGame`, a pure simulation in normalised 0..1 coordinates advanced by `step(dt)` (the screen only draws it and feeds drag input), and `MiniGameRules` (bets 5/10/20; 12 catches refunds, 20 pays 1.5x, 30 pays 2x). The bet is taken at `startRound`, so quitting mid-round loses it. Net results are stored as `bonusCoins`, which `CoinRules.balance` adds. **Profit is capped at 30 coins per calendar day** (`DAILY_WIN_CAP`) on purpose, so the game cannot replace walking as the way to earn coins; keep that when changing payouts. `MiniGameScreen` is a full-screen mode that hides the tab bar.

Step pipeline: sensor value, `StepRules.delta` against the stored last counter, then `StepDay` (history and charts) plus `stepsEarned` (coins). The first reading only sets a baseline; steps taken while the app never ran are attributed to the day of the next reading.

## Design system

The UI follows Rares's Liquid Glass design guide in `~/AiMemmory/DESIGN.md`, ported from web to Compose; read it before changing UI. Key points as implemented:
- Glass is only floating chrome (tab bar, stat chips over the room art, toasts): `Modifier.glass()` uses Haze (`dev.chrisbanes.haze:haze-blur`) over a shared `LocalHazeState` source. Content sits on `Modifier.panel()` frosted fills with no blur, so glass never nests in glass. Over the room artwork glass uses a fixed smoky tint so white figures stay legible in both themes.
- Dark is the reference theme; light is supported through `PetColors` tokens, never hard-coded colours in screens.
- Motion: 120ms press scale 0.97 (`Modifier.pressable`), one-time 360ms ease-out arrival with 30ms stagger (`Modifier.materialize`), critically damped spring for the sliding thumb in tab bars and segmented controls, one-time sweep for charts. `LocalReduceMotion` (system animator scale 0) removes travel and scale.
- Typography is the system font with size-specific tracking and tabular figures.

## Things to know

- The pet sprite frames (`idle*`, `hungry*`, `dance*`, `feed*`, `a1..a10` for waking up) and food art are the original assets. Frame sequences and timings are in `PetSprite.kt`. Some food JPGs have opaque white backgrounds, which is why `FoodImage` draws every dish on a white plate.
- Food and room ids (`rice`, `bg1`, ...) are stored in the database, so renaming one orphans owned items.
- Dead pet (`lives == 0`): the overlay offers "Bring back" (`GameRepository.revive()`, costs `Difficulty.reviveCost` coins, restores full hunger and `reviveLives`, keeps pantry, rooms and remaining coins) or "Start over" (`restart()`: clears game state and pantry, keeps step history and the sensor baseline). To see the overlay on the emulator, `adb root` and set the date forward (`adb shell date 112012002026.00`); the pet then starves while the app is closed.
- The launcher icon still uses `@drawable/icon1` directly; lint reports the old mipmap icons as unused and misplaced. An adaptive icon has not been made yet.
- Not verified on real hardware: live step counting with a real sensor, and the hunger notification path through `PetCheckWorker` (tested only by reading the code).
