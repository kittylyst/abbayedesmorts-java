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
- [ ] Enemy behaviour
- [ ] Animation system
- [ ] Full game completion / polish

## Tech Stack

| Concern | Technology |
|---------|-----------|
| Language | Java 17 |
| Build | Maven |
| Windowing / OpenGL | LWJGL3 3.3.6 (GLFW, OpenGL, OpenAL, STB, Assimp) |
| Audio | OpenAL via LWJGL3 |
| Data (maps/config) | Jackson 2.18 (JSON), plain-text map files |
| Formatting | Spotless + Google Java Format 1.23.0 |
| Testing | JUnit Jupiter 5.9.2, Mockito 5.15.2, JaCoCo |

## Architecture

The project uses a **loose ECS** approach — entities are Java objects that hold components,
but the architecture is pragmatic rather than a strict data-oriented ECS framework.

### Main Packages

| Package | Responsibility |
|---------|---------------|
| `abbaye` | Entry point (`AbbayeMain`), top-level config (`Config`), dialog (`GameDialog`) |
| `abbaye.basic` | Core value types: `Actor`, `BoundingBox2`, `Vector2/3f/4f`, `Corners`, `Renderable`, `Clock` |
| `abbaye.model` | Game entities: `Player`, `Enemy`, `Room`, `Stage`, `Layer`, `StatusDisplay`, enums (`Facing`, `Vertical`) |
| `abbaye.graphics` | Rendering: `GLManager`, `StageRenderer`, `Texture`, `Color` |
| `abbaye.logs` | Logger abstraction: `GameLogger`, `JulLogger`, `NoopLogger`, `StdoutLogger` |

### Key Resources

- `src/main/resources/map/map.txt` — room/level layout
- `src/main/resources/map/enemies.txt` — enemy placement data
- `src/main/resources/shaders/` — GLSL vertex and fragment shaders (`game.*`, `splash.*`)
- `src/main/resources/tiles.png` — sprite/tile atlas

### Test Structure

Tests live in `src/test/java/abbaye/`. Game-logic tests (especially collision) run **headless**
(no OpenGL context required). See `TestPlayerCollision`, `TestPlayerCollisionPassing`, `TestRooms`, `TestStage`.
A shared `Utils` test helper exists for common setup.

## Key Stakeholders / Users

- Single developer / hobby project
- Target audience: fans of the original *Abbaye Des Morts* game
