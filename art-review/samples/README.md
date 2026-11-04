# Sample checkpoint

Generated with the built-in imagegen tool on 2026-11-04. Sources are preserved alongside review PNGs and candidate WebP exports. No assets have been copied into res.

- style-review.png: four samples on near-black, pale and sunset-gradient backgrounds. The gradient is a contrast swatch, not an emulator screenshot or the full room renderer.
- small-size-review.png: rice at 64px and coin at 22px on both themes. These are raster-pixel checks; on-device dp checks remain pending.
- classic-idle1.webp and red-panda-idle1.webp: 600×650, transparent, quality-90 WebP (30,002 and 36,232 bytes). Lossless versions exceeded the skin budget; lossless PNG masters are retained.
- rice.webp and coin.webp: 600×600, transparent, lossless WebP (95,446 and 140,504 bytes).

The classic and edited red panda use the same 550×596 transform and +25,+12 placement on a 600×650 canvas. The generated source heights differ by one pixel, so this is a visual pose match, not a validated production animation rig. Feet sit around y=590. Full frame alignment and all-eight-room checks remain pending.

Visual review: all four read on these three swatches; dark limbs remain visible, cream faces have shaded edges, and neither food nor coin has a white box. Rice and paw coin remain recognisable at small size. There is more fine surface texture in the generated masters than the proposed smooth-art guide intended; it recedes at display size but should not be increased. The coin is the glossiest asset and can be softened if desired. Preview sheets use PNG masters; inspect WebP candidates separately before production use.

Audit: ../AUDIT.md. Proposed art direction: ../STYLE-GUIDE.md. Full asset replacement, launcher changes, build/lint and emulator screenshots are deliberately deferred until sample approval, as requested.
