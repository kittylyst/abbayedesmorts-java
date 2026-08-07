/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Maps {@link GameEvent} values to lists of {@link Runnable} handlers. Handlers are registered at
 * init time and fired synchronously in registration order when an event is dispatched.
 *
 * <p>Firing an event with no registered handlers is a no-op.
 */
public final class TriggerRegistry {

  private final Map<GameEvent, List<Runnable>> handlers = new EnumMap<>(GameEvent.class);

  /**
   * Registers {@code handler} to be called whenever {@code event} is fired. Multiple handlers for
   * the same event are called in registration order.
   */
  public void register(GameEvent event, Runnable handler) {
    handlers.computeIfAbsent(event, e -> new ArrayList<>()).add(handler);
  }

  /**
   * Invokes all handlers registered for {@code event}, in registration order. Does nothing if no
   * handlers have been registered for the event.
   */
  public void fire(GameEvent event) {
    var list = handlers.get(event);
    if (list != null) {
      for (var handler : list) {
        handler.run();
      }
    }
  }
}
