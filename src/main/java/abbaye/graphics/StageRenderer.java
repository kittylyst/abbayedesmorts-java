/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import abbaye.model.Stage;
import abbaye.model.Tiles;

public class StageRenderer {
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

  public void init() {
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

  public void render(Tiles tilemap, int screenWidth, int screenHeight) {
    glUseProgram(shaderProgram);

    // Set up orthographic projection
    float[] projection = createOrthographicMatrix(0, screenWidth, screenHeight, 0, -1, 1);
    glUniformMatrix4fv(projectionLocation, false, projection);

    glBindVertexArray(VAO);

    // Render each tile
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
