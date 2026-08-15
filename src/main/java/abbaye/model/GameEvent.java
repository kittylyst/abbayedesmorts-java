/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

/**
 * Names a game-level occurrence that can be fired by any entity and handled by zero or more
 * registered {@link Runnable}s in {@link TriggerRegistry}. Entities that fire an event have no
 * knowledge of what handlers (if any) respond to it.
 */
public enum GameEvent {
  /** The player has rung the bell in the Tower of the Bell. */
  BELL_RUNG,

  /** The player has touched a heart collectible tile (401–404). */
  HEART_COLLECTED,

  /** The player has touched a cross collectible tile (409–412). */
  CROSS_COLLECTED,

  /** The player has touched a waypoint-cross tile (321–326). */
  WAYPOINT_REACHED,
}
