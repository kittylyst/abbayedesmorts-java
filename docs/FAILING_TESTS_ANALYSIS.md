# Failing Tests Analysis

## Summary

Two tests are currently failing in `TestPlayerCollisionPassing`:

1. **`testRightWallCollisionWhenStanding`** (Line 75): Expected 0 but got 1
2. **`testStepsFromEscapeWhenStanding`** (Line 113): Expected 1 but got 0

## Test 1: `testRightWallCollisionWhenStanding`

### Test Setup
- Wall placed at column `checkX = xCell + 2 = 1 + 2 = 3`
- Player positioned at:
- First assertion: `xPos = xCell * tileSize - 1 = 7` pixels
- Second assertion: `xPos = xCell * tileSize + 1 = 9` pixels

### Problem Analysis

**Player's right edge calculation:**
- `points[3] = (xPos + 13 * PIXELS_PER_TILE) / PIXELS_PER_TILE`
- For `xPos = 7`: `points[3] = (7 + 104) / 8 = 13.875 → 13`
- For `xPos = 9`: `points[3] = (9 + 104) / 8 = 14.125 → 14`

**Issue:** The player's right edge is at column 13-14, but the wall is at column 3. The test is checking collision with tiles at column 14-15, not column 3.

**Current buggy distance calculation (Line 476-479):**
```java
if (((points[3] + 1) * PIXELS_PER_TILE)
		- (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```

For `xPos = 7`:
- `(points[3] + 1) * 8 = 14 * 8 = 112` pixels
- `pos.x() / 8 + 15 = 7/8 + 15 = 15.875` (mixed units!)
- Distance = `112 - 15.875 = 96.125` pixels
- Collision? `96.125 < 1.1` = **False** ✓

For `xPos = 9`:
- `(points[3] + 1) * 8 = 15 * 8 = 120` pixels
- `pos.x() / 8 + 15 = 9/8 + 15 = 16.125` (mixed units!)
- Distance = `120 - 16.125 = 103.875` pixels
- Collision? `103.875 < 1.1` = **False** ✓

**But the test is failing!** This suggests:
1. The test is checking the wrong tile (column 14-15 instead of column 3)
2. There might be a solid tile at column 14-15 that's triggering collision
3. The test logic itself may be incorrect

### Root Cause

The test places a wall at column 3, but positions the player such that its right edge is at column 13-14. The collision detection checks tiles at `points[3] + 1 = 14-15`, which is far from the wall at column 3.

**The test should position the player so that `points[3] + 1` is near column 3.**

To have `points[3] + 1 = 3`:
- `points[3] = 2`
- `(xPos + 104) / 8 = 2`
- `xPos + 104 = 16`
- `xPos = -88` (invalid, player would be off-screen)

To have `points[3] + 1 = 4` (just past the wall):
- `points[3] = 3`
- `(xPos + 104) / 8 = 3`
- `xPos + 104 = 24`
- `xPos = -80` (still invalid)

**The test setup is fundamentally incorrect.** The player needs to be positioned much further left, or the wall needs to be placed much further right.

## Test 2: `testStepsFromEscapeWhenStanding`

### Test Setup
- Steps placed at columns 22-25 in row 13 (via `setStep(stage, yCell)`)
- Player positioned at:
- First assertion: `xPos = 1156` pixels
- Second assertion: `xPos = 1156.25` pixels

### Problem Analysis

**Player's right edge calculation:**
- For `xPos = 1156`: `points[3] = (1156 + 104) / 8 = 157`
- For `xPos = 1156.25`: `points[3] = (1156.25 + 104) / 8 = 157.53125 → 157`

**Issue:** `points[3] = 157` is **way out of bounds**! `NUM_COLUMNS = 32`, so valid tile columns are 0-31.

**Correct positioning:**
To have `points[3] = 22` (at the step position):
- `(xPos + 104) / 8 = 22`
- `xPos + 104 = 176`
- `xPos = 72` pixels

**Current buggy distance calculation:**
For `xPos = 1156.25`:
- `(points[3] + 1) * 8 = 158 * 8 = 1264` pixels
- `pos.x() / 8 + 15 = 1156.25/8 + 15 = 144.53125 + 15 = 159.53125` (mixed units!)
- Distance = `1264 - 159.53125 = 1104.46875` pixels
- Collision? `1104.46875 < 1.1` = **False** ✓

**But even with correct positioning (`xPos = 72`):**
- `(points[3] + 1) * 8 = 23 * 8 = 184` pixels
- `pos.x() / 8 + 15 = 72/8 + 15 = 9 + 15 = 24` (mixed units!)
- Distance = `184 - 24 = 160` pixels
- Collision? `160 < 1.1` = **False** ✗ (should be True)

