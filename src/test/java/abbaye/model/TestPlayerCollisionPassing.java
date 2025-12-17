/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Facing.LEFT;
import static abbaye.model.Facing.RIGHT;
import static abbaye.model.Player.COLLISION_RIGHT;
import static abbaye.model.Player.PIXELS_PER_TILE;
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

/** Tests for Player collision detection that are currently passing */
public class TestPlayerCollisionPassing {

  private Stage stage;
  private Layer layer;
  private Player player;

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {
    AbbayeMain.setGlEnabled(false);
  }

  @BeforeEach
  public void setUp() {
    stage = new Stage();
    layer = new Layer();
    player = Player.of(layer, stage);
    layer.setPlayer(player);
    layer.setStage(stage);
    layer.init();
  }

  @Test
  public void testNoCollisionWhenStandingInEmptySpace() {
    // Position player in middle of empty space
    player.setPos(new Vector2(500, 500));
    setDirection(player, RIGHT);
    setPrivateField(player, "walk", false);
    // No walls around

    player.update();
    boolean hasCollision = player.checkCollision();

    assertFalse(hasCollision, "Should not detect collision in empty space");
  }

  @Test
  public void testRightWallCollisionWhenStanding() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);
    var xCell = 1; // starting xCell pos
    // Place solid wall to the right
    int checkX = xCell + 2;
    for (int y = 4; y < yCell + 3 && y < NUM_ROWS; y++) {
      setTile(stage, checkX, y, 1);
    }

    // Position player standing very close to right wall but not close enough trigger collision
    setDirection(player, RIGHT);
    setCrouch(player, false);
    setPrivateField(player, "walk", false);

    var tileSize = Stage.getTileSize();
    float xPos = xCell * tileSize - 1; // Close to wall but not touching
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.update();
    var collisions = player.getCollisions();
    assertEquals(0, collisions[COLLISION_RIGHT], "Should not detect collision to right");

    // Make player walk
    setPrivateField(player, "walk", true);
    player.update();
    player.update();
    collisions = player.getCollisions();
    System.out.println(player);
    assertEquals(1, collisions[COLLISION_RIGHT], "Should detect collision to right");
  }

  @Test
  public void testStepsFromEscapeWhenStanding() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);
    // Place steps as positioned in 1-1
    setStep(stage, yCell);

    // Position player very close to step to trigger collision
    float tileSize = Stage.getTileSize();
    setDirection(player, RIGHT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    int[] collisions;
    float xPos = 1088;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.update();
    collisions = player.getCollisions();
    assertEquals(0, collisions[COLLISION_RIGHT], "Should not detect collision to right");

    player.update();
    player.update();
    player.update();
    collisions = player.getCollisions();
    assertEquals(1, collisions[COLLISION_RIGHT], "Should detect collision to right");
  }

  @Test
  public void testNoCollisionWithPassableTiles() {
    float tileSize = Stage.getTileSize();
    player.setPos(new Vector2(10 * tileSize, 10 * tileSize));
    setDirection(player, RIGHT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    // Place passable tiles (16, 37, 38 are passable)
    int playerTileX = (int) ((player.getPos().x() + 13 * PIXELS_PER_TILE) / tileSize);
    int playerTileY = (int) ((player.getPos().y() + 1 * PIXELS_PER_TILE) / tileSize);
    setTiles(stage, playerTileX + 1, playerTileY, playerTileX + 1, playerTileY + 3, 16);

    player.update();
    var collisions = player.getCollisions();
    assertEquals(0, collisions[COLLISION_RIGHT], "Should not collide with passable tile 16");
  }

  @Test
  public void testNoRoofCollisionWhenNotJumping() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);

    var xCell = 10;
    yCell = 10;

    // Place roof above player
    setTiles(stage, xCell - 1, yCell - 1, yCell + 3, yCell - 1, 1);

    float tileSize = Stage.getTileSize();
    player.setPos(new Vector2(xCell * tileSize, yCell * tileSize));
    setJump(player, NEUTRAL);

    player.update();
    // Roof collision only checked during jump
    assertFalse(player.checkCollision(), "Should not check roof collision when not jumping");
  }

  @Test
  public void testSmallPlatformTile38FallRight() {
    float tileSize = Stage.getTileSize();
    player.setPos(new Vector2(10 * tileSize, 10 * tileSize));
    setDirection(player, RIGHT);
    setJump(player, NEUTRAL);

    // Place platform tile 38 below player on the left side
    int playerTileX = (int) ((player.getPos().x() + 1 * PIXELS_PER_TILE) / tileSize);
    int playerTileY = (int) ((player.getPos().y() + 23 * PIXELS_PER_TILE) / tileSize);
    setTile(stage, playerTileX, playerTileY + 1, 38);

    Vector2 posBefore = player.getPos();
    player.checkCollision();
    Vector2 posAfter = player.getPos();

    // Player should fall through platform when moving right
    assertTrue(
        posAfter.y() > posBefore.y(), "Player should fall through platform 38 when moving right");
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

    boolean hasCollision = player.checkCollision();

    assertTrue(hasCollision, "Collision check should complete");
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

    boolean hasCollision = player.checkCollision();

    assertTrue(hasCollision, "Collision check should complete");
  }

  @Test
  public void testInvisibleGroundRoomLake() {
    float tileSize = Stage.getTileSize();
    // Position player high up (y/8 < 4) at column 2
    player.setPos(new Vector2(2 * tileSize, 2 * tileSize));
    setJump(player, NEUTRAL);

    Vector2 posBefore = player.getPos();
    player.checkCollision();
    Vector2 posAfter = player.getPos();

    // Player should fall (invisible ground in ROOM_LAKE)
    assertTrue(
        posAfter.y() > posBefore.y(), "Player should fall through invisible ground in ROOM_LAKE");
  }

  @Test
  public void testBoundaryConditionsLeftEdge() {
    float tileSize = Stage.getTileSize();
    // Position player at left edge
    player.setPos(new Vector2(0, 10 * tileSize));
    setDirection(player, LEFT);
    setCrouch(player, false);

    // Should not crash when checking left collision at edge
    assertDoesNotThrow(() -> player.checkCollision(), "Should handle left edge boundary");
  }

  @Test
  public void testBoundaryConditionsRightEdge() {
    float tileSize = Stage.getTileSize();
    // Position player near right edge
    player.setPos(new Vector2((NUM_COLUMNS - 2) * tileSize, 10 * tileSize));
    setDirection(player, RIGHT);
    setCrouch(player, false);

    // Should not crash when checking right collision at edge
    assertDoesNotThrow(() -> player.checkCollision(), "Should handle right edge boundary");
  }

  @Test
  public void testBoundaryConditionsTopEdge() {
    float tileSize = Stage.getTileSize();
    // Position player at top edge
    player.setPos(new Vector2(10 * tileSize, 0));
    setJump(player, JUMP);

    // Should not crash when checking roof collision at top
    assertDoesNotThrow(() -> player.checkCollision(), "Should handle top edge boundary");
  }
}
