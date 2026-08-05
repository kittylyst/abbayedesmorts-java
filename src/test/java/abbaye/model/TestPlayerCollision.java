/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.Facing.LEFT;
import static abbaye.model.Facing.RIGHT;
import static abbaye.model.Player.*;
import static abbaye.model.Stage.*;
import static abbaye.model.TileAtlas.*;
import static abbaye.model.Utils.*;
import static abbaye.model.Vertical.*;
import static org.junit.jupiter.api.Assertions.*;

import abbaye.AbbayeMain;
import abbaye.basic.Vector2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestPlayerCollision {

  private Stage stage;
  private Layer layer;
  private Player player;

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {
    AbbayeMain.setGlEnabled(false);
  }

  @BeforeEach
  public void setUp() {
    // Create a test stage with empty tiles (0 = empty)
    stage = new Stage();
    layer = new Layer();
    player = Player.of(layer, stage);
    layer.setPlayer(player);
    layer.setStage(stage);
    layer.init();
  }

  @Test
  public void testLeftWallCollisionWhenStanding() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);

    float tileSize = Stage.getTileSize();

    var xCell = 1; // starting xCell pos
    // Place solid wall to the left
    for (int y = 4; y < yCell + 3 && y < NUM_ROWS; y++) {
      setTile(stage, xCell, y, 1);
    }

    // Position player very close to left wall to satisfy distance check
    setDirection(player, LEFT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    float xPos = 2 * tileSize;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.update();
    assertFalse(player.isCollidingLeft(), "Should not detect collision to left");

    player.update();
    assertTrue(player.isCollidingLeft(), "Should detect collision with left wall");
  }

  @Test
  public void testGroundCollisionSnapsToGround() {
    float tileSize = Stage.getTileSize();

    // Position player just above ground within snap distance
    float startY = 10 * tileSize - 184; // So points[7] = 10, points[7] + 1 = 11
    float xPos = 10 * tileSize; // So points[0] > 0
    player.setPos(new Vector2(xPos, startY));
    setJump(player, NEUTRAL);

    // Place ground below player
    int points7 = (int) ((startY + 23 * PIXELS_PER_TILE) / tileSize);
    int groundTileY = points7 + 1;
    if (groundTileY >= 0 && groundTileY < NUM_ROWS) {
      int checkX1 = (int) ((xPos + 1 * PIXELS_PER_TILE) / tileSize);
      int checkX2 = (int) ((xPos + 7 * PIXELS_PER_TILE) / tileSize);
      int checkX3 = (int) ((xPos + 8 * PIXELS_PER_TILE) / tileSize);
      int checkX4 = (int) ((xPos + 13 * PIXELS_PER_TILE) / tileSize);
      if (checkX1 >= 0 && checkX1 < NUM_COLUMNS) setTile(stage, checkX1, groundTileY, 1);
      if (checkX2 >= 0 && checkX2 < NUM_COLUMNS) setTile(stage, checkX2, groundTileY, 1);
      if (checkX3 >= 0 && checkX3 < NUM_COLUMNS) setTile(stage, checkX3, groundTileY, 1);
      if (checkX4 >= 0 && checkX4 < NUM_COLUMNS) setTile(stage, checkX4, groundTileY, 1);
    }

    Vector2 posBefore = player.getPos();
    player.update();
    Vector2 posAfter = player.getPos();

    // Player should snap to ground (position should change and be closer to ground)
    assertNotEquals(posBefore.y(), posAfter.y(), "Player position should change");
    assertTrue(posAfter.y() >= posBefore.y(), "Player should move down toward ground");
  }

  @Test
  public void testGravityWhenNoGround() {
    float tileSize = Stage.getTileSize();
    float startY = 5 * tileSize;
    player.setPos(new Vector2(10 * tileSize, startY));
    setJump(player, NEUTRAL);

    // No ground below - all empty

    Vector2 posBefore = player.getPos();
    player.update();
    Vector2 posAfter = player.getPos();

    // Player should fall (gravity applied)
    assertTrue(posAfter.y() > posBefore.y(), "Player should fall when no ground");
    assertEquals(FALL, getJump(player), "Jump state should be FALL when falling");
  }

  @Test
  @Disabled("Crouching unimplemented so far")
  public void testInvisibleWallRoomCaveCrouching() {
    stage.toWaypoint(new Player.Waypoint(2, 2, 0, 0));

    float tileSize = Stage.getTileSize();
    player.setPos(new Vector2(2 * tileSize, 5 * tileSize));
    setDirection(player, LEFT);
    setCrouch(player, true);

    // In ROOM_CAVE, at row 5, columns 0-1 should not collide
    int crouchTileY = (int) ((player.getPos().y() + 16) / tileSize);
    setTile(stage, 0, crouchTileY, 1);
    setTile(stage, 1, crouchTileY, 1);

    player.update();
    assertTrue(player.isCollidingLeft(), "Should collide with invisible wall when crouching");
  }

  @Test
  @Disabled("Crouching unimplemented so far")
  public void testInvisibleWallRoomBeastCrouching() {
    float tileSize = Stage.getTileSize();
    player.setPos(new Vector2(29 * tileSize, 5 * tileSize));
    setDirection(player, RIGHT);
    setCrouch(player, true);

    // In ROOM_BEAST, at row 5, columns 28-31 should not collide
    int crouchTileY = (int) ((player.getPos().y() + 16) / tileSize);
    setTile(stage, 28, crouchTileY, 1);
    setTile(stage, 29, crouchTileY, 1);
    setTile(stage, 30, crouchTileY, 1);
    setTile(stage, 31, crouchTileY, 1);

    player.update();
    assertTrue(player.isCollidingRight(), "Should collide with invisible wall");
  }

  @Test
  public void testRoofCollisionDuringJump() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);

    setSolidLevel(stage, yCell - 3, yCell - 2, false);

    float tileSize = Stage.getTileSize();
    var xCell = 10;

    player.setPos(new Vector2(xCell * tileSize, yCell * tileSize));
    setJump(player, JUMP);
    setHeight(player, 20); // Player is in mid-jump

    player.update();
    assertFalse(player.isCollidingUp(), "Should not detect collision with roof yet");
    player.update();
    assertFalse(player.isCollidingUp(), "Should not detect collision with roof yet");

    player.update();
    assertTrue(player.isCollidingUp(), "Should detect collision with roof during jump");
  }

  @Test
  @Disabled("Crouching unimplemented so far")
  public void testCrouchLeftWallCollision() {
    float tileSize = Stage.getTileSize();
    float xPos = 9 * tileSize + 0.5f;
    player.setPos(new Vector2(xPos, 10 * tileSize));
    setDirection(player, LEFT);
    setCrouch(player, true);

    int crouchTileY = (int) ((10 * tileSize + 16) / tileSize);
    int points0 = (int) ((xPos + 1 * PIXELS_PER_TILE) / tileSize);
    int checkX = points0 - 1;
    if (checkX >= 0 && crouchTileY >= 0 && crouchTileY < NUM_ROWS && points0 != 0) {
      setTile(stage, checkX, crouchTileY, 1);
    }

    player.update();
    assertTrue(player.isCollidingLeft(), "Should detect left wall collision when crouching");
  }

  @Test
  @Disabled("Crouching unimplemented so far")
  public void testCrouchRightWallCollision() {
    float tileSize = Stage.getTileSize();
    float xPos = 15 * tileSize - 0.5f;
    player.setPos(new Vector2(xPos, 10 * tileSize));
    setDirection(player, RIGHT);
    setCrouch(player, true);

    int crouchTileY = (int) ((10 * tileSize + 16) / tileSize);
    int points3 = (int) ((xPos + 13 * PIXELS_PER_TILE) / tileSize);
    int checkX = points3 + 1;
    if (checkX < NUM_COLUMNS
        && crouchTileY >= 0
        && crouchTileY < NUM_ROWS
        && points3 != NUM_COLUMNS - 1) {
      setTile(stage, checkX, crouchTileY, 1);
    }

    player.update();
    assertTrue(player.isCollidingRight(), "Should detect right wall collision when crouching");
  }

  @Test
  public void testSpecialTile128Collision() {
    float tileSize = Stage.getTileSize();
    float xPos = 9 * tileSize + 0.5f;
    player.setPos(new Vector2(xPos, 10 * tileSize));
    setDirection(player, LEFT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    int points0 = (int) ((xPos + 1 * PIXELS_PER_TILE) / tileSize);
    int points4 = (int) ((10 * tileSize + 1 * PIXELS_PER_TILE) / tileSize);
    if (points0 >= 0 && points4 >= 0 && points4 < NUM_ROWS && points0 < NUM_COLUMNS) {
      setTile(stage, points0, points4, 128);
      if (points0 - 1 >= 0) {
        setTile(stage, points0 - 1, points4, 1);
      }
    }

    player.update();
    assertTrue(player.isCollidingLeft(), "Should detect collision with special tile 128");
  }

  @Test
  public void testSpecialTile344RightCollision() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);

    // Place tile 344 to the right like the door in 1-3
    setTile(stage, 31, yCell - 1, TILE_SPECIAL_RIGHT);
    setTile(stage, 31, yCell, TILE_SPECIAL_RIGHT);
    setTile(stage, 31, yCell + 1, TILE_SPECIAL_RIGHT);
    setTile(stage, 31, yCell + 2, TILE_SPECIAL_RIGHT);

    float tileSize = Stage.getTileSize();
    setDirection(player, RIGHT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    float xPos = 1792.0f;
    for (int i = 0; i < 64; i += 1) {
      player.setPos(new Vector2(xPos, yCell * tileSize));
      player.checkCollisions();
      assertFalse(player.isCollidingRight(), "Should not detect collision to right");
      xPos += 1;
    }

    xPos = 1857.0f;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.checkCollisions();
    assertTrue(player.isCollidingRight(), "Should detect collision to right with special tile 344");
  }

  @Test
  public void testSpecialTile348LeftCollision() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);
    float tileSize = Stage.getTileSize();

    // Place tile 348 to the left
    setTile(stage, 2, yCell - 1, TILE_SPECIAL_LEFT);
    setTile(stage, 2, yCell, TILE_SPECIAL_LEFT);
    setTile(stage, 2, yCell + 1, TILE_SPECIAL_LEFT);
    setTile(stage, 2, yCell + 2, TILE_SPECIAL_LEFT);

    float xPos = 4 * tileSize;

    setDirection(player, LEFT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    // Move left loop
    for (int dx = 63; dx >= 0; dx -= 1) {
      player.setPos(new Vector2(xPos, yCell * tileSize));
      player.checkCollisions();
      assertFalse(player.isCollidingLeft(), "Should not detect collision to left");
      xPos -= 1;
    }

    xPos = 3 * tileSize - 1.0f;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.checkCollisions();
    assertTrue(player.isCollidingLeft(), "Should detect collision with special tile 348");
  }

  // What is this test supposed to do?
  @Test
  @Disabled
  public void testSmallPlatformTile38FallLeft() {
    float tileSize = Stage.getTileSize();
    int yCell = 10;

    float xPos = 540.0f;
    float yPos = yCell * tileSize;
    player.setPos(new Vector2(xPos, yPos));
    setDirection(player, LEFT);
    setJump(player, NEUTRAL);

    // Place platform tile 38 below player at points[3] position (right side)
    int points3 = (int) ((xPos + 13 * PIXELS_PER_TILE) / tileSize);
    int points7 = (int) ((yPos + 23 * PIXELS_PER_TILE) / tileSize);
    int checkY = points7 + 1;
    if (checkY >= 0 && checkY < NUM_ROWS && points3 >= 0 && points3 < NUM_COLUMNS) {
      setTile(stage, points3, checkY, 38);
    }

    Vector2 posBefore = player.getPos();
    player.update();
    Vector2 posAfter = player.getPos();

    // Player should fall through platform when moving left
    assertTrue(
        posAfter.y() > posBefore.y(), "Player should fall through platform 38 when moving left");
  }

  @Test
  public void testInvisibleGroundRoomCave() {
    float tileSize = Stage.getTileSize();
    float xPos = 2 * tileSize - 7; // Adjust so points[0] == 2
    float yPos = 19 * tileSize - 184 + tileSize; // So points[7] + 1 = 20 (within bounds)
    player.setPos(new Vector2(xPos, yPos));
    setJump(player, NEUTRAL);

    Vector2 posBefore = player.getPos();
    player.update();
    Vector2 posAfter = player.getPos();

    // Player should fall (invisible ground means no ground collision)
    assertTrue(posAfter.y() > posBefore.y(), "Player should fall through invisible ground");
  }

  @Test
  public void testGroundSnapWhenCloseToGround() {
    float tileSize = Stage.getTileSize();
    float startY = 10 * tileSize - 184; // So points[7] = 10, points[7] + 1 = 11
    player.setPos(new Vector2(10 * tileSize, startY));
    setJump(player, NEUTRAL);

    int points7 = (int) ((startY + 23 * PIXELS_PER_TILE) / tileSize);
    int groundTileY = points7 + 1;
    if (groundTileY >= 0 && groundTileY < NUM_ROWS) {
      int checkX1 = (int) ((10 * tileSize + 1 * PIXELS_PER_TILE) / tileSize);
      int checkX2 = (int) ((10 * tileSize + 7 * PIXELS_PER_TILE) / tileSize);
      int checkX3 = (int) ((10 * tileSize + 8 * PIXELS_PER_TILE) / tileSize);
      int checkX4 = (int) ((10 * tileSize + 13 * PIXELS_PER_TILE) / tileSize);
      if (checkX1 >= 0 && checkX1 < NUM_COLUMNS) setTile(stage, checkX1, groundTileY, 1);
      if (checkX2 >= 0 && checkX2 < NUM_COLUMNS) setTile(stage, checkX2, groundTileY, 1);
      if (checkX3 >= 0 && checkX3 < NUM_COLUMNS) setTile(stage, checkX3, groundTileY, 1);
      if (checkX4 >= 0 && checkX4 < NUM_COLUMNS) setTile(stage, checkX4, groundTileY, 1);
    }

    player.update();
    Vector2 posAfter = player.getPos();

    assertTrue(posAfter.y() >= startY, "Player should move down toward ground");
    Vertical jumpState = getJump(player);
    assertTrue(jumpState == NEUTRAL || jumpState == FALL, "Jump state should be NEUTRAL or FALL");
  }

  @Test
  public void testPassableTile16ActsAsGround() {
    float tileSize = Stage.getTileSize();
    float startY = 10 * tileSize;
    float xPos = 10 * tileSize;
    player.setPos(new Vector2(xPos, startY));
    setJump(player, NEUTRAL);

    int points7 = (int) ((startY + 23 * PIXELS_PER_TILE) / tileSize);
    int groundTileY = points7 + 1;
    int checkX = (int) ((xPos + 8 * PIXELS_PER_TILE) / tileSize);
    setTile(stage, checkX, groundTileY, TILE_PASSABLE);

    Vector2 posBefore = player.getPos();
    player.update();
    Vector2 posAfter = player.getPos();

    assertTrue(posAfter.y() <= posBefore.y(), "Player should not fall through passable tile 16");
    assertEquals(NEUTRAL, getJump(player), "Player should remain grounded over passable tile 16");
  }

  @Test
  public void testBoundaryConditionsBottomEdge() {
    float tileSize = Stage.getTileSize();
    float yPos = 20 * tileSize - 184;
    player.setPos(new Vector2(10 * tileSize, yPos));
    setJump(player, NEUTRAL);

    assertDoesNotThrow(() -> player.update(), "Should handle bottom edge boundary");
  }

  @Test
  public void testStaticHazardCheckBottomEdgeDoesNotThrow() {
    float tileSize = Stage.getTileSize();
    player.setPos(new Vector2(10 * tileSize, 19 * tileSize));
    setJump(player, NEUTRAL);

    setTile(stage, 10, NUM_ROWS - 1, TILE_STATIC_HAZARD);

    assertDoesNotThrow(
        () -> player.update(),
        "Static hazard check should handle bottom edge without out-of-bounds");
  }

  @Test
  public void testTileGridSamplingBottomRightEdgeDoesNotThrow() {
    float tileSize = Stage.getTileSize();

    player.setPos(
        new Vector2((NUM_COLUMNS - 2) * tileSize, (NUM_ROWS - 3) * tileSize + tileSize - 0.1f));
    setDirection(player, RIGHT);
    setJump(player, NEUTRAL);
    setPrivateField(player, "walk", true);

    assertDoesNotThrow(
        () -> player.update(),
        "Tile grid sampling at bottom-right edge should not throw out-of-bounds");
  }
}
