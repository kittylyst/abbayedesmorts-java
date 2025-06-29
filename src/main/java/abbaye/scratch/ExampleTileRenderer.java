/* Copyright (C) The Authors 2025 */
package abbaye.scratch;

import static abbaye.graphics.GLManager.Z_ZERO;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

import abbaye.basic.Corners;
import abbaye.graphics.GLManager;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

public class ExampleTileRenderer {

  private static final float Z_ZERO = 0.0f;
  private long window;
  private int shaderProgram;
  private int VAO, VBO, EBO;
  private int textureId;

  // Sprite atlas properties
  //  private int tileSize = 32; // Size of each tile in pixels
  private final int tilesPerRow = 125; // Calculated tiles per row // atlasWidth / tileSize;
  private int tilesPerCol = 30; // Calculated tiles per column // atlasHeight / tileSize;

  // Tilemap data (each value represents a tile index in the atlas)

  private int[][] tilemap = {
    {7, 8, 0, 0, 0, 0, 7, 8},
    {10, 13, 0, 0, 0, 0, 0, 0},
    {7, 8, 16, 0, 0, 0, 0, 0},
    {10, 13, 0, 0, 0, 0, 0, 0},
    {7, 8, 0, 0, 0, 0, 0, 0},
    {10, 13, 0, 0, 0, 0, 0, 0},
    {12, 8, 7, 10, 13, 0, 0, 0},
    {10, 9, 10, 9, 10, 13, 0, 0},
    //          {0, 0, 0, 0, 0, 0, 0, 0},
    //          {0, 0, 0, 0, 0, 0, 0, 0},
  };

  // From 1-2
  //        007 008 000 000 000 000 007 008
  //        010 013 000 000 000 000 000 000
  //        007 008 016 000 000 000 000 000
  //        010 013 000 000 000 000 000 000
  //        007 008 000 000 000 000 000 000
  //        010 013 000 000 000 000 000 000
  //        012 008 007 010 013 000 000 000
  //        010 009 010 009 010 013 000 000

  private Map<Integer, Corners> cache = new HashMap<>();

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

