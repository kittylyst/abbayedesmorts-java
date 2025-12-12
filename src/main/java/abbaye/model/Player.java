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

  // Collision array indices (UDLR - D is unused and handled by gravity effects)
  static final int COLLISION_UP = 0;
  static final int COLLISION_DOWN = 1; // Unused but documented
  static final int COLLISION_LEFT = 2;
  static final int COLLISION_RIGHT = 3;

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

  // Position calculation offsets
  private static final int WALL_COLLISION_LEFT_OFFSET = 7;
  private static final int WALL_COLLISION_RIGHT_OFFSET = 15;
  private static final int PLAYER_HEIGHT_PIXELS = 24;
  private static final int GROUND_SNAP_OFFSET_MULTIPLIER = 3;
  private static final int PLATFORM_FALL_THRESHOLD_X = 5;
  private static final int PLATFORM_FALL_OFFSET_X = 2;
  private static final int PLATFORM_CHECK_X_OFFSET_RIGHT = 1;

  // Special values
  private static final int SCREEN_BOTTOM_TELEPORT_TILES = 300;
  private static final int DEBUG_LOG_FREQUENCY = 10;

  // GL fields
  private GLManager manager;

  private Layer layer;
  private Stage stage;
  private GameLogger logger = Config.config().getLogger();

  // FIXME
  private int counter = 0;

  // Physical attributes - in pixels
  /* Top-left corner of player  */
  private Vector2 pos = new Vector2(0, 0);
  private Vector2 v = new Vector2(0, 0);
  private boolean crouch = false;

  // From C code
  private Facing direction = RIGHT;
  private Vertical jump = NEUTRAL;
  private float height; /* Limit of jump */
  private int animation;
  private int ground; /* Y-coordinate pixel where the ground is beneath the player */

  /* Collisions in directions UDLR - D is unused and handled by gravity effects */
  private int[] collision = {0, 0, 0, 0};
  private Waypoint last = new Waypoint(0, 1, 192.0f, 1088.0f); // x = 100.0f

  private int crosses = 0; // (previously state[1])
  private int lives = 5;
  private int[] flags = new int[7];
  private boolean walk = false;

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

  public Corners makeCorners(int tileX, int tileY) {
    float u1 = (float) tileX / TILES_PER_ROW;
    float v1 = (float) tileY / TILES_PER_COL;
    float u2 = (float) (tileX + 1) / TILES_PER_ROW;
    float v2 = (float) (tileY + 1) / TILES_PER_COL;

    if (getDirection() == RIGHT) {
      return new Corners(u2, 1 - v1, u1, 1 - v2);
    }

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
      // posX and posY represent where we're going to render
      // tileCoords represents where in the tile texture to pick out the player tile that we'll
      // render
      posX = pos.x();
      posY = pos.y();
      tileCoords = makeCorners(44, 11);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x() + tileDisplaySize;
      posY = pos.y();
      tileCoords = makeCorners(45, 11);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x();
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(44, 12);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(45, 12);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x();
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(44, 13);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(45, 13);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);
    } else {
      // RIGHT
      posX = pos.x();
      posY = pos.y();
      tileCoords = makeCorners(45, 11);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      //        X: 1153.0 Y: 1088.0
      //                [64.0, 0.0, 0.0, 0.0, 0.0, 64.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1153.0,
      // 1088.0, 0.0, 1.0]

      posX = pos.x() + tileDisplaySize;
      posY = pos.y();
      tileCoords = makeCorners(44, 11);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x();
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(45, 12);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize;
      tileCoords = makeCorners(44, 12);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x();
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(45, 13);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);

      posX = pos.x() + tileDisplaySize;
      posY = pos.y() + tileDisplaySize + tileDisplaySize;
      tileCoords = makeCorners(44, 13);
      manager.renderTile(tileCoords, tileDisplaySize, posX, posY);
    }

    return false;
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
          case GLFW_KEY_TAB:
            {
              logger.info(this.toString());
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

  int[][] getTileGrid() {
    int[][] out = new int[4][3];
    float resize = Stage.getTileSize();

    var leftX = (int) (pos.x() / resize);
    var midX = 1 + (int) (pos.x() / resize);
    var rightX = 1 + (int) ((pos.x() + resize - 0.1) / resize);

    var topY = (int) (pos.y() / resize);
    var upperMidY = 1 + (int) (pos.y() / resize);
    var lowerMidY = 2 + (int) (pos.y() / resize);
    var bottomY = 2 + (int) ((pos.y() + resize - 0.1) / resize);

    var currentRoomData = stage.getScreen(stage.getRoom());

    out[0] =
        new int[] {
          currentRoomData[topY][leftX], currentRoomData[topY][midX], currentRoomData[topY][rightX]
        };
    out[1] =
        new int[] {
          currentRoomData[upperMidY][leftX],
          currentRoomData[upperMidY][midX],
          currentRoomData[upperMidY][rightX]
        };
    out[2] =
        new int[] {
          currentRoomData[lowerMidY][leftX],
          currentRoomData[lowerMidY][midX],
          currentRoomData[lowerMidY][rightX]
        };
    out[3] =
        new int[] {
          currentRoomData[bottomY][leftX],
          currentRoomData[bottomY][midX],
          currentRoomData[bottomY][rightX]
        };
    return out;
  }

  //  void oldCode() {
  //      CHECKS:
  //      for (var n = 0; n < 4; n += 1) {
  //          // Boundary check: ensure we're within valid tile coordinates
  //          if ((xpoints[0] <= 0) || (xpoints[3] + 1 >= NUM_COLUMNS) || (ypoints[n] + 1 >=
  // NUM_ROWS)) {
  //              break CHECKS;
  //          }
  //
  //          // Only check collisions in the direction the player is moving
  //          if (((xpoints[0] > 0) && (direction == LEFT))
  //                  || ((xpoints[3] + 1 < NUM_COLUMNS) && (direction == RIGHT))) {
  //              // Get tile types at collision points
  //
  //              // Left: check tile to the left of player's left edge
  //              tileLeft = currentRoomData[ypoints[n]][xpoints[0] - 1];
  //
  //              // Right: check tile to the right of player's right edge
  //              tileRight = currentRoomData[ypoints[n]][xpoints[3] + 1];
  //
  //              if (counter++ % DEBUG_LOG_FREQUENCY == 0) {
  //                  logger.debug(
  //                          pos
  //                                  + " ; tileLeft: "
  //                                  + tileLeft
  //                                  + " ; tileRight: "
  //                                  + tileRight
  //                                  + " ; ground: "
  //                                  + ground
  //                                  + " ; xp: "
  //                                  + Arrays.toString(xpoints)
  //                                  + " ; yp: "
  //                                  + Arrays.toString(ypoints));
  //              }
  //
  //              // Check left collision
  //              // A tile is solid if it's a regular solid tile OR a special collision tile
  //              if (((tileLeft > 0)
  //                      && (tileLeft < TILE_SOLID_MAX)
  //                      && (tileLeft != TILE_PASSABLE)
  //                      && (tileLeft != TILE_PLATFORM)
  //                      && (tileLeft != TILE_PASSABLE_VARIANT_1))
  //                      || ((currentRoomData[ypoints[0]][xpoints[0]] == TILE_SPECIAL_COLLISION)
  //                      || (tileLeft == TILE_SPECIAL_LEFT))) {
  //
  //                  // Calculate distance from player's left edge to the wall
  //                  // Wall position: (xpoints[0] - 1) * PIXELS_PER_TILE (left edge of tile to the
  // left)
  //                  // Player left edge: pos.x() + WALL_COLLISION_LEFT_OFFSET
  //                  // Distance = player position - wall position
  //                  if (pos.x() - ((xpoints[0] - 1) * PIXELS_PER_TILE +
  // WALL_COLLISION_LEFT_OFFSET)
  //                          < COLLISION_DISTANCE_THRESHOLD) {
  //                      collision[COLLISION_LEFT] = 1;
  //                  }
  //              }
  //
  //              // Check right collision
  //              // A tile is solid if it's a regular solid tile OR a special right collision tile
  //              if (((tileRight > 0)
  //                      && (tileRight < TILE_SOLID_MAX)
  //                      && (tileRight != TILE_PASSABLE)
  //                      && (tileRight != TILE_PLATFORM)
  //                      && (tileRight != TILE_PASSABLE_VARIANT_1))
  //                      || (tileRight == TILE_SPECIAL_RIGHT)) {
  //
  //                  // Calculate distance from player position to the wall
  //                  // Wall position: (xpoints[3] + 1) * PIXELS_PER_TILE +
  // WALL_COLLISION_RIGHT_OFFSET
  //                  // Player position: pos.x()
  //                  // Distance = wall position - player position
  //                  if (((xpoints[3] + 1) * PIXELS_PER_TILE)
  //                          - (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)
  //                          < COLLISION_DISTANCE_THRESHOLD) {
  //                      collision[COLLISION_RIGHT] = 1;
  //                  }
  //              }
  //          }
  //      }
  //  }

  // Crouched code goes here
  //      // FIXME Are these directions correct?
  //      if (((xpoints[0] != 0) && (direction == LEFT))
  //          || ((xpoints[3] != NUM_COLUMNS - 1) && (direction == RIGHT))) {
  //        r =
  //            (int)
  //                ((pos.y() + COLLISION_CROUCH_HEIGHT_OFFSET * PIXELS_PER_TILE)
  //                    / Stage.getTileSize());
  //        tileLeft = currentRoomData[r][xpoints[0] - 1];
  //        tileRight = currentRoomData[r][xpoints[3] + 1];
  //        if (((tileLeft > 0) && (tileLeft < TILE_SOLID_MAX) && (tileLeft !=
  // TILE_PASSABLE_VARIANT_1))
  //            || ((currentRoomData[r][xpoints[0]] == TILE_SPECIAL_COLLISION)
  //                || ((tileLeft > TILE_SPECIAL_LEFT_MIN) && (tileLeft <
  // TILE_SPECIAL_LEFT_MAX)))) {
  //          if (pos.x() - ((xpoints[0] - 1) * PIXELS_PER_TILE + WALL_COLLISION_LEFT_OFFSET)
  //              < COLLISION_DISTANCE_THRESHOLD) {
  //            collision[COLLISION_LEFT] = 1;
  //          }
  //        }
  //        if (((tileRight > 0)
  //                && (tileRight < TILE_SOLID_MAX)
  //                && (tileRight != TILE_PASSABLE_VARIANT_1))
  //            || ((tileRight > TILE_SPECIAL_RIGHT_MIN) && (tileRight <
  // TILE_SPECIAL_RIGHT_MAX))) {
  //          if (((xpoints[3] + 1) * PIXELS_PER_TILE)
  //                  - (pos.x() / PIXELS_PER_TILE + WALL_COLLISION_RIGHT_OFFSET)
  //              < COLLISION_DISTANCE_THRESHOLD) {
  //            collision[COLLISION_RIGHT] = 1;
  //          }
  //        }
  //      }
  //      /* Invisible wall */
  //      if ((room == ROOM_CAVE.index()) && (r == INVISIBLE_WALL_CROUCH_ROW)) {
  //        if ((xpoints[0] - 1 == 0) || (xpoints[0] - 1 == 1)) collision[COLLISION_LEFT] = 0;
  //        if ((xpoints[3] + 1 == 0) || (xpoints[3] + 1 == 1)) collision[COLLISION_RIGHT] = 0;
  //      }
  //      if ((room == ROOM_BEAST.index()) && (r == INVISIBLE_WALL_CROUCH_ROW)) {
  //        if ((xpoints[0] - 1 > ROOM_BEAST_INVISIBLE_WALL_START)
  //            && (xpoints[0] - 1 < ROOM_BEAST_INVISIBLE_WALL_END)) {
  //          collision[COLLISION_LEFT] = 0;
  //        }
  //        if ((xpoints[3] + 1 > ROOM_BEAST_INVISIBLE_WALL_START)
  //            && (xpoints[3] + 1 < ROOM_BEAST_INVISIBLE_WALL_END)) {
  //          collision[COLLISION_RIGHT] = 0;
  //        }
  //      }

  public void calculateCollision() {
    int[] xpoints = {0, 0, 0, 0};
    int[] ypoints = {0, 0, 0, 0};

    float gravity = Config.config().getGravity();

    float resize = Stage.getTileSize();
    xpoints[0] = (int) ((pos.x() + COLLISION_LEFT_EDGE_OFFSET * PIXELS_PER_TILE) / resize);
    xpoints[1] = (int) ((pos.x() + COLLISION_LEFT_MID_OFFSET * PIXELS_PER_TILE) / resize);
    xpoints[2] = (int) ((pos.x() + COLLISION_CENTER_X_OFFSET * PIXELS_PER_TILE) / resize);
    xpoints[3] = (int) ((pos.x() + COLLISION_RIGHT_EDGE_OFFSET * PIXELS_PER_TILE) / resize);
    ypoints[0] = (int) ((pos.y() + COLLISION_TOP_EDGE_OFFSET * PIXELS_PER_TILE) / resize);
    ypoints[1] = (int) ((pos.y() + COLLISION_MID_HEIGHT_OFFSET * PIXELS_PER_TILE) / resize);
    ypoints[2] = (int) ((pos.y() + COLLISION_LOWER_MID_OFFSET * PIXELS_PER_TILE) / resize);
    ypoints[3] = (int) ((pos.y() + COLLISION_BOTTOM_EDGE_OFFSET * PIXELS_PER_TILE) / resize);

    // Reset collision state
    collision[COLLISION_UP] = 0;
    collision[COLLISION_DOWN] = 0;
    collision[COLLISION_LEFT] = 0;
    collision[COLLISION_RIGHT] = 0;

    var room = stage.getRoom();
    var currentRoomData = stage.getScreen(room);

    var points = getTileGrid();

    /* Left & Right collisions */
    if (crouch) {
      /* Collision with Jean ducking */

    } else {
      for (var y = 0; y < 4; y++) {
        var tile = points[y][0];
        if (tile > 0 && (tile < TILE_SOLID_MAX) && (tile != TILE_PASSABLE_VARIANT_1)
            || ((tile == TILE_SPECIAL_COLLISION)
                || ((tile > TILE_SPECIAL_LEFT_MIN) && (tile < TILE_SPECIAL_LEFT_MAX)))) {
          collision[COLLISION_LEFT] = 1;
        }

        tile = points[y][2];
        if (((tile > 0) && (tile < TILE_SOLID_MAX) && (tile != TILE_PASSABLE_VARIANT_1))
            || ((tile > TILE_SPECIAL_RIGHT_MIN) && (tile < TILE_SPECIAL_RIGHT_MAX))) {
          collision[COLLISION_RIGHT] = 1;
        }
      }
    }

    //    int tileLeft = 0;
    //    int tileRight = 0;
    int[] blground = {0, 0, 0, 0};
    int[] blroof = {0, 0};

    /* Touch ground collision */
    //    blground[0] = points[0][0];
    blground[0] = currentRoomData[ypoints[3] + 1][xpoints[0]];
    blground[1] = currentRoomData[ypoints[3] + 1][xpoints[1]];
    blground[2] = currentRoomData[ypoints[3] + 1][xpoints[2]];
    blground[3] = currentRoomData[ypoints[3] + 1][xpoints[3]];

    if (jump != JUMP) {
      /* Invisible ground */
      if (((room == ROOM_CAVE.index())
              && (ypoints[3] + 1 > INVISIBLE_GROUND_ROW_THRESHOLD)
              && (xpoints[0] == INVISIBLE_GROUND_COLUMN))
          || ((room == ROOM_LAKE.index())
              && ((pos.y() / PIXELS_PER_TILE) < 4)
              && (xpoints[0] == INVISIBLE_GROUND_COLUMN))) {
        pos = new Vector2(pos.x(), pos.y() + gravity);
        jump = FALL;
      } else {
        if (((blground[0] > 0) && (blground[0] < TILE_SOLID_MAX))
            || ((blground[1] > 0) && (blground[1] < TILE_SOLID_MAX))
            || ((blground[2] > 0) && (blground[2] < TILE_SOLID_MAX))
            || ((blground[3] > 0) && (blground[3] < TILE_SOLID_MAX))) {
          ground = (ypoints[3] + 1) * (int) Stage.getTileSize();
          if (ypoints[3] + 1 > SCREEN_BOTTOM_ROW_THRESHOLD) {
            /* Dirty trick to make Jean go bottom of the screen */
            ground = SCREEN_BOTTOM_TELEPORT_TILES * PIXELS_PER_TILE;
          }
          if (ground - pos.y() - PLAYER_HEIGHT_PIXELS > gravity * (int) Stage.getTileSize()) {
            pos = new Vector2(pos.x(), pos.y() + gravity);
          } else {
            /* Near ground */
            pos =
                new Vector2(
                    pos.x(),
                    ground - GROUND_SNAP_OFFSET_MULTIPLIER * PIXELS_PER_TILE * PIXELS_PER_TILE);
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
              < (xpoints[3] * PIXELS_PER_TILE + PLATFORM_FALL_THRESHOLD_X))
          //          && (push[2] == 1)
          && (jump == NEUTRAL)) {
        pos = new Vector2(pos.x(), pos.y() + gravity);
        jump = FALL;
      }
    }
    if (direction == RIGHT) {
      if ((blground[0] == TILE_PLATFORM)
          && ((pos.x() + PLATFORM_CHECK_X_OFFSET_RIGHT * PIXELS_PER_TILE)
              > (xpoints[0] * PIXELS_PER_TILE + PLATFORM_FALL_OFFSET_X * PIXELS_PER_TILE))
          //          && (push[3] == 1)
          && (jump == NEUTRAL)) {
        pos = new Vector2(pos.x(), pos.y() + gravity);
        jump = FALL;
      }
    }

    if ((jump == JUMP) && (ypoints[0] > 0)) {
      /* Touch roof collision */
      blroof[0] = currentRoomData[ypoints[0] - 1][xpoints[0]];
      blroof[1] = currentRoomData[ypoints[0] - 1][xpoints[3]];

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
        if ((pos.y() - 1) - ((ypoints[0] - 1) * PIXELS_PER_TILE + WALL_COLLISION_LEFT_OFFSET)
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
    if (stagedata[1 + pos.tileY()][pos.tileX()] == TILE_STATIC_HAZARD
        || stagedata[1 + pos.tileY()][1 + pos.tileX()] == TILE_STATIC_HAZARD
        || stagedata[2 + pos.tileY()][pos.tileX()] == TILE_STATIC_HAZARD
        || stagedata[2 + pos.tileY()][1 + pos.tileX()] == TILE_STATIC_HAZARD
        || stagedata[3 + pos.tileY()][pos.tileX()] == TILE_STATIC_HAZARD
        || stagedata[3 + pos.tileY()][1 + pos.tileX()] == TILE_STATIC_HAZARD) {
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
