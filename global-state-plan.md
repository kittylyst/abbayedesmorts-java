# Global State & Event System Plan

## Top-Level Overview

Introduce a lightweight global state + event system using three new pieces:

1. **`GameState`** — a single mutable object (owned by `Layer`) that holds all
persistent cross-entity facts for a play-through: which waypoint was last
saved, how many lives and crosses the player has, and which named world-state
flags are set (e.g. `BELL_RUNG`).

2. **`GameEvent`** enum — names every game-level occurrence that can trigger
side-effects (`BELL_RUNG`, `CROSS_COLLECTED`, `WAYPOINT_SAVED`, …).

3. **`TriggerRegistry`** — a thin map from `GameEvent → List<Runnable>` owned
by `Layer`. Handlers are registered once at init time in `AbbayeMain` and
fired by entities without any knowledge of what the handlers do.

The bell example is implemented as the first concrete trigger:
- `Player.checkStaticObject()` fires `BELL_RUNG` when it detects contact with
the bell tile (tiles 301–304 in screen 2 / Tower of the Bell).
- A handler registered in `AbbayeMain.initLayer()` calls
`stage.clearTilesWhere(...)` to remove `TILE_DOOR (154)` from the two rooms
that contain a door: screen 10 (ROOM_BEAST, row 17 col 23) and screen 19
(ROOM_RIVER, row 16 col 21). This tile mutation is permanent for the session
because it writes directly into `Stage.stagedata`.

No event bus library is added. The whole system is ~60 lines of new code.

---

## Sub-Tasks

---

### Sub-Task 1 — `GameEvent` enum

**Intent**
Define the vocabulary of named game-level occurrences. Starting with `BELL_RUNG`
but designed so new events can be added by appending enum values.

**Expected Outcomes**
- `abbaye/model/GameEvent.java` exists with at least `BELL_RUNG`.
- Compiles cleanly.
- A trivial test (optional) verifies the enum values are present.

**Todo List**
1. Create `src/main/java/abbaye/model/GameEvent.java`.
2. Declare as a `public enum GameEvent` in package `abbaye.model`.
3. Add value `BELL_RUNG`.
4. Add a Javadoc comment explaining the contract:
*"Names a game-level occurrence that can be fired by any entity and handled
by zero or more registered Runnables in `TriggerRegistry`."*

**Relevant Context**
- Package: `abbaye.model` (same package as `InputEvent`, which follows the same
enum-of-named-events pattern).
- `InputEvent` in `src/main/java/abbaye/model/InputEvent.java` is the model
to follow for style.

**Status** — `[ ] pending`

---

### Sub-Task 2 — `TriggerRegistry`

**Intent**
A thin, testable class that maps `GameEvent` values to lists of `Runnable`
handlers, supports registration, and dispatches synchronously. Owned by `Layer`.

**Expected Outcomes**
- `abbaye/model/TriggerRegistry.java` exists.
- `register(GameEvent, Runnable)` appends a handler.
- `fire(GameEvent)` invokes all registered handlers for that event in
registration order.
- `Layer` holds one `TriggerRegistry` field and exposes a `fireEvent(GameEvent)`
method that delegates to it.
- Unit test(s) in `src/test/java/abbaye/model/` verify:
- Registering and firing a handler calls it exactly once.
- Firing with no registered handlers does nothing (no exception).
- Multiple handlers for the same event are all called in order.

**Todo List**
1. Create `src/main/java/abbaye/model/TriggerRegistry.java`.
2. Internal state: `Map<GameEvent, List<Runnable>> handlers` (EnumMap for efficiency).
3. Implement `register(GameEvent event, Runnable handler)`.
4. Implement `fire(GameEvent event)` — get the list (may be empty), call each handler.
5. Add `private final TriggerRegistry triggers = new TriggerRegistry()` to `Layer`.
6. Add `public void fireEvent(GameEvent event)` to `Layer` — delegates to `triggers.fire(event)`.
7. Add `public void onEvent(GameEvent event, Runnable handler)` to `Layer` — delegates to `triggers.register(event, handler)`.
8. Write `TestTriggerRegistry` in `src/test/java/abbaye/model/`.

**Relevant Context**
- `Layer` is at `src/main/java/abbaye/model/Layer.java`.
- `Layer` already uses `Optional`, `List`, etc. — same style.
- Tests follow the headless pattern in `TestEnemyBehaviour` / `TestPlayerInput`.

**Status** — `[ ] pending`

---

### Sub-Task 3 — `GameState`

**Intent**
A single object that holds all persistent cross-entity facts for a play-through.
Starts with a `Set<GameEvent>` of "fired and acknowledged" flags (so that
`BELL_RUNG` is persistent) plus the fields currently scattered across `Player`
(`lives`, `crosses`, `last` waypoint). The player fields stay on `Player` for
now — they will be migrated in a follow-on task if desired. For this task,
`GameState` only needs the flag set.

**Expected Outcomes**
- `abbaye/model/GameState.java` exists.
- `setFlag(GameEvent)` / `isFlagSet(GameEvent)` methods allow handlers to record
that an event has been permanently acknowledged.
- `Layer` holds one `GameState` and exposes `getGameState()`.
- `AbbayeMain.initLayer()` can access `layer.getGameState()`.

