# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

"Virtual Pet Pompi" is a single-module Android app (Java, package `com.rares.virtualpetpompi`) from a bachelor's thesis. The phone's step counter is converted into in-game coins, which are spent in a shop on food and backgrounds for a virtual pet. The pet loses hunger over time and dies if neglected. No README, lint config, Cursor/Copilot rules, or CI exist.

## Build and test

Toolchain (upgraded 2026-11-04): AGP 9.5.0, Gradle 9.8.2, compileSdk 37, targetSdk 37, minSdk 26, Java 8 source level, no Kotlin. Build with **JDK 17** (Corretto 17 is at `~/Library/Java/JavaVirtualMachines/corretto-17.0.11/Contents/Home`); JDK 22 is too new for this Gradle/AGP.

```bash
./gradlew assembleDebug              # build the debug APK
./gradlew installDebug               # install on a connected device or emulator
./gradlew testDebugUnitTest          # JVM unit tests (src/test)
./gradlew testDebugUnitTest --tests "com.rares.virtualpetpompi.ExampleUnitTest"   # one test class
./gradlew connectedDebugAndroidTest  # instrumented tests (src/androidTest), needs a device
```

Environment on the Mac: the Android SDK is at `/opt/homebrew/share/android-commandlinetools` (Homebrew cask; platform 35 and 37.0, build-tools 35.0.0 and 36.0.0). `local.properties` (gitignored) points `sdk.dir` there; recreate it on a fresh checkout. Repositories are `google()` and `mavenCentral()` only (jcenter removed); `eazegraph` still resolves from Central.

`./gradlew lintDebug` passes (0 errors, about 200 warnings, mostly deprecations). Smoke-tested on an API 37 emulator on 2026-11-04 (launches, health foreground service runs with its notification, insets fine, no crashes). Emulator setup: AVD `pet37` (system image `android-37.0;google_apis;arm64-v8a`); run `emulator -avd pet37 -no-audio -gpu swiftshader_indirect` from `$ANDROID_HOME/emulator`, then `./gradlew installDebug`. The emulator has no real step sensor, so the step-to-coin flow is untested end to end.

The only tests are the Android Studio template stubs (`ExampleUnitTest`, `ExampleInstrumentedTest`). There is effectively no test coverage.

## Architecture

Package layout under `app/src/main/java/com/rares/virtualpetpompi/`:
- `activity/`: `MainActivity` (the pet screen, about 600 lines and the real hub), plus `ShopActivity`, `FoodActivity`, `BackgroundActivity`, `StatisticsActivity`, `SettingsActivity`. All are portrait-locked and built with view binding by `findViewById`.
- `repository/`: `CoinRepository`, `HungerRepository`, `FoodRepository`, `BackgroundRepository`. These are thin wrappers over `SharedPreferences` that each activity instantiates itself. They are not a data layer in the Room or ViewModel sense.
- `service/`: `StepsService` (foreground step counter), `DataBase` (SQLite helper), `HungerNotification` (broadcast receiver), `ShutdownReceiver`.
- `model/Food`, `util/Util` (day-boundary helpers), `util/OnSwipeTouchListener`.

### The step to coin to spend pipeline (spans several files)

1. `StepsService` registers a `TYPE_STEP_COUNTER` listener. That sensor reports cumulative steps since device boot, so the service tracks offsets. `onSensorChanged` writes the *steps since the last reset* into `myPrefs["total"]`. The service restarts itself via `AlarmManager` every hour and in `onTaskRemoved`, and runs as a foreground service with an ongoing progress notification.
2. `updateIfNecessary()` persists the raw counter to SQLite (`DataBase.saveCurrentSteps`) and inserts a per-day row through `insertNewDay` when a new day starts. Day rows store a *negative offset* of the counter at midnight, so a day's steps are `counter + offset`. The `pedometer` prefs name, `correctShutdown` flag and the "BootReceiver" comment suggest this logic was adapted from an open-source pedometer app; there is no `BootReceiver` in this repo. `ShutdownReceiver` flushes the counter before reboot because the sensor resets to 0 on boot.
3. `CoinRepository.calculateCoins()` (run in its constructor and after every `removeAmount`) computes `coins = myPrefs["total"] / coins["divide"] + recover["prevCoins"] - coins["spentCoins"]`, clamped at 0. Difficulty sets `divide` (100 for normal, 10 for easy). Coins are mirrored into the SQLite `User` table and `coins["totalCoins"]`.
4. `FoodActivity` and `BackgroundActivity` (the two shop screens, reached from `ShopActivity`) call `CoinRepository.removeAmount()`, then add the item through `FoodRepository` or `BackgroundRepository`.

### Hunger and lives
`HungerRepository.hunger()` runs at construction and subtracts `minutes / 20` from `hunger["hungerLevel"]` (0 to 100), based on a timestamp in the `time` prefs. `MainActivity.manageLifes()` spends one life when hunger hits 0 and refills hunger. At 0 lives the pet is dead and the reset button calls `deleteEverithing()`, which clears every prefs file plus the food, coin and hunger repositories. `HungerNotification` is a manifest-registered receiver that posts a notification on the `hunger` channel. `MainActivity` creates the channel and schedules it with a daily `setRepeating` alarm, guarded by the `firstTimeSetNotification` flag (which `ShutdownReceiver` clears so the alarm is re-armed after a reboot).

### State is split across many stores
Persistent state lives in about 14 named `SharedPreferences` files, plus one SQLite database. Prefs names are string literals scattered across classes: `myPrefs`, `coins`, `recover`, `hunger`, `time`, `food`, `currentValue`, `backgrounds`, `savedLifes`, `firstTime`, `pedometer`. The activities and the service both write them directly (for example, `MainActivity.setDifficulty()` and `deleteEverithing()` edit prefs that the repositories own). When changing any of this, grep for the literal prefs name across the whole module, because there is no single owner for a given key.

`DataBase` (name `stepsData`, version 3) is a singleton `SQLiteOpenHelper` with a manual **reference-counted `close()`**: every `getInstance()` must be paired with a `close()`, and the real close only happens when the counter reaches zero. `onUpgrade` only handles version 1; versions 2 and 3 have no migration path. `DataBase` holds the day-by-day `stepsData` table plus `User`, `achievement` and `hasAchievement` tables (achievements are only minimally used).

### Conventions and quirks to know
- Code comments are in a mix of English and Romanian; identifiers like `stepsPoza` and `deleteEverithing` are legacy spellings. Don't "fix" them unless asked, since prefs keys and resource names depend on strings.
- Resource and drawable names (`a10`, `icon1`, frame-animation XMLs like `idle_anim.xml`) are looked up by name at runtime via `getResources().getIdentifier(...)` for backgrounds and food, so renaming a drawable can break the shop and inventory without a compile error.
- Permissions: `ACTIVITY_RECOGNITION` and `POST_NOTIFICATIONS` are requested at runtime with Dexter in `MainActivity`. `StepsService` is a `health`-type foreground service started with `startForegroundService`. Notification code checks `POST_NOTIFICATIONS` itself before `notify()`.
- Targeting SDK 37 means edge-to-edge is enforced: every activity calls `Util.applySystemBarInsets(this)` after `setContentView` to pad the content for system bars. All PendingIntents use `FLAG_IMMUTABLE`. The hourly save alarm is inexact (`setAndAllowWhileIdle`) so no exact-alarm permission is needed.
