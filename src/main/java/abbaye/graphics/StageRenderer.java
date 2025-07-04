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

      // Bind texture
      glBindTexture(GL_TEXTURE_2D, tilesTexture);
      glBindVertexArray(manager.getVAO());

      var shaderProgram = manager.getShaderProgram();
      glUseProgram(shaderProgram);

      // FIXME Are these tiles square?
      var tileDisplaySize = Stage.getTileSize();

      // Render each tile of this room
      for (int y = 0; y < Stage.NUM_ROWS; y++) {
        for (int x = 0; x < Stage.NUM_COLUMNS; x++) {
          var tileCoords = tilemap.getCorners(x, y);

          float displayPosX = x * tileDisplaySize;
          float displayPosY = y * tileDisplaySize;

          manager.renderTile(tileCoords, tileDisplaySize, displayPosX, displayPosY);
        }
      }

      glBindVertexArray(manager.getVAO());
      glUseProgram(manager.getShaderProgram());
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

    float[] projectionMatrix = {
      1.0f, 0.0f, 0.0f, 0.0f,
      0.0f, 1.0f, 0.0f, 0.0f,
      0.0f, 0.0f, -1.0f, 0.0f,
      0.0f, 0.0f, 0.0f, 1.0f
    };

    return matrix;
  }

  public float[] createTransformMatrix(float x, float y, float width, float height) {
    float[] matrix = new float[16];
    matrix[0] = width; // Scale X
    matrix[5] = height; // Scale Y
    matrix[10] = 1.0f; // Scale Z
    matrix[12] = x; // Translate X
    matrix[13] = y; // Translate Y
    matrix[15] = 1.0f; // W component
    return matrix;
  }

  private float[] createTranslationMatrix(float x, float y, float z) {
    float[] matrix = new float[16];
    matrix[0] = 1;
    matrix[5] = 1;
    matrix[10] = 1;
    matrix[15] = 1;
    matrix[12] = x;
    matrix[13] = y;
    matrix[14] = z;
    return matrix;
  }

  private float[] createScaleMatrix(float x, float y, float z) {
    float[] matrix = new float[16];
    matrix[0] = x;
    matrix[5] = y;
    matrix[10] = z;
    matrix[15] = 1;
    return matrix;
  }

  private float[] multiplyMatrices(float[] a, float[] b) {
    float[] result = new float[16];
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        for (int k = 0; k < 4; k++) {
          result[i * 4 + j] += a[i * 4 + k] * b[k * 4 + j];
        }
      }
    }
    return result;
  }
}