    setupShaders();
    setupBuffers();
    textureId = GLManager.loadTexture("/tiles.png", true, false);

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
      0.5f, 0.5f, Z_ZERO, 1.0f, 1.0f, // top right
      -0.5f, 0.5f, Z_ZERO, 0.0f, 1.0f, // bottom right
      -0.5f, -0.5f, Z_ZERO, 0.0f, 0.0f, // bottom left
      0.5f, -0.5f, Z_ZERO, 1.0f, 0.0f // top left
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
    //    glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

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
    float tileDisplaySize = 8.0f; // Size to display each tile on screen
    //    tilemap.length
    for (int row = 0; row < 8; row++) {
      // tilemap[row].length
      for (int col = 0; col < 8; col++) {
        int tileIndex = tilemap[row][col]; // tileIndex is the index into a 1-dim array

        // Calculate texture coordinates for this tile in the atlas
        int tileX = tileIndex % tilesPerRow;
        int tileY = tileIndex / tilesPerRow;

        // Calculate position on screen
        float x = col * 8 * tileDisplaySize;
        float y = row * 8 * tileDisplaySize;

//        float u1 = (float) tileX / tilesPerRow;
//        float v1 = (float) tileY / tilesPerCol;
//        float u2 = (float) (tileX + 1) / tilesPerRow;
//        float v2 = (float) (tileY + 1) / tilesPerCol;
//
//        // Update texture coordinates in vertex buffer
//        var c = new Corners(u1, v1, u2, v2);
        var c = computeTextureCoords(tileIndex, new int[2], 0, 0);
        cache.computeIfAbsent(
            tileIndex,
            t -> {
              System.out.println("Tile type: " + t + " has corners: " + c);
              return c;
            });
        updateTileVertices(c);

        // Set model matrix for position and scale
        //        float[] model = createTranslationMatrix(x, y, 0);
        //        float[] scale = createScaleMatrix(tileDisplaySize, tileDisplaySize, 1);
        //        float[] finalModel = multiplyMatrices(model, scale);

        float[] finalModel = {
          8 * tileDisplaySize, 0, 0, 0, 0, 8 * tileDisplaySize, 0, 0, 0, 0, 1, 0, x, y, 0, 1
        };

        //        System.out.println("M1: "+ Arrays.toString(finalModel));
        //        System.out.println("M2: "+ Arrays.toString(finalModel2));

        int modelLoc = glGetUniformLocation(shaderProgram, "model");
        glUniformMatrix4fv(modelLoc, false, finalModel);

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
      }
    }
  }

  static class SDL_Rect {
    public int x;
    public int y;
    public int w;
    public int h;

    public SDL_Rect(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }

    @Override
    public String toString() {
      return "SDL_Rect{" + "posX=" + 8 * x + ", posY=" + 8 * y + ", w=" + w + ", h=" + h + '}';
    }
  }

    Corners computeTextureCoords(int data, int[] counter, int changeflag, int changetiles) {
      SDL_Rect srctiles = new SDL_Rect(0, 0, 8, 8);
      if ((data > 0) && (data != 99)) {
        if (data < 200) {
          srctiles.w = 8;
          srctiles.h = 8;
          if (data < 101) {
            srctiles.y = 0;
            if (data == 84) /* Cross brightness */
              srctiles.x = (data - 1) * 8 + (counter[0] / 8 * 8);
            else
              srctiles.x = (data - 1) * 8;
          } else {
            if (data == 154) { /* Door */
              srctiles.x = 600 + ((counter[0] / 8) * 16);
              srctiles.y = 0;
              srctiles.w = 16;
              srctiles.h = 24;
            } else {
              srctiles.y = 8;
              srctiles.x = (data - 101) * 8;
            }
          }
        }
        if ((data > 199) && (data < 300)) {
          srctiles.x = (data - 201) * 48;
          srctiles.y = 16;
          srctiles.w = 48;
          srctiles.h = 48;
        }
        if ((data > 299) && (data < 399)) {
          srctiles.x = 96 + ((data - 301) * 8);
          srctiles.y = 16;
          srctiles.w = 8;
          srctiles.h = 8;
          /* Door movement */
          //                        if ((room == ROOM_CHURCH) && ((counter[1] > 59) && (counter[1] < 71))) {
            //                            if ((data == 347) || (data == 348) || (data == 349) || (data == 350)) {
            //                                destiles.x += 2;
            //                            }
            //                        }
          }
          /* Hearts */
          if ((data > 399) && (data < 405)) {
            srctiles.x = 96 + ((data - 401) * 8) + (32 * (counter[0] / 15));
            srctiles.y = 24;
            srctiles.w = 8;
            srctiles.h = 8;
          }
          /* Crosses */
          if ((data > 408) && (data < 429)) {
            srctiles.x = 96 + ((data - 401) * 8) + (32 * (counter[1] / 23));
            srctiles.y = 24;
            srctiles.w = 8;
            srctiles.h = 8;
          }

          if ((data > 499) && (data < 599)) {
            srctiles.x = 96 + ((data - 501) * 8);
            srctiles.y = 32;
            srctiles.w = 8;
            srctiles.h = 8;
          }
          if ((data > 599) && (data < 650)) {
            srctiles.x = 96 + ((data - 601) * 8);
            srctiles.y = 56;
            srctiles.w = 8;
            srctiles.h = 8;
          }
          if (data == 650) { /* Cup */
            srctiles.x = 584;
            srctiles.y = 87;
            srctiles.w = 16;
            srctiles.h = 16;
          }
          if ((data == 152) || (data == 137) || (data == 136)) {
            if (changeflag == 0) {
              srctiles.y = srctiles.y + (changetiles * 120);
              //                            SDL_RenderCopy(renderer,tiles,&srctiles,&destiles);
            }
          } else {
            srctiles.y = srctiles.y + (changetiles * 120);
            //                        SDL_RenderCopy(renderer,tiles,&srctiles,&destiles);
          }
      }
      float u1 = (float) srctiles.x / (8 * tilesPerRow);
      float v1 = (float) srctiles.y / (8 * tilesPerCol);
      float u2 = (float) (srctiles.x + srctiles.w) / (8 * tilesPerRow);
      float v2 = (float) (srctiles.y + srctiles.h) / (8 * tilesPerCol);

//      System.out.println(srctiles);
      return new Corners(u1, v1, u2, v2);
    }

  private void updateTileVertices(Corners c) {
    //    float[] vertices = {
    //      // positions           // texture coords
    //      1.0f, 1.0f, Z_ZERO, u2, v1, // top right
    //      1.0f, 0.0f, Z_ZERO, u2, v2, // bottom right
    //      0.0f, 0.0f, Z_ZERO, u1, v2, // bottom left
    //      0.0f, 1.0f, Z_ZERO, u1, v1 // top left
    //    };

    float[] vertices = {
      // positions           // texture coords
      1.0f, 1.0f, Z_ZERO, c.u2(), c.v1(), // top right
      1.0f, 0.0f, Z_ZERO, c.u2(), c.v2(), // bottom right
      0.0f, 0.0f, Z_ZERO, c.u1(), c.v2(), // bottom left
      0.0f, 1.0f, Z_ZERO, c.u1(), c.v1() // top left
    };

    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
  }

  private void updateTileVertices(float u1, float v1, float u2, float v2) {
    //    float[] vertices = {
    //      // positions           // texture coords
    //      1.0f, 1.0f, Z_ZERO, u2, v1, // top right
    //      1.0f, 0.0f, Z_ZERO, u2, v2, // bottom right
    //      0.0f, 0.0f, Z_ZERO, u1, v2, // bottom left
    //      0.0f, 1.0f, Z_ZERO, u1, v1 // top left
    //    };

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
    matrix[0] = 1.0f / (right - left);
    matrix[5] = 1.0f / (top - bottom);
    matrix[10] = -1.0f / (far - near);
    matrix[12] = -(right + left) / (right - left);
    matrix[13] = -(top + bottom) / (top - bottom);
    matrix[14] = -(far + near) / (far - near);
    matrix[15] = 1.0f;
    return matrix;
  }

  //
  //  public float[] createTranslationMatrix(float x, float y, float z) {
  //    float[] matrix = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, x, y, z, 1};
  //
  //    return matrix;
  //  }
  //
  //  public float[] createScaleMatrix(float x, float y, float z) {
  //    float[] matrix = {x, 0, 0, 0, 0, y, 0, 0, 0, 0, z, 0, 0, 0, 0, 1};
  //    return matrix;
  //  }
  //
  //  public float[] multiplyMatrices(float[] a, float[] b) {
  //    float[] result = new float[16];
  //    for (int i = 0; i < 4; i++) {
  //      for (int j = 0; j < 4; j++) {
  //        for (int k = 0; k < 4; k++) {
  //          result[i * 4 + j] += a[i * 4 + k] * b[j * 4 + k];
  //        }
  //      }
  //    }
  //    return result;
  //  }

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
