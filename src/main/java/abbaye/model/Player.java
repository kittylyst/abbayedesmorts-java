/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.graphics.GLManager.*;
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

  public int[] getCollisions() {
    return collision;
  }

  public record Waypoint(int roomX, int roomY, float x, float y) {
    Waypoint(int roomX, int roomY, Vector2 pos) {
      this(roomX, roomY, pos.x(), pos.y());
    }

    public Vector2 getPos() {
      return new Vector2(x, y);
    }
  }

  // Constants
  public static final int RIGHT_EDGE = 243;
  public static final int LEFT_EDGE = 0;
  public static final int BOTTOM_EDGE = 146;
  public static final int TOP_EDGE = 0;

  // Collision array indices (UDLR - D is unused and handled by gravity effects)
  private static final int COLLISION_UP = 0;
  private static final int COLLISION_DOWN = 1; // Unused but documented
  private static final int COLLISION_LEFT = 2;
  private static final int COLLISION_RIGHT = 3;

  // Tile type IDs
  private static final int TILE_PASSABLE = 16;
  private static final int TILE_PASSABLE_VARIANT_1 = 37;
  private static final int TILE_PLATFORM = 38;
  private static final int TILE_SOLID_MAX = 100;
  private static final int TILE_SPECIAL_COLLISION = 128;
  private static final int TILE_SPECIAL_RIGHT = 344;
  private static final int TILE_SPECIAL_LEFT = 348;
  private static final int TILE_SPECIAL_RIGHT_MIN = 342;
  private static final int TILE_SPECIAL_RIGHT_MAX =
      347; // Exclusive upper bound for crouch range check
  private static final int TILE_SPECIAL_LEFT_MIN = 346;
  private static final int TILE_SPECIAL_LEFT_MAX = 351;

  // Collision detection thresholds (in pixels)
  private static final float COLLISION_DISTANCE_THRESHOLD = 1.1f;
  private static final float COLLISION_ROOF_DISTANCE_THRESHOLD = 1.0f;

  // Collision point pixel offsets (relative to player position, in tiles)
  private static final int COLLISION_LEFT_EDGE_OFFSET = 1;
  private static final int COLLISION_LEFT_MID_OFFSET = 7;
  private static final int COLLISION_CENTER_X_OFFSET = 8;
  private static final int COLLISION_RIGHT_EDGE_OFFSET = 13;
  private static final int COLLISION_TOP_EDGE_OFFSET = 1;
  private static final int COLLISION_MID_HEIGHT_OFFSET = 8;
  private static final int COLLISION_LOWER_MID_OFFSET = 15;
  private static final int COLLISION_BOTTOM_EDGE_OFFSET = 23;
  private static final int COLLISION_CROUCH_HEIGHT_OFFSET = 16;

  // Room-specific collision constants
  private static final int INVISIBLE_WALL_CROUCH_ROW = 5;
  private static final int INVISIBLE_GROUND_ROW_THRESHOLD = 19;
  private static final int INVISIBLE_GROUND_COLUMN = 2;
  private static final int ROOM_BEAST_INVISIBLE_WALL_START = 27;
  private static final int ROOM_BEAST_INVISIBLE_WALL_END = 32;
  private static final int SCREEN_BOTTOM_ROW_THRESHOLD = 21;

  // GL fields
  private GLManager manager;

  private Layer layer;
  private Stage stage;
  private GameLogger logger = Config.config().getLogger();

  // FIXME
  private int counter = 0;

  // Physicality - in pixels
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

  // Updated
  private int[] collision = {
    0, 0, 0, 0
  }; /* Collisions in directions UDLR - D is unused and handled by gravity effects */
  private Waypoint last = new Waypoint(0, 1, 100.0f, 1088.0f);

  //  private int[] checkpoint = new int[4];
  private int crosses = 0; // (previously state[1])
  private int lives = 5;
  private int[] flags = new int[7];
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

  @Override
  public boolean render() {
    if (!Config.config().getGLActive()) {
      return false;
    }

    if (counter % 500 == 0) {
      System.out.println(stage.getCache());
    }

    var tileDisplaySize = Stage.getTileSize();
    float posX, posY;
    Corners tileCoords;

    if (direction == LEFT) {
      // posX and posY represent where we're going to render
      // tileCoords represents where in the tile texture to pick out the player tile that we'll
      // render
      posX = pos.x();
      posY = pos.y();
      tileCoords = makeCorners(44, 11);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y();
      tileCoords = makeCorners(45, 11);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x();
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(44, 12);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(45, 12);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x();
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(44, 13);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(45, 13);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));
    } else {
      posX = pos.x();
      posY = pos.y();
      tileCoords = makeCorners(45, 11);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y();
      tileCoords = makeCorners(44, 11);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x();
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(45, 12);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(44, 12);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x();
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(45, 13);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(44, 13);
      manager.renderTile(tileCoords, playerMatrix(posX, posY, tileDisplaySize));
    }

    return false;
  }

  // Needs to take into account which way a player is facing.
  // May need to move this to Actor if we need to do the same trick for enemies
  float[] playerMatrix(float posX, float posY, float tileDisplaySize) {
    float[] translate = createTranslationMatrix(posX, posY, 0);
    int horizontalFlip = 1;
    if (getDirection() == RIGHT) {
      horizontalFlip = -1;
    }
    float[] scale = createScaleMatrix(horizontalFlip * tileDisplaySize, tileDisplaySize, 1);
    return multiplyMatrices(scale, translate);
  }

  public Vector2 newPosition() {
    if (checkStaticHazard()) {
      logger.info("Static hazard hit");
      if (lives <= 0) {
        lives = 5;
        logger.info("Resetting lives, need to exit game here instead");
      } else {
        lives -= 1;
      }
      stage.toWaypoint(last);
      return last.getPos();
    }
    if (checkCollision()) {
      logger.info("Collision detected: " + Arrays.toString(collision));
    }
    if (checkStaticObject()) {
      logger.debug("Static object detected");
    }

    float dx = 0;
    float dy = 0;

    /* Jump */
    if (jump == JUMP) {
      //      if (jean->height == 0) /* Jump sound */
      //        Mix_PlayChannel(-1, fx[3], 0);
      if (height < 56) {
        height += 1.6;
        if ((collision[COLLISION_UP] == 0) && (height < 44)) {
          dy -= 1.5;
        }
        animation = 0;
      } else {
        jump = FALL;
        collision[COLLISION_UP] = 0;
      }
    }

    /* Move to right */
    if (direction == RIGHT && walk) {
      if (collision[COLLISION_RIGHT] == 0) {
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
      if (collision[COLLISION_LEFT] == 0) {
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

  public void calculateCollision() {
    int blleft = 0;
    int blright = 0;
    int[] blground = {0, 0, 0, 0};
    int[] blroof = {0, 0};
    int[] points = {0, 0, 0, 0, 0, 0, 0, 0};
    int r = 0;

    float gravity = Config.config().getGravity();

    float resize = Stage.getTileSize();
    points[0] = (int) ((pos.x() + COLLISION_LEFT_EDGE_OFFSET * PIXELS_PER_TILE) / resize);
    points[1] = (int) ((pos.x() + COLLISION_LEFT_MID_OFFSET * PIXELS_PER_TILE) / resize);
    points[2] = (int) ((pos.x() + COLLISION_CENTER_X_OFFSET * PIXELS_PER_TILE) / resize);
    points[3] = (int) ((pos.x() + COLLISION_RIGHT_EDGE_OFFSET * PIXELS_PER_TILE) / resize);
    points[4] = (int) ((pos.y() + COLLISION_TOP_EDGE_OFFSET * PIXELS_PER_TILE) / resize);
    points[5] = (int) ((pos.y() + COLLISION_MID_HEIGHT_OFFSET * PIXELS_PER_TILE) / resize);
    points[6] = (int) ((pos.y() + COLLISION_LOWER_MID_OFFSET * PIXELS_PER_TILE) / resize);
    points[7] = (int) ((pos.y() + COLLISION_BOTTOM_EDGE_OFFSET * PIXELS_PER_TILE) / resize);

    // Reset collision state
    collision[COLLISION_UP] = 0;
    collision[COLLISION_DOWN] = 0;
    collision[COLLISION_LEFT] = 0;
    collision[COLLISION_RIGHT] = 0;

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
          if (((blleft > 0)
                  && (blleft < TILE_SOLID_MAX)
                  && (blleft != TILE_PASSABLE)
                  && (blleft != TILE_PLATFORM)
                  && (blleft != TILE_PASSABLE_VARIANT_1))
              || ((stagedata[points[4]][points[0]] == TILE_SPECIAL_COLLISION)
                  || (blleft == TILE_SPECIAL_LEFT))) {
            if (pos.x() - ((points[0] - 1) * PIXELS_PER_TILE + 7) < COLLISION_DISTANCE_THRESHOLD) {
              collision[COLLISION_LEFT] = 1;
            }
          }
          if (((blright > 0)
                  && (blright < TILE_SOLID_MAX)
                  && (blright != TILE_PASSABLE)
                  && (blright != TILE_PLATFORM)
                  && (blright != TILE_PASSABLE_VARIANT_1))
              || (blright == TILE_SPECIAL_RIGHT)) {
            if (((points[3] + 1) * PIXELS_PER_TILE) - (pos.x() / PIXELS_PER_TILE + 14)
                < COLLISION_DISTANCE_THRESHOLD) {
              collision[COLLISION_RIGHT] = 1;
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
        r =
            (int)
                ((pos.y() + COLLISION_CROUCH_HEIGHT_OFFSET * PIXELS_PER_TILE)
                    / Stage.getTileSize());
        blleft = stagedata[r][points[0] - 1];
        blright = stagedata[r][points[3] + 1];
        if (((blleft > 0) && (blleft < TILE_SOLID_MAX) && (blleft != TILE_PASSABLE_VARIANT_1))
            || ((stagedata[r][points[0]] == TILE_SPECIAL_COLLISION)
                || ((blleft > TILE_SPECIAL_LEFT_MIN) && (blleft < TILE_SPECIAL_LEFT_MAX)))) {
          if (pos.x() - ((points[0] - 1) * PIXELS_PER_TILE + 7) < COLLISION_DISTANCE_THRESHOLD) {
            collision[COLLISION_LEFT] = 1;
          }
        }
        if (((blright > 0) && (blright < TILE_SOLID_MAX) && (blright != TILE_PASSABLE_VARIANT_1))
            || ((blright > TILE_SPECIAL_RIGHT_MIN) && (blright < TILE_SPECIAL_RIGHT_MAX))) {
          if (((points[3] + 1) * PIXELS_PER_TILE) - (pos.x() / PIXELS_PER_TILE + 14)
              < COLLISION_DISTANCE_THRESHOLD) {
            collision[COLLISION_RIGHT] = 1;
          }
        }
      }
      /* Invisible wall */
      if ((room == ROOM_CAVE.index()) && (r == INVISIBLE_WALL_CROUCH_ROW)) {
        if ((points[0] - 1 == 0) || (points[0] - 1 == 1)) collision[COLLISION_LEFT] = 0;
        if ((points[3] + 1 == 0) || (points[3] + 1 == 1)) collision[COLLISION_RIGHT] = 0;
      }
      if ((room == ROOM_BEAST.index()) && (r == INVISIBLE_WALL_CROUCH_ROW)) {
        if ((points[0] - 1 > ROOM_BEAST_INVISIBLE_WALL_START)
            && (points[0] - 1 < ROOM_BEAST_INVISIBLE_WALL_END)) {
          collision[COLLISION_LEFT] = 0;
        }
        if ((points[3] + 1 > ROOM_BEAST_INVISIBLE_WALL_START)
            && (points[3] + 1 < ROOM_BEAST_INVISIBLE_WALL_END)) {
          collision[COLLISION_RIGHT] = 0;
        }
      }
    }

    /* Touch ground collision */
    blground[0] = stagedata[points[7] + 1][points[0]];
    blground[1] = stagedata[points[7] + 1][points[1]];
    blground[2] = stagedata[points[7] + 1][points[2]];
    blground[3] = stagedata[points[7] + 1][points[3]];

    if (jump != JUMP) {
      /* Invisible ground */
      if (((room == ROOM_CAVE.index())
              && (points[7] + 1 > INVISIBLE_GROUND_ROW_THRESHOLD)
              && (points[0] == INVISIBLE_GROUND_COLUMN))
          || ((room == ROOM_LAKE.index())
              && ((pos.y() / PIXELS_PER_TILE) < 4)
              && (points[0] == INVISIBLE_GROUND_COLUMN))) {
        pos = new Vector2(pos.x(), pos.y() + gravity);
        jump = FALL;
      } else {
        if (((blground[0] > 0) && (blground[0] < TILE_SOLID_MAX))
            || ((blground[1] > 0) && (blground[1] < TILE_SOLID_MAX))
            || ((blground[2] > 0) && (blground[2] < TILE_SOLID_MAX))
            || ((blground[3] > 0) && (blground[3] < TILE_SOLID_MAX))) {
          ground = (points[7] + 1) * (int) Stage.getTileSize();
          if (points[7] + 1 > SCREEN_BOTTOM_ROW_THRESHOLD) {
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
    if (direction == LEFT) {
      if ((blground[3] == TILE_PLATFORM)
          && ((pos.x() + COLLISION_RIGHT_EDGE_OFFSET * PIXELS_PER_TILE)
              < (points[3] * PIXELS_PER_TILE + 5))
          //          && (push[2] == 1)
          && (jump == NEUTRAL)) {
        pos = new Vector2(pos.x(), pos.y() + gravity);
        jump = FALL;
      }
    }
    if (direction == RIGHT) {
      if ((blground[0] == TILE_PLATFORM)
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
              && (blroof[0] < TILE_SOLID_MAX)
              && (blroof[0] != TILE_PASSABLE)
              && (blroof[0] != TILE_PLATFORM)
              && (blroof[0] != TILE_PASSABLE_VARIANT_1))
          || ((blroof[1] > 0)
              && (blroof[1] < TILE_SOLID_MAX)
              && (blroof[1] != TILE_PASSABLE)
              && (blroof[1] != TILE_PLATFORM)
              && (blroof[1] != TILE_PASSABLE_VARIANT_1))) {
        if ((pos.y() - 1) - ((points[4] - 1) * PIXELS_PER_TILE + 7)
            < COLLISION_ROOF_DISTANCE_THRESHOLD) {
          collision[COLLISION_UP] = 1;
        }
      }
    }
  }

  /**
   * This is (only) for collisions with walls
   *
   * @return
   */
  public boolean checkCollision() {
    calculateCollision();
    return (collision[COLLISION_UP]
            + collision[COLLISION_DOWN]
            + collision[COLLISION_LEFT]
            + collision[COLLISION_RIGHT])
        > 0;
  }

  /**
   * @return true if player has touched a static hazard
   */
  public boolean checkStaticHazard() {
    var stagedata = stage.getScreen(stage.getRoom());

    /* Touch static hazard */
    if (stagedata[1 + pos.tileY()][pos.tileX()] == 5
        || stagedata[1 + pos.tileY()][1 + pos.tileX()] == 5
        || stagedata[2 + pos.tileY()][pos.tileX()] == 5
        || stagedata[2 + pos.tileY()][1 + pos.tileX()] == 5
        || stagedata[3 + pos.tileY()][pos.tileX()] == 5
        || stagedata[3 + pos.tileY()][1 + pos.tileX()] == 5) {
      return true;
    }

    return false;
  }

  public boolean checkStaticObject() {
    int room = stage.getRoom();
    var stagedata = stage.getScreen(room);

    /* Touch hearts */
    if (room == ROOM_ASHES.index()) {
      if (((stagedata[1 + pos.tileY()][pos.tileX()] > 400)
              && (stagedata[1 + pos.tileY()][pos.tileX()] < 405))
          || ((stagedata[1 + pos.tileY()][1 + pos.tileX()] > 400)
              && (stagedata[1 + pos.tileY()][1 + pos.tileX()] < 405))) {
        if (pos.tileX() > 160) {
          stagedata[7][23] = 0;
          stagedata[7][24] = 0;
          stagedata[8][23] = 0;
          stagedata[8][24] = 0;
        } else {
          stagedata[18][8] = 0;
          stagedata[18][9] = 0;
          stagedata[19][8] = 0;
          stagedata[19][9] = 0;
        }
        if (lives < 9) {
          lives += 1;
        }
        //        Mix_PlayChannel(-1, fx[2], 0);
        return true;
      }
    } else {
      if (((stagedata[1 + pos.tileY()][pos.tileX()] > 400)
              && (stagedata[1 + pos.tileY()][pos.tileX()] < 405))
          || ((stagedata[1 + pos.tileY()][1 + pos.tileX()] > 400)
              && (stagedata[1 + pos.tileY()][1 + pos.tileX()] < 405))) {
        for (var v = 0; v < 22; v++) {
          for (var h = 0; h < 32; h++) {
            if ((stagedata[v][h] > 400) && (stagedata[v][h] < 405)) stagedata[v][h] = 0;
          }
        }
        if (lives < 9) {
          lives += 1;
          //        Mix_PlayChannel(-1, fx[2], 0);
        }
        return true;
      }
    }

    /* Touch crosses */
    if (((stagedata[1 + pos.tileY()][pos.tileX()] > 408)
            && (stagedata[1 + pos.tileY()][pos.tileX()] < 413))
        || ((stagedata[1 + pos.tileY()][1 + pos.tileX()] > 408)
            && (stagedata[1 + pos.tileY()][1 + pos.tileX()] < 413))) {
      for (var v = 0; v < 22; v++) {
        for (var h = 0; h < 32; h++) {
          if ((stagedata[v][h] > 408) && (stagedata[v][h] < 413)) stagedata[v][h] = 0;
        }
      }
      crosses += 1;
      //        Mix_PlayChannel(-1, fx[2], 0);

      return true;
    }

    // 321 - 326
    /* Touch waypoint crosses */
    if (((stagedata[1 + pos.tileY()][pos.tileX()] > 320)
            && (stagedata[1 + pos.tileY()][pos.tileX()] < 327))
        || ((stagedata[1 + pos.tileY()][1 + pos.tileX()] > 320)
            && (stagedata[1 + pos.tileY()][1 + pos.tileX()] < 327))) {
      for (var v = 0; v < 22; v++) {
        for (var h = 0; h < 32; h++) {
          // FIXME - Don't nuke the waypoint cross, toggle instead.
          if ((stagedata[v][h] > 320) && (stagedata[v][h] < 327)) stagedata[v][h] = 0;
        }
      }
      // Update waypoint
      logger.info("Updating waypoint here: " + last);
      last = new Waypoint(stage.getRoomX(), stage.getRoomY(), pos);
      //        Mix_PlayChannel(-1, fx[2], 0);

      return true;
    }

    return false;
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
        last.getPos(); // new Vector2(Config.config().getScreenWidth() / 2, 1088.0f); // FIXME
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
        + ", waypoint="
        + last
        + ", flags="
        + Arrays.toString(flags)
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

  public int getLives() {
    return lives;
  }

  public int getCrosses() {
    return crosses;
  }
}
