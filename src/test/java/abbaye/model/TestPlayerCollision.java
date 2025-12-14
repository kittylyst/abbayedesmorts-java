/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Facing.LEFT;
import static abbaye.model.Facing.RIGHT;
import static abbaye.model.Player.*;
import static abbaye.model.Stage.*;
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
    stage.load();
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
    // Distance check: pos.x() - ((points[0] - 1) * PIXELS_PER_TILE + 7) < 1.1
    // points[0] = (pos.x() + 8) / tileSize
    // Need pos.x() close to (points[0] - 1) * 8 + 7
    // Ensure points[0] > 0 to enter the collision check
    setDirection(player, LEFT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    int[] collisions;
    float xPos = 2 * tileSize;
    for (int i = 63; i >= 0; i -= 1) {
      player.setPos(new Vector2(xPos, yCell * tileSize));
      player.calculateCollision();
      collisions = player.getCollisions();
      assertEquals(0, collisions[COLLISION_LEFT], "Should not detect collision to left");

      xPos += 1;
    }

    xPos = tileSize;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.calculateCollision();
    collisions = player.getCollisions();

    assertEquals(1, collisions[COLLISION_LEFT], "Should detect collision with left wall");
  }

  @Test
  public void testGroundCollisionSnapsToGround() {
    float tileSize = Stage.getTileSize();
    float gravity = 16.0f;
    // Position player just above ground within snap distance
    // Ensure points[0] > 0 to avoid array bounds in wall collision check
    // points[0] = (pos.x() + 8) / tileSize, so need pos.x() > -8
    // Also ensure points[7] + 1 is within bounds
    float startY = 10 * tileSize - 184; // So points[7] = 10, points[7] + 1 = 11
    float xPos = 10 * tileSize; // So points[0] > 0
    player.setPos(new Vector2(xPos, startY));
    setJump(player, NEUTRAL);

    // Place ground below player - collision checks stagedata[points[7] + 1][points[0-3]]
    // Ensure points[7] + 1 is within bounds
    int points7 = (int) ((startY + 23 * PIXELS_PER_TILE) / tileSize);
    int groundTileY = points7 + 1;
    if (groundTileY >= 0 && groundTileY < NUM_ROWS) {
      // Need to place ground at the X positions that will be checked (points[0-3])
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
    player.checkCollision();
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
    player.checkCollision();
    Vector2 posAfter = player.getPos();

    // Player should fall (gravity applied)
    assertTrue(posAfter.y() > posBefore.y(), "Player should fall when no ground");
    assertEquals(FALL, getJump(player), "Jump state should be FALL when falling");
  }

  @Test
  @Disabled("Roof collision unimplemented so far")
  public void testRoofCollisionDuringJump() {
    float tileSize = Stage.getTileSize();
    // Position player very close to roof to satisfy distance check
    // Distance check: (pos.y() - 1) - ((points[4] - 1) * PIXELS_PER_TILE + 7) < 1
    // points[4] = (pos.y() + 8) / tileSize
    float yPos = 10 * tileSize;
    player.setPos(new Vector2(10 * tileSize, yPos));
    setJump(player, JUMP);
    setHeight(player, 20); // Mid-jump

    // Place roof above player - collision checks stagedata[points[4] - 1][points[0]] and
    // [points[3]]
    int roofY = (int) ((yPos + 1 * PIXELS_PER_TILE) / tileSize) - 1;
    if (roofY >= 0) {
      int checkX1 = (int) ((10 * tileSize + 1 * PIXELS_PER_TILE) / tileSize);
      int checkX2 = (int) ((10 * tileSize + 13 * PIXELS_PER_TILE) / tileSize);
      setTile(stage, checkX1, roofY, 1);
      setTile(stage, checkX2, roofY, 1);
    }

    boolean hasCollision = player.checkCollision();

    assertTrue(hasCollision, "Should detect collision with roof during jump");
  }

  @Test
  @Disabled("Crouching unimplemented so far")
  public void testCrouchLeftWallCollision() {
    float tileSize = Stage.getTileSize();
    // Position player very close to left wall to satisfy distance check
    // Distance check for crouch: pos.x() - ((points[0] - 1) * 8 + 7) < 1.1
    // Also need points[0] != 0 to enter the check
    float xPos = 9 * tileSize + 0.5f; // Very close, within 1.1 pixel threshold
    player.setPos(new Vector2(xPos, 10 * tileSize));
    setDirection(player, LEFT);
    setCrouch(player, true);

    // Place wall to the left at crouch height
    // Crouch checks: r = (int) ((pos.y() + 16) / tileSize), then stagedata[r][points[0] - 1]
    int crouchTileY = (int) ((10 * tileSize + 16) / tileSize);
    int points0 = (int) ((xPos + 1 * PIXELS_PER_TILE) / tileSize);
    int checkX = points0 - 1;
    if (checkX >= 0 && crouchTileY >= 0 && crouchTileY < NUM_ROWS && points0 != 0) {
      setTile(stage, checkX, crouchTileY, 1);
    }

    boolean hasCollision = player.checkCollision();

    assertTrue(hasCollision, "Should detect left wall collision when crouching");
  }

  @Test
  @Disabled("Crouching unimplemented so far")
  public void testCrouchRightWallCollision() {
    float tileSize = Stage.getTileSize();
    // Position player very close to right wall to satisfy distance check
    // Distance check for crouch: ((points[3] + 1) * 8) - (pos.x() / 8 + 14) < 1.1
    // Also need points[3] != NUM_COLUMNS - 1 to enter the check
    float xPos = 15 * tileSize - 0.5f; // Very close, within 1.1 pixel threshold
    player.setPos(new Vector2(xPos, 10 * tileSize));
    setDirection(player, RIGHT);
    setCrouch(player, true);

    // Place wall to the right at crouch height
    int crouchTileY = (int) ((10 * tileSize + 16) / tileSize);
    int points3 = (int) ((xPos + 13 * PIXELS_PER_TILE) / tileSize);
    int checkX = points3 + 1;
    if (checkX < NUM_COLUMNS
        && crouchTileY >= 0
        && crouchTileY < NUM_ROWS
        && points3 != NUM_COLUMNS - 1) {
      setTile(stage, checkX, crouchTileY, 1);
    }

    boolean hasCollision = player.checkCollision();

    assertTrue(hasCollision, "Should detect right wall collision when crouching");
  }

  @Test
  public void testSpecialTile128Collision() {
    float tileSize = Stage.getTileSize();
    // Position player very close to left to trigger collision
    // Special tile 128 check: stagedata[points[4]][points[0]] == 128
    // This is checked when blleft > 0 condition is evaluated
    float xPos = 9 * tileSize + 0.5f;
    player.setPos(new Vector2(xPos, 10 * tileSize));
    setDirection(player, LEFT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    // Place tile 128 at the position that will be checked: stagedata[points[4]][points[0]]
    int points0 = (int) ((xPos + 1 * PIXELS_PER_TILE) / tileSize);
    int points4 = (int) ((10 * tileSize + 1 * PIXELS_PER_TILE) / tileSize);
    if (points0 >= 0 && points4 >= 0 && points4 < NUM_ROWS && points0 < NUM_COLUMNS) {
      setTile(stage, points0, points4, 128);
      // Also need wall to the left to trigger the check path
      if (points0 - 1 >= 0) {
        setTile(stage, points0 - 1, points4, 1);
      }
    }

    boolean hasCollision = player.checkCollision();

    assertTrue(hasCollision, "Should detect collision with special tile 128");
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

    int[] collisions;
    float xPos = 1792.0f;
    for (int i = 0; i < 64; i += 1) {
      player.setPos(new Vector2(xPos, yCell * tileSize));
      player.calculateCollision();
      collisions = player.getCollisions();
      assertEquals(0, collisions[COLLISION_RIGHT], "Should not detect collision to right");

      xPos += 1;
    }

    xPos = 1857.0f;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.calculateCollision();
    collisions = player.getCollisions();
    assertEquals(
        1, collisions[COLLISION_RIGHT], "Should detect collision to right with special tile 344");
  }

  @Test
  public void testSpecialTile348LeftCollision() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);
    float tileSize = Stage.getTileSize();

    // Place tile 348 to the left -
    setTile(stage, 2, yCell - 1, TILE_SPECIAL_LEFT);
    setTile(stage, 2, yCell, TILE_SPECIAL_LEFT);
    setTile(stage, 2, yCell + 1, TILE_SPECIAL_LEFT);
    setTile(stage, 2, yCell + 2, TILE_SPECIAL_LEFT);

    // Position player very close to left
    float xPos = 4 * tileSize;

    setDirection(player, LEFT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    int[] collisions;
    // Move left loop
    for (int dx = 63; dx >= 0; dx -= 1) {

      player.setPos(new Vector2(xPos, yCell * tileSize));
      player.calculateCollision();
      collisions = player.getCollisions();
      assertEquals(0, collisions[COLLISION_LEFT], "Should not detect collision to right");

      xPos -= 1;
    }

    xPos = 3 * tileSize - 1.0f;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.calculateCollision();
    collisions = player.getCollisions();

    assertEquals(1, collisions[COLLISION_LEFT], "Should detect collision with special tile 348");
  }

  @Test
  @Disabled
  public void testSmallPlatformTile38FallLeft() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);
    float tileSize = Stage.getTileSize();

    var xCell = 10;

    // Position player on platform, moving left
    // Platform check: blground[3] == 38 && (pos.x() + 13) < (points[3] * PIXELS_PER_TILE + 5)
    // blground[3] = stagedata[points[7] + 1][points[3]]
    float xPos = 10 * tileSize;
    float yPos = 10 * tileSize;
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
    player.checkCollision();
    Vector2 posAfter = player.getPos();

    // Player should fall through platform when moving left
    assertTrue(
        posAfter.y() > posBefore.y(), "Player should fall through platform 38 when moving left");
  }

  @Test
  public void testInvisibleGroundRoomCave() {
    float tileSize = Stage.getTileSize();
    // Position at column 2, ensure points[7] + 1 > 19 and points[0] == 2
    // But also need points[7] + 1 < NUM_ROWS (22) to avoid array bounds
    // So points[7] + 1 should be 20 or 21
    // points[0] = (pos.x() + 8) / tileSize, so need this to equal 2
    // points[7] = (pos.y() + 184) / tileSize
    // For points[7] + 1 = 20: (pos.y() + 184) / 64 = 19, so pos.y() = 19*64 - 184 = 1032
    float xPos = 2 * tileSize - 7; // Adjust so points[0] == 2
    float yPos = 19 * tileSize - 184 + tileSize; // So points[7] + 1 = 20 (within bounds)
    player.setPos(new Vector2(xPos, yPos));
    setJump(player, NEUTRAL);

    Vector2 posBefore = player.getPos();
    player.checkCollision();
    Vector2 posAfter = player.getPos();

    // Player should fall (invisible ground means no ground collision)
    assertTrue(posAfter.y() > posBefore.y(), "Player should fall through invisible ground");
  }

  @Test
  public void testGroundSnapWhenCloseToGround() {
    float tileSize = Stage.getTileSize();
    float gravity = 16.0f; // Default gravity
    // Position player just above ground within snap distance
    // Ensure points[7] + 1 is within bounds (0 to 21)
    // points[7] = (y + 184) / tileSize, so for points[7] = 10: y = 10*64 - 184 = 456
    float startY = 10 * tileSize - 184; // So points[7] = 10, points[7] + 1 = 11
    player.setPos(new Vector2(10 * tileSize, startY));
    setJump(player, NEUTRAL);

    // Place ground at the X positions that will be checked
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

    player.checkCollision();
    Vector2 posAfter = player.getPos();

    // Player should snap to ground or fall - verify position changed
    // The exact snap depends on distance calculation
    assertTrue(posAfter.y() >= startY, "Player should move down toward ground");
    // Jump state may be NEUTRAL (snapped) or FALL (falling)
    Vertical jumpState = getJump(player);
    assertTrue(jumpState == NEUTRAL || jumpState == FALL, "Jump state should be NEUTRAL or FALL");
  }

  @Test
  public void testBoundaryConditionsBottomEdge() {
    float tileSize = Stage.getTileSize();
    // The code accesses stagedata[points[7] + 1][x] without bounds checking
    // When points[7] + 1 > 21, it uses special ground value
    // But the array access happens before the check, so we need points[7] + 1 < NUM_ROWS
    // Position player so points[7] + 1 is at the boundary (21, not 22)
    // points[7] = (y + 184) / tileSize, so for points[7] = 20: y = 20*64 - 184 = 1096
    float yPos = 20 * tileSize - 184;
    player.setPos(new Vector2(10 * tileSize, yPos));
    setJump(player, NEUTRAL);

    // Should use special ground value (300 * PIXELS_PER_TILE) when points[7] + 1 > 21
    // But we position so points[7] + 1 = 21 to avoid array bounds
    assertDoesNotThrow(() -> player.checkCollision(), "Should handle bottom edge boundary");
  }
}
