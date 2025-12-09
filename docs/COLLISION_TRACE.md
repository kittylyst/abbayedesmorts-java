# Code Path Trace: `testStepsFromEscapeWhenStanding()`

## Test Setup

**Test Configuration:**
- `yCell = 12` (player Y position in tile coordinates)
- `xCell = 17` (starting X position in tile coordinates, not directly used)
- Floor set at row `yCell + 3 = 15` using `setFloor(stage, 15)`
- Steps created using `setStep(stage, yCell)` which sets:
- Row 13 (`floorLevel + 1`): Contains step tiles at columns 22-25: `[3, 4, 3, 4]`
- Row 14 (`floorLevel + 2`): Contains step tiles at columns 20-25: `[3, 4, 1, 2, 1, 2]`

**Player State:**
- Direction: `RIGHT`
- Crouch: `false`
- Walk: `true`
- Jump: `NEUTRAL` (default)

**Test Positions:**
1. First assertion: `xPos = 1156` pixels, `yPos = 96` pixels (12 * 8)
2. Second assertion: `xPos = 1156.25` pixels, `yPos = 96` pixels

---

## Code Path Through `calculateCollision()`

### Step 1: Calculate Collision Points (Lines 417-424)

**For xPos = 1156 pixels:**
- `resize = Stage.getTileSize() = 8.0f`
- `points[0]` (left edge X): `(1156 + 1*8) / 8 = 1164 / 8 = 145.5 → 145` (tile column)
- `points[1]` (left mid X): `(1156 + 7*8) / 8 = 1212 / 8 = 151.5 → 151`
- `points[2]` (center X): `(1156 + 8*8) / 8 = 1220 / 8 = 152.5 → 152`
- `points[3]` (right edge X): `(1156 + 13*8) / 8 = 1260 / 8 = 157.5 → 157`
- `points[4]` (top edge Y): `(96 + 1*8) / 8 = 104 / 8 = 13` (tile row)
- `points[5]` (mid height Y): `(96 + 8*8) / 8 = 160 / 8 = 20`
- `points[6]` (lower mid Y): `(96 + 15*8) / 8 = 216 / 8 = 27` (out of bounds, NUM_ROWS = 22)
- `points[7]` (bottom edge Y): `(96 + 23*8) / 8 = 280 / 8 = 35` (out of bounds)

**Note:** `points[6]` and `points[7]` are out of bounds (>= NUM_ROWS = 22), which will affect the collision checks.

**For xPos = 1156.25 pixels:**
- `points[0]`: `(1156.25 + 8) / 8 = 1164.25 / 8 = 145.53125 → 145`
- `points[1]`: `(1156.25 + 56) / 8 = 1212.25 / 8 = 151.53125 → 151`
- `points[2]`: `(1156.25 + 64) / 8 = 1220.25 / 8 = 152.53125 → 152`
- `points[3]`: `(1156.25 + 104) / 8 = 1260.25 / 8 = 157.53125 → 157`
- Y points remain the same

### Step 2: Reset Collision State (Lines 426-430)

All collision flags reset to 0:
- `collision[COLLISION_UP] = 0`
- `collision[COLLISION_DOWN] = 0`
- `collision[COLLISION_LEFT] = 0`
- `collision[COLLISION_RIGHT] = 0`

### Step 3: Left & Right Collision Detection (Lines 435-484)

**Condition Check (Line 436):**
- `!crouch` is `true` (player is standing), so this block executes

**Loop: `for (var n = 4; n < 8; n += 1)` (Line 438)**
- Iterates through vertical collision points: `n = 4, 5, 6, 7`
- These correspond to `points[4]` (top), `points[5]` (mid), `points[6]` (lower mid), `points[7]` (bottom)

**Boundary Check (Line 439):**
- `points[0] <= 0`? `145 <= 0`? **No**
- `points[3] + 1 >= NUM_COLUMNS`? `157 + 1 = 158 >= 32`? **No**
- `points[n] + 1 >= NUM_ROWS`?
- For `n = 4`: `13 + 1 = 14 < 22` ✓
- For `n = 5`: `20 + 1 = 21 < 22` ✓
- For `n = 6`: `27 + 1 = 28 >= 22` ✗ **BREAK**
- For `n = 7`: Not reached

**Result:** Loop only executes for `n = 4` and `n = 5` before breaking.

**Direction Check (Line 442-443):**
- `direction == RIGHT` is `true`
- `points[3] + 1 < NUM_COLUMNS`? `157 + 1 = 158 < 32`? **No** (158 >= 32)
- **Wait, this is wrong!** `points[3] = 157`, so `points[3] + 1 = 158`, which is way beyond `NUM_COLUMNS = 32`.

**Issue Identified:** The player's X position of 1156 pixels results in `points[3] = 157`, which is far beyond the valid tile column range (0-31). This suggests the test setup may be incorrect, or the collision detection should handle out-of-bounds positions differently.

**For n = 4 (top edge, row 13):**
- `blright = currentRoomData[13][157 + 1]` = `currentRoomData[13][158]`
- This is an **array index out of bounds** error! `NUM_COLUMNS = 32`, so valid indices are 0-31.

