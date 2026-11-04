# Proposed direction: soft sculpted storybook

Status: approved by the user after the blue rice-bowl revision; production authorised unsupervised. Glass stays in UI chrome; artwork uses a satin finish with broad, soft shading.

- Pompi: oversized round head, small pear-shaped body, round ears, large asymmetric-looking oval eye patches, small nose and friendly eyes. Preserve the old seated panda's identity while regularising its geometry. No realistic fur strands or heavy ink contour.
- Palette: warm porcelain #F7F3EB, cool shaded ivory #D4DCE1, charcoal #252B35 with lifted lit edges #465361. Restrained blush #E9ADA4. Food uses appetising muted ochre, terracotta and leaf green. Coin: honey #E8B44F, light #FFE3A0, shade #B5782B. Red panda: russet #B95E38, amber #DA9059, cream markings and charcoal legs.
- Shape/line: continuous rounded silhouettes; use a fine darker local-colour edge where necessary, never a thick black sticker border. A subtle cool rim separates dark limbs from near-black rooms; slightly darker ivory edges preserve pale-on-pale contrast. No external glow.
- Light: broad soft key from upper left, shadow lower right. Low gloss, no plastic specular blobs, grain, photographic texture or baked ground plane. Transparent outside each object, including between limbs.
- Food: one compact serving, three-quarter view with a consistent shallow viewing angle. Broad ingredient shapes survive 64dp. Rice sample uses one muted cobalt-blue bowl (#365B91), with lighter blue edges for dark-backdrop separation, no chopsticks or extra props. Genuine transparency.
- UI icons: simple silhouettes with the same shallow satin volume; no text or currency symbols. Paw-embossed single coin, coral heart, friendly skull.
- Rooms: opaque portrait gradients and simple natural silhouettes; no text, calm lower 60%. Meadow, a deep forest Dusk, a bamboo Garden and a blue Sky complement the code-painted rooms while keeping their existing labels.

## Registration for production

All pet/skin canvases 600×650. Nominal centre x=300, ground-contact baseline y=590. Target seated silhouette about 430px wide and 465px tall; preserve head scale in standing poses and keep hands/ears within margins. Breathing moves torso/head only; feet stay anchored. Wake-up sleep poses flatten around the same baseline. Generate the classic master first, then edit the exact corresponding classic pose for each skin. Register outputs explicitly; an image prompt alone cannot guarantee pixel registration.

Classic names/counts and PetSprite.kt playback stay unchanged. Skin names/counts follow the request. Red-panda tail must fit within the shared canvas and not displace the torso. Production exports will be lossless WebP if needed to meet <100 KB skin / ~150 KB other-image budgets.

## Checkpoint and next stage

Four separate sample assets: classic idle, an edit of that idle into red panda, rice, coin. Samples live outside res and are not wired into the app. Show them on near-black, pale and colourful backdrops before approval. Final pose registration, compression and full animation checks happen after style approval. The user approved continuing with all frame sets, food, rooms, launcher assets and verification.
