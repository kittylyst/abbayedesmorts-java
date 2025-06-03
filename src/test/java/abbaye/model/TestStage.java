/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestStage {

  @Test
  public void loadMap() {
    var stage = new Stage();
    stage.load();
    var level0 = stage.getLevel(0);
    assertEquals(22, level0.length);
  }
}
