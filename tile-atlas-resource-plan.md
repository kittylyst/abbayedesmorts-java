# TileAtlas Resource-File Mapping Plan

## Top-Level Overview

`TileAtlas.getCorners(int tileType)` (`src/main/java/abbaye/model/TileAtlas.java:137`)
currently computes a texture rectangle with ~10 hardcoded `if` ranges and formulas, then
converts it to UV `Corners`. The mapping data is mixed with code, so it cannot be reused
by another game or edited without recompiling.

**Goal:** move the `tileType -> pixel rectangle` data into a JSON resource file. `TileAtlas`
becomes a loader plus a thin remapper. Behaviour must stay bit-for-bit identical.

**Approach (decisions already confirmed with the user):**

- **Format:** JSON, parsed with Jackson (already a dependency, 2.18.0).
- **Shape:** fully expanded, one entry per tile ID (0..650), each a pixel rect `{x,y,w,h}`.
No ranges and no formulas in the file. The file also carries the atlas metadata
(pixel width/height and base tile size) and a `default` rect for IDs not listed.
- **File content = static data only:** frame 0 and 8-bit variant. The file is a pure
`id -> rect` table.
- **Runtime remap stays in Java** (behaviour unchanged, still dead until wired):
- animation frame x-offset driven by `counter[0]` / `counter[1]`
- `+120` px y-offset when `is16Bit`
- torch/flame exception when `changeflag`
- `TILE_EMPTY` and `TILE_TRAP_DOOR` are exempt from the 16-bit y-offset, as in the
	current code
- **Public API unchanged:** `getCorners`, `getCache`, `setIs16Bit`, `setChangeflag`, and all
tile-ID constants stay on `TileAtlas`. `Stage`, `StageRenderer` and `StatusDisplay`
are not touched.
- **No `SpriteSheetMap` split yet.** Everything stays inside `TileAtlas`. The split is
recorded as future work in the wiki design notes.

**Non-goals:**
- Wiring the animation counters, `is16Bit` or `changeflag` to a game loop.
- Changing the cache semantics. The cache is still keyed by tile-type ID and is not
invalidated when the flags change, as today.
- Moving the tile-ID constants out of Java.
- Extracting a shared module.

**Project rules that apply to every sub-task:**
- Every code change carries a `//!` justification comment block naming the test(s) that
fail without it (global rule `change-justification.md`).
- Run `mvn spotless:apply`, then the full test suite. The baseline is 165 passing and
3 skipped.
- Keep the code free of GLFW and GL types so tests stay headless.

---

## Sub-Tasks

---

### Sub-Task 1 — Design-notes wiki page

**Intent**
Record the design and the long-term reuse strategy first, so later sub-tasks refer to it.

**Expected Outcomes**
- `_context/wiki/image-mapping.md` exists and is linked from `_context/wiki/index.md`.

**Todo List**
1. Write `image-mapping.md` covering: motivation, JSON schema, what is data and what is
runtime remap, the 16-bit exemption quirk, the long-term goal (image handling and
mapping as a reusable concern), and the deferred `SpriteSheetMap` split.
2. Add a row to the Files table in `index.md`.

**Relevant Context**
- `_context/wiki/index.md`, `_context/wiki/project.md` (design invariants).

**Status** — `[x] done`

---

### Sub-Task 2 — Parity oracle and JSON generator (test scope)

**Intent**
Produce the JSON file from the *existing* logic, and keep the existing logic as a
test-only oracle. This guarantees the data is correct and gives a safety net for the
rewrite.

**Expected Outcomes**
- A test-scope copy of the current `getCorners` algorithm that returns pixel rects
(a "legacy reference").
- A test-scope generator that writes `src/main/resources/tiles/tile-atlas.json`
for IDs 0..650.
- The committed JSON file, with deterministic ordering (ascending ID) so diffs stay
reviewable.

**Todo List**
1. Copy the current rect-computation logic into a test-only class (counters fixed at 0,
flags as parameters), exposing the pixel rect before UV conversion.
2. Write the generator (a test-scope `main` or a disabled/manual test) that serialises
the atlas metadata, the `default` rect `(0,0,8,8)` and the per-ID rects with Jackson.
Emit static frame-0 8-bit rects only. The 16-bit offset is not baked in.
3. Run it once and commit the generated JSON.
4. Sanity-check a few entries by hand: ID 0 -> x=992; ID 154 (door) -> 16x24; ID 650 (cup)
-> `584,87,16,16`; ID 99 -> `0,0,8,8`.

**Relevant Context**
- `TileAtlas.java:137-232` (source of the logic).
- `src/test/java/abbaye/model/TestTileAtlas.java` (existing assertions the output must keep
satisfying).
- Resource conventions: `src/main/resources/map/*.txt`, loaded via `Class.getResourceAsStream`.
- Test oracle: `src/test/java/abbaye/model/LegacyTileAtlasReference.java` (`PixelRect` and `computePixelRect(id, is16Bit, changeflag)`).
- Generator: `src/test/java/abbaye/model/TileAtlasJsonGenerator.java` (`TileAtlasFile` root model).
- Generated file: `src/main/resources/tiles/tile-atlas.json` (root keys: `width` (int), `height` (int), `tileSize` (int), `default` (`{x,y,w,h}`), `tiles` (map string id -> `{x,y,w,h}`)).