**For n = 5 (mid height, row 20):**
- `blright = currentRoomData[20][157 + 1]` = `currentRoomData[20][158]`
- Also out of bounds.

---

## Problem Analysis

The test case `testStepsFromEscapeWhenStanding()` has a fundamental issue:

1. **Player X position is too large:** `xPos = 1156` pixels corresponds to tile column 144+ (after adding collision offsets), which is far beyond the valid range of 0-31.

2. **The test appears to be testing a specific edge case** where the player is positioned very close to a step, but the positioning calculation is incorrect.

3. **The hardcoded values** (`1156`, `1156.25`) suggest this was discovered through trial and error, but the underlying calculation may be wrong.

## Corrected Understanding

Looking at the test more carefully:
- `checkX = xCell + 2 = 17 + 2 = 19`
- The step tiles are at columns 22-25 in row 13
- The player should be positioned such that `points[3]` (right edge) is around column 22-23 to detect collision with the step

**Correct positioning calculation:**
- To have `points[3]` (right edge) at column 22:
- `points[3] = (xPos + 13*8) / 8 = 22`
- `xPos + 104 = 176`
- `xPos = 72` pixels

- To have `points[3]` at column 23:
- `xPos = 80` pixels

The test's `xPos = 1156` would result in `points[3] = 157`, which is completely wrong.

---

## Actual Code Path (Assuming Corrected Position)

If we assume the player should be at `xPos = 72` (to have right edge at column 22):

**Collision Points:**
- `points[0] = (72 + 8) / 8 = 10`
- `points[1] = (72 + 56) / 8 = 16`
- `points[2] = (72 + 64) / 8 = 17`
- `points[3] = (72 + 104) / 8 = 22` ✓ (matches step position)

**Right Collision Check (n = 4, row 13):**
- `blright = currentRoomData[13][22 + 1] = currentRoomData[13][23] = 4` (from setStep row1)
- Tile 4 is solid (`4 > 0 && 4 < 100 && 4 != 16 && 4 != 38 && 4 != 37`) ✓
- Distance check (Line 476-479):
- `(points[3] + 1) * PIXELS_PER_TILE = 23 * 8 = 184`
- `pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET = 72/8 + 15 = 9 + 15 = 24`
- **Wait, this calculation looks wrong!** It should be:
	- `(points[3] + 1) * PIXELS_PER_TILE - (pos.x() + WALL_COLLISION_RIGHT_OFFSET)`
- But the code has: `(points[3] + 1) * PIXELS_PER_TILE - (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)`
- This mixes pixels and tiles incorrectly!

**Line 476-479 Analysis:**
```java
if ((points[3] + 1) * PIXELS_PER_TILE
		- (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```

This calculates:
- `(points[3] + 1) * 8` = pixels (tile boundary in pixels)
- `pos.x() / 8 + 15` = tiles + offset (mixed units!)

This is a **unit mismatch bug** in the collision detection code!

---

## Bug Identified: Unit Mismatch in Right Collision Detection

**Line 476-479 (standing) and Line 507-509 (crouching):**

The right collision distance calculation has a **unit mismatch bug**:

```java
if ((points[3] + 1) * PIXELS_PER_TILE
		- (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```

**Problem:**
- `(points[3] + 1) * PIXELS_PER_TILE` = pixels ✓
- `pos.x() / PIXELS_PER_TILE` = tiles (converted from pixels)
- `WALL_COLLISION_RIGHT_OFFSET = 15` = pixels (based on naming and left offset being 7 pixels)
- **Mixing tiles + pixels** in the subtraction

**Compare with left collision (correct):**
```java
if (pos.x() - ((points[0] - 1) * PIXELS_PER_TILE + WALL_COLLISION_LEFT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```
- All values are in pixels ✓

**The right collision should be:**
```java
if ((points[3] + 1) * PIXELS_PER_TILE
		- (pos.x() + WALL_COLLISION_RIGHT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```

---

## Conclusion

The test `testStepsFromEscapeWhenStanding()` exercises a code path that:

1. **Has a unit mismatch bug** in the right collision distance calculation (Line 476-479 and 507-509)
- Mixes pixels and tiles in the distance calculation
- Should use `pos.x() + WALL_COLLISION_RIGHT_OFFSET` instead of `pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET`

2. **Uses incorrect hardcoded positions** (`xPos = 1156` and `1156.25`)
- These result in `points[3] = 157`, which is far beyond valid tile columns (0-31)
- Should use `xPos = 72` to have `points[3] = 22` (at the step position)

3. **May trigger array bounds issues** with the current hardcoded positions
- Accessing `currentRoomData[row][158]` when `NUM_COLUMNS = 32` (valid indices: 0-31)

**Recommended Fixes:**
1. Fix the unit mismatch bug in right collision detection
2. Update test to use correct positioning (`xPos = 72` or `80` pixels)
3. Remove debug `System.out.println()` statements
4. Clean up commented-out code
