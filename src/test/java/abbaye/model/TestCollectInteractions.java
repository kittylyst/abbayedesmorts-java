/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.GameEvent.*;
import static abbaye.model.GameEventHandlerFactory.*;
import static abbaye.model.TileAtlas.*;
import static org.junit.jupiter.api.Assertions.*;

import abbaye.AbbayeMain;
import abbaye.basic.Vector2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Headless tests for the three collect interactions: hearts, crosses, and waypoint crosses. Each
 * interaction follows the established pattern: Player.checkStaticObject() detects tile → fires
 * GameEvent → GameEventHandlerFactory handler mutates world state and player state.
 */
class TestCollectInteractions {

  private Stage stage;
  private Layer layer;
  private Player player;
  private int screen;

  @BeforeAll
  static void disableGl() {
    AbbayeMain.setGlEnabled(false);
  }

  @BeforeEach
  void setUp() {
    stage = Stage.of();
    layer = new Layer();
    player = Player.of(layer, stage);
    layer.setPlayer(player);
    layer.setStage(stage);

    // Register the same handlers that AbbayeMain wires at runtime
    layer.onEvent(HEART_COLLECTED, heartCollectedEvent(stage, player));
    layer.onEvent(CROSS_COLLECTED, crossCollectedEvent(stage, player));
    layer.onEvent(WAYPOINT_REACHED, waypointReachedEvent(stage));

    screen = stage.getRoom();
    // Ensure no stray collectible tiles at the test position
    stage.getScreen(screen)[1][0] = TILE_EMPTY;
    stage.getScreen(screen)[1][1] = TILE_EMPTY;
  }

  // ── Heart tests ─────────────────────────────────────────────────────────────

  /** Touching a heart tile returns true and fires HEART_COLLECTED. */
  @Test
  void touchingHeartTileReturnsTrue() {
    stage.setTile(screen, 1, 0, 401);
    player.setPos(new Vector2(0, 0));

    assertTrue(player.checkStaticObject(), "checkStaticObject should return true on heart contact");
  }

  /** After collecting a heart, all heart tiles (401–404) are cleared from the room. */
  @Test
  void heartTilesAreClearedAfterCollection() {
    stage.setTile(screen, 1, 0, 401);
    stage.setTile(screen, 1, 1, 402);
    stage.setTile(screen, 2, 0, 403);
    stage.setTile(screen, 2, 1, 404);
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(TILE_EMPTY, stage.getScreen(screen)[1][0], "Heart tile 401 should be cleared");
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[1][1], "Heart tile 402 should be cleared");
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[2][0], "Heart tile 403 should be cleared");
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[2][1], "Heart tile 404 should be cleared");
  }

  /** Collecting a heart increments the player's life count. */
  @Test
  void heartCollectionIncrementsLives() {
    int livesBefore = player.getLives();
    stage.setTile(screen, 1, 0, 401);
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(livesBefore + 1, player.getLives(), "Lives should increase by 1 after heart");
  }

  /** Lives are capped at 9: collecting a heart at max lives leaves the count unchanged. */
  @Test
  void heartCollectionCappedAtNineLives() {
    // Max out lives via repeated addLife calls
    while (player.getLives() < 9) {
      player.addLife();
    }
    stage.setTile(screen, 1, 0, 401);
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(9, player.getLives(), "Lives should not exceed 9");
  }

  /** No event fires when no heart tile is present. */
  @Test
  void noHeartEventWhenNoTile() {
    int livesBefore = player.getLives();
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(livesBefore, player.getLives(), "Lives should be unchanged when no heart tile");
  }

  // ── Cross tests ──────────────────────────────────────────────────────────────

  /** Touching a cross tile returns true. */
  @Test
  void touchingCrossTileReturnsTrue() {
    stage.setTile(screen, 1, 0, 409);
    player.setPos(new Vector2(0, 0));

    assertTrue(player.checkStaticObject(), "checkStaticObject should return true on cross contact");
  }

  /** After collecting a cross, all cross tiles (409–412) are cleared from the room. */
  @Test
  void crossTilesAreClearedAfterCollection() {
    stage.setTile(screen, 1, 0, 409);
    stage.setTile(screen, 1, 1, 410);
    stage.setTile(screen, 2, 0, 411);
    stage.setTile(screen, 2, 1, 412);
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(TILE_EMPTY, stage.getScreen(screen)[1][0], "Cross tile 409 should be cleared");
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[1][1], "Cross tile 410 should be cleared");
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[2][0], "Cross tile 411 should be cleared");
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[2][1], "Cross tile 412 should be cleared");
  }

  /** Collecting a cross increments the player's cross count. */
  @Test
  void crossCollectionIncrementsCrossCount() {
    int crossesBefore = player.getCrosses();
    stage.setTile(screen, 1, 0, 409);
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(crossesBefore + 1, player.getCrosses(), "Cross count should increase by 1");
  }

  /** No cross count change when no cross tile is present. */
  @Test
  void noCrossEventWhenNoTile() {
    int crossesBefore = player.getCrosses();
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(crossesBefore, player.getCrosses(), "Cross count should be unchanged");
  }

  // ── Waypoint tests ───────────────────────────────────────────────────────────

  /** Touching a waypoint-cross tile returns true. */
  @Test
  void touchingWaypointTileReturnsTrue() {
    stage.setTile(screen, 1, 0, 321);
    player.setPos(new Vector2(0, 0));

    assertTrue(
        player.checkStaticObject(), "checkStaticObject should return true on waypoint contact");
  }

  /** After reaching a waypoint, all waypoint tiles (321–326) are cleared from the room. */
  @Test
  void waypointTilesAreClearedAfterReaching() {
    stage.setTile(screen, 1, 0, 321);
    stage.setTile(screen, 1, 1, 322);
    player.setPos(new Vector2(0, 0));
    player.checkStaticObject();

    assertEquals(TILE_EMPTY, stage.getScreen(screen)[1][0], "Waypoint tile 321 should be cleared");
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[1][1], "Waypoint tile 322 should be cleared");
  }

  /** Reaching a waypoint updates the player's respawn position to the current location. */
  @Test
  void waypointReachedUpdatesRespawnPosition() {
    // Tile at row=1, col=0 → player must be at baseTileY=0, baseTileX=0 → pos.y < 64, pos.x < 64
    stage.setTile(screen, 1, 0, 321);
    Vector2 detectionPos = new Vector2(0, 0);
    player.setPos(detectionPos);
    player.checkStaticObject();

    // After enemy contact the player should respawn at the newly recorded waypoint (detectionPos)
    int livesBefore = player.getLives();
    player.onEnemyContact();

    // Lives should decrease (confirming contact path ran) and pos should match the recorded
    // waypoint
    assertEquals(livesBefore - 1, player.getLives(), "Enemy contact should decrement lives");
    assertEquals(detectionPos.x(), player.getPos().x(), 0.01f, "Respawn X should match waypoint");
    assertEquals(detectionPos.y(), player.getPos().y(), 0.01f, "Respawn Y should match waypoint");
  }
}
