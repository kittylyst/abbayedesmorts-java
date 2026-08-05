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
- [ ] Enemy behaviour
- [ ] Animation system
- [ ] Full game completion / polish

## Tech Stack

| Concern | Technology |
|---------|-----------|
| Language | Java 21 |
| Build | Maven (cross-platform profiles); requires `JAVA_HOME=/opt/jdk-21.0.2+13` on this machine |
| Windowing / OpenGL | LWJGL3 3.3.6 (GLFW, OpenGL, OpenAL, STB, Assimp) |
| Audio | OpenAL via LWJGL3 |
| Data (maps/config) | Jackson 2.18 (JSON), plain-text map files |
| Formatting | Spotless + Google Java Format 1.23.0 |
| Testing | JUnit Jupiter 5.9.2, Mockito 5.15.2, JaCoCo 0.8.12 |

## Architecture

The project uses a **loose ECS** approach — entities are Java objects that hold components,
but the architecture is pragmatic rather than a strict data-oriented ECS framework.

### Main Packages

| Package | Responsibility |
|---------|---------------|
| `abbaye` | Entry point (`AbbayeMain`), top-level config (`Config`), dialog (`GameDialog`), input translation (`InputHandler`) |
| `abbaye.basic` | Core value types: `Actor`, `BoundingBox2`, `Vector2/3f/4f`, `Corners`, `Renderable`, `Clock` |
| `abbaye.model` | Game entities: `Player`, `Enemy`, `Room`, `Stage`, `Layer`, `StatusDisplay`, enums (`Facing`, `Vertical`, `InputEvent`) |
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

## Key Stakeholders / Users

- Single developer / hobby project
- Target audience: fans of the original *Abbaye Des Morts* game
