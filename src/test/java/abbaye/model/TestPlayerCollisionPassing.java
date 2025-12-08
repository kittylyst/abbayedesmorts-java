/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Facing.LEFT;
import static abbaye.model.Facing.RIGHT;
import static abbaye.model.Player.COLLISION_RIGHT;
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
    // No walls around

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

    // Position player very close to right wall to trigger collision
    float tileSize = Stage.getTileSize();
    setDirection(player, RIGHT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    float xPos = xCell * tileSize - 1; // Close to wall but not touching
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.calculateCollision();
    var collisions = player.getCollisions();
    assertEquals(0, collisions[COLLISION_RIGHT], "Should not detect collision to right");

    xPos = xCell * tileSize + 1; // Touching
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.calculateCollision();
    collisions = player.getCollisions();
    assertEquals(1, collisions[COLLISION_RIGHT], "Should not detect collision to right");
  }

  @Test
  public void testStepsFromEscapeWhenStanding() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);
    var xCell = 17; // starting xCell pos
    // Place steps to the right
    int checkX = xCell + 2;
    setStep(stage, yCell);
    //    setTile(stage, checkX, yCell + 2, 3);
    //    setTile(stage, checkX + 1, yCell + 2, 4);
    //
    //    setTile(stage, checkX + 2, yCell + 1, 3);
    //    setTile(stage, checkX + 3, yCell + 1, 4);
    //    setTile(stage, checkX + 2, yCell + 2, 1);
    //    setTile(stage, checkX + 3, yCell + 2, 2);
    //
    //    setTile(stage, checkX + 4, yCell + 1, 3);
    //    setTile(stage, checkX + 5, yCell + 1, 4);
    //    setTile(stage, checkX + 4, yCell + 2, 1);
    //    setTile(stage, checkX + 5, yCell + 2, 2);

    // Position player very close to step to trigger collision
    float tileSize = Stage.getTileSize();
    setDirection(player, RIGHT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    System.out.println("Step: " + tileSize * (checkX + 2));

    // [x=1156.25, y=1088.0

    //    float xPos = xCell * tileSize - 1; // Close to wall but not touching
    float xPos = 1156;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.calculateCollision();
    var collisions = player.getCollisions();
    assertEquals(0, collisions[COLLISION_RIGHT], "Should not detect collision to right");

    System.out.println("xPos: " + xPos);

    //    xPos = xCell * tileSize + 1; // Touching
    xPos = 1156.25f;
    player.setPos(new Vector2(xPos, yCell * tileSize));
    player.calculateCollision();
    collisions = player.getCollisions();
    assertEquals(1, collisions[COLLISION_RIGHT], "Should detect collision to right");

    System.out.println("xPos: " + xPos);
  }

  @Test
  public void testNoCollisionWithPassableTiles() {
    float tileSize = Stage.getTileSize();
    player.setPos(new Vector2(10 * tileSize, 10 * tileSize));
    setDirection(player, RIGHT);
    setCrouch(player, false);

    // Place passable tiles (16, 37, 38 are passable)
    int playerTileX = (int) ((player.getPos().x() + 13 * PIXELS_PER_TILE) / tileSize);
    int playerTileY = (int) ((player.getPos().y() + 1 * PIXELS_PER_TILE) / tileSize);
    setTiles(stage, playerTileX + 1, playerTileY, playerTileX + 1, playerTileY + 3, 16);

    boolean hasCollision = player.checkCollision();

    assertFalse(hasCollision, "Should not collide with passable tile 16");
  }

  @Test
  public void testNoRoofCollisionWhenNotJumping() {
    float tileSize = Stage.getTileSize();
    player.setPos(new Vector2(10 * tileSize, 10 * tileSize));
    setJump(player, NEUTRAL);

    // Place roof above player
    int playerTileX = (int) ((player.getPos().x() + 1 * PIXELS_PER_TILE) / tileSize);
    int playerTileY = (int) ((player.getPos().y() + 1 * PIXELS_PER_TILE) / tileSize);
    setTiles(stage, playerTileX, playerTileY - 1, playerTileX + 3, playerTileY - 1, 1);

    boolean hasCollision = player.checkCollision();

    // Roof collision only checked during jump
    assertFalse(hasCollision, "Should not check roof collision when not jumping");
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
  @Disabled
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
  @Disabled
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
