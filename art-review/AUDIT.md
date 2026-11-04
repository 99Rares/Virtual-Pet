# Original art audit — 2026-11-04

Scope: inspected all 47 raster files through labelled contact sheets, plus both drawable XML files as source. Read PetSprite.kt, RoomBackground.kt, Common.kt, strings and manifest. Original assets remain unchanged. Dimensions, byte sizes and bounds are in originals/inventory.json. Bounds for opaque images describe colour extent, not alpha.

## Pet (20 frames)

- idle1–3: recognisable, charming round panda; irregular hand-drawn contour, almost pink whites and solid black limbs. Head contour, ear shape and hands change between frames. No dimensional light or edge separation on black backgrounds. Idle alpha bottom varies from y=550 to 556 (exclusive bounds); redraw drift accompanies the intended breathing.
- hungry1–2: subdued eyes, but silhouette changes at the ears/head rather than communicating hunger through a controlled droop. Bottom y=549 in both; not aligned with idle's y=556 reference.
- dance1–3: broad standing pose, but 522×500 bounds versus idle1's 438×421; bottom y=613, substantially lower than idle. This causes a visible size/placement jump when changing clips. Preserve the expressive standing pose while fixing foot baseline and head scale.
- feed1–2: cheerful closed eyes and small arm changes; eating action is hard to read without a visible mouth/cheek beat. Baseline already consistent at y=556. Preserve two frames.
- a1–10: sleep → rise → stretch → sit is legible. Head, ears and torso are redrawn inconsistently, with horizontal shifts and bottom bounds y=502–574. Some height change is intentional, but the contact sheet shows registration drift beyond that. Anchor ground contact and use a shared character model throughout.
- All 20 have genuine alpha and 600×650 canvases. Existing resolution is sufficient; drawing consistency is the weakness.

Keep frame counts, resource names, order and timing exactly: idle 1/2/3 at 220ms; hungry 1/2 at 250ms; wake a1…a10 at 110ms; dance 1/2/3/2/3/2/1/2/3/2/1 at 130ms; feed 1/2 repeated three times at 200ms. No Kotlin changes needed.

## Food (19 items)

18 are fully opaque, including all PNGs except rice. White boxes are not limited to the two JPEGs. Rice has alpha but baked diagonal stripes across and around the drawing. Cherries are only 250×250, rice 512×512; the other 17 are 600×600.

| Resources | Findings |
| --- | --- |
| rice | Heavy brown outline, orange rice, striped contamination; stylistically separate from everything else. |
| cirese | Thick fuzzy black line, low resolution and JPEG edge contamination. |
| clatitecucafea, englishbreakfast, fruits1, cafea | Flat stock-style dishes; multiple vessels/objects, mismatched visual scale. Crêpes read as pancakes. Breakfast is only eggs/sausages; coffee has both pot and cup. |
| carbonara, salad, soup, steak1, steak2, cartofipai | Inconsistent white/blue-rimmed crockery, perspective and detail. Long flat plates occupy far less height than burger/cola. Pasta strands, salad pieces and steak garnishes become noise at 64dp. Fries are thick rather than straw-like. |
| burger, cola, hotdog | Strong gradients, saturated reds/yellows and tiny highlights; taller silhouettes than plated dishes. Lighting varies. |
| nuggets, onionrings, gogosi, pizza | Textured/glossy clip art, busy accompaniments and toppings; detail exceeds the panda style. Onion carton graphics are distracting at small scale. |

Burger (188,529 B), onion rings (177,111 B) exceed the approximate 150 KB target. Rebuild all as coherent transparent illustrations, one grouped serving per image, with consistent occupied area and upper-left light. Some recipes necessarily contain several ingredients; avoid separate decorative props.

## Rooms

All four are opaque 1500×2500 images, appropriate for full-screen backgrounds. bg0 is essentially a solid emerald fill; bg1 has dark diagonal facets; bg2 is a green-to-pale vertical gradient; bg3 has intersecting green ribbons with abrupt edges. Resolution is sufficient and files are already compact. bg3's edges compete with the pet/chrome; bg0 has little sense of place.

RoomBackground.kt uses gradients and soft landscape/wave silhouettes for sunset, night and ocean, plus translucent blossom circles. Recommendation: redo bg0–3 as restrained green environments (meadow, forest, mist, bamboo), retaining IDs, with broad forms and quiet lower 60%. Keep detail near the top. Backgrounds should remain opaque; transparency applies to isolated sprites/icons/food, since empty room pixels would expose the host surface. This interpretation preserves the role of the existing full-screen scenes.

## UI and launcher

- coinimage: flat pound-symbol coin stack. Overlapping disks and the currency glyph become clutter at chip size; replace with one gold coin and a simple paw emboss.
- heart: glossy red emoji, 160×160; sufficient at small sizes but not for enlargement. Replace with a rounded coral heart using restrained shading.
- skull: airbrushed grey skull/crossbones; scary, detailed and over budget (153,932 B). Replace with a gentle simplified ivory/charcoal hazard icon, retaining recognition.
- icon1: opaque green square despite RGBA storage, sleepy low-contrast panda; no adaptive safe-zone design.
- ic_launcher_background.xml: Android Studio's green construction-grid template, unrelated to the app.
- ic_stat_pet.xml: clean white paw vector at 24dp, suitable as a notification silhouette; retain.
- Manifest points both icon fields to drawable/icon1. Existing mipmap-anydpi-v26 adaptive XMLs reference a template foreground in drawable-v24 (found during the production-wide resource check), rather than a Pompi foreground; no monochrome layer exists. Launcher configuration and obsolete mipmaps will be addressed after sample approval.

## Verification boundary

This is the audit/sample checkpoint. No app resources, layouts or Kotlin have changed. Build/lint, emulator screenshots, animation and eight-room skin checks belong to the production stage after approval; no runtime verification claimed here.
