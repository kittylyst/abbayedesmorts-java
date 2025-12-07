# Magic Numbers Catalog: `calculateCollision()` Method

## Overview
This document catalogs all magic numbers found in the `Player.calculateCollision()` method and suggests symbolic constant names for the most important ones.

---

## 1. Collision Point Pixel Offsets (Lines 384-391)

These define the collision detection points around the player sprite:

| Value | Location | Purpose | Suggested Name |
|-------|----------|---------|-----------------|
| `1` | `points[0]`, `points[4]` | Left edge X offset, Top edge Y offset | `COLLISION_LEFT_EDGE_OFFSET = 1`<br>`COLLISION_TOP_EDGE_OFFSET = 1` |
| `7` | `points[1]` | Left-middle X offset | `COLLISION_LEFT_MID_OFFSET = 7` |
| `8` | `points[2]`, `points[5]` | Center X offset, Mid-height Y offset | `COLLISION_CENTER_X_OFFSET = 8`<br>`COLLISION_MID_HEIGHT_OFFSET = 8` |
| `13` | `points[3]` | Right edge X offset | `COLLISION_RIGHT_EDGE_OFFSET = 13` |
| `15` | `points[6]` | Lower-mid Y offset | `COLLISION_LOWER_MID_OFFSET = 15` |
| `23` | `points[7]` | Bottom edge Y offset (feet) | `COLLISION_BOTTOM_EDGE_OFFSET = 23` |
| `16` | Line 450 (crouch) | Crouch height Y offset | `COLLISION_CROUCH_HEIGHT_OFFSET = 16` |

**Priority: HIGH** - These define the collision box geometry and are critical for understanding the collision detection system.

---

## 2. Tile Type IDs

| Value | Location | Purpose | Suggested Name |
|-------|----------|---------|-----------------|
| `16` | Lines 425, 433, 543, 548 | Passable tile | `TILE_PASSABLE = 16` |
| `37` | Lines 425, 433, 453, 459, 543, 548 | Passable tile variant | `TILE_PASSABLE_VARIANT_1 = 37` |
| `38` | Lines 425, 433, 518, 527, 544, 549 | Platform tile (fall-through) | `TILE_PLATFORM = 38` |
| `100` | Lines 425, 432, 453, 459, 542, 547 | Upper bound for solid tiles | `TILE_SOLID_MAX = 100` |
| `128` | Lines 426, 454 | Special collision tile | `TILE_SPECIAL_COLLISION = 128` |
| `342` | Line 460 | Special tile range start | `TILE_SPECIAL_RIGHT_MIN = 342` |
| `344` | Lines 436, 460 | Special right collision tile | `TILE_SPECIAL_RIGHT = 344` |
| `346` | Line 454 | Special tile range start | `TILE_SPECIAL_LEFT_MIN = 346` |
| `348` | Lines 426, 454 | Special left collision tile | `TILE_SPECIAL_LEFT = 348` |
| `351` | Line 454 | Special tile range end | `TILE_SPECIAL_LEFT_MAX = 351` |

**Priority: HIGH** - Tile IDs are fundamental to collision logic and should be constants for maintainability.

---

## 3. Distance Thresholds (Collision Detection Precision)

| Value | Location | Purpose | Suggested Name |
|-------|----------|---------|-----------------|
| `1.1` | Lines 427, 437, 455, 461 | Wall collision distance threshold (pixels) | `COLLISION_DISTANCE_THRESHOLD = 1.1f` |
| `1` | Line 551 | Roof collision distance threshold (pixels) | `COLLISION_ROOF_DISTANCE_THRESHOLD = 1.0f` |

**Priority: HIGH** - These thresholds determine when collisions are detected and are critical for game feel.

---

## 4. Array Indices and Loop Bounds

