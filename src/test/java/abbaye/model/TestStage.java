/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.glfw.GLFW.*;

import abbaye.AbbayeMain;
import abbaye.basic.Vector2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public class TestStage {

  public GLFWKeyCallbackI moveCallback(Stage stage) {
    return (window, key, scancode, action, mods) -> {
      if (action == GLFW_RELEASE) {
        switch (key) {
          case GLFW_KEY_ESCAPE:
            {
              glfwSetWindowShouldClose(window, true);
              break;
            }
          case GLFW_KEY_RIGHT:
            {
              stage.moveRight();
              break;
            }
          case GLFW_KEY_LEFT:
            {
              stage.moveLeft();
              break;
            }
          case GLFW_KEY_DOWN:
            {
              stage.moveDown();
              break;
            }
          case GLFW_KEY_UP:
            {
              stage.moveUp();
              break;
            }
          default:
        }
      }
    };
  }

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
    System.out.println(p.getPos());

    assertTrue(true);
  }
}
