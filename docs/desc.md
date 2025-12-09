## Project Summary

**abbayedesmorts-java** is a Java port of "Abbaye Des Morts" (GPL) using LWJGL 3.3.6 for OpenGL rendering. It's a 2D platformer.

### Architecture

**Core Components:**
- **Entry Point**: `AbbayeMain.java` - Initializes GLFW window, manages game loop, handles input
- **Model Layer**: 
  - `Player.java` - Character with physics, collision detection, movement (walk, jump, crouch)
  - `Enemy.java` - Stub implementation
  - `Stage.java` - Tilemap management (5x5 grid of screens, 32x22 tiles per screen)
  - `Layer.java` - Manages renderables (player, stage, status display)
  - `StatusDisplay.java` - UI rendering (lives, crosses, room titles)
  - `Room.java` - Enum of 25 game rooms

**Graphics System:**
- `GLManager.java` - OpenGL shader program and texture management (two shader programs: "game" and "dialog")
- `StageRenderer.java` - Renders tilemap using OpenGL
- `Texture.java` - Texture loading via STB Image
- Custom shaders (`game.vert/frag`, `splash.vert/frag`)

**Utilities:**
- `Vector2.java` - 2D vector math with tile coordinate conversion
- `BoundingBox2.java` - Collision detection
- `Actor.java` - Sealed interface for game entities (Player, Enemy)
- `Renderable.java` - Interface for renderable objects
- `Clock.java` - Frame timing using GLFW time

**Configuration:**
- `Config.java` - Singleton config with level-based property overrides
- `GameLogger.java` - Logging facade with implementations (StdoutLogger, JulLogger, NoopLogger)

### Build & Dependencies

- **Maven** project, Java 17+
- **LWJGL 3.3.6** (OpenGL, GLFW, STB Image)
- **Jackson** for JSON serialization
- **JUnit 5** + **Mockito** for testing
- **Spotless** for code formatting (Google Java Format)
- **JaCoCo** for code coverage

### Current State

- Player movement, collision detection, and rendering implemented
- Stage/tilemap loading and rendering working
- Status display (lives, crosses) functional
- Enemy system is a stub
- Game dialog system partially implemented (intro splash screen)
- Multiple FIXME comments indicate incomplete features

### Key Features

- Tile-based rendering with texture atlas
- Player physics (gravity, jumping, walking, crouching)
- Collision detection with walls, ground, hazards
- Room-based level system with waypoints
- Configurable logging and graphics (headless mode support)
- Modular architecture with clear separation of concerns

The codebase follows Java best practices with sealed interfaces, records, and a modular structure.

### Collision detection