| Value | Location | Purpose | Suggested Name |
|-------|----------|---------|-----------------|
| `0` | Multiple | Array start, zero offset, collision array index | Already clear in context |
| `1` | Multiple | Array offset, increment | Already clear in context |
| `4` | Line 405 | Loop start (vertical collision points) | `COLLISION_POINT_VERTICAL_START = 4` |
| `8` | Line 405 | Loop end (vertical collision points) | `COLLISION_POINT_VERTICAL_END = 8` |
| `5` | Lines 467, 471 | Crouch row for invisible walls | `INVISIBLE_WALL_CROUCH_ROW = 5` |
| `19` | Line 485 | Invisible ground row threshold | `INVISIBLE_GROUND_ROW_THRESHOLD = 19` |
| `21` | Line 495 | Bottom screen row threshold | `SCREEN_BOTTOM_ROW_THRESHOLD = 21` |
| `27` | Line 472 | ROOM_BEAST invisible wall start column | `ROOM_BEAST_INVISIBLE_WALL_START = 27` |
| `32` | Line 472 | ROOM_BEAST invisible wall end column | `ROOM_BEAST_INVISIBLE_WALL_END = 32` |
| `2` | Lines 485, 486 | Invisible ground column | `INVISIBLE_GROUND_COLUMN = 2` |

**Priority: MEDIUM** - Some are clear in context, but room-specific values should be constants.

---

## 5. Position Calculation Offsets

| Value | Location | Purpose | Suggested Name |
|-------|----------|---------|-----------------|
| `7` | Lines 427, 455 | Left wall collision X offset | `WALL_COLLISION_LEFT_OFFSET = 7` |
| `14` | Lines 437, 461 | Right wall collision X offset | `WALL_COLLISION_RIGHT_OFFSET = 14` |
| `24` | Line 499 | Player height for ground calculation | `PLAYER_HEIGHT_PIXELS = 24` |
| `3` | Line 503 | Ground snap offset multiplier | `GROUND_SNAP_OFFSET_MULTIPLIER = 3` |
| `5` | Line 519 | Platform fall-through X threshold | `PLATFORM_FALL_THRESHOLD_X = 5` |
| `2` | Line 528 | Platform fall-through X offset | `PLATFORM_FALL_OFFSET_X = 2` |
| `13` | Line 519 | Platform check X offset | `PLATFORM_CHECK_X_OFFSET = 13` |
| `1` | Line 528 | Platform check X offset (right) | `PLATFORM_CHECK_X_OFFSET_RIGHT = 1` |

**Priority: MEDIUM** - These are used in position calculations and could benefit from named constants.

---

## 6. Special Values

| Value | Location | Purpose | Suggested Name |
|-------|----------|---------|-----------------|
| `300` | Line 497 | Bottom screen teleport Y position (tiles) | `SCREEN_BOTTOM_TELEPORT_TILES = 300` |
| `8` | Lines 455, 461, 486 | Pixel-to-tile conversion (hardcoded) | Should use `PIXELS_PER_TILE` constant instead |
| `10` | Line 413 | Debug log frequency (every N frames) | `DEBUG_LOG_FREQUENCY = 10` |

**Priority: LOW to MEDIUM** - The `300` value is a "dirty trick" per comment and should be documented. The hardcoded `8` should definitely use `PIXELS_PER_TILE`.

---

## 7. Collision Array Indices

| Value | Location | Purpose | Suggested Name |
|-------|----------|---------|-----------------|
| `0` | Lines 394, 552 | Roof/Up collision | `COLLISION_UP = 0` |
| `1` | Line 395 | Down collision (unused) | `COLLISION_DOWN = 1` |
| `2` | Lines 396, 428, 456, 468, 472 | Left collision | `COLLISION_LEFT = 2` |
| `3` | Lines 397, 438, 462, 469, 473 | Right collision | `COLLISION_RIGHT = 3` |

**Priority: HIGH** - These indices are used throughout the collision system and should be named constants.

---

## Recommended Implementation Priority

### **Priority 1 (Critical - Implement First):**
1. **Collision array indices** (`COLLISION_UP`, `COLLISION_LEFT`, `COLLISION_RIGHT`)
2. **Tile type IDs** (all tile constants)
3. **Distance thresholds** (`COLLISION_DISTANCE_THRESHOLD`, `COLLISION_ROOF_DISTANCE_THRESHOLD`)

### **Priority 2 (High Value):**
4. **Collision point offsets** (the 1, 7, 8, 13, 15, 23, 16 values)
5. **Room-specific constants** (invisible wall/ground values)

