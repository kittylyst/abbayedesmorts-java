# Image Mapping Design Notes

Design notes for moving the tile-type to texture-rectangle mapping out of code and into a
resource file. Status: **planned**. See `tile-atlas-resource-plan.md` at the repo root for
the work breakdown.

## Why

`TileAtlas.getCorners(int tileType)` is a pure function (the animation counters are always
0 and `setIs16Bit` / `setChangeflag` have no callers), but its mapping is hardcoded as ~10
`if` ranges and formulas. The data is mixed with code, so it cannot be reused or edited
without recompiling.

## Long-term strategy

Image handling and the tile/sprite map are a separate concern from the Abbaye game logic.
The aim is to extract them so other games can reuse them. Moving the mapping into a
resource file is the first step: the file holds the data, and the code only loads and remaps.

## Design decisions

- **Format:** JSON, parsed with Jackson (already a dependency).
- **Location:** `src/main/resources/tiles/tile-atlas.json`.
- **Expanded form:** one entry per tile ID (0..650), each a pixel rect `{x, y, w, h}`.
  There are no ranges or formulas in the file. The loader is a plain lookup.
- **File shape:** atlas metadata (pixel width/height, base tile size), a `default` rect
  for unlisted IDs, and the `tiles` map.
- **Pure table:** the file holds only the static, frame-0, 8-bit rects.
- **Runtime remap stays in Java:**
  - animation frame x-offset (`counter[0]`, `counter[1]`)
  - `+120` px y-offset for the 16-bit variant
  - `changeflag` exception for torch/flame tiles
  - `TILE_EMPTY` and `TILE_TRAP_DOOR` are **exempt** from the 16-bit y-offset. This is a
    quirk of the original code and must be preserved.
- **API unchanged:** `getCorners`, `getCache`, the setters and the tile-ID constants stay
  on `TileAtlas`. The constants stay in Java.
- **Cache:** still keyed by tile-type ID and not invalidated when the flags change, as
  today.
- **Pixels, not UVs, in the file:** the file stays human-editable and independent of the
  atlas image size. UVs are derived at load time.

## Verification strategy

The JSON is generated once from the existing `getCorners` logic by a test-scope generator.
A parity test then compares the new implementation to a test-only copy of the old
algorithm for every ID (including out-of-table IDs) across all `is16Bit` / `changeflag`
combinations. Comparison is exact, so the float arithmetic order must be kept.

## Deferred / future work

- **`SpriteSheetMap` split:** a game-agnostic class (JSON load, rect lookup, UV conversion,
  no Abbaye imports), with `TileAtlas` as a thin Abbaye-specific wrapper holding the
  constants and the animation/16-bit remap. Deliberately not done in this change.
- Moving the animation and variant rules into the file (frame lists, alternate rects).
- Wiring the counters, `is16Bit` and `changeflag` to the game loop.
- Extracting a shared module.
