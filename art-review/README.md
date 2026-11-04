# Pompi art refresh

Start with [DELIVERY.md](DELIVERY.md) for results, file changes, checks and review links.

Branch: `art-refresh`. App commits remain local; do not push without the user's instruction.

The classic/red-panda idle, rice and coin samples were approved after changing the rice bowl to a deeper blue. The user authorised completing the rest unsupervised. `AUDIT.md` preserves the original findings; `STYLE-GUIDE.md` records the direction, and `PROMPTS.md` plus `generated/**` JSON files record generation inputs. Built-in imagegen was used throughout; ImageMagick/Pillow handle export sizing, registration and review compositing.

## Review images

- `classic-contact-sheet.png`: all 20 classic frames with centre and baseline guides.
- `classic-frames-row.png`: classic frames in one row.
- `food-contact-sheet.png`: all 19 foods.
- `icons-contact-sheet.png`: coin, heart and skull.
- `rooms-contact-sheet.png`: all eight room palettes.
- `launcher-review.png`: circle, rounded-square and monochrome masks.
- `<skin>-contact-sheet.png`: each skin's 20 matching frames.
- `room-checks/`: every frame of each set over every room.
- `animations/`: all five sets side by side, using the unchanged PetSprite frame order and delays.
- `screenshots/`: actual API 37 pet37 emulator captures, including both UI themes.

`generated/` retains high-resolution sources. `registered/` retains PNG export masters. These review/source files are outside Android resources and do not increase APK size. Shipping images are WebP resources.

## Reproduce local checks

The review tools require Python with Pillow (13.0.0 used), plus ImageMagick for the launcher SVG preview. Run from the repo root:

```sh
python art-review/tools/review_art.py
python art-review/tools/room_reviews.py
python art-review/tools/room_overviews.py
python art-review/tools/animation_reviews.py
python art-review/tools/verify_exports.py
```

`export_art.py` re-exports the retained masters by group. `emulator_state.py` creates screenshot-only fixtures in the installed debug app's emulator DataStore; it does not modify app code. Room review composites reproduce the four painted room palettes and broad shapes; star/petal scatter is representative rather than pixel-identical to Kotlin's seeded RNG. Actual room screenshots provide the runtime reference.
