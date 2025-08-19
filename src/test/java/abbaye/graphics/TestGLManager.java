/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static abbaye.graphics.GLManager.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import abbaye.AbbayeMain;
import abbaye.Config;
import org.junit.jupiter.api.Test;

public class TestGLManager {

  @Test
  public void testMatrices() {
    Config.config().setHeadless(true);
    AbbayeMain.glStaticInit();

    var tileSize = 0.7f;
    var x = 0.6f;
    var y = 0.4f;

    float[] testModel = {tileSize, 0, 0, 0, 0, tileSize, 0, 0, 0, 0, 1, 0, x, y, Z_ZERO, 1};

    float[] translateModel = createTranslationMatrix(x, y, 0);
    float[] scaleModel = createScaleMatrix(tileSize, tileSize, 1);
    float[] finalModel = multiplyMatrices(scaleModel, translateModel);

    assertArrayEquals(testModel, finalModel);
  }
}
