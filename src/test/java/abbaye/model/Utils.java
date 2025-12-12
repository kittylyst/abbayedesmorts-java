/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Stage.*;

import java.lang.reflect.Field;

public class Utils {
  private Utils() {}

  /** Helper to get private field using reflection */
  @SuppressWarnings("unchecked")
  static <T> T getPrivateField(Object obj, String fieldName) {
    try {
      Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(obj);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get field " + fieldName, e);
    }
  }

  /** Helper to set the floor in the current room */
  static void setStep(Stage stage, final int floorLevel) {
    int room = stage.getRoom();
    var stagedata = stage.getScreen(room);
    int[] row1 = {
      101, 102, 103, 101, 0, 0, 0, 102,
      103, 0, 101, 0, 102, 103, 0, 102,
      103, 0, 101, 0, 0, 0, 3, 4,
      3, 4, 0, 104, 105, 0, 0, 0
    };
    int[] row2 = {
      101, 102, 103, 101, 0, 0, 0, 102,
      103, 0, 101, 0, 102, 103, 0, 102,
      103, 0, 101, 0, 3, 4, 1, 2,
      1, 2, 0, 102, 103, 0, 101, 101
    };
    for (int x = 0; x < NUM_COLUMNS; x += 1) {
      stagedata[floorLevel + 1][x] = row1[x];
      stagedata[floorLevel + 2][x] = row2[x];
    }
  }

  /** Helper to set the floor in the current room */
  static void setFloor(Stage stage, final int floorLevel) {
    int room = stage.getRoom();
    var stagedata = stage.getScreen(room);
    // y == 16 special case - topsoil
    for (int x = 0; x < NUM_COLUMNS; x += 1) {
      stagedata[floorLevel][x] = x % 2 == 0 ? TILE_TOPSOIL1 : TILE_TOPSOIL2;
    }
    // Bedrock
    for (int y = floorLevel + 1; y < NUM_ROWS; y += 1) {
      for (int x = 0; x < NUM_COLUMNS; x += 1) {
        stagedata[y][x] = x % 2 == 0 ? TILE_BEDROCK1 : TILE_BEDROCK2;
      }
    }
  }

  /** Helper to set a tile in the current room */
  static void setTile(Stage stage, int x, int y, int tileType) {
    int room = stage.getRoom();
    var stagedata = stage.getScreen(room);
    if (y >= 0 && y < NUM_ROWS && x >= 0 && x < NUM_COLUMNS) {
      stagedata[y][x] = tileType;
    }
  }

  /** Helper to set a rectangular area of tiles */
  static void setTiles(Stage stage, int x1, int y1, int x2, int y2, int tileType) {
    for (int y = y1; y <= y2 && y < NUM_ROWS; y++) {
      for (int x = x1; x <= x2 && x < NUM_COLUMNS; x++) {
        setTile(stage, x, y, tileType);
      }
    }
  }

  /** Helper to set private field using reflection */
  static void setPrivateField(Object obj, String fieldName, Object value) {
    try {
      Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(obj, value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set field " + fieldName, e);
    }
  }

  static void setDirection(Player player, Facing direction) {
    setPrivateField(player, "direction", direction);
  }

  static void setCrouch(Player player, boolean crouch) {
    setPrivateField(player, "crouch", crouch);
  }

  static void setJump(Player player, Vertical jump) {
    setPrivateField(player, "jump", jump);
  }

  static void setHeight(Player player, float height) {
    setPrivateField(player, "height", height);
  }

  static Vertical getJump(Player player) {
    return getPrivateField(player, "jump");
  }
}
