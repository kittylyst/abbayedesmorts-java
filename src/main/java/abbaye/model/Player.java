/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.graphics.GLManager.*;
import static abbaye.model.Stage.TILES_PER_COL;
import static abbaye.model.Stage.TILES_PER_ROW;
import static org.lwjgl.glfw.GLFW.*;

import abbaye.Config;
import abbaye.basic.*;
import abbaye.graphics.GLManager;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public final class Player implements Actor {

  // GL fields
  private GLManager manager = GLManager.get("game");

  private Layer layer;
  private Stage stage;

  // Physicality
  private Vector2 pos = new Vector2(0, 0);
  private Vector2 v = new Vector2(0, 0);
  private boolean crouch = false;

  // From C code
  private int direction = GLFW_HAT_RIGHT;
  private int jump; /* 1-Up, 2-Down */
  private float height; /* Limit of jump */
  private int animation;
  private float gravity;
  private int[] points = new int[8]; /* Points of collision */
  private int ground; /* Pixel where is ground */
  private int[] collision = {0, 0, 0, 0}; /* Collisions, in 4 directions */
  private int[] checkpoint = new int[4];
  private int[] state = new int[2]; /* Vidas y cruces */
  private int[] flags = new int[7];
  private int death;
  private boolean walk = false;

  //  int push[4]; /* Pulsaciones de teclas */

  @Override
  public void destroy() {
    Actor.super.destroy();
  }

  @Override
  public BoundingBox2 getBB() {
    return new BoundingBox2(Vector2.ORIGIN, Vector2.ORIGIN); // pos, size);
  }

  public boolean checkHit() {
    return false;
  }

  static Corners makeCorners(int tileX, int tileY) {
    float u1 = (float) tileX / TILES_PER_ROW;
    float v1 = (float) tileY / TILES_PER_COL;
    float u2 = (float) (tileX + 1) / TILES_PER_ROW;
    float v2 = (float) (tileY + 1) / TILES_PER_COL;

    return new Corners(u1, 1 - v1, u2, 1 - v2);
    // For 44, 11
    //    tileCoords = new Corners(0.352f, 0.633333f, 0.36f, 0.6f); // 1 - y coords
    //    tileCoords = new Corners(0.35f, 0.65f, 0.3625f, 0.6f); // 1 - y coords
  }

  @Override
  public boolean render() {
    if (!Config.config().getGLActive()) {
      return false;
    }

    var tileDisplaySize = Stage.getTileSize();
    var posX = pos.x();
    var posY = pos.y();
    var tileCoords = makeCorners(44, 11);
    manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

    posX = pos.x() + tileDisplaySize;
    posY = pos.y();
    tileCoords = makeCorners(45, 11);
    manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

    posX = pos.x();
    posY = pos.y() + tileDisplaySize;
    tileCoords = makeCorners(44, 12);
    manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

    posX = pos.x() + tileDisplaySize;
    posY = pos.y() + tileDisplaySize;
    tileCoords = makeCorners(45, 12);
    manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

    posX = pos.x();
    posY = pos.y() + tileDisplaySize + tileDisplaySize;
    tileCoords = makeCorners(44, 13);
    manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

    posX = pos.x() + tileDisplaySize;
    posY = pos.y() + tileDisplaySize + tileDisplaySize;
    tileCoords = makeCorners(45, 13);
    manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

    return false;
  }

  public Vector2 newPosition() {
    float dx = 0;
    float dy = 0;

    /* Jump */
    if (jump == 1) {
      //      if (jean->height == 0) /* Jump sound */
      //        Mix_PlayChannel(-1, fx[3], 0);
      if (height < 56) {
        height += 1.6;
        if ((collision[0] == 0) && (height < 44)) dy -= 1.5;
        animation = 0;
      } else {
        jump = 2;
        collision[0] = 0;
      }
    }

    /* Move to right */
    if (direction == GLFW_HAT_RIGHT && walk) {
      if (collision[3] == 0) {
        if (jump == 0) {
          if (animation < 13) animation += 1;
          else animation = 0;
        }
        if (crouch) {
          dx += 0.30;
        } else {
          dx += 0.65;
        }
      }
    }

    /* Move to left */
    if (direction == GLFW_HAT_LEFT && walk) {
      if (collision[2] == 0) {
        if (jump == 0) {
          if (animation < 13) animation += 1;
          else animation = 0;
        }
        if (crouch) {
          dx -= 0.30;
        } else {
          dx -= 0.65;
        }
      }
    }
    //    System.out.println("direction: "+ direction +" ; walk: "+ walk +" ; dx: "+ dx +" ; dy: "+
    // dy);

    return new Vector2(pos.x() + getMoveSpeed() * dx, pos.y() + getMoveSpeed() * dy);
  }

  @Override
  public boolean update() {
    pos = newPosition();
    if (pos.x() < 0) {
      if (stage.moveLeft()) {
        pos = new Vector2(248, pos.y());
      } else {
        pos = new Vector2(0, pos.y());
      }
    }
    if (pos.x() > 256) {
      if (stage.moveRight()) {
        pos = new Vector2(0, pos.y());
      } else {
        pos = new Vector2(248, pos.y());
      }
    }

    if (pos.y() < 0) {
      if (stage.moveUp()) {
        pos = new Vector2(pos.x(), 200);
      } else {
        pos = new Vector2(pos.x(), 0);
      }
    }
    if (pos.y() > 200) {
      if (stage.moveDown()) {
        pos = new Vector2(pos.x(), 0);
      } else {
        pos = new Vector2(pos.x(), 200);
      }
    }

    return true;
  }

  public GLFWKeyCallbackI moveCallback() {
    return (window, key, scancode, action, mods) -> {
      if (action == GLFW_PRESS) {
        switch (key) {
          case GLFW_KEY_RIGHT:
            {
              direction = GLFW_HAT_RIGHT;
              walk = true;
              break;
            }
          case GLFW_KEY_LEFT:
            {
              direction = GLFW_HAT_LEFT;
              walk = true;
              break;
            }
          case GLFW_KEY_DOWN:
            {
              crouch = true;
              break;
            }
          case GLFW_KEY_UP:
            {
              jump = 1;
              break;
            }
        }
      }
      if (action == GLFW_RELEASE) {
        switch (key) {
          case GLFW_KEY_ESCAPE:
            {
              glfwSetWindowShouldClose(window, true);
              break;
            }
          case GLFW_KEY_RIGHT:
            {
              direction = GLFW_HAT_RIGHT;
              walk = false;
              break;
            }
          case GLFW_KEY_LEFT:
            {
              direction = GLFW_HAT_LEFT;
              walk = false;
              break;
            }
          case GLFW_KEY_DOWN:
            {
              crouch = false;
              break;
            }
          case GLFW_KEY_UP:
            {
              break;
            }
          default:
        }
      }
    };
  }

  public static class PlayerSerializer extends JsonSerializer<Player> {
    @Override
    public void serialize(
        Player player, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
  }

  private Player(Layer layer, Stage stage) {
    this.layer = layer;
    this.stage = stage;
    this.pos =
        new Vector2(Config.config().getScreenWidth() / 2, Config.config().getScreenHeight() / 2);
  }

  public static Player of(Layer layer, Stage stage) {
    return new Player(layer, stage);
  }

  // Getters

  @Override
  public Vector2 getPos() {
    return pos;
  }

  @Override
  public Vector2 getV() {
    return v;
  }

  @Override
  public Vector2 getSize() {
    return new Vector2(16, 24);
  }

  @Override
  public int getDirection() {
    return direction;
  }
}
