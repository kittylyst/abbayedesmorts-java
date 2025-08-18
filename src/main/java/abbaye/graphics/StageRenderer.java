/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import abbaye.basic.Renderable;
import abbaye.model.Stage;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

public class StageRenderer implements Renderable {
  private final long window;
  private final int tilesTexture;

  private Stage tilemap;
  private GLManager manager;

  public StageRenderer(long window) {
    this.window = window;
    this.manager = GLManager.get("game");
    this.tilesTexture = GLManager.loadTexture("/tiles.png", true, true);
  }

  public void init(Stage stage) {
    tilemap = stage;
  }

  public boolean render() {
    // Update viewport
    try (MemoryStack stack = stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      glfwGetFramebufferSize(window, width, height);
      glViewport(0, 0, width.get(0), height.get(0));

      // Set up orthographic projection
      float[] projection = createOrthographicMatrix(0, width.get(0), height.get(0), 0, -1, 1);
      glUniformMatrix4fv(manager.getProjectionLocation(), false, projection);
    }

    manager.bindTexture(tilesTexture);

    var tileDisplaySize = Stage.getTileSize();

    // Render each tile of this room
    for (int y = 0; y < Stage.NUM_ROWS; y += 1) {
      for (int x = 0; x < Stage.NUM_COLUMNS; x += 1) {
        var tileCoords = tilemap.getCorners(x, y);

        float displayPosX = x * tileDisplaySize;
        float displayPosY = y * tileDisplaySize;

        manager.renderTile(tileCoords, tileDisplaySize, displayPosX, displayPosY);
      }
    }

    return true;
  }

  /////////////// Matrix helpers

  public static float[] createOrthographicMatrix(
      float left, float right, float bottom, float top, float near, float far) {
    float[] matrix = new float[16];
    matrix[0] = 2.0f / (right - left);
    matrix[5] = 2.0f / (top - bottom);
    matrix[10] = -2.0f / (far - near);
    matrix[12] = -(right + left) / (right - left);
    matrix[13] = -(top + bottom) / (top - bottom);
    matrix[14] = -(far + near) / (far - near);
    matrix[15] = 1.0f;

    //    float[] projectionMatrix = {
    //      1.0f, 0.0f, 0.0f, 0.0f,
    //      0.0f, 1.0f, 0.0f, 0.0f,
    //      0.0f, 0.0f, -1.0f, 0.0f,
    //      0.0f, 0.0f, 0.0f, 1.0f
    //    };

    return matrix;
  }
}
