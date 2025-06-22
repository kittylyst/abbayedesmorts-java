/* Copyright (C) The Authors 2025 */
package abbaye.scratch;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

import abbaye.graphics.GLManager;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

public class ExampleTileRenderer {

  private static final float Z_ZERO = 0.0f;
  private long window;
  private int shaderProgram;
  private int VAO, VBO, EBO;
  private int textureId;

  // Sprite atlas properties
//  private int atlasWidth = 32; // Total atlas width in pixels
//  private int atlasHeight = 32; // Total atlas height in pixels
//  private int tileSize = 32; // Size of each tile in pixels
  private int tilesPerRow; // Calculated tiles per row
  private int tilesPerCol; // Calculated tiles per column

  // Tilemap data (each value represents a tile index in the atlas)
  private int[][] tilemap = {
    {0, 1, 2, 3, 0, 1, 2, 3},
    {4, 5, 6, 7, 4, 5, 6, 7},
    {8, 9, 10, 11, 8, 9, 10, 11},
    {12, 13, 14, 15, 12, 13, 14, 15},
    {0, 1, 2, 3, 0, 1, 2, 3},
    {4, 5, 6, 7, 4, 5, 6, 7}
  };

  // Vertex shader source
  private final String vertexShaderSource =
      "#version 330 core\n"
          + "layout (location = 0) in vec3 aPos;\n"
          + "layout (location = 1) in vec2 aTexCoord;\n"
          + "uniform mat4 projection;\n"
          + "uniform mat4 model;\n"
          + "out vec2 TexCoord;\n"
          + "void main() {\n"
          + "    gl_Position = projection * model * vec4(aPos, 1.0);\n"
          + "    TexCoord = aTexCoord;\n"
          + "}\n";

  // Fragment shader source
  private final String fragmentShaderSource =
      "#version 330 core\n"
          + "out vec4 FragColor;\n"
          + "in vec2 TexCoord;\n"
          + "uniform sampler2D ourTexture;\n"
          + "void main() {\n"
          + "    FragColor = texture(ourTexture, TexCoord);\n"
          + "    if(FragColor.a < 0.1) discard;\n"
          + "}\n";

  public void run() {
    glInit();
    loop();
    cleanup();
  }

  private void glInit() {
    GLFWErrorCallback.createPrint(System.err).set();

    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

    window = glfwCreateWindow(800, 600, "LWJGL Tilemap Example", NULL, NULL);
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);

    GL.createCapabilities();

    // FIXME
    // Calculate tiles per row/column
    tilesPerRow = 32; // atlasWidth / tileSize;
    tilesPerCol = 8; // atlasHeight / tileSize;

    setupShaders();
    setupBuffers();
    textureId = GLManager.loadTexture("/tiles.png", true);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Setup key callback
    glfwSetKeyCallback(
            window,
            (window, key, scancode, action, mods) -> {
              if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
              }
            });
  }

  private void setupShaders() {
    int vertexShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertexShader, vertexShaderSource);
    glCompileShader(vertexShader);

    int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragmentShader, fragmentShaderSource);
    glCompileShader(fragmentShader);

    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
  }

  private void setupBuffers() {
    // Quad vertices (will be repositioned for each tile)
    float[] vertices = {
      // positions     // texture coords
      1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // top right
      1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // bottom right
      0.0f, 0.0f, 0.0f, 0.0f, 1.0f, // bottom left
      0.0f, 1.0f, 0.0f, 0.0f, 0.0f // top left
    };

    int[] indices = {
      0, 1, 3,
      1, 2, 3
    };

    VAO = glGenVertexArrays();
    VBO = glGenBuffers();
    EBO = glGenBuffers();

    glBindVertexArray(VAO);

    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

    // Position attribute
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
    glEnableVertexAttribArray(0);

    // Texture coordinate attribute
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
    glEnableVertexAttribArray(1);
  }

  private void loop() {
    glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

    // Set up projection matrix (orthographic for 2D)
    final float[] projection = createOrthographicMatrix(0, 800, 600, 0, -1, 1);

    while (!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT);

      int projLoc = glGetUniformLocation(shaderProgram, "projection");
      glUniformMatrix4fv(projLoc, false, projection);

      glUseProgram(shaderProgram);
      glBindTexture(GL_TEXTURE_2D, textureId);
      glBindVertexArray(VAO);

      // Render the tilemap
      renderTilemap();

      glfwSwapBuffers(window);
      glfwPollEvents();
    }
  }

  private void renderTilemap() {
    float tileDisplaySize = 64.0f; // Size to display each tile on screen

    for (int row = 0; row < tilemap.length; row++) {
      for (int col = 0; col < tilemap[row].length; col++) {
        int tileIndex = tilemap[row][col]; // tileIndex is the index into a 1-dim array

        // Calculate texture coordinates for this tile in the atlas
        int tileX = tileIndex % tilesPerRow;
        int tileY = tileIndex / tilesPerRow;

        // FIXME Is this correct?
        float u1 = (float) tileX / tilesPerRow;
        float v1 = (float) tileY / tilesPerCol;
        float u2 = (float) (tileX + 1) / tilesPerRow;
        float v2 = (float) (tileY + 1) / tilesPerCol;

        // FIXME Is this correct?
        // Calculate position on screen
        float x = col * tileDisplaySize;
        float y = row * tileDisplaySize;

        // Update texture coordinates in vertex buffer
        updateTileVertices(x, y, tileDisplaySize, u1, v1, u2, v2);

        // Set model matrix for position and scale
        float[] model = createTranslationMatrix(x, y, 0);
        float[] scale = createScaleMatrix(tileDisplaySize, tileDisplaySize, 1);
        float[] finalModel = multiplyMatrices(model, scale);

        int modelLoc = glGetUniformLocation(shaderProgram, "model");
        glUniformMatrix4fv(modelLoc, false, finalModel);

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
      }
    }
  }

  private void updateTileVertices(
      float x, float y, float size, float u1, float v1, float u2, float v2) {
    float[] vertices = {
      // positions           // texture coords
      1.0f, 1.0f, Z_ZERO, u2, v1, // top right
      1.0f, 0.0f, Z_ZERO, u2, v2, // bottom right
      0.0f, 0.0f, Z_ZERO, u1, v2, // bottom left
      0.0f, 1.0f, Z_ZERO, u1, v1 // top left
    };

    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
  }

  // Matrix utility methods
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

  private void cleanup() {
    glDeleteVertexArrays(VAO);
    glDeleteBuffers(VBO);
    glDeleteBuffers(EBO);
    glDeleteProgram(shaderProgram);
    glDeleteTextures(textureId);

    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }

  public static void main(String[] args) {
    new ExampleTileRenderer().run();
  }
}
