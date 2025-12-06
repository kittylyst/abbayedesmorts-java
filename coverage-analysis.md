# Test Coverage Analysis

## Current Coverage Summary
- **Overall**: 25% instruction coverage, 16% branch coverage
- **Total**: 5,533 missed instructions, 506 missed branches

## High Priority Areas for Test Coverage Improvement

### 1. Player Class (35% coverage, 282 missed branches)
**Critical business logic not covered:**

- **Movement and Physics**:
- `newPosition()` - Complex movement logic with jump mechanics, walking, crouching
- `update()` - Room boundary transitions, screen wrapping logic
- `moveCallback()` - Input handling for all key states (PRESS/RELEASE for all directions)
- Jump mechanics: height calculation, fall transitions, collision during jump
- Crouch movement speed differences
- Animation state management during movement

- **Collision Detection** (282 missed branches):
- `checkCollision()` - Complex wall collision detection with multiple edge cases:
	- Left/right collisions (standing vs crouching)
	- Ground collision detection with gravity
	- Roof collision during jumps
	- Special platform logic (tile 38)
	- Room-specific collision exceptions (ROOM_CAVE, ROOM_BEAST)
	- Invisible walls and ground
- `checkStaticHazard()` - Hazard detection (tile type 5)
- `checkStaticObject()` - Collectible detection:
	- Hearts (tiles 400-404)
	- Crosses (tiles 408-412)
	- Waypoint crosses (tiles 320-326)
	- Room-specific logic (ROOM_ASHES)

- **State Management**:
- Lives management and respawn logic
- Waypoint system updates
- Direction changes (LEFT/RIGHT)
- Jump state transitions (JUMP → FALL → NEUTRAL)

### 2. Stage Class (17% coverage, 61 missed branches)
**Critical functionality not covered:**

- **Map Loading**:
- `load(String mapResource)` - Resource loading from different sources
- Error handling for malformed map files
- Map parsing logic (3-character tile IDs)

- **Room Navigation**:
- `moveLeft()`, `moveRight()`, `moveUp()`, `moveDown()` - Boundary checking
- `getRoom()` - Room index calculation
- `toWaypoint()` - Waypoint restoration

- **Tile Rendering**:
- `getCorners(int tileType)` - Complex tile type mapping logic:
	- Different tile ranges (0-99, 100-199, 200-299, 300-399, 400-499, 500-599, 600-650)
	- Special tiles (doors, hearts, crosses, cup)
	- 16-bit mode handling
	- Texture coordinate calculation
- `getCorners(int x, int y)` - Tile coordinate to texture coordinate conversion
- Cache management for tile corners

### 3. Layer Class (50% coverage, 5 missed branches)
**Missing coverage:**

- `update()` - Full update cycle with collision detection
- `render()` - Rendering order verification
- Error handling in update loop (try-catch around player updates)
- Debug logging functionality

### 4. Config Class (37% coverage, 28 missed branches)
**Missing coverage:**

- **Property Resolution**:
- Level-based property overrides (`.level1`, `.level2`, etc.)
- Property type parsing (int, float, boolean, string)
- Error handling for invalid property values
- Default value fallback logic

- **Logger Initialization**:
- Different logger types (stdout, jul, noop)
- Log level parsing and validation

- **Configuration Loading**:
- Resource-based loading
- File-based loading
- Error handling for missing/invalid config files

### 5. Zero Coverage Classes (Critical Business Logic)

#### StatusDisplay (0% coverage)
- `init()` - Glyph initialization for digits and room titles
- `render()` - Complete rendering pipeline:
- Heart and cross icon rendering
- Text rendering for lives and crosses
- Room title rendering
- `renderText()` - Text rendering with line breaks
- `renderStaticTitle()` - Static tile rendering
- `getWidth()`, `getHeight()` - Text dimension calculations

#### GameDialog (0% coverage)
- `render()` - State-based rendering (START, END, INACTIVE)
- `startTurn()` - Game state transition
- `reset()` - Dialog state reset
- Input handling for dialog interactions

#### StageRenderer (0% coverage)
- `init()` - Renderer initialization
- `render()` - Complete tilemap rendering:
- Viewport updates
- Orthographic projection setup
- Tile-by-tile rendering loop

#### Texture (0% coverage)
- `of(String path, boolean isResource, boolean shouldFlip)` - Texture loading
- `loadResourceAsBuffer()` - Resource to ByteBuffer conversion
- Error handling for missing resources
- STB Image integration

#### Clock (0% coverage)
- `init()` - Timer initialization
- `updateTimer()` - Frame timing calculations
- FPS calculation logic

#### BoundingBox2 (0% coverage)
- `overlaps()` - Collision detection between bounding boxes
- Edge calculation methods (left, right, top, bottom)

### 6. Low Coverage Utility Classes

#### Vector2 (5% coverage)
- `magnitude()`, `normalize()`, `scale()` - Vector math operations
- `tileX()`, `tileY()` - Coordinate conversion

#### Vector3f, Vector4f (0% coverage)
- All vector math operations (add, subtract, scale, dot, cross, lerp)
- These are utility classes but may be used in future graphics work

#### GLManager (29% coverage, 7 missed branches)
- Shader compilation and linking
- Texture binding and management
- Matrix operations (translation, scale, multiplication, orthographic)
- VAO/VBO/EBO management
- Error handling for shader compilation failures

### 7. AbbayeMain (8% coverage, 31 missed branches)
**Critical initialization and game loop:**

- `glInit()` - Complete GLFW window initialization
- `loop()` - Main game loop with rendering and update cycles
- `initLayer()` - Complete game layer setup
- `cleanup()` - Resource cleanup
- Error handling throughout initialization

## Detailed Method-Level Coverage Gaps

