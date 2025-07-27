/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Facing.LEFT;
import static abbaye.model.Facing.RIGHT;
import static abbaye.model.Room.*;
import static abbaye.model.Stage.*;
import static abbaye.model.Vertical.*;
import static org.lwjgl.glfw.GLFW.*;

import abbaye.AbbayeMain;
import abbaye.Config;
import abbaye.basic.*;
import abbaye.graphics.GLManager;
import abbaye.logs.GameLogger;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Arrays;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public final class Player implements Actor {

  public static final int RIGHT_EDGE = 243;
  public static final int LEFT_EDGE = 0;
  public static final int BOTTOM_EDGE = 146;
  public static final int TOP_EDGE = 0;
  // GL fields
  private GLManager manager;

  private Layer layer;
  private Stage stage;
  private GameLogger logger = Config.config().getLogger();

  // FIXME
  private int counter = 0;

  // Physicality
  private Vector2 pos = new Vector2(0, 0);
  private Vector2 v = new Vector2(0, 0);
  private boolean crouch = false;

  // From C code
  private Facing direction = RIGHT;
  private Vertical jump = NEUTRAL;
  private float height; /* Limit of jump */
  private int animation;
  private int[] points = new int[8]; /* Points of collision */
  private int ground; /* Y-coordinate pixel where the ground is beneath the player */
  private int[] collision = {
    0, 0, 0, 0
  }; /* Collisions in directions UDLR - D is unused and handled by gravity effects */
  private int[] checkpoint = new int[4];
  private int[] state = new int[2]; /* Vidas y cruces */
  private int[] flags = new int[7];
  private int death;
  private boolean walk = false;

  //  int push[4]; /* Pulsaciones de teclas */

  @Override
  public void init() {
    if (AbbayeMain.isGlEnabled()) {
      manager = GLManager.get("game");
    }
  }

  @Override
  public void destroy() {
    logger.info("Collision detected, should destroy");
  }

  @Override
  public BoundingBox2 getBB() {
    return new BoundingBox2(Vector2.ORIGIN, Vector2.ORIGIN); // pos, size);
  }

  static Corners makeCorners(int tileX, int tileY) {
    float u1 = (float) tileX / TILES_PER_ROW;
    float v1 = (float) tileY / TILES_PER_COL;
    float u2 = (float) (tileX + 1) / TILES_PER_ROW;
    float v2 = (float) (tileY + 1) / TILES_PER_COL;

    return new Corners(u1, 1 - v1, u2, 1 - v2);
  }

  @Override
  public boolean render() {
    if (!Config.config().getGLActive()) {
      return false;
    }

    var tileDisplaySize = Stage.getTileSize();
    float posX, posY;
    Corners tileCoords;

    if (direction == LEFT) {
      posX = pos.x();
      posY = pos.y();
      tileCoords = makeCorners(44, 11);
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
    } else {
      posX = pos.x();
      posY = pos.y();
      tileCoords = makeCorners(45, 11);
      manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y();
      tileCoords = makeCorners(44, 11);
      manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

      posX = pos.x();
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(45, 12);
      manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(44, 12);
      manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

      posX = pos.x();
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(45, 13);
      manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(44, 13);
      manager.renderTile(tileCoords, renderMatrix(posX, posY, tileDisplaySize));
    }

    return false;
  }

  public Vector2 newPosition() {
    if (checkCollision()) {
      logger.info("Collision detected: " + Arrays.toString(collision));
    }

    float dx = 0;
    float dy = 0;

    /* Jump */
    if (jump == JUMP) {
      //      if (jean->height == 0) /* Jump sound */
      //        Mix_PlayChannel(-1, fx[3], 0);
      if (height < 56) {
        height += 1.6;
        if ((collision[0] == 0) && (height < 44)) {
          dy -= 1.5;
        }
        animation = 0;
      } else {
        jump = FALL;
        collision[0] = 0;
      }
    }

    /* Move to right */
    if (direction == RIGHT && walk) {
      if (collision[3] == 0) {
        if (jump == NEUTRAL) {
          if (animation < 13) {
            animation += 1;
          } else {
            animation = 0;
          }
        }
        if (crouch) {
          dx += 0.30;
        } else {
          dx += 0.65;
        }
      }
    }

    /* Move to left */
    if (direction == LEFT && walk) {
      if (collision[2] == 0) {
        if (jump == NEUTRAL) {
          if (animation < 13) {
            animation += 1;
          } else {
            animation = 0;
          }
        }
        if (crouch) {
          dx -= 0.30;
        } else {
          dx -= 0.65;
        }
      }
    }

    return new Vector2(pos.x() + getMoveSpeed() * dx, pos.y() + getMoveSpeed() * dy);
  }

  @Override
  public boolean update() {
    if (pos.x() < LEFT_EDGE) {
      if (stage.moveLeft()) {
        pos = new Vector2(PIXELS_PER_TILE * (RIGHT_EDGE - 1), pos.y());
      } else {
        pos = new Vector2(0, pos.y());
      }
    }
    if (pos.x() >= PIXELS_PER_TILE * RIGHT_EDGE) {
      if (stage.moveRight()) {
        pos = new Vector2(0, pos.y());
      } else {
        pos = new Vector2(PIXELS_PER_TILE * RIGHT_EDGE, pos.y());
      }
    }
    if (pos.y() < TOP_EDGE) {
      if (stage.moveUp()) {
        pos = new Vector2(pos.x(), PIXELS_PER_TILE * (BOTTOM_EDGE - 3));
      } else {
        pos = new Vector2(pos.x(), TOP_EDGE);
      }
    }
    if (pos.y() > PIXELS_PER_TILE * (BOTTOM_EDGE - 3)) {
      if (stage.moveDown()) {
        pos = new Vector2(pos.x(), TOP_EDGE);
      } else {
        pos = new Vector2(pos.x(), PIXELS_PER_TILE * (BOTTOM_EDGE - 3));
      }
    }

    pos = newPosition();
    return true;
  }

  public GLFWKeyCallbackI moveCallback() {
    return (window, key, scancode, action, mods) -> {
      if (action == GLFW_PRESS) {
        switch (key) {
          case GLFW_KEY_RIGHT:
            {
              direction = RIGHT;
              walk = true;
              break;
            }
          case GLFW_KEY_LEFT:
            {
              direction = LEFT;
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
              jump = JUMP;
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
              direction = RIGHT;
              walk = false;
              break;
            }
          case GLFW_KEY_LEFT:
            {
              direction = LEFT;
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
              jump = NEUTRAL;
              break;
            }
          default:
        }
      }
    };
  }

  public boolean checkHit() {
    return false;
  }

  /**
   * This is currently only for collisions with walls, but note that we have no enemies in the game
   * yet
   *
   * @return
   */
  public boolean checkCollision() {
    int blleft = 0;
    int blright = 0;
    int[] blground = {0, 0, 0, 0};
    int[] blroof = {0, 0};
    int[] points = {0, 0, 0, 0, 0, 0, 0, 0};
    int r = 0;

    float gravity = Config.config().getGravity();

    float resize = Stage.getTileSize();
    points[0] = (int) ((pos.x() + 1 * PIXELS_PER_TILE) / resize);
    points[1] = (int) ((pos.x() + 7 * PIXELS_PER_TILE) / resize);
    points[2] = (int) ((pos.x() + 8 * PIXELS_PER_TILE) / resize);
    points[3] = (int) ((pos.x() + 13 * PIXELS_PER_TILE) / resize);
    points[4] = (int) ((pos.y() + 1 * PIXELS_PER_TILE) / resize);
    points[5] = (int) ((pos.y() + 8 * PIXELS_PER_TILE) / resize);
    points[6] = (int) ((pos.y() + 15 * PIXELS_PER_TILE) / resize);
    points[7] = (int) ((pos.y() + 23 * PIXELS_PER_TILE) / resize);

    collision[0] = 0;
    collision[1] = 0;
    collision[2] = 0;
    collision[3] = 0;

    int room = stage.getRoom();
    var stagedata = stage.getScreen(room);

    /* Left & Right collisions */
    if (!crouch) {
      CHECKS:
      for (var n = 4; n < 8; n += 1) {
        if ((points[0] <= 0) || (points[3] + 1 >= NUM_COLUMNS) || (points[n] + 1 >= NUM_ROWS)) {
          break CHECKS;
        }
        if (((points[0] > 0) && (direction == LEFT))
            || ((points[3] + 1 < NUM_COLUMNS) && (direction == RIGHT))) {
          blleft = stagedata[points[n]][points[0] - 1];
          blright = stagedata[points[n]][points[3] + 1];
          if (counter++ % 10 == 0) {
            logger.debug(
                pos
                    + " ; blleft: "
                    + blleft
                    + " ; blright: "
                    + blright
                    + " ; ground: "
                    + ground
                    + " ; "
                    + Arrays.toString(points));
          }
          if (((blleft > 0) && (blleft < 100) && (blleft != 16) && (blleft != 38) && (blleft != 37))
              || ((stagedata[points[4]][points[0]] == 128) || (blleft == 348))) {
            if (pos.x() - ((points[0] - 1) * PIXELS_PER_TILE + 7) < 1.1) {
              collision[2] = 1;
            }
          }
          if (((blright > 0)
                  && (blright < 100)
                  && (blright != 16)
                  && (blright != 38)
                  && (blright != 37))
              || (blright == 344)) {
            if (((points[3] + 1) * PIXELS_PER_TILE) - (pos.x() / PIXELS_PER_TILE + 14) < 1.1) {
              collision[3] = 1;
            }
          }
        }
      }
    }

    /* Collision with Jean ducking */
    if (crouch) {
      // FIXME Are these directions correct?
      if (((points[0] != 0) && (direction == LEFT))
          || ((points[3] != NUM_COLUMNS - 1) && (direction == RIGHT))) {
        r = (int) ((pos.y() + 16) / 8);
        blleft = stagedata[r][points[0] - 1];
        blright = stagedata[r][points[3] + 1];
        if (((blleft > 0) && (blleft < 100) && (blleft != 37))
            || ((stagedata[r][points[0]] == 128) || ((blleft > 346) && (blleft < 351)))) {
          if (pos.x() - ((points[0] - 1) * 8 + 7) < 1.1) {
            collision[2] = 1;
          }
        }
        if (((blright > 0) && (blright < 100) && (blright != 37))
            || ((blright > 342) && (blright < 347))) {
          if (((points[3] + 1) * 8) - (pos.x() / 8 + 14) < 1.1) {
            collision[3] = 1;
          }
        }
      }
      /* Invisible wall */
      if ((room == ROOM_CAVE.index()) && (r == 5)) {
        if ((points[0] - 1 == 0) || (points[0] - 1 == 1)) collision[2] = 0;
        if ((points[3] + 1 == 0) || (points[3] + 1 == 1)) collision[3] = 0;
      }
      if ((room == ROOM_BEAST.index()) && (r == 5)) {
        if ((points[0] - 1 > 27) && (points[0] - 1 < 32)) collision[2] = 0;
        if ((points[3] + 1 > 27) && (points[3] + 1 < 32)) collision[3] = 0;
      }
    }

    /* Touch ground collision */
    blground[0] = stagedata[points[7] + 1][points[0]];
    blground[1] = stagedata[points[7] + 1][points[1]];
    blground[2] = stagedata[points[7] + 1][points[2]];
    blground[3] = stagedata[points[7] + 1][points[3]];

    if (jump != JUMP) {
      /* Invisible ground */
      if (((room == ROOM_CAVE.index()) && (points[7] + 1 > 19) && (points[0] == 2))
          || ((room == ROOM_LAKE.index()) && ((pos.y() / 8) < 4) && (points[0] == 2))) {
        pos = new Vector2(pos.x(), pos.y() + gravity);
        jump = FALL;
      } else {
        if (((blground[0] > 0) && (blground[0] < 100))
            || ((blground[1] > 0) && (blground[1] < 100))
            || ((blground[2] > 0) && (blground[2] < 100))
            || ((blground[3] > 0) && (blground[3] < 100))) {
          ground = (points[7] + 1) * (int) Stage.getTileSize();
          if (points[7] + 1 > 21) {
            /* Dirty trick to make Jean go bottom of the screen */
            ground = 300 * PIXELS_PER_TILE;
          }
          if (ground - pos.y() - 24 > gravity * (int) Stage.getTileSize()) {
            pos = new Vector2(pos.x(), pos.y() + gravity);
          } else {
            /* Near ground */
            pos = new Vector2(pos.x(), ground - 3 * PIXELS_PER_TILE * 8);
            height = 0;
            jump = NEUTRAL;
            flags[5] = 0;
          }
        } else {
          /* In air, ground not near */
          pos = new Vector2(pos.x(), pos.y() + gravity);
          jump = FALL;
        }
      }
    }

    /* Check small platforms */
    // FIXME Are these directions correct?
    if (direction == LEFT) {
      if ((blground[3] == 38)
          && ((pos.x() + 13) < (points[3] * PIXELS_PER_TILE + 5))
          //          && (push[2] == 1)
          && (jump == NEUTRAL)) {
        pos = new Vector2(pos.x(), pos.y() + gravity);
        jump = FALL;
      }
    }
    if (direction == RIGHT) {
      if ((blground[0] == 38)
          && ((pos.x() + 1) > (points[0] + 2))
          //          && (push[3] == 1)
          && (jump == NEUTRAL)) {
        pos = new Vector2(pos.x(), pos.y() + gravity);
        jump = FALL;
      }
    }

    if ((jump == JUMP) && (points[4] > 0)) {
      /* Touch roof collision */
      blroof[0] = stagedata[points[4] - 1][points[0]];
      blroof[1] = stagedata[points[4] - 1][points[3]];

      if (((blroof[0] > 0)
              && (blroof[0] < 100)
              && (blroof[0] != 16)
              && (blroof[0] != 38)
              && (blroof[0] != 37))
          || ((blroof[1] > 0)
              && (blroof[1] < 100)
              && (blroof[1] != 16)
              && (blroof[1] != 38)
              && (blroof[1] != 37))) {
        if ((pos.y() - 1) - ((points[4] - 1) * PIXELS_PER_TILE + 7) < 1) {
          collision[0] = 1;
        }
      }
    }
    return (collision[0] + collision[1] + collision[2] + collision[3]) > 0;
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
    this.pos = new Vector2(Config.config().getScreenWidth() / 2, 1088.0f); // FIXME
  }

  public static Player of(Layer layer, Stage stage) {
    return new Player(layer, stage);
  }

  @Override
  public String toString() {
    return "Player{"
        + "manager="
        + manager
        + ", layer="
        + layer
        + ", stage="
        + stage
        + ", pos="
        + pos
        + ", v="
        + v
        + ", crouch="
        + crouch
        + ", direction="
        + direction
        + ", jump="
        + jump
        + ", height="
        + height
        + ", animation="
        + animation
        + ", points="
        + Arrays.toString(points)
        + ", ground="
        + ground
        + ", collision="
        + Arrays.toString(collision)
        + ", checkpoint="
        + Arrays.toString(checkpoint)
        + ", state="
        + Arrays.toString(state)
        + ", flags="
        + Arrays.toString(flags)
        + ", death="
        + death
        + ", walk="
        + walk
        + '}';
  }

  // Getters and setters

  @Override
  public Vector2 getPos() {
    return pos;
  }

  public void setPos(Vector2 pos) {
    this.pos = pos;
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
  public Facing getDirection() {
    return direction;
  }
}
