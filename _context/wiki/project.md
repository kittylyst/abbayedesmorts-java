# Project Overview

## What This Project Is

A Java port of [Abbaye Des Morts GPL](https://github.com/nevat/abbayedesmorts-gpl), a 2D platform game originally written in C.
The port is a clean rewrite using modern Java idioms and an object-oriented / ECS architecture rather than a direct translation of the C source.

**Author**: Ben Evans (@kittylyst)
**License**: GPL (inherited from the original)

## Goals and Objectives

- Port the complete game (all rooms, enemies, mechanics) to Java/LWJGL3
- Redesign the codebase with a loose ECS architecture — not a line-for-line C translation
- Add features incrementally: collision detection and player control first, then enemies and animation

## Current Status

Mid-stage work-in-progress:

- [x] Basic collision detection
- [x] Player control
- [x] Input decoupled from domain (P1 — `InputEvent` enum + `InputHandler`)
- [x] `Vector2` decoupled from `Stage` (P2 — `Stage.toTileX/Y` helpers)
- [x] `GLManager` static initialiser removed (P3 — explicit `GLManager.initAll()`)
- [x] Cross-platform Maven build (Mac arm64/x86_64, Linux x86_64/arm64 profiles)
- [x] Build toolchain upgraded to Java 21 / compiler-plugin 3.13.0 / JaCoCo 0.8.12
- [x] Test coverage expanded: `BoundingBox2`, `Vector2`, `Config` (54 new tests)
- [x] `mvn compile exec:exec` run target — spawns a fresh `java` process so `-XstartOnFirstThread` is a real JVM flag (not a program arg) on macOS profiles; note `exec:java` no longer works (no `mainClass` configured)
- [x] `TILE_*` constants migrated to `TileAtlas` (public, canonical); removed from `Stage`
- [x] `TILES_PER_ROW` / `TILES_PER_COL` moved to `TileAtlas`; circular `Stage` ↔ `TileAtlas` import eliminated
- [x] `Stage` no longer imports GLFW (dead import removed; model invariant now enforced)
- [x] `Player.getCollisions()` removed; replaced with typed `isCollidingUp/Down/Left/Right()` accessors
- [x] `InputHandler` TAB/SPACE guarded by `gameDialog.isActive()` — no longer fires mid-game; `DEBUG_DUMP` now reachable
- [x] `Stage.clearTilesWhere()` bounds-checked to match `clearTile()` (consistent, no AIOOBE on bad screen index)
- [x] `EnemyType` enum fully populated (13 concrete values + `UNKNOWN`; codes, sizes from C source)
- [x] `EnemyData` record — raw parsed slot from `enemies.txt`; `fromFields(int[])` factory
- [x] `Stage.loadEnemies()` — parses all 25 screens × 7 slots; called automatically from `Stage.load()`
- [x] `Stage.buildEnemies(int screen)` — constructs `List<Enemy>` for a screen, converting C pixel coords to Java world coords
- [x] `Layer` wired to enemy list — `update()` and `render()` loop over enemies each frame
- [x] `AbbayeMain.initLayer()` populates enemies from initial room on startup
- [x] `Stage` room transitions now refresh the active enemy list — screen changes and waypoint teleports rebuild enemies for the new room and push them into the current `Layer`
- [x] `docs/ENEMIES_FORMAT.md` — authoritative field-by-field format reference decoded from C GPL source
- [x] 29 new tests: `TestEnemyType` (9), `TestEnemyParsing` (20); test baseline now 141 passing, 5 skipped
- [x] `Enemy.update()` — patrol movement for types 1–9: advance in current direction by `patrolSpeed` per tick, reverse at `limitLeft`/`limitRight`; types ≥ 10 are stationary stubs
- [x] `Enemy.hitBox()` / `Player.hitBox()` — bounding boxes derived from adjust offsets (enemy) and C body dimensions (player); used for contact detection
- [x] `Player.onEnemyContact()` — decrements lives and respawns player at last waypoint, mirroring C `jean.death = 1` path
- [x] `Layer.checkEnemyContact()` — per-tick overlap check wired into `Layer.update()`; stops after first contact per tick
- [x] `TestEnemyBehaviour` (9 tests) — patrol direction/reversal, non-patrol no-movement, hit box offsets, contact → life loss, contact → waypoint teleport; test baseline now 150 passing, 5 skipped
- [ ] Enemy movement behaviour (gravity, type-specific logic for non-patrol types ≥ 10)
- [ ] Crouching collision — uncomment branch, fix unit-mismatch bug, re-enable 5 disabled tests
- [ ] Animation system (player walk/jump/crouch frames; enemy frame cycling)
- [ ] Full game completion / polish

## Tech Stack

| Concern | Technology |
|---------|-----------|
| Language | Java 21 |
| Build | Maven (cross-platform profiles); requires a Java 21 JDK — set `JAVA_HOME` to the local install, whose location is OS- and installation-specific (currently `/Library/Java/JavaVirtualMachines/java21/Contents/Home` on this machine); run game with `mvn compile exec:exec` |
| Windowing / OpenGL | LWJGL3 3.3.6 (GLFW, OpenGL, OpenAL, STB, Assimp) |
| Audio | OpenAL via LWJGL3 |
| Data (maps/config) | Jackson 2.18 (JSON), plain-text map files |
| Formatting | Spotless 2.44.5 + Google Java Format 1.27.0 |
| Testing | JUnit Jupiter 5.9.2, Mockito 5.15.2, JaCoCo 0.8.12 |

## Architecture

The project uses a **loose ECS** approach — entities are Java objects that hold components,
but the architecture is pragmatic rather than a strict data-oriented ECS framework.

### Main Packages

| Package | Responsibility |
|---------|---------------|
| `abbaye` | Entry point (`AbbayeMain`), top-level config (`Config`), dialog (`GameDialog`), input translation (`InputHandler`) |
| `abbaye.basic` | Core value types: `Actor`, `BoundingBox2`, `Vector2/3f/4f`, `Corners`, `Renderable`, `Clock` |
| `abbaye.model` | Game entities: `Player`, `Enemy`, `Room`, `Stage`, `Layer`, `StatusDisplay`, enums (`EnemyType`, `Facing`, `Vertical`, `InputEvent`) |
| `abbaye.graphics` | Rendering: `GLManager`, `StageRenderer`, `Texture`, `Color` |
| `abbaye.logs` | Logger abstraction: `GameLogger`, `JulLogger`, `NoopLogger`, `StdoutLogger` |

### Input Architecture (post-P1)

```
GLFW key event
      │
      ▼
InputHandler          ← sole owner of GLFWKeyCallbackI; lives in abbaye package
      │
      ├─ ESC          → glfwSetWindowShouldClose
      ├─ TAB/SPACE    → GameDialog.startTurn()
      └─ arrows/down  → Player.handleInput(InputEvent)
                               │
                               ▼
                        Player internal state
                        (walk, direction, crouch, jump)
                        — no GLFW import in Player
```

### Key Design Invariants

- `Player`, `Enemy`, and all `abbaye.model` / `abbaye.basic` classes **must not import GLFW types**.
- `Vector2` **must not import** `abbaye.model.Stage`. Use `Stage.toTileX(float)` / `Stage.toTileY(float)` for pixel→tile conversion.
- `GLManager.initAll()` **must be called after** `GL.createCapabilities()` in `AbbayeMain.glInit()`. Never call it from a static initialiser.
- All game-logic tests **must run headless** (no OpenGL/GLFW context). Follow `TestPlayerCollision`, `TestPlayerInput` patterns.
- All tile-type integer constants (`TILE_*`) live in `TileAtlas` (public). `Stage` imports them via `import static abbaye.model.TileAtlas.*`. Do not re-declare them elsewhere.
- Texture atlas grid dimensions (`TILES_PER_ROW`, `TILES_PER_COL`) also live in `TileAtlas`. `Stage` does **not** declare them; `TileAtlas` does **not** import `Stage`.
- `Player` exposes collision state via `isCollidingUp/Down/Left/Right()` boolean accessors. The raw `int[] collision` array is internal; `getCollisions()` has been removed.

### Key Resources

- `src/main/resources/map/map.txt` — room/level layout
- `src/main/resources/map/enemies.txt` — enemy placement data
- `src/main/resources/shaders/` — GLSL vertex and fragment shaders (`game.*`, `splash.*`)
- `src/main/resources/tiles.png` — sprite/tile atlas

### Test Structure

Tests live in `src/test/java/abbaye/`. Game-logic tests run **headless** (no OpenGL context required).

| Test class | What it covers |
|---|---|
| `TestBoundingBox2` | `left/right/top/bottom` edges, `overlaps` (all cases), record equality |
| `TestConfig` | Default properties, all getters/defaults, all logger sinks, level/highScore mutation, headless override, singleton guards |
| `TestEnemyBehaviour` | `Enemy.update()` patrol movement (direction, boundary reversal, non-patrol no-move); `Enemy.hitBox()` adjust offsets; `Layer` enemy–player contact → life loss and waypoint teleport |
| `TestEnemyParsing` | `Stage.loadEnemies()` slot counts, all 15 field values for key screens, `buildEnemies()` position scaling and direction mapping |
| `TestEnemyType` | `fromCode()` for all known codes, fallback to `UNKNOWN`, `code` field, sprite sizes for all 5 size categories |
| `TestGameDialog` | `currentSplashPage()` — splash page flip every 5 s, wrap-around at 10 s |
| `TestPlayerCollision` | Wall, roof, ground, platform collision (5 tests `@Disabled` pending crouch implementation) |
| `TestPlayerCollisionPassing` | Pass-through tile behaviour |
| `TestPlayerInput` | `InputEvent` → `Player` state (headless, no GLFW) |
| `TestRooms` | Room navigation (all passing) |
| `TestStage` | Stage loading and tile data |
| `TestStageMutation` | Stage tile mutation helpers |
| `TestTileAtlas` | UV range, caching, specific tile mappings |
| `TestVector2` | `magnitude`, `normalize`, `scale`, record equality |
| `Utils` | Shared test helpers (reflection-based field access, tile setup) |

### Disabled Tests

5 tests in `TestPlayerCollision` are `@Disabled`:

- 4 × crouching collision tests — blocked on `Player.checkCollisions()` crouch branch being commented out
- 1 × `testSmallPlatformTile38FallLeft` — needs a clear contract before re-enabling

These are **not regressions**; they are intentionally deferred until crouching is implemented.

## Known Pre-existing Issues

- ~~`TestRooms.testRoomSwitchRightLeft` and `testRoomNoSwitchLeft` fail with a room-coordinate mismatch~~ — **resolved**: both tests pass as of the `bob_initial_refactor` branch.
- `Player.checkStaticObject()` line `Stage.toTileX(pos.x()) > 160` compares a tile index (0–31) against 160 — always false. Original C compared pixel coordinates. Marked `// FIXME`; correct tile-column threshold TBD.
- ~~`TestGLManager.testMatrices()` was not truly headless: it called `AbbayeMain.glStaticInit()` → `glfwInit()`, which needs macOS window-server access, so in a GUI-less environment (CI, sandboxed shell) the suite would *hang* in `[NSApplication run]` rather than fail~~ — **resolved**: the `glStaticInit()`/`setHeadless()` calls were dead weight (the test only exercises pure matrix math) and have been removed; the whole suite is now genuinely headless. Beware reintroducing `glfwInit()` in tests.

## Key Stakeholders / Users

- Single developer / hobby project
- Target audience: fans of the original *Abbaye Des Morts* game