**With fixed calculation (all pixels):**
- `(points[3] + 1) * 8 = 184` pixels
- `pos.x() + 15 = 72 + 15 = 87` pixels
- Distance = `184 - 87 = 97` pixels
- Collision? `97 < 1.1` = **False** ✗ (still should be True)

Wait, even with the fixed calculation, the distance is 97 pixels, which is way larger than the threshold of 1.1 pixels. This suggests the test expectations are wrong, or the collision detection logic needs to check a different condition.

### Root Cause

1. **Unit mismatch bug** in right collision detection (Lines 476-479, 507-509)
2. **Incorrect test positioning** - uses `xPos = 1156` instead of `xPos = 72`
3. **Test expectations may be wrong** - even with correct positioning and fixed calculation, distance is 97 pixels, which is far from the 1.1 pixel threshold

## Unit Mismatch Bug

**Location:** `Player.java` Lines 476-479 (standing) and 507-509 (crouching)

**Current (buggy) code:**
```java
if (((points[3] + 1) * PIXELS_PER_TILE)
		- (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```

**Problem:** Mixes pixels and tiles:
- `(points[3] + 1) * PIXELS_PER_TILE` = pixels ✓
- `pos.x() / PIXELS_PER_TILE` = tiles (converted from pixels)
- `WALL_COLLISION_RIGHT_OFFSET = 15` = pixels (based on naming and left offset)
- Result: Subtracting tiles + pixels from pixels

**Compare with left collision (correct):**
```java
if (pos.x() - ((points[0] - 1) * PIXELS_PER_TILE + WALL_COLLISION_LEFT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```
All values are in pixels ✓

**Fixed code should be:**
```java
if (((points[3] + 1) * PIXELS_PER_TILE)
		- (pos.x() + WALL_COLLISION_RIGHT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```

## Recommended Fixes

### Priority 1: Fix Unit Mismatch Bug

**Location:** `Player.java` Lines 476-479 (standing) and 507-509 (crouching)

**Current (buggy) code:**
```java
if (((points[3] + 1) * PIXELS_PER_TILE)
		- (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```

**Fixed code:**
```java
if (((points[3] + 1) * PIXELS_PER_TILE)
		- (pos.x() + WALL_COLLISION_RIGHT_OFFSET)
	< COLLISION_DISTANCE_THRESHOLD) {
```

**Rationale:** The left collision correctly uses all pixels. The right collision should match this pattern.

### Priority 2: Fix Test Positioning

**`testRightWallCollisionWhenStanding`:**
- **Problem:** Wall at column 3, but player's right edge is at column 13-14
- **Solution:** Reposition player so that `points[3] + 1` is near column 3, OR move wall to column 13-14
- **Calculation:** To have `points[3] + 1 = 4` (just past wall at column 3):
- `points[3] = 3`
- `(xPos + 104) / 8 = 3`
- `xPos = -80` (invalid, off-screen)
- **Better approach:** Move wall to column 14-15 where player's right edge will be

**`testStepsFromEscapeWhenStanding`:**
- **Problem:** Uses `xPos = 1156`, resulting in `points[3] = 157` (out of bounds)
- **Solution:** Use `xPos = 72` to have `points[3] = 22` (at step position)
- **Calculation:**
- Steps are at columns 22-25 in row 13
- To have `points[3] = 22`: `xPos = 22*8 - 13*8 = 72` pixels
- To have `points[3] = 23`: `xPos = 80` pixels

### Priority 3: Clean Up Test Code

- Remove debug `System.out.println()` statements (lines 112, 123, 132)
- Remove commented-out code (lines 93-104)
- Update test comments to reflect correct positioning

### Priority 4: Verify Collision Logic

After fixing the unit mismatch, verify that the distance calculation produces reasonable values:
- With `xPos = 72`, `points[3] = 22`:
- Fixed calculation: `(23 * 8) - (72 + 15) = 184 - 87 = 97` pixels
- This is still much larger than the 1.1 pixel threshold
- **Question:** Is the threshold correct? Or should we check `points[3] * PIXELS_PER_TILE` instead of `(points[3] + 1) * PIXELS_PER_TILE`?

**Note:** The left collision uses `(points[0] - 1) * PIXELS_PER_TILE`, which is the tile boundary to the left. The right collision uses `(points[3] + 1) * PIXELS_PER_TILE`, which is the tile boundary to the right. This asymmetry may be intentional, but the unit mismatch bug must be fixed first.
