# Crouching Collision Plan

## Overview

Implement the crouching left/right wall-collision branch in `Player.checkCollisions()`, fix the
unit-mismatch bug in the commented-out C code, and re-enable the 2 basic crouching wall-collision
tests in `TestPlayerCollision`. The 2 invisible-wall tests stay disabled; no new constants needed.

**Scope**: `Player.java`, `TestPlayerCollision.java`. No other files need to change.

**Non-goals**: crouching animation frames, hitBox changes, new input handling (all already in
place).

---

## Background: The Unit-Mismatch Bug

The commented-out crouch branch in `Player.checkCollisions()` (lines 440–486) has two bugs:

1. **Wrong Y offset**: Uses `COLLISION_CROUCH_HEIGHT_OFFSET * PIXELS_PER_TILE = 16 * 8 = 128`
Java pixels for the row computation. The disabled tests expect `pos.y() + 16` (i.e. 16 Java
pixels directly). The correct formula, matching the tests, is:
```
r = (int) ((pos.y() + 16) / tileSize)
```

2. **Missing constants**: `WALL_COLLISION_LEFT_OFFSET`, `WALL_COLLISION_RIGHT_OFFSET`, and
`TILE_SPECIAL_LEFT_MIN` are referenced but never declared anywhere. Analysis of the tests
shows these proximity checks are the wrong approach for this codebase — the standing-collision
branch does *not* use proximity thresholds, just tile presence. The crouch branch should
follow the same pattern: set collision if the adjacent tile is solid, with no distance
sub-check.

3. **Invisible-wall logic inverted in CAVE**: The comment in the C code says "columns 0-1 should
not collide" when crouching in ROOM_CAVE at row 5, but the disabled test asserts the opposite —
`assertTrue(player.isCollidingLeft(), ...)`. Looking at the test setup: it sets tiles at
columns 0 and 1, places the player at x=2*tileSize facing LEFT, and expects a collision.
The invisible-wall exemption applies when `xpoints[0] - 1` is exactly 0 or 1 — i.e., the
player's left edge is at column 1 (one tile from the left boundary). The test places the
player two tiles in so `xpoints[0] - 1 = 1` — which *is* in the exemption zone. But then the
test expects collision=true. This means the test and the commented code disagree about the
invisible-wall logic. **Resolution**: treat the CAVE and BEAST invisible-wall branches as
out-of-scope for this task. The 4 disabled tests do not exercise the invisible-wall suppression
path; implement wall collision cleanly first, leaving the invisible-wall exemptions for a
separate task.

> **Note (post-analysis)**: Re-reading `testInvisibleWallRoomCaveCrouching`: the player is at
> `x = 2 * tileSize`, xpoints[0] = `(2*tileSize + 1*PIXELS_PER_TILE) / tileSize` =
> `(128 + 8) / 64 = 2`. So `xpoints[0] - 1 = 1` — the tile being checked IS column 1, which
> is in the "invisible wall" zone (cols 0-1). The test places a solid tile there and expects
> `isCollidingLeft() == true`. So the test is verifying that even in ROOM_CAVE the collision
> IS detected (i.e. the invisible-wall exemption does NOT suppress it at this position/row).
> But ROOM_CAVE waypoint 0 is screen index 2, and `INVISIBLE_WALL_CROUCH_ROW = 5`. At
> `y = 5 * tileSize` the crouchTileY = `(5*64 + 16) / 64 = 5`. So `r == INVISIBLE_WALL_CROUCH_ROW`.
> The commented code would set `collision[COLLISION_LEFT] = 0` here (suppressing it), but the
> test expects it to be 1 (detected). This is a genuine conflict.
>
> **Resolution**: Skip the invisible-wall exemption logic entirely. Only implement the basic
> wall collision. The two "invisible wall" tests (`testInvisibleWallRoomCaveCrouching`,
> `testInvisibleWallRoomBeastCrouching`) should NOT be re-enabled in this task — they require
> careful re-analysis of the original C intent. Only `testCrouchLeftWallCollision` and
> `testCrouchRightWallCollision` will be re-enabled.

---

## Sub-Tasks

---

### Sub-Task 1 — Implement Crouching Wall Collision in `Player.checkCollisions()`

**Intent**: Replace the empty `if (crouch) { /* Collision with Jean ducking */ }` block and the
dead commented block that follows it with a working crouching left/right collision check.

**Expected Outcomes**:
- When crouching, `collision[COLLISION_LEFT]` and `collision[COLLISION_RIGHT]` are set
correctly based on the single tile row at `y = (pos.y() + 16) / tileSize`.
- The crouching path uses the same tile-solidarity conditions as the standing path (solid range,
special-collision tile, special-left range, special-right range).
- The direction guard (`xpoints[0] != 0` and `xpoints[3] != NUM_COLUMNS - 1`) from the
original C code is preserved to avoid out-of-bounds lookups.
- No proximity sub-check (no `WALL_COLLISION_LEFT_OFFSET` / distance threshold).
- The invisible-wall exemption blocks for ROOM_CAVE and ROOM_BEAST are left as a `// TODO`
comment, not implemented.

