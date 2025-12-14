# Collision Detection Algorithm in Player Class

## Overview

The `Player` class implements a tile-based collision detection system for a 2D platformer game. The algorithm uses discrete collision points sampled from the player's bounding box to check for collisions with solid tiles in the game world.

The tiles are 8x8 pixels, and the Player and other objects use a `Vector2` which represents the position in pixels.

## Main Method: `calculateCollision()`

The collision detection is performed by the `calculateCollision()` method (lines 406-620), which:
1. Calculates collision points based on player position
2. Checks collisions in four directions (Up, Down, Left, Right)
3. Handles special cases (crouching, platforms, invisible walls/ground)
4. Updates player position based on ground collision

## Collision Point Calculation

The algorithm uses 8 discrete collision points derived from the player's position.

```java
points[0] = (pos.x() + 1*8) / 8  // Left edge X (tile column)
points[1] = (pos.x() + 7*8) / 8  // Left mid X
points[2] = (pos.x() + 8*8) / 8  // Center X
points[3] = (pos.x() + 13*8) / 8 // Right edge X
points[4] = (pos.y() + 1*8) / 8  // Top edge Y (tile row)
points[5] = (pos.y() + 8*8) / 8  // Mid height Y
points[6] = (pos.y() + 15*8) / 8 // Lower mid Y
points[7] = (pos.y() + 23*8) / 8 // Bottom edge Y
```

These points represent key positions on the player's bounding box, converted to tile coordinates.

## Collision Types

### 1. Left & Right Wall Collisions (Standing)

**Lines 435-484**: When the player is standing (not crouching):

1. **Boundary Check**: Validates that collision points are within screen bounds
- `points[0] > 0` (not at left edge)
- `points[3] + 1 < NUM_COLUMNS` (not at right edge)
- `points[n] + 1 < NUM_ROWS` (not at bottom)

2. **Direction Check**: Only checks collisions in the direction the player is moving
- Left collision: only when `direction == LEFT` and `points[0] > 0`
- Right collision: only when `direction == RIGHT` and `points[3] + 1 < NUM_COLUMNS`

3. **Tile Check**: For each vertical position (n = 4, 5, 6, 7):
- Checks tile at `points[n][points[0] - 1]` for left collision
- Checks tile at `points[n][points[3] + 1]` for right collision

4. **Solid Tile Detection**: A tile is considered solid if:
- `tile > 0 && tile < TILE_SOLID_MAX (100)` AND
- `tile != TILE_PASSABLE (16)` AND
- `tile != TILE_PLATFORM (38)` AND
- `tile != TILE_PASSABLE_VARIANT_1 (37)`
- OR it's a special collision tile (TILE_SPECIAL_COLLISION, TILE_SPECIAL_LEFT, TILE_SPECIAL_RIGHT)

5. **Distance Check**: If a solid tile is found, calculates distance to determine if collision should trigger:
- **Left**: `pos.x() - ((points[0] - 1) * 8 + 7) < 1.1` ✓ (all pixels)
- **Right**: `((points[3] + 1) * 8) - (pos.x() / 8 + 15) < 1.1` ⚠️ **BUG: Mixes pixels and tiles**
	- `(points[3] + 1) * 8` = pixels
	- `pos.x() / 8` = tiles (converted from pixels)
	- `15` = pixels (WALL_COLLISION_RIGHT_OFFSET)
	- This creates incorrect distance calculations

### 2. Left & Right Wall Collisions (Crouching)

**Lines 486-529**: When the player is crouching:

1. Uses a different vertical position: `r = (pos.y() + 16*8) / 8` (crouch height)
2. Checks tiles at `r[points[0] - 1]` and `r[points[3] + 1]`
3. Similar solid tile detection logic
4. **Distance Check**:
- **Left**: `pos.x() - ((points[0] - 1) * 8 + 7) < 1.1` ✓ (all pixels)
- **Right**: `((points[3] + 1) * 8) - (pos.x() / 8 + 15) < 1.1` ⚠️ **BUG: Mixes pixels and tiles**
	- Same unit mismatch as standing collision

5. **Invisible Wall Override**: Special handling for certain rooms:
- `ROOM_CAVE`: Clears collision at row 5, columns 0-1
- `ROOM_BEAST`: Clears collision at row 5, columns 27-31

### 3. Ground Collision

**Lines 531-575**: Handles player falling and landing:

1. **Ground Tile Check**: Checks four tiles below the player's bottom edge:
- `blground[0] = currentRoomData[points[7] + 1][points[0]]`
- `blground[1] = currentRoomData[points[7] + 1][points[1]]`
- `blground[2] = currentRoomData[points[7] + 1][points[2]]`
- `blground[3] = currentRoomData[points[7] + 1][points[3]]`

