/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static abbaye.graphics.GLManager.createOrthographicMatrix;
import static abbaye.graphics.GLManager.createTransformMatrix;
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

  private Tiles tilemap;
  private GLManager manager;

  public StageRenderer(long window) {
    this.window = window;
    this.manager = GLManager.get("game");
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
          int tileType = tilemap.getTile(x, y);
          if (tileType >= 0) {
            // Calculate tile position
            float posX = x * tilemap.getTileSize();
            float posY = y * tilemap.getTileSize();

            // Create model matrix for this tile
            float[] model =
                createTransformMatrix(posX, posY, tilemap.getTileSize(), tilemap.getTileSize());
            glUniformMatrix4fv(manager.getModelLocation(), false, model);

            // Set color based on tile type
            //    // Set texture uniform
            //            glUniform1i(glGetUniformLocation(shaderProgram, "color"), 0);

            int colorLocation = glGetUniformLocation(manager.getShaderProgram(), "color");
            switch (tileType) {
              case 0 -> glUniform3f(colorLocation, 0.2f, 0.8f, 0.2f); // Green (grass)
              case 1 -> glUniform3f(colorLocation, 0.5f, 0.5f, 0.5f); // Gray (stone)
              case 2 -> glUniform3f(colorLocation, 0.8f, 0.6f, 0.2f); // Brown (dirt)
              default -> glUniform3f(colorLocation, 1.0f, 1.0f, 1.0f); // White
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
}
