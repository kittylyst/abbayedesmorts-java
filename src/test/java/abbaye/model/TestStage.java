/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import abbaye.AbbayeMain;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestStage {

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {
    AbbayeMain.setGlEnabled(false);
  }

  @Test
  public void loadMap() {
    var stage = new Stage();
    stage.load();
    var level0 = stage.getScreen(0);
    assertEquals(22, level0.length);
  }
}
