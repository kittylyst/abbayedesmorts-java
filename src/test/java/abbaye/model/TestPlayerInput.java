/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.Facing.LEFT;
import static abbaye.model.Facing.RIGHT;
import static abbaye.model.InputEvent.*;
import static abbaye.model.Vertical.*;
import static org.junit.jupiter.api.Assertions.*;

import abbaye.AbbayeMain;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Headless tests for Player.handleInput(). No GL/GLFW context required. */
public class TestPlayerInput {

  private Stage stage;
  private Layer layer;
  private Player player;

  @BeforeAll
  public static void setUpBeforeClass() {
    AbbayeMain.setGlEnabled(false);
  }

  @BeforeEach
  public void setUp() {
    stage = Stage.of();
    layer = new Layer();
    player = Player.of(layer, stage);
    layer.setPlayer(player);
    layer.setStage(stage);
    layer.init();
  }

  @Test
  public void moveRightStartSetsDirectionAndWalk() {
    player.handleInput(MOVE_RIGHT_START);
    assertEquals(RIGHT, player.getDirection());
    assertTrue((Boolean) Utils.getPrivateField(player, "walk"));
  }

  @Test
  public void moveRightEndClearsWalk() {
    player.handleInput(MOVE_RIGHT_START);
    player.handleInput(MOVE_RIGHT_END);
    assertEquals(RIGHT, player.getDirection());
    assertFalse((Boolean) Utils.getPrivateField(player, "walk"));
  }

  @Test
  public void moveLeftStartSetsDirectionAndWalk() {
    player.handleInput(MOVE_LEFT_START);
    assertEquals(LEFT, player.getDirection());
    assertTrue((Boolean) Utils.getPrivateField(player, "walk"));
  }

  @Test
  public void moveLeftEndClearsWalk() {
    player.handleInput(MOVE_LEFT_START);
    player.handleInput(MOVE_LEFT_END);
    assertEquals(LEFT, player.getDirection());
    assertFalse((Boolean) Utils.getPrivateField(player, "walk"));
  }

  @Test
  public void crouchStartSetsCrouch() {
    player.handleInput(CROUCH_START);
    assertTrue((Boolean) Utils.getPrivateField(player, "crouch"));
  }

  @Test
  public void crouchEndClearsCrouch() {
    player.handleInput(CROUCH_START);
    player.handleInput(CROUCH_END);
    assertFalse((Boolean) Utils.getPrivateField(player, "crouch"));
  }

  @Test
  public void jumpStartSetsJump() {
    player.handleInput(JUMP_START);
    assertEquals(JUMP, Utils.getJump(player));
  }

  @Test
  public void jumpEndResetsJumpToNeutral() {
    player.handleInput(JUMP_START);
    player.handleInput(JUMP_END);
    assertEquals(NEUTRAL, Utils.getJump(player));
  }

  @Test
  public void allEventsHandledWithoutException() {
    // Smoke test — every enum constant must be handled without throwing
    for (InputEvent event : InputEvent.values()) {
      assertDoesNotThrow(() -> player.handleInput(event));
    }
  }
}
