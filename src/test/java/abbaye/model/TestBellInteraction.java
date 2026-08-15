/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.GameEvent.BELL_RUNG;
import static abbaye.model.GameEventHandlerFactory.bellRungEvent;
import static abbaye.model.TileAtlas.*;
import static org.junit.jupiter.api.Assertions.*;

import abbaye.AbbayeMain;
import abbaye.basic.Vector2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Headless tests for the bell-ringing interaction: player contacts bell tiles → {@code BELL_RUNG}
 * event fires → handler clears door tiles.
 */
class TestBellInteraction {

  private static final int BELL_RUNG_TILE_OFFSET = 4;

  private Stage stage;
  private Layer layer;
  private Player player;

  @BeforeAll
  static void disableGl() {
    AbbayeMain.setGlEnabled(false);
  }

  @BeforeEach
  void setUp() {
    stage = Stage.of(2, 0);
    stage.load("/map/map.txt");
    layer = new Layer();
    player = Player.of(layer, stage);
    layer.setPlayer(player);
    layer.setStage(stage);

    // Register the same handler that AbbayeMain will register at runtime
    layer.onEvent(BELL_RUNG, bellRungEvent(layer.getGameState(), stage));
  }

  /**
   * When the player stands at tile column 0, row 0 and tile (row=1, col=0) is a bell tile (301),
   * checkStaticObject() should return true, clear the bell tiles, and fire BELL_RUNG.
   */
  @Test
  void touchingBellTileFiresBellRungEvent() {
    // Place bell tile at the player's left foot position: row baseTileY+1, col baseTileX.
    // Player pos = (0, 0) → baseTileX=0, baseTileY=0 → foot tile at row=1, col=0.
    int screen = stage.getRoom();
    stage.getScreen(screen)[1][0] = 301; // one of the bell tiles

    player.setPos(new Vector2(0, 0));

    boolean result = player.checkStaticObject();

    assertTrue(result, "checkStaticObject should return true when bell is touched");
    assertTrue(
        layer.getGameState().isFlagSet(BELL_RUNG),
        "BELL_RUNG flag should be set in GameState after ringing");
  }

  /**
   * After the bell is rung the bell tiles (301–304) should be changed in the current room,
   * preventing the event from firing a second time.
   */
  @Test
  void bellTilesAreModifiedAfterRinging() {
    int screen = stage.getRoom();
    stage.setTile(screen, 1, 0, 301);
    stage.setTile(screen, 1, 1, 302);
    stage.setTile(screen, 2, 0, 303);
    stage.setTile(screen, 2, 1, 304);
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(
        301 + BELL_RUNG_TILE_OFFSET,
        stage.getScreen(screen)[1][0],
        "Bell tile 301 should become 305");
    assertEquals(
        302 + BELL_RUNG_TILE_OFFSET,
        stage.getScreen(screen)[1][1],
        "Bell tile 302 should become 306");
    assertEquals(
        303 + BELL_RUNG_TILE_OFFSET,
        stage.getScreen(screen)[2][0],
        "Bell tile 303 should become 307");
    assertEquals(
        304 + BELL_RUNG_TILE_OFFSET,
        stage.getScreen(screen)[2][1],
        "Bell tile 304 should become 308");
  }

  /** When no bell tiles are present the event should not fire, leaving the GameState flag unset. */
  @Test
  void noEventWhenNoBellTilePresent() {
    // Default stage has no bell tiles at player position (0,0); room is loaded from map
    player.setPos(new Vector2(0, 0));

    // Ensure the room has no bell tiles at the foot positions
    int screen = stage.getRoom();
    stage.getScreen(screen)[1][0] = TILE_EMPTY;
    stage.getScreen(screen)[1][1] = TILE_EMPTY;

    player.checkStaticObject();

    assertFalse(
        layer.getGameState().isFlagSet(BELL_RUNG),
        "BELL_RUNG flag should not be set if no bell tile was touched");
  }
}