**Status** — `[x] done`

---

### Sub-Task 3 — Load the JSON and replace the hardcoded branch

**Intent**
Make `TileAtlas.getCorners` a lookup plus remap, without changing its observable
behaviour.

**Expected Outcomes**
- `TileAtlas` loads `/tiles/tile-atlas.json` once and builds an `id -> rect` table.
- `getCorners` = cached lookup, else `table.get(id)` or the default rect, then the runtime
remap, then UV conversion (`/ atlasWidth`, `/ atlasHeight`, with the v-flip `1 - v`).
- The `if` ladder at `TileAtlas.java:145-222` is gone. `TILES_PER_ROW` and `TILES_PER_COL`
remain as public constants, consistent with the JSON atlas metadata (a test asserts they
match).
- A missing or malformed resource fails fast with a clear exception at construction.

**Todo List**
1. Add small package-private record types for the file shape (atlas metadata, rect, root).
2. Load with Jackson in the `TileAtlas` constructor, from the classpath resource.
3. Re-implement the runtime remap for the static-to-animated cases:
- tile 84: x += `(counter[0] / 8) * 8`
- tile 154 (door): x += `(counter[0] / 8) * 16`
- tiles 401-404: x += `32 * (counter[0] / 15)`
- tiles 409-428: x += `32 * (counter[1] / 23)`
- y += 120 when `is16Bit`, except for `TILE_EMPTY`, `TILE_TRAP_DOOR`, and the
	torch/flame tiles when `changeflag` is true.
4. Keep the counters as local zero-valued placeholders, as they are now.
5. Delete `SDLRect` and the old ladder.

**Relevant Context**
- `TileAtlas.java` only. The callers go through `Stage`, so no signature may change.
- `Corners` is `record Corners(float u1, float v1, float u2, float v2)`.
- The JSON could be loaded in a static initialiser or per instance. Per instance is simpler
and `Stage` holds a single `TileAtlas`.

**Status** — `[ ] pending`

---

### Sub-Task 4 — Parity tests and cleanup

**Intent**
Prove equivalence over the whole ID space, including the flag variants and unlisted IDs,
and then decide what to keep.

**Expected Outcomes**
- A test comparing new `TileAtlas.getCorners(id)` to the legacy oracle for all IDs in a
range that extends past the table (for example -1..700), across the four combinations of
`is16Bit` and `changeflag`. Comparison must be exact (`Corners` record equality, floats
computed the same way).
- Existing `TestTileAtlas` tests pass unchanged.
- A test that `TILES_PER_ROW` and `TILES_PER_COL` agree with the JSON metadata.
- A test that a missing resource produces a clear failure.
- Decision recorded: the legacy oracle is kept only while it is useful. The default is to
keep the parity test but replace the oracle with a small golden-value set once the user
approves removing it.

**Todo List**
1. Write the exhaustive parity test.
2. Add the constants-vs-metadata test and the failure-mode test.
3. Run `mvn spotless:apply` and the full suite. Expect 165 passing + new tests, 3 skipped.
4. Ask the user whether to remove the legacy oracle and generator or keep them.

**Relevant Context**
- `src/test/java/abbaye/model/TestTileAtlas.java`.
- The float conversion must use the same expression order as today
(`(float) x / (8 * TILES_PER_ROW)`) to avoid 1-ulp differences. Use the JSON metadata
values in the same arithmetic.

**Status** — `[ ] pending`

---

### Sub-Task 5 — Wiki update

**Intent**
Make sure the wiki matches the final implementation.

**Expected Outcomes**
- `image-mapping.md` updated with the final resource path, schema and test names.
- `project.md` key-resources section lists `tiles/tile-atlas.json`.
- `index.md` test baseline count updated.

**Todo List**
1. Update the three wiki files.
2. Offer the user the update before writing, as `AGENTS.md` requires.

**Relevant Context**
- `_context/wiki/project.md`, `_context/wiki/index.md`.

**Status** — `[ ] pending`

---

## Open Risks

- **Float parity:** the UV arithmetic must stay identical, or exact-equality parity fails.
- **16-bit exemptions:** `TILE_EMPTY` and `TILE_TRAP_DOOR` skip the y-offset today. Easy to
lose when moving to a data-driven design, and the parity test must cover it.
- **Unlisted IDs:** IDs outside 0..650 and the gaps (for example 399, 405-408, 429-499)
currently resolve to `(0,0,8,8)` plus the 16-bit offset. The generator lists every ID in
0..650 explicitly, and the `default` rect covers the rest.
