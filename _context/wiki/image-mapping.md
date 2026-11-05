# Image Mapping Design Notes

Design notes for moving the tile-type to texture-rectangle mapping out of code and into a
resource file. Status: **implemented** (`TileAtlas` loads `tiles/tile-atlas.json`). See
`tile-atlas-resource-plan.md` at the repo root for the work breakdown.

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

## Final implementation

- **Resource:** `src/main/resources/tiles/tile-atlas.json`, loaded from the classpath as
  `/tiles/tile-atlas.json`.
- **Schema:** root keys `width` (1000), `height` (240), `tileSize` (8), `default`
  (`{x,y,w,h}`), and `tiles` (string ID -> `{x,y,w,h}`, IDs 0..650 in ascending order).
- **Loading:** the `TileAtlas()` constructor reads the file with Jackson into the
  package-private records `TileAtlas.AtlasFile` and `TileAtlas.Rect`. A missing resource
  throws `IllegalStateException`; a read or parse failure throws `UncheckedIOException`.
  The package-private `TileAtlas(String resourcePath)` constructor exists so tests can
  exercise the failure path.
- **Lookup:** `getCorners` = cache, else `tiles.getOrDefault(id, default)`, then the runtime
  remap, then UV conversion using the JSON `width` / `height`.
- **`module-info.java`:** `opens abbaye.model to com.fasterxml.jackson.databind` is required
  so Jackson can instantiate the package-private records on the module path.
- **Changing a rect:** edit the JSON directly. `TileAtlasJsonGenerator` regenerates it from
  the legacy oracle, which would overwrite hand edits, and the parity test would then fail
  for any intentional change.

## Verification strategy

The JSON was generated once from the original `getCorners` logic by a test-scope generator.
Parity tests compare the new implementation to a test-only copy of the old algorithm.
Comparison is exact, so the float arithmetic order must be kept.

Test classes in `src/test/java/abbaye/model/`:

| Class | Role |
|---|---|
| `LegacyTileAtlasReference` | Test-only copy of the old if-ladder (`computePixelRect`, `computeCorners`) |
| `TileAtlasJsonGenerator` | Writes the JSON from the oracle; the test is `@Disabled`, run it manually |
| `TestLegacyTileAtlasReference` | Validates the oracle output for known IDs |
| `TestTileAtlasParity` | Exact parity for IDs 0..700 across all four `is16Bit` / `changeflag` combinations; `TILES_PER_ROW` / `TILES_PER_COL` agree with the JSON metadata; a missing resource fails fast |

Parity covers IDs 0..700, not negative IDs. The old ladder extrapolated a meaningless
negative x for negative IDs, while the new lookup falls back to the default rect. The map
parser reads non-negative 3-digit fields, so negative IDs never reach `getCorners`.

**Decision (2026-11-05):** keep the legacy oracle and generator for now. Revisit when the
`SpriteSheetMap` split happens.

## Deferred / future work

- **`SpriteSheetMap` split:** a game-agnostic class (JSON load, rect lookup, UV conversion,
  no Abbaye imports), with `TileAtlas` as a thin Abbaye-specific wrapper holding the
  constants and the animation/16-bit remap. Deliberately not done in this change.
- Moving the animation and variant rules into the file (frame lists, alternate rects).
- Wiring the counters, `is16Bit` and `changeflag` to the game loop.
- Extracting a shared module.
