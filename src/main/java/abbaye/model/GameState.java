/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Holds persistent cross-entity facts for a single play-through. State here survives room
 * transitions and is the authoritative source for "has X happened yet?" queries.
 *
 * <p>Flags are recorded by calling {@link #setFlag(GameEvent)} from a {@link TriggerRegistry}
 * handler; they are never cleared during normal play.
 */
public final class GameState {

  private final Set<GameEvent> flags = EnumSet.noneOf(GameEvent.class);

  /** Records that {@code event} has been permanently acknowledged. Idempotent. */
  public void setFlag(GameEvent event) {
    flags.add(event);
  }

  /** Returns {@code true} if {@code event} has been acknowledged via {@link #setFlag}. */
  public boolean isFlagSet(GameEvent event) {
    return flags.contains(event);
  }
}
