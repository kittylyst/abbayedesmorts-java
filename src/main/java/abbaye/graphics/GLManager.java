/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import abbaye.Config;
import abbaye.basic.Corners;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class GLManager {

  private static Map<String, GLManager> managers = new HashMap<>();

  public static final float Z_ZERO = 0.0f;

  // Quad vertices (position + texture coordinates)
  public static final float[] VERTICES = {
    // positions        // texture coords
    -0.5f, 0.5f, Z_ZERO, 0.0f, 1.0f, // top left
    0.5f, 0.5f, Z_ZERO, 1.0f, 1.0f, // top right
    0.5f, -0.5f, Z_ZERO, 1.0f, 0.0f, // bottom right
    -0.5f, -0.5f, Z_ZERO, 0.0f, 0.0f // bottom left
  };

  public static final int[] INDICES = {
    0, 1, 3, // first triangle
    1, 2, 3 // second triangle
  };

  private GLManager() {}

  public static synchronized GLManager get(String shaderName) {
    var mgr = managers.get(shaderName);
    if (mgr == null) {
      throw new IllegalArgumentException("Unknown shader: " + shaderName);
    }
    return mgr;
  }

  static {
    if (Config.config().getGLActive()) {
      // Splash screen shaders
      var manager = new GLManager();
      manager.init("/shaders/splash.vert", "/shaders/splash.frag");
      // Get locations for uniforms
      manager.projectionLocation = glGetUniformLocation(manager.shaderProgram, "projection");
      // Position attribute
      glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
      glEnableVertexAttribArray(0);

      // Texture coordinate attribute
      glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
      glEnableVertexAttribArray(1);

      manager.textures.put("introSplash", Texture.of("/intro.png", true, true));

      managers.put("dialog", manager);

      // Main game shaders
      manager = new GLManager();
      manager.init("/shaders/game.vert", "/shaders/game.frag");
      // Get locations for uniforms
      manager.projectionLocation = glGetUniformLocation(manager.shaderProgram, "projection");
      manager.modelLocation = glGetUniformLocation(manager.shaderProgram, "model");

      // Position attribute
      glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
      glEnableVertexAttribArray(0);

      // Texture coordinate attribute
      glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
      glEnableVertexAttribArray(1);

      // Game textures
      manager.textures.put("fonts", Texture.of("/fonts.png", true, true));
      manager.textures.put("tiles", Texture.of("/tiles.png", true, true));

      managers.put("game", manager);
    }
  }

  private int shaderProgram;
  private int VAO, VBO, EBO;
  private int projectionLocation, modelLocation;

  private Map<String, Texture> textures = new HashMap<>();

  public void init(String pathVertex, String pathFragment) {
    // Enable blending for transparency
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    String vertexSource = "";
    String fragmentSource = "";
    try {
      vertexSource = new String(GLManager.class.getResourceAsStream(pathVertex).readAllBytes());
      fragmentSource = new String(GLManager.class.getResourceAsStream(pathFragment).readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Create and compile shaders
    int vertexShader = createShader(GL_VERTEX_SHADER, vertexSource);
    int fragmentShader = createShader(GL_FRAGMENT_SHADER, fragmentSource);

    // Create shader program
    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    // Check for linking errors
    if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
      String info = glGetProgramInfoLog(shaderProgram);
      throw new RuntimeException("Shader program linking failed: " + info);
    }

    // Delete shaders (they're linked into program now)
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    // Setup vertex data and buffers
    VAO = glGenVertexArrays();
    VBO = glGenBuffers();
    EBO = glGenBuffers();

    glBindVertexArray(VAO);

    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

    //    // DEBUG
    //    glValidateProgram(shaderProgram);
    //    if (glGetProgrami(shaderProgram, GL_VALIDATE_STATUS) == 0) {
    //      System.err.println("Warning validating Shader code: " +
    // glGetProgramInfoLog(shaderProgram, 1024));
    //    }

  }

  /**
   * Bind texture for render
   *
   * @param name
   */
  public void bindTexture(String name) {
    Texture texture = textures.get(name);
    glBindTexture(GL_TEXTURE_2D, texture.getId());
    glBindVertexArray(VAO);
    glUseProgram(shaderProgram);
  }

  public void cleanup() {
    glDeleteVertexArrays(VAO);
    glDeleteBuffers(VBO);
    glDeleteProgram(shaderProgram);
  }

  /**
   * @param tileCoords
   */
  public void updateTileVertices(Corners tileCoords) {
    var u1 = tileCoords.u1();
    var v1 = tileCoords.v1();
    var u2 = tileCoords.u2();
    var v2 = tileCoords.v2();

    float[] vertices = {
      // positions           // texture coords
      1.0f, 0.0f, Z_ZERO, u2, v1, // bottom right
      1.0f, 1.0f, Z_ZERO, u2, v2, // top right
      0.0f, 1.0f, Z_ZERO, u1, v2, // top left
      0.0f, 0.0f, Z_ZERO, u1, v1 // bottom left
    };

    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
  }

  ///////////// Helpers

  public static int createShader(int type, String source) {
    int shader = glCreateShader(type);
    glShaderSource(shader, source);
    glCompileShader(shader);

    if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
      String info = glGetShaderInfoLog(shader);
      throw new RuntimeException("Shader compilation failed: " + info);
    }

    return shader;
  }

  /////////////////////////////////////////
  // Render methods

  /**
   * @param tileCoords - the texture coords in float (0 < u, v < 1) coords
   * @param tileSize - the size of the tile in display pixel, i.e. as it appears to the player
   * @param x - the x coordinate in display pixels
   * @param y - the y coordinate in display pixels
   */
  public void renderTile(Corners tileCoords, float tileSize, float x, float y) {
    float[] translate = createTranslationMatrix(x, y, 0);
    float[] scale = createScaleMatrix(tileSize, tileSize, 1);

    float[] finalModel = multiplyMatrices(scale, translate);

    renderTile(tileCoords, finalModel);
  }

    /**
     *
     * @param tileCoords - the texture coords in float (0 < u, v < 1) coords
     * @param finalModel - a prebuilt transformation matrix
     */
  public void renderTile(Corners tileCoords, final float[] finalModel) {
    updateTileVertices(tileCoords);

    int modelLoc = glGetUniformLocation(shaderProgram, "model");
    glUniformMatrix4fv(modelLoc, false, finalModel);

    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
  }

  ///////////// Getters

  public int getShaderProgram() {
    return shaderProgram;
  }

  public int getProjectionLocation() {
    return projectionLocation;
  }

  public int getVAO() {
    return VAO;
  }

  public int getVBO() {
    return VBO;
  }

  ///////////// Utilities

  public static float[] createTransformMatrix(float x, float y, float width, float height) {
    float[] matrix = new float[16];
    matrix[0] = width; // Scale X
    matrix[5] = height; // Scale Y
    matrix[10] = 1.0f; // Scale Z
    matrix[12] = x; // Translate X
    matrix[13] = y; // Translate Y
    matrix[15] = 1.0f; // W component
    return matrix;
  }

  public static float[] createTranslationMatrix(float x, float y, float z) {
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

  public static float[] createScaleMatrix(float x, float y, float z) {
    float[] matrix = new float[16];
    matrix[0] = x;
    matrix[5] = y;
    matrix[10] = z;
    matrix[15] = 1;
    return matrix;
  }

  public static float[] multiplyMatrices(float[] a, float[] b) {
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
}
