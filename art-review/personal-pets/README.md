# Pupsy and Kira skins

Two complete skins are integrated into the existing shop and animation catalog: Pupsy (140 coins) and Kira (160 coins). Each includes 20 animation frames and a shop preview. Kira uses the exact original idle image selected by the user; later face studies are retained only as alternatives.

## Review

- [Side-by-side preview](lineup.png)
- [Pupsy frames](pupsy-contact-sheet.png) and [Kira frames](kira-contact-sheet.png)
- [Pupsy across eight rooms](pupsy-rooms-overview.png) and [Kira across eight rooms](kira-rooms-overview.png)
- Animated dark/pale comparisons: [idle](idle.webp), [hungry](hungry.webp), [dance](dance.webp), [feed](feed.webp), [wake](wake.webp)
- [Runtime checks](runtime-checks.json): both purchased and equipped, dance and Catch avatars observed; Kira selection persisted across a cold restart. Original emulator preferences were restored byte-for-byte and display overrides reset.
- Actual emulator captures are in [screenshots](screenshots/). Room sheets are composited art checks; screenshots show the running app.
- [Design selection](SELECTION.md)

## Implementation and checks

`Skins.kt` adds stable ids `pupsy` and `kira`; `strings.xml` adds their names and descriptions. Existing ownership, purchase, selection, PetSprite, and Catch behavior uses these catalog entries. Playback order and timing remain unchanged. [integration.patch](integration.patch) isolates the additions to these shared files.

- Debug app and instrumentation APK build passed.
- 43 unit tests and 15 instrumented tests passed.
- Lint: zero errors, 201 warnings and one hint retained.
- All 42 new WebPs pass alpha, 600 × 650 canvas, baseline, foot alignment, margins, per-file budget, and final-wake/idle/preview equality checks.
- New exports total 2,085,328 bytes; largest file 66,228 bytes. All six paid skin sets total 5,750,394 bytes.
- Selected Kira source SHA-256: `a654cb63383a0e37b15b9512c96550fa1ab7755a9fb19091b1793aa210deb361`.
- [Build results](build-checks.json), [asset results](asset-checks.json), [preserved-work check](pre-existing-work-check.json), [Gradle log](gradle.log), [instrumentation log](instrumentation.log).

## Assets and prompts

Production used built-in `image_gen`, with one generated image per pose, followed by registration and WebP export. Original sources and the exact prompt for each call are saved together under [Pupsy sources](../generated/skins/pupsy/) and [Kira sources](../generated/skins/kira/). Registered PNG masters are under `art-review/registered/skins/{pupsy,kira}/`; shipping assets are `app/src/main/res/drawable/{pupsy,kira}_*.webp`. The selected original Kira PNG is preserved byte-for-byte.

Rebuild review artifacts with a Pillow-enabled Python environment:

```sh
python art-review/tools/personal_pet_review.py --export
```

The emulator fixture utility is restricted to `emulator-5554` and refuses non-emulator devices. Runtime review uses temporary game preferences; the original preferences are restored afterward. No physical phone was changed. The emulator briefly showed a system-process unresponsive dialog after resizing; choosing Wait recovered it.

Existing staged and unstaged resource, database, and balance changes were preserved. Application changes are uncommitted and have not been pushed.