### **Priority 3 (Nice to Have):**
6. **Position calculation offsets** (7, 14, 24, etc.)
7. **Special values** (300, debug frequency)

---

## Suggested Constant Definitions

```java
// Collision array indices
private static final int COLLISION_UP = 0;
private static final int COLLISION_DOWN = 1;  // Unused but documented
private static final int COLLISION_LEFT = 2;
private static final int COLLISION_RIGHT = 3;

// Tile type IDs
private static final int TILE_PASSABLE = 16;
private static final int TILE_PASSABLE_VARIANT_1 = 37;
private static final int TILE_PLATFORM = 38;
private static final int TILE_SOLID_MAX = 100;
private static final int TILE_SPECIAL_COLLISION = 128;
private static final int TILE_SPECIAL_RIGHT = 344;
private static final int TILE_SPECIAL_LEFT = 348;
private static final int TILE_SPECIAL_RIGHT_MIN = 342;
private static final int TILE_SPECIAL_LEFT_MIN = 346;
private static final int TILE_SPECIAL_LEFT_MAX = 351;

// Collision detection thresholds
private static final float COLLISION_DISTANCE_THRESHOLD = 1.1f;
private static final float COLLISION_ROOF_DISTANCE_THRESHOLD = 1.0f;

// Collision point pixel offsets (relative to player position)
private static final int COLLISION_LEFT_EDGE_OFFSET = 1;
private static final int COLLISION_LEFT_MID_OFFSET = 7;
private static final int COLLISION_CENTER_X_OFFSET = 8;
private static final int COLLISION_RIGHT_EDGE_OFFSET = 13;
private static final int COLLISION_TOP_EDGE_OFFSET = 1;
private static final int COLLISION_MID_HEIGHT_OFFSET = 8;
private static final int COLLISION_LOWER_MID_OFFSET = 15;
private static final int COLLISION_BOTTOM_EDGE_OFFSET = 23;
private static final int COLLISION_CROUCH_HEIGHT_OFFSET = 16;

// Room-specific collision constants
private static final int INVISIBLE_WALL_CROUCH_ROW = 5;
private static final int INVISIBLE_GROUND_ROW_THRESHOLD = 19;
private static final int INVISIBLE_GROUND_COLUMN = 2;
private static final int ROOM_BEAST_INVISIBLE_WALL_START = 27;
private static final int ROOM_BEAST_INVISIBLE_WALL_END = 32;

// Screen boundaries
private static final int SCREEN_BOTTOM_ROW_THRESHOLD = 21;
private static final int SCREEN_BOTTOM_TELEPORT_TILES = 300;

// Position calculation offsets
private static final int WALL_COLLISION_LEFT_OFFSET = 7;
private static final int WALL_COLLISION_RIGHT_OFFSET = 14;
private static final int PLAYER_HEIGHT_PIXELS = 24;
private static final int GROUND_SNAP_OFFSET_MULTIPLIER = 3;
private static final int PLATFORM_FALL_THRESHOLD_X = 5;
private static final int PLATFORM_CHECK_X_OFFSET_LEFT = 13;
private static final int PLATFORM_CHECK_X_OFFSET_RIGHT = 1;
private static final int PLATFORM_FALL_OFFSET_X = 2;

// Debug
private static final int DEBUG_LOG_FREQUENCY = 10;
```

---

## Notes

1. **Hardcoded `8` values**: Lines 455, 461, and 486 use hardcoded `8` instead of `PIXELS_PER_TILE`. These should be replaced with the constant.

2. **Inconsistent units**: Some calculations mix pixels and tiles without clear conversion. Consider helper methods like `pixelsToTiles(int pixels)`.

3. **Magic number `300`**: Line 497 has a comment "Dirty trick to make Jean go bottom of the screen" - this should be documented as a constant with explanation.

4. **Collision point array**: The `points[]` array indices could be named constants, but the current 0-7 indexing is relatively clear in context.

5. **Loop bounds**: The loop `for (var n = 4; n < 8; n += 1)` could use named constants for clarity.
