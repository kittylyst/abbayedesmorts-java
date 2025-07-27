/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import abbaye.AbbayeMain;
import abbaye.basic.Vector2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestStage {

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {
    AbbayeMain.setGlEnabled(false);
  }

  @Test
  public void loadMapTestCollision() {
    final var stage = new Stage();
    stage.load();
    final var level4 = stage.getScreen(4);
    assertEquals(22, level4.length);

    final var layer = new Layer();
    final var p = Player.of(layer, stage);
    p.setPos(new Vector2(600, 1040));
    layer.setPlayer(p);
    layer.init();

    for (var i = 0; i < 30; i += 1) {
      layer.update();
      //      if (i % 10 == 0) {
      //        System.out.println(p.getPos());
      //      }
    }

    assertTrue(true);
  }
}