**Todo List**
1. Create `src/main/java/abbaye/model/GameState.java`.
2. Internal state: `Set<GameEvent> flags` (EnumSet).
3. Implement `setFlag(GameEvent)` and `isFlagSet(GameEvent)`.
4. Add `private final GameState gameState = new GameState()` to `Layer`.
5. Add `public GameState getGameState()` accessor to `Layer`.
6. Write at least two unit tests for `GameState` in `TestGameState`:
- A freshly constructed `GameState` has no flags set.
- After `setFlag(BELL_RUNG)`, `isFlagSet(BELL_RUNG)` is true.

**Relevant Context**
- `Layer` is the natural owner because it already owns all entities.
- `GameState` must not import GLFW — keep it in `abbaye.model`.
- `EnumSet.noneOf(GameEvent.class)` is the correct initializer.

**Status** — `[ ] pending`

---

### Sub-Task 4 — Bell tile detection in `Player`

**Intent**
When the player walks into the bell sprite tiles (301–304) in the Tower of the
Bell room, fire `GameEvent.BELL_RUNG` via `layer.fireEvent(...)`. `Player`
must not know what that event does.

**Expected Outcomes**
- `Player.checkStaticObject()` detects tiles 301–304 (all four cells of the
2×2 bell sprite) at the player's feet and fires `BELL_RUNG`.
- The tiles are cleared from the room (like waypoint crosses are cleared
on collection) so the bell cannot be rung twice.
- `Player` has no dependency on door tiles, room indices, or `GameState`.
- A unit test in `TestPlayerInput` or a new `TestBellInteraction` class verifies
that touching a bell tile calls `layer.fireEvent(BELL_RUNG)` (use Mockito to
mock `Layer`).

**Todo List**
1. Add a constant `TILE_BELL_MIN = 300` and `TILE_BELL_MAX = 305` to
`TileAtlas` (range covering tiles 301–304 inclusive, exclusive upper bound).
2. In `Player.checkStaticObject()`, after the waypoint-cross block, add:
```
/* Ring the bell */
if (touching any tile in range (TILE_BELL_MIN, TILE_BELL_MAX) at feet positions) {
	stage.clearTilesWhere(room, t -> t > TILE_BELL_MIN && t < TILE_BELL_MAX);
	layer.fireEvent(GameEvent.BELL_RUNG);
	return true;
}
```
3. Follow exactly the same tile-detection pattern used for waypoint crosses
(two foot positions: `baseTileX` and `baseTileX + 1`, row `baseTileY + 1`).
4. Write the unit test.

**Relevant Context**
- `checkStaticObject()` is at `Player.java` around line 667.
- Bell tiles `301 302` / `303 304` occupy rows 1–2, cols 5–6 of screen 2
(`ROOM_TOWER`). The player's feet touch row `baseTileY + 1`, so the player
standing at tile-row 0 would detect tiles at row 1 — correct.
- The `isBetweenExclusive` helper on `Player` line 733 must be used.
- The `layer` field on `Player` is already present (line 79) but unused — this
is its first real use. Confirm `layer` is non-null in the test via mocking.

**Status** — `[ ] pending`

---

### Sub-Task 5 — Register bell→door handler in `AbbayeMain`

**Intent**
Wire the trigger: when `BELL_RUNG` fires, set the `BELL_RUNG` flag in
`GameState` and clear `TILE_DOOR` from the two rooms that contain a door.
All wiring lives in `AbbayeMain.initLayer()`.

**Expected Outcomes**
- `AbbayeMain.initLayer()` registers exactly one handler on `BELL_RUNG`.
- The handler calls `gameState.setFlag(BELL_RUNG)` (idempotent).
- The handler calls `stage.clearTilesWhere(10, t -> t == TILE_DOOR)` for
screen 10 (ROOM_BEAST, which contains `154` at row 17, col 23).
- The handler calls `stage.clearTilesWhere(19, t -> t == TILE_DOOR)` for
screen 19 (ROOM_RIVER, which contains `154` at row 16, col 21).
- No entity class (`Player`, `Enemy`, `Stage`) is changed in this sub-task.

**Todo List**
1. In `AbbayeMain.initLayer()`, after `layer.setEnemies(enemies)`, add:
```java
var gs = layer.getGameState();
layer.onEvent(GameEvent.BELL_RUNG, () -> {
	gs.setFlag(GameEvent.BELL_RUNG);
	stage.clearTilesWhere(ROOM_BEAST.index(), t -> t == TILE_DOOR);
	stage.clearTilesWhere(ROOM_RIVER.index(), t -> t == TILE_DOOR);
});
```
2. Add the necessary static imports (`Room.*`, `TileAtlas.*`, `GameEvent.*`)
to `AbbayeMain.java`.
3. Verify the handler is idempotent: calling it twice must not break anything
(clearing an already-cleared tile is a no-op in `Stage.clearTilesWhere`).

**Relevant Context**
- `AbbayeMain.initLayer()` is at line 194.
- `ROOM_BEAST.index()` == 10; `ROOM_RIVER.index()` == 19.
- `Stage.clearTilesWhere` is bounds-checked and safe to call for any screen index.
- `TileAtlas.TILE_DOOR = 154`.
- `Room` is already imported via `abbaye.model.*` in `AbbayeMain`.

**Status** — `[ ] pending`

---

## Implementation Note for Agent Mode

Process sub-tasks **in order**: 1 → 2 → 3 → 4 → 5. Each builds on the previous.

After each sub-task:
- Run `mvn test -pl . -q` (headless) to confirm no regressions.
- Update the sub-task status to `[x] done`.
- Record any discovered information (e.g. exact line numbers affected) as
context notes in the relevant following sub-task before moving on.
