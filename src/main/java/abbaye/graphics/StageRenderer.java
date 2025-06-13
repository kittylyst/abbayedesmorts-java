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
  private int shaderProgram;
  private int VAO, VBO;
  private int projectionLocation, modelLocation;

  private static final String VERTEX_SHADER =
      """
      #version 330 core
      layout (location = 0) in vec2 aPos;
      layout (location = 1) in vec2 aTexCoord;

      uniform mat4 projection;
      uniform mat4 model;

      out vec2 TexCoord;

      void main() {
          gl_Position = projection * model * vec4(aPos, 0.0, 1.0);
          TexCoord = aTexCoord;
      }
      """;

  private static final String FRAGMENT_SHADER =
      """
      #version 330 core
      out vec4 FragColor;

      in vec2 TexCoord;
      uniform vec3 color;

      void main() {
          FragColor = vec4(color, 1.0);
      }
      """;
  private Tiles tilemap;

  public StageRenderer(long window) {
    this.window = window;
  }

  public void init(Stage stage) {
    tilemap = stage;
    // Create shader program
    int vertexShader = createShader(GL_VERTEX_SHADER, VERTEX_SHADER);
    int fragmentShader = createShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == 0) {
      throw new RuntimeException(
          "Error linking shader program: " + glGetProgramInfoLog(shaderProgram));
    }

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    // Get uniform locations
    projectionLocation = glGetUniformLocation(shaderProgram, "projection");
    modelLocation = glGetUniformLocation(shaderProgram, "model");

    // Create quad vertices
    float[] vertices = {
      // positions     // texture coords
      0.0f, 1.0f, 0.0f, 1.0f,
      1.0f, 0.0f, 1.0f, 0.0f,
      0.0f, 0.0f, 0.0f, 0.0f,
      0.0f, 1.0f, 0.0f, 1.0f,
      1.0f, 1.0f, 1.0f, 1.0f,
      1.0f, 0.0f, 1.0f, 0.0f
    };

    VAO = glGenVertexArrays();
    VBO = glGenBuffers();

    glBindVertexArray(VAO);

    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

    // Position attribute
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
    glEnableVertexAttribArray(0);

    // Texture coordinate attribute
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
    glEnableVertexAttribArray(1);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
  }

  private int createShader(int type, String source) {
    int shader = glCreateShader(type);
    glShaderSource(shader, source);
    glCompileShader(shader);

    if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
      throw new RuntimeException("Error compiling shader: " + glGetShaderInfoLog(shader));
    }

    return shader;
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

      glUseProgram(shaderProgram);

      // Set up orthographic projection
      float[] projection = createOrthographicMatrix(0, width.get(0), height.get(0), 0, -1, 1);
      glUniformMatrix4fv(projectionLocation, false, projection);

      glBindVertexArray(VAO);

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
            glUniformMatrix4fv(modelLocation, false, model);

            // Set color based on tile type
            int colorLocation = glGetUniformLocation(shaderProgram, "color");
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

      glBindVertexArray(0);
      glUseProgram(0);
    }
    return true;
  }

  private float[] createOrthographicMatrix(
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

  private float[] createTransformMatrix(float x, float y, float width, float height) {
    float[] matrix = new float[16];
    matrix[0] = width; // Scale X
    matrix[5] = height; // Scale Y
    matrix[10] = 1.0f; // Scale Z
    matrix[12] = x; // Translate X
    matrix[13] = y; // Translate Y
    matrix[15] = 1.0f; // W component
    return matrix;
  }

  public void cleanup() {
    glDeleteVertexArrays(VAO);
    glDeleteBuffers(VBO);
    glDeleteProgram(shaderProgram);
  }
}
