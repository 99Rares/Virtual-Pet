# Virtual Pet Pompi

An Android virtual pet built with Kotlin and Jetpack Compose. Walk to earn coins, feed Pompi, decorate its room, collect skins and play Catch.

## Open in VS Code

Open this repository's root folder, then install the recommended **Kotlin by JetBrains** and **XML by Red Hat** extensions when prompted. Open a `.kt` file and let Kotlin import the Gradle project. If an older Kotlin extension is enabled, disable it **for this workspace** to avoid competing language services.

The shared `.vscode` configuration includes Kotlin formatting, build diagnostics in the Problems panel, reduced file watching for generated assets, and Android development tasks. Microsoft's Java Gradle importer is disabled for this workspace so Kotlin's importer owns the Android project.

### Local prerequisites

- JDK **17** installed. Gradle's checked-in daemon criteria select it even when the editor's default Java is newer; no machine-specific JDK path is committed.
- Android SDK with platform API **37**, platform-tools, accepted SDK licenses, and an emulator or USB-debuggable device.
- Bash for the development tasks: macOS, Linux, or VS Code connected to a WSL environment with its own Linux SDK/JDK. Native Windows PowerShell tasks are not provided; `gradlew.bat` is available for direct Gradle commands.

Point `ANDROID_HOME` at the SDK, or create the ignored `local.properties` file:

```properties
sdk.dir=/absolute/path/to/Android/sdk
```

The task runner also discovers standard macOS/Linux SDK locations and Homebrew's SDK. On this Mac the SDK is `/opt/homebrew/share/android-commandlinetools`, and the existing AVD is `pet37`. The SDK package names for this installation are `platforms;android-37.0` and `system-images;android-37.0;google_apis;arm64-v8a`.

Run **Terminal → Run Task → Android: Check setup** first. It prints the Gradle JVM criteria, SDK platform, device status and installed AVDs. To install missing SDK packages or create another emulator, use Android's [command-line tools](https://developer.android.com/tools) and [AVD manager](https://developer.android.com/tools/avdmanager).

## Daily workflow

| Action | VS Code task / shortcut |
| --- | --- |
| Check local tools | **Android: Check setup** |
| Build debug APK | **Cmd+Shift+B** / **Ctrl+Shift+B** |
| Start `pet37` or another AVD | **Android: Start emulator** (leave its terminal running) |
| List devices or AVDs | **Android: Devices** / **Android: List emulators** |
| Build, install and open Pompi | **Android: Run app**, or select **Run Pompi on Android** and press **F5** |
| Run JVM unit tests | **Tasks: Run Test Task** / **Android: Unit tests** |
| Run Android lint | **Android: Lint** |
| Build + unit tests + lint | **Android: Verify all** |
| Run device/instrumented tests | Terminal: `bash scripts/dev.sh gradle connectedDebugAndroidTest` |
| Follow the app's logs | **Android: App logs** (Ctrl+C to stop) |
| Build an unsigned release APK | **Android: Build release** |

Wait for the emulator to finish booting before running the app. With multiple devices, set `ANDROID_SERIAL` in the editor's environment or use `bash scripts/dev.sh run <serial>` in the terminal. The runner installs on exactly one device and retains its app data.

F5 is a **build/install/launch shortcut**, using VS Code's built-in terminal launcher. It does not attach an Android breakpoint debugger. JetBrains' [Kotlin language server](https://kotlinlang.org/docs/kotlin-lsp.html) currently has experimental Android Gradle support. Use Android Studio for Compose Preview, Layout Inspector, profiling and full Android debugging; the same source and Gradle wrapper work in either editor.

### Terminal equivalents

```bash
bash scripts/dev.sh doctor
bash scripts/dev.sh build
bash scripts/dev.sh test
bash scripts/dev.sh lint
bash scripts/dev.sh check
bash scripts/dev.sh emulator pet37
bash scripts/dev.sh run
bash scripts/dev.sh logcat
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Unit-test report: `app/build/reports/tests/testDebugUnitTest/index.html`. Lint report: `app/build/reports/lint-results-debug.html`.

### Test walking on the emulator

The emulator has no physical step counter. Debug builds provide a receiver for **cumulative** readings:

```bash
bash scripts/dev.sh steps 10000  # first reading establishes the baseline
bash scripts/dev.sh steps 18200  # next reading adds 8,200 steps
```

The **Android: Inject step counter** task prompts for the same value. Continue increasing the counter for subsequent readings. This modifies the selected debug device's game state; it is not a real step-sensor test.

## Troubleshooting

- **Kotlin import is stale:** run **IntelliJ: Reload Workspace** from the Command Palette. Check the **Kotlin by JetBrains — Build** output channel for import errors. The build includes Android resources in unit tests because the importer realizes Compose preview tasks that require them.
- **No matching Java 17 installation:** install JDK 17, then retry. `./gradlew --version` shows the daemon criteria; the launcher JVM may still show your newer default Java, which is expected.
- **SDK missing:** set `ANDROID_HOME` or `sdk.dir`, install API 37 and platform-tools, and run the setup check again. A fresh SDK may need `sdkmanager --licenses` before Gradle can install required build tools.
- **No device / unauthorized / multiple devices:** run the Devices task; finish booting the emulator or accept USB debugging on the phone. Pass a serial or set `ANDROID_SERIAL` when necessary.
- **Lint finds warnings:** inspect the report. Duplicate artwork previews and raster placement can produce warnings; no lint suppressions are introduced by this setup.

## Project layout

- `app/src/main/java/com/rares/virtualpetpompi/`: domain rules, persistence, Compose UI and background work.
- `app/src/test/`: JVM unit tests; `app/src/androidTest/`: device tests.
- `gradle/libs.versions.toml`: dependency/plugin versions.
- `scripts/dev.sh`: the same development commands used by VS Code tasks.
- [skins.md](skins.md): skin catalog and artwork contract.
- [art-review/](art-review/README.md): art sources, contact sheets and verification history.

Device data stays local. Real-device step counting and hunger notifications still need hardware verification.