**Todo List**:
1. In `Player.checkCollisions()`, locate the `if (crouch) { /* Collision with Jean ducking */ }`
block at line ~519.
2. Remove the long block of commented-out C code (lines 440–487) entirely.
3. Implement the crouch branch:
```
if (crouch) {
	int r = (int) ((pos.y() + 16) / tileSize);
	if (xpoints[0] != 0) {
	int tileLeft = tileAt(currentRoomData, r, xpoints[0] - 1);
	if ((tileLeft > 0 && tileLeft < TILE_SOLID_MAX && tileLeft != TILE_PASSABLE_VARIANT_1)
		|| (tileAt(currentRoomData, r, xpoints[0]) == TILE_SPECIAL_COLLISION)
		|| ((tileLeft > TILE_CLOSED_DOOR4) && (tileLeft < TILE_SPECIAL_LEFT_MAX))) {
		collision[COLLISION_LEFT] = 1;
	}
	}
	if (xpoints[3] != NUM_COLUMNS - 1) {
	int tileRight = tileAt(currentRoomData, r, xpoints[3] + 1);
	if ((tileRight > 0 && tileRight < TILE_SOLID_MAX && tileRight != TILE_PASSABLE_VARIANT_1)
		|| ((tileRight > TILE_SPECIAL_RIGHT_MIN) && (tileRight < TILE_SPECIAL_RIGHT_MAX))) {
		collision[COLLISION_RIGHT] = 1;
	}
	}
	// TODO: invisible-wall exemptions for ROOM_CAVE and ROOM_BEAST (row 5) need re-analysis
}
```
4. Run `mvn spotless:apply` and `mvn test` to confirm no compile errors and no regressions
among the currently passing tests.

**Relevant Context**:
- `src/main/java/abbaye/model/Player.java` — `checkCollisions()` method, lines 488–644
- `src/main/java/abbaye/model/Player.java` — `getTileGrid()`, lines 398–438
- `src/main/java/abbaye/model/Player.java` — constants: `COLLISION_LEFT_EDGE_OFFSET = 1`,
`COLLISION_RIGHT_EDGE_OFFSET = 13`, `PIXELS_PER_TILE = 8`, `COLLISION_CROUCH_HEIGHT_OFFSET = 16`
- `src/main/java/abbaye/model/TileAtlas.java` — tile range constants

**Status**: [x] done

---

### Sub-Task 2 — Re-enable 2 Crouching Collision Tests

**Intent**: Remove `@Disabled` from `testCrouchLeftWallCollision` and
`testCrouchRightWallCollision`, confirm they pass. Leave all four previously-disabled crouch
tests disabled (2 invisible-wall tests unchanged; they need separate re-analysis).

**Expected Outcomes**:
- `testCrouchLeftWallCollision` passes.
- `testCrouchRightWallCollision` passes.
- `testInvisibleWallRoomCaveCrouching` and `testInvisibleWallRoomBeastCrouching` remain `@Disabled`
unchanged.
- Total test count: 2 more passing, still 5 disabled (3 unrelated + 2 invisible-wall).

**Todo List**:
1. In `TestPlayerCollision.java`:
- Remove `@Disabled("Crouching unimplemented so far")` from `testCrouchLeftWallCollision`
	(line ~184).
- Remove `@Disabled("Crouching unimplemented so far")` from `testCrouchRightWallCollision`
	(line ~204).
- Leave `testInvisibleWallRoomCaveCrouching` and `testInvisibleWallRoomBeastCrouching` exactly
	as-is.
2. Run `mvn test` and verify the two re-enabled tests pass and no other tests regress.

**Relevant Context**:
- `src/test/java/abbaye/model/TestPlayerCollision.java` — lines 183–224

**Status**: [x] done

---

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| Use `pos.y() + 16` (not `COLLISION_CROUCH_HEIGHT_OFFSET * PIXELS_PER_TILE`) for the row | Tests use raw `+ 16`; `* PIXELS_PER_TILE` would give 128 which is 2 full tiles, not matching test intent |
| No proximity sub-check (distance threshold) | Standing collision branch has none; tests don't set up proximity scenarios; simpler and consistent |
| Skip invisible-wall exemptions | The commented C code conflicts with the test assertions for ROOM_CAVE; needs separate analysis |
| No new constants — use `TILE_CLOSED_DOOR4` for left-special lower bound | Identical to the standing branch; no reason to introduce a new alias |
| No changes to `TileAtlas.java` | `TILE_SPECIAL_LEFT_MIN` is not needed; all required constants already exist |
