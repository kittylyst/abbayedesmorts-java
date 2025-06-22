/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import abbaye.basic.Renderable;
import abbaye.model.Stage;
import abbaye.model.Tiles;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

public class StageRenderer implements Renderable {
  private final long window;
  private final int tilesTexture;

  private Tiles tilemap;
  private GLManager manager;

  public StageRenderer(long window) {
    this.window = window;
    this.manager = GLManager.get("game");
    this.tilesTexture = GLManager.loadTexture("/tiles.png", true);
  }

  public void init(Stage stage) {
    tilemap = stage;
  }

  //  public void render(int[] counter, boolean changeflag, int changetiles) {
  //    var room = roomy * 5 + roomx;
  //    for (var coordy = 0; coordy <= 21; coordy++) {
  //      for (var coordx = 0; coordx <= 31; coordx++) {
  //        int tileType = stagedata[room][coordy][coordx];
  //        var srctiles = new Stage.SDL_Rect(0, 0, 8, 8);
  //
  //        if ((tileType > 0) && (tileType != 99)) {
  //          if (tileType < 200) {
  //            srctiles.w = 8;
  //            srctiles.h = 8;
  //            if (tileType < 101) {
  //              srctiles.y = 0;
  //              if (tileType == 84) /* Cross brightness */
  //                srctiles.x = (tileType - 1) * 8 + (counter[0] / 8 * 8);
  //              else srctiles.x = (tileType - 1) * 8;
  //            } else {
  //              if (tileType == 154) {
  //                /* Door */
  //                srctiles.x = 600 + ((counter[0] / 8) * 16);
  //                srctiles.y = 0;
  //                srctiles.w = 16;
  //                srctiles.h = 24;
  //              } else {
  //                srctiles.y = 8;
  //                srctiles.x = (tileType - 101) * 8;
  //              }
  //            }
  //          }
  //          if ((tileType > 199) && (tileType < 300)) {
  //            srctiles.x = (tileType - 201) * 48;
  //            srctiles.y = 16;
  //            srctiles.w = 48;
  //            srctiles.h = 48;
  //          }
  //          if ((tileType > 299) && (tileType < 399)) {
  //            srctiles.x = 96 + ((tileType - 301) * 8);
  //            srctiles.y = 16;
  //            srctiles.w = 8;
  //            srctiles.h = 8;
  //          }
  //          /* Hearts */
  //          if ((tileType > 399) && (tileType < 405)) {
  //            srctiles.x = 96 + ((tileType - 401) * 8) + (32 * (counter[0] / 15));
  //            srctiles.y = 24;
  //            srctiles.w = 8;
  //            srctiles.h = 8;
  //          }
  //          /* Crosses */
  //          if ((tileType > 408) && (tileType < 429)) {
  //            srctiles.x = 96 + ((tileType - 401) * 8) + (32 * (counter[1] / 23));
  //            srctiles.y = 24;
  //            srctiles.w = 8;
  //            srctiles.h = 8;
  //          }
  //
  //          if ((tileType > 499) && (tileType < 599)) {
  //            srctiles.x = 96 + ((tileType - 501) * 8);
  //            srctiles.y = 32;
  //            srctiles.w = 8;
  //            srctiles.h = 8;
  //          }
  //          if ((tileType > 599) && (tileType < 650)) {
  //            srctiles.x = 96 + ((tileType - 601) * 8);
  //            srctiles.y = 56;
  //            srctiles.w = 8;
  //            srctiles.h = 8;
  //          }
  //          if (tileType == 650) {
  //            /* Cup */
  //            srctiles.x = 584;
  //            srctiles.y = 87;
  //            srctiles.w = 16;
  //            srctiles.h = 16;
  //          }
  //          if ((tileType == 152) || (tileType == 137) || (tileType == 136)) {
  //            if (changeflag) {
  //              srctiles.y = srctiles.y + (changetiles * 120);
  //              //              SDL_RenderCopy(srctiles);
  //            }
  //          } else {
  //            srctiles.y = srctiles.y + (changetiles * 120);
  //            //            SDL_RenderCopy(srctiles);
  //          }
  //        }
  //      }
  //    }
  //  }

  public boolean render() {
    // Update viewport
    try (MemoryStack stack = stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      glfwGetFramebufferSize(window, width, height);
      glViewport(0, 0, width.get(0), height.get(0));

      var shaderProgram = manager.getShaderProgram();
      glUseProgram(shaderProgram);

      // Set up orthographic projection
      float[] projection = createOrthographicMatrix(0, width.get(0), height.get(0), 0, -1, 1);
      glUniformMatrix4fv(manager.getProjectionLocation(), false, projection);

      glBindVertexArray(manager.getVAO());

      // Render each tile of this room
      for (int y = 0; y < Stage.NUM_ROWS; y++) {
        for (int x = 0; x < Stage.NUM_COLUMNS; x++) {
          int tileIndex = tilemap.getTile(x, y);
          if (tileIndex >= 0) {
            // Calculate tile position
            float posX = x * tilemap.getTileSize();
            float posY = y * tilemap.getTileSize();

            //            // FIXME Code from ExampleTileRenderer
            //
            //            int u1 = 0, v1 = 0, u2 = 32, v2 = 32;
            //            // Update texture coordinates in vertex buffer
            //            updateTileVertices(x, y, tilemap.getTileSize(), u1, v1, u2, v2);
            //
            //            // Set model matrix for position and scale
            //            float[] model = createTranslationMatrix(x, y, 0);
            //
            //
            //            float[] scale = createScaleMatrix(tilemap.getTileSize(),
            //             tilemap.getTileSize(), 1);
            //            float[] finalModel = multiplyMatrices(model, scale);
            //
            //            int modelLoc = glGetUniformLocation(shaderProgram, "model");
            //            glUniformMatrix4fv(modelLoc, false, finalModel);
            //
            //            // END Code from ExampleTileRenderer

            // Create model matrix for this tile
            float[] model =
                createTransformMatrix(posX, posY, tilemap.getTileSize(), tilemap.getTileSize());

            glUniformMatrix4fv(manager.getModelLocation(), false, model);

            // Set solid color based on tile type
            int modelLoc = glGetUniformLocation(manager.getShaderProgram(), "color");
            switch (tileIndex) {
              case 0 -> glUniform3f(modelLoc, 0.2f, 0.8f, 0.2f); // Green (grass)
              case 1 -> glUniform3f(modelLoc, 0.5f, 0.5f, 0.5f); // Gray (stone)
              case 2 -> glUniform3f(modelLoc, 0.8f, 0.6f, 0.2f); // Brown (dirt)
              default -> glUniform3f(modelLoc, 1.0f, 1.0f, 1.0f); // White
            }

            glDrawArrays(GL_TRIANGLES, 0, 6);
          }
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
    return matrix;
  }

  // FIXME What is this matrix for? It looks a 4x4 matrix - in column-row format maybe?
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

  private void updateTileVertices(
      float x, float y, float size, float u1, float v1, float u2, float v2) {
    float[] vertices = {
      // positions           // texture coords
      1.0f, 1.0f, 0.0f, u2, v1, // top right
      1.0f, 0.0f, 0.0f, u2, v2, // bottom right
      0.0f, 0.0f, 0.0f, u1, v2, // bottom left
      0.0f, 1.0f, 0.0f, u1, v1 // top left
    };

    glBindBuffer(GL_ARRAY_BUFFER, manager.getVBO());
    glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
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
