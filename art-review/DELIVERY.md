# Pompi art refresh delivery

Completed 2026-11-04 on mac. Branch: `art-refresh`. All app commits are local; no app branch push.

## Direction and scope

Soft sculpted storybook forms, warm ivory and charcoal, satin shading and upper-left light match the glass UI. The rice bowl is muted cobalt blue for contrast. Subjects have real alpha transparency; rooms remain intentionally opaque. Quiet lower room areas keep the pet and controls readable.

- Replaced all 20 classic frames, 19 foods, 3 UI icons and 4 room bitmaps with WebP, retaining resource IDs.
- Added `classic_preview` and 4 complete skins: 20 matching frames plus preview each. Names, descriptions and suggested prices are in [skins.md](../skins.md).
- Added safe-zone adaptive foreground/background and a themed monochrome layer, updated both launcher mipmaps and manifest, removed unused legacy launcher assets.
- The only Kotlin change is `FoodImage` in Common.kt: transparent food now sits directly on its surrounding surface. Frame sequences, delays, game logic, layout and room IDs remain unchanged.
- [Exact replaced, deleted and added app files](verification/app-file-changes.txt). High-resolution sources, per-asset prompts and registered PNG masters are retained under `generated/` and `registered/`, outside APK resources.

## Quick visual review

- [Classic frames](classic-contact-sheet.png), [single row](classic-frames-row.png), [food](food-contact-sheet.png), [UI icons](icons-contact-sheet.png), [rooms](rooms-contact-sheet.png), [launcher masks](launcher-review.png).
- [Skin lineup on dark and pale backdrops](skin-lineup.png).
- [Red Panda frames](red_panda-contact-sheet.png), [Polar Cub frames](polar_bear-contact-sheet.png), [Space Pompi frames](astronaut-contact-sheet.png), [Cozy Pompi frames](cozy-contact-sheet.png).
- Eight-room checks: [Red Panda](red_panda-rooms-overview.png), [Polar Cub](polar_bear-rooms-overview.png), [Space Pompi](astronaut-rooms-overview.png), [Cozy Pompi](cozy-rooms-overview.png). Full-size 20-frame sheets for every set/room are under `room-checks/`.
- [Required dark/light emulator screenshots](screenshots-contact-sheet.png) and [all eight actual room screens](rooms-runtime.png). Individual PNGs are in `screenshots/`.
- Animated WebP previews under `animations/`: idle, hungry, dance, feed and wake, with all five sets side by side and original playback order/delays.

## Verification

- Corretto JDK 17.0.11: `./gradlew clean assembleDebug testDebugUnitTest lintDebug` passed. [Build log](verification/gradle.log), [summary](verification/build-checks.json), [lint XML](verification/lint-results-debug.xml).
- 38 unit tests passed, 0 failures/errors/skips. Lint: 0 errors, 241 warnings, 1 hint. Warnings include 85 intentional unused previews/skin resources, 132 IconLocation notices for images in drawable, and 5 intentional duplicate preview/final-wake images; other findings remain outside the authorized code scope. No warning suppression was added.
- [Export checks](verification/asset-checks.json): 132/132 raster resources present, no duplicate names/JPEGs, expected dimensions and alpha, all pet baselines at y=590. Each skin image is below 100 KB (largest 76,064 bytes); paid skins total 3,665,066 bytes. All shipping rasters total 5,386,938 bytes. Other images are below 150 KB.
- API 37 `pet37`: installed and ran the final debug APK. Captured idle, hungry, pantry, food shop, rooms shop and pinned home-screen launcher icon in both themes; inspected all eight rooms. Debug step injection produced 8,200 displayed steps. Screenshot-only DataStore fixtures set hunger/owned rooms; pantry items were bought through the UI.
- Reviewed skin contact sheets for corresponding poses, foot alignment, clipping and transparency; reviewed every pose over all eight room palettes. The final wake frame equals idle1 for a seamless return. PNG masters retain the export registration.

## Observations and limits

An initial incremental build retained a stale removed launcher-foreground reference; a clean build resolved it. The failed log is retained as `verification/gradle-incremental-failure.log`.

Pixel Launcher briefly stopped responding during shortcut placement at the higher emulator resolution. Restarting it and using 720×1280 resolved the issue; actual pinned-icon screenshots were then captured. Diagnostics are retained in `verification/emulator-last-anr.txt` and `emulator-crash-buffer.txt`. No Pompi crash was observed.

Skins are intentionally not wired into Kotlin. Their review uses compositing, as requested. Painted-room composites reproduce palettes and broad shapes; seeded star/petal scatter is representative rather than pixel-identical. Actual room screenshots provide the runtime reference. The emulator has no physical step sensor; real-device sensor and reminder checks remain outside this art refresh.