2. **Invisible Ground**: Special cases where ground doesn't exist:
- `ROOM_CAVE`: Row > 19, column 2
- `ROOM_LAKE`: Y < 4 tiles, column 2
- Player falls through these areas

3. **Ground Detection**: If any of the four tiles is solid:
- Calculates ground position: `ground = (points[7] + 1) * 8`
- Special case: If `points[7] + 1 > 21`, teleports to bottom (300 tiles down)

4. **Snap to Ground**: If player is close to ground:
- Distance check: `ground - pos.y() - 24 > gravity * 8`
- If close enough: Snaps player to ground position
- Resets jump state and height

5. **Falling**: If no ground detected, applies gravity

### 4. Platform Collision

**Lines 577-597**: Handles special platform tiles (tile 38) that can be fallen through:

1. **Left Direction**: If moving left and standing on platform:
- Checks if player's right edge is past platform threshold
- If so, player falls through platform

2. **Right Direction**: If moving right and standing on platform:
- Checks if player's left edge is past platform threshold
- If so, player falls through platform

### 5. Roof Collision

**Lines 599-619**: Only checked when player is jumping (`jump == JUMP`):

1. Checks two tiles above player's top edge:
- `blroof[0] = currentRoomData[points[4] - 1][points[0]]`
- `blroof[1] = currentRoomData[points[4] - 1][points[3]]`

2. Solid tile detection (same rules as wall collision)

3. **Distance Check**: `(pos.y() - 1) - ((points[4] - 1) * 8 + 7) < 1.0`
- Uses `COLLISION_ROOF_DISTANCE_THRESHOLD = 1.0` (slightly different from wall threshold)

## Collision State

The algorithm maintains a collision state array:
```java
collision[COLLISION_UP] = 0 or 1
collision[COLLISION_DOWN] = 0 or 1  // Unused, handled by gravity
collision[COLLISION_LEFT] = 0 or 1
collision[COLLISION_RIGHT] = 0 or 1
```

This state is reset at the start of each `calculateCollision()` call and updated as collisions are detected.

## Key Constants

- **Collision Thresholds**:
- `COLLISION_DISTANCE_THRESHOLD = 1.1` pixels (walls)
- `COLLISION_ROOF_DISTANCE_THRESHOLD = 1.0` pixels (roof)

- **Player Dimensions**:
- Width: 13 tiles (104 pixels) from left edge to right edge
- Height: 23 tiles (184 pixels) from top to bottom
- Collision offsets: Left = 7 pixels, Right = 15 pixels

- **Tile Types**:
- `TILE_PASSABLE = 16`: Can walk through
- `TILE_PLATFORM = 38`: Can stand on, but can fall through when moving
- `TILE_SOLID_MAX = 100`: Tiles < 100 are generally solid
- Special tiles: 128, 342-351 (special collision behaviors)

## Algorithm Flow

1. **Calculate collision points** from player position
2. **Reset collision state** to all zeros
3. **Check left/right collisions** (standing or crouching)
4. **Check ground collision** and apply gravity/snapping
5. **Check platform collision** (fall-through logic)
6. **Check roof collision** (if jumping)

## Known Issues

1. **Unit Mismatch Bug** (Lines 477, 508): Right collision distance calculation mixes pixels and tiles:
- **Current (buggy)**: `((points[3] + 1) * PIXELS_PER_TILE) - (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)`
- **Should be**: `((points[3] + 1) * PIXELS_PER_TILE) - (pos.x() + WALL_COLLISION_RIGHT_OFFSET)`
- **Problem**: `pos.x() / PIXELS_PER_TILE` converts pixels to tiles, but `WALL_COLLISION_RIGHT_OFFSET` is in pixels, creating a mixed-unit calculation
- **Impact**: Causes incorrect distance calculations for right-side collisions, potentially missing collisions or triggering false positives
- **Status**: ⚠️ **Still present in current code** (not yet fixed)

2. **Direction Dependency**: Collision checks are only performed when the player is moving in that direction, which may miss collisions from other sources (e.g., being pushed).

3. **Discrete Sampling**: The algorithm uses discrete points rather than continuous collision detection, which could miss collisions in edge cases.

## Usage

The collision detection is called via:
- `calculateCollision()`: Performs all collision checks and updates state
- `checkCollision()`: Calls `calculateCollision()` and returns true if any collision detected

The collision state is then used in `newPosition()` to prevent movement in directions where collisions are detected.
