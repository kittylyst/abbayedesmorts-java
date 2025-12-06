/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Stage.NUM_COLUMNS;
import static abbaye.model.Stage.NUM_ROWS;

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
