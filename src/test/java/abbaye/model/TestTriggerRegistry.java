/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.GameEvent.BELL_RUNG;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestTriggerRegistry {

  private TriggerRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new TriggerRegistry();
  }

  @Test
  void firingWithNoHandlersDoesNothing() {
    // Must not throw
    assertDoesNotThrow(() -> registry.fire(BELL_RUNG));
  }

  @Test
  void registeredHandlerIsCalledOnFire() {
    List<String> calls = new ArrayList<>();
    registry.register(BELL_RUNG, () -> calls.add("rung"));

    registry.fire(BELL_RUNG);

    assertEquals(List.of("rung"), calls);
  }

  @Test
  void handlerIsCalledExactlyOncePerFire() {
    List<Integer> calls = new ArrayList<>();
    registry.register(BELL_RUNG, () -> calls.add(1));

    registry.fire(BELL_RUNG);
    registry.fire(BELL_RUNG);

    assertEquals(2, calls.size());
  }

  @Test
  void multipleHandlersCalledInRegistrationOrder() {
    List<String> calls = new ArrayList<>();
    registry.register(BELL_RUNG, () -> calls.add("first"));
    registry.register(BELL_RUNG, () -> calls.add("second"));

    registry.fire(BELL_RUNG);

    assertEquals(List.of("first", "second"), calls);
  }
}
