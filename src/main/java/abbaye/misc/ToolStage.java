/* Copyright (C) The Authors 2025 */
package abbaye.misc;

import static abbaye.model.Stage.NUM_COLUMNS;
import static abbaye.model.Stage.NUM_ROWS;

import abbaye.model.Stage;
import java.util.HashMap;
import java.util.Map;

public class ToolStage {

  private static Map<Integer, Character> ROGUE = new HashMap<>();

  static {
    ROGUE.put(0, ' ');
    ROGUE.put(32, 'A');
    ROGUE.put(33, 'B');
    ROGUE.put(34, 'C');
    ROGUE.put(35, 'D');
  }

  public static void main(String[] args) {
    var m = new ToolStage();
    var stage = new Stage();
    stage.load();
    m.prettyPrint(stage);
  }

  void prettyPrint(Stage stage) {
    for (var s = 0; s < Stage.NUM_SCREENS; s += 1) {
      int[][] screen = stage.getScreen(s);
      for (var y = 0; y < NUM_ROWS; y += 1) {
        for (var x = 0; x < NUM_COLUMNS; x += 1) {
          System.out.print(ROGUE.getOrDefault(screen[y][x], '*'));
        }
        System.out.println();
      }
      System.out.println();
      System.out.println();
    }
  }
}
