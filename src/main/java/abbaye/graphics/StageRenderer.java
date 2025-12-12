/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import abbaye.basic.Renderable;
import abbaye.model.Stage;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

public class StageRenderer implements Renderable {
  private final long window;

  private Stage tilemap;
  private GLManager manager;

  public StageRenderer(long window) {
    this.window = window;
    this.manager = GLManager.get("game");
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
      float[] projection =
          GLManager.createOrthographicMatrix(0, width.get(0), height.get(0), 0, -1, 1);
      glUniformMatrix4fv(manager.getProjectionLocation(), false, projection);
    }

    manager.bindTexture("tiles");

    var tileDisplaySize = Stage.getTileSize();

    // Render each tile of this room
    for (int tileY = 0; tileY < Stage.NUM_ROWS; tileY += 1) {
      for (int tileX = 0; tileX < Stage.NUM_COLUMNS; tileX += 1) {
        var tileCoords = tilemap.getCorners(tileX, tileY);

        float displayPosX = tileX * tileDisplaySize;
        float displayPosY = tileY * tileDisplaySize;

        manager.renderTile(tileCoords, tileDisplaySize, displayPosX, displayPosY);
      }
    }

    return true;
  }

  /////////////// Matrix helpers

}
