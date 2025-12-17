/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Facing.RIGHT;
import static abbaye.model.Player.COLLISION_RIGHT;
import static abbaye.model.Utils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import abbaye.AbbayeMain;
import abbaye.basic.Vector2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestRooms {
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
  public void testRoomSwitchRightLeft() {
    // Make basic field
    var yCell = 12;
    setFloor(stage, yCell + 3);
    float tileSize = Stage.getTileSize();

    var xCell = 30; // starting xCell pos

    // Position player very close to right side
    setDirection(player, RIGHT);
    setCrouch(player, false);
    setPrivateField(player, "walk", true);

    int[] collisions;
    player.setPos(new Vector2(xCell * tileSize, yCell * tileSize));
    player.update();
    collisions = player.getCollisions();
    assertEquals(0, collisions[COLLISION_RIGHT], "Should not detect collision to right");
    assertEquals(5, stage.getRoom(), "Should be in Room 5");

    player.update();
    collisions = player.getCollisions();
    assertEquals(0, collisions[COLLISION_RIGHT], "Should not detect collision to right");
    assertEquals(6, stage.getRoom(), "Should be in Room 6");
  }
}