### Player Class Methods (by coverage %)

**Well Covered:**
- `checkStaticHazard()` - 97% coverage (only 6 missed branches)
- Constructor and factory methods - 100% coverage

**Partially Covered:**
- `checkCollision()` - 49% instruction, 20% branch (150 missed branches, 531 missed instructions)
- Most complex method with many conditional paths
- Crouch vs standing collision logic
- Room-specific exceptions (ROOM_CAVE, ROOM_BEAST)
- Ground, roof, and wall collision branches
- `checkStaticObject()` - 22% instruction, 10% branch (57 missed branches)
- Heart collection logic (ROOM_ASHES special case)
- Cross collection
- Waypoint cross updates
- `newPosition()` - 22% instruction, 17% branch (33 missed branches)
- Jump mechanics (height < 56, height < 44)
- Movement with collision checks
- Crouch vs normal movement speed
- Animation state management
- `update()` - 24% instruction, 25% branch (12 missed branches)
- Room boundary transitions (all 4 directions)
- Screen wrapping logic

**Not Covered (0%):**
- `render()` - 0% (312 missed instructions, 6 missed branches)
- `moveCallback()` lambda - 0% (57 missed instructions, 15 missed branches)
- All key press/release handlers
- Direction changes
- Walk state management
- Crouch state management
- Jump state management
- `playerMatrix()` - 0% (25 missed instructions, 2 missed branches)
- `toString()` - 0% (45 missed instructions)
- All getters: `getBB()`, `getSize()`, `getV()`, `getDirection()`, `getLives()`, `getCrosses()`
- `destroy()` - 0%

## Recommended Test Priorities

### Priority 1: Core Game Logic
1. **Player collision detection** - Most complex logic with many branches
2. **Player movement and physics** - Core gameplay mechanics
3. **Stage tile rendering** - Complex tile type mapping
4. **Config property resolution** - Used throughout the application

### Priority 2: Rendering Pipeline
1. **StatusDisplay** - UI rendering
2. **StageRenderer** - Tilemap rendering
3. **GameDialog** - Dialog system

### Priority 3: Utilities
1. **BoundingBox2** - Collision utilities
2. **Vector2** - Math utilities
3. **Clock** - Timing utilities

### Priority 4: Infrastructure
1. **Texture loading** - Resource management
2. **GLManager** - Graphics infrastructure
3. **AbbayeMain** - Application lifecycle

## Testing Strategy Recommendations

1. **Unit Tests for Business Logic**:
- Player movement and collision (can test without GL)
- Stage tile mapping and room navigation
- Config property resolution

2. **Integration Tests**:
- Layer update and render cycles
- Complete game initialization flow

3. **Mock-Based Tests**:
- Rendering classes (mock GL calls)
- Texture loading (mock file I/O)
- GLManager operations

4. **Headless Mode Tests**:
- Use `AbbayeMain.setGlEnabled(false)` for tests that don't need rendering
- Test game logic without OpenGL initialization

## Progress Tracking

### Completed Areas
_Update this section as test coverage is improved_

- [x] Player collision detection (`checkCollision()`) - Test infrastructure created, 24 test cases added covering:
- Basic collision scenarios (empty space, walls, ground, roof)
- Special tiles (128, 344, 348, platform 38)
- Crouch vs standing collisions
- Room-specific logic (ROOM_CAVE, ROOM_BEAST, ROOM_LAKE)
- Boundary conditions
- Note: Some tests need refinement based on actual collision detection behavior
- [ ] Player movement and physics (`newPosition()`, `update()`)
- [ ] Player input handling (`moveCallback()`)
- [ ] Player static object detection (`checkStaticObject()`)
- [ ] Stage tile rendering (`getCorners()`, room navigation)
- [ ] StatusDisplay rendering and initialization
- [ ] GameDialog state management
- [ ] StageRenderer tilemap rendering
- [ ] Config property resolution
- [ ] Layer update and render cycles
- [ ] BoundingBox2 collision utilities
- [ ] Vector2 math operations
- [ ] Clock timing functionality

### In Progress
_List areas currently being worked on_

- Player collision detection tests - Some tests need adjustment for precise collision detection logic

### Notes
_Add notes about test implementation strategies, challenges, or decisions made during testing_

**Player Collision Testing (2025-12-06)**:
- Created `TestPlayerCollision.java` with 24 comprehensive test cases
- Used reflection to access private fields (direction, crouch, jump, height) since no public setters exist
- Tests cover: wall collisions (left/right, standing/crouching), ground/gravity, roof collisions, special tiles, room-specific logic, boundary conditions
- Challenge: Collision detection has very specific distance and position requirements that need careful test setup
- Some tests currently failing due to precise collision detection logic - need refinement based on actual behavior
- Test infrastructure established with helper methods for tile manipulation and state setting

---

## Keeping This Document Updated

To keep this coverage analysis current:

1. **After adding tests**: Run `mvn clean test jacoco:report` and review the updated coverage
2. **Update Progress Tracking**: Mark completed areas and add notes about implementation
3. **Regenerate coverage stats**: Use the command below to get updated coverage percentages:
```bash
mvn clean test jacoco:report -q && \
cat target/site/jacoco/jacoco.csv | \
awk -F',' 'NR>1 {missed=$4; covered=$5; total=missed+covered; if(total>0) {cov=(covered/total)*100; if(cov<50 && total>50) print $2"."$3": "cov"% coverage ("missed" missed, "covered" covered)"}}' | \
sort -t: -k2 -n
```
4. **Review method-level coverage**: Check `target/site/jacoco/index.html` for detailed method coverage
5. **Commit updates**: Keep the document in sync with test improvements

**Last Updated**: 2025-12-06 (Initial analysis)
