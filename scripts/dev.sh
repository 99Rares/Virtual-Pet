#!/usr/bin/env bash
# Shared entry point for VS Code tasks and terminal development (macOS/Linux/WSL).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
PACKAGE=com.rares.virtualpetpompi

fail() { printf '%s\n' "$*" >&2; exit 1; }
usage() {
    cat <<'HELP'
Usage: bash scripts/dev.sh <command> [arguments]
  doctor             Check Gradle/JDK, SDK, devices and emulators
  build              Build the debug APK (default VS Code build task)
  test               Run JVM unit tests
  lint               Run Android lint
  check              Build, run unit tests and lint
  release            Build the unsigned release APK
  clean              Clean build outputs
  gradle <tasks...>   Run arbitrary Gradle tasks
  devices            List connected devices
  emulators          List installed AVDs
  emulator [avd]     Start an AVD (default: pet37); keep this terminal open
  run [serial]       Build, install and launch on one device
  logcat [serial]    Follow logs for the running app
  steps <counter> [serial]
                     Inject a cumulative debug step counter
Set ANDROID_SERIAL when more than one device is connected.
SDK discovery: ANDROID_HOME, ANDROID_SDK_ROOT, local.properties, standard locations.
HELP
}

sdk() {
    local candidate configured=""
    if [[ -f local.properties ]]; then
        configured="$(sed -n 's/^sdk\.dir[[:space:]]*=[[:space:]]*//p' local.properties | tail -n 1)"
    fi
    for candidate in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" "$configured" \
        "$HOME/Library/Android/sdk" "$HOME/Android/Sdk" \
        /opt/homebrew/share/android-commandlinetools /usr/local/share/android-commandlinetools; do
        if [[ -n "$candidate" && -x "$candidate/platform-tools/adb" ]]; then
            SDK="$candidate"
            export ANDROID_HOME="$SDK" ANDROID_SDK_ROOT="$SDK"
            ADB="$SDK/platform-tools/adb"
            return
        fi
    done
    fail 'Android SDK not found. Set ANDROID_HOME or sdk.dir in local.properties (see README.md).'
}

device() {
    local serial="${1:-${ANDROID_SERIAL:-}}" available count
    if [[ -z "$serial" ]]; then
        available="$("$ADB" devices | awk 'NR>1 && $2=="device" {print $1}')"
        count="$(printf '%s\n' "$available" | awk 'NF {n++} END {print n+0}')"
        [[ "$count" != 0 ]] || fail 'No authorized device. Start the emulator task or connect/authorize your phone.'
        [[ "$count" == 1 ]] || fail 'Multiple devices. Set ANDROID_SERIAL or pass a serial shown by the devices task.'
        serial="$available"
    fi
    [[ "$("$ADB" -s "$serial" get-state 2>/dev/null | tr -d '\r')" == device ]] \
        || fail "Device $serial is offline or unauthorized. Check the devices task."
    DEVICE="$serial"
}

gradle() { sdk; ./gradlew --console=plain "$@"; }
command="${1:-help}"
[[ $# == 0 ]] || shift
case "$command" in
    help|-h|--help) usage ;;
    build) gradle assembleDebug "$@" ;;
    test) gradle testDebugUnitTest "$@" ;;
    lint) gradle lintDebug "$@" ;;
    check) gradle assembleDebug testDebugUnitTest lintDebug "$@" ;;
    release) gradle assembleRelease "$@" ;;
    clean) gradle clean "$@" ;;
    gradle) gradle "$@" ;;
    doctor)
        gradle --version
        sdk
        printf '\nAndroid SDK: %s\n' "$SDK"
        platform_found=false
        for platform in "$SDK"/platforms/*; do
            [[ -f "$platform/source.properties" && -f "$platform/android.jar" ]] || continue
            api="$(sed -n 's/^AndroidVersion.ApiLevel[[:space:]]*=[[:space:]]*//p' "$platform/source.properties" | tr -d '\r[:space:]')"
            if [[ "${api%%.*}" == 37 ]]; then
                printf 'SDK platform: %s\n' "$platform"
                platform_found=true
                break
            fi
        done
        "$platform_found" || fail 'Missing SDK API 37. Install the API 37 platform with sdkmanager.'
        "$ADB" version
        "$ADB" devices -l
        if [[ -x "$SDK/emulator/emulator" ]]; then "$SDK/emulator/emulator" -list-avds; fi
        ;;
    devices) sdk; "$ADB" devices -l ;;
    emulators) sdk; "$SDK/emulator/emulator" -list-avds ;;
    emulator)
        sdk
        [[ -x "$SDK/emulator/emulator" ]] || fail 'Android emulator is not installed in this SDK.'
        exec "$SDK/emulator/emulator" -avd "${1:-pet37}" -no-audio -gpu swiftshader_indirect
        ;;
    run)
        sdk; device "${1:-}"
        gradle assembleDebug
        "$ADB" -s "$DEVICE" install -r app/build/outputs/apk/debug/app-debug.apk
        "$ADB" -s "$DEVICE" shell am start -W -n "$PACKAGE/.MainActivity"
        ;;
    logcat)
        sdk; device "${1:-}"
        pid="$("$ADB" -s "$DEVICE" shell pidof "$PACKAGE" | tr -d '\r' || true)"
        [[ -n "$pid" ]] || fail 'Pompi is not running. Run the app first.'
        exec "$ADB" -s "$DEVICE" logcat --pid="$pid"
        ;;
    steps)
        [[ "${1:-}" =~ ^[0-9]+$ ]] || fail 'Usage: bash scripts/dev.sh steps <cumulative-counter> [serial]'
        counter="$1"; sdk; device "${2:-}"
        "$ADB" -s "$DEVICE" shell am broadcast -n "$PACKAGE/.DebugStepReceiver" --el counter "$counter"
        ;;
    *) usage >&2; fail "Unknown command: $command" ;;
esac
