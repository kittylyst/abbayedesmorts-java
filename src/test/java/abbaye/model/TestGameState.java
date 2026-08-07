/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.GameEvent.BELL_RUNG;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestGameState {

  private GameState gameState;

  @BeforeEach
  void setUp() {
    gameState = new GameState();
  }

  @Test
  void freshGameStateHasNoFlagsSet() {
    assertFalse(gameState.isFlagSet(BELL_RUNG));
  }

  @Test
  void setFlagMakesFlagQueryTrue() {
    gameState.setFlag(BELL_RUNG);
    assertTrue(gameState.isFlagSet(BELL_RUNG));
  }

  @Test
  void setFlagIsIdempotent() {
    gameState.setFlag(BELL_RUNG);
    gameState.setFlag(BELL_RUNG);
    assertTrue(gameState.isFlagSet(BELL_RUNG));
  }
}
