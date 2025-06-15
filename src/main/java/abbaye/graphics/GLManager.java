/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

public final class GLManager {

  private static Map<String, GLManager> managers = new HashMap<>();

  // Quad vertices (position + texture coordinates)
  public static final float[] VERTICES = {
    // positions        // texture coords
    -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, // top left
    0.5f, 0.5f, 0.0f, 1.0f, 1.0f, // top right
    0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
    -0.5f, -0.5f, 0.0f, 0.0f, 0.0f // bottom left
  };

  public static final int[] INDICES = {
    0, 1, 2,
    2, 3, 0
  };

  public static float[] PROJECTION_MATRIX = {
    2.0f, 0.0f, 0.0f, 0.0f,
    0.0f, 2.0f, 0.0f, 0.0f,
    0.0f, 0.0f, -1.0f, 0.0f,
    0.0f, 0.0f, 0.0f, 1.0f
  };

  private static Map<String, Integer> textures = new HashMap<>();

  private GLManager() {}

  static {
    var manager = new GLManager();
    manager.init("/shaders/splash.shd", "/shaders/splash.frag");
    managers.put("dialog", manager);
    manager = new GLManager();
    manager.init("/shaders/game.shd", "/shaders/game.frag");
    managers.put("game", manager);
  }

  public static synchronized GLManager get(String s) {
    return managers.get(s);
  }

  private int shaderProgram;
  private int VAO, VBO, EBO;
  private int projectionLocation, modelLocation;

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

    // Position attribute
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
    glEnableVertexAttribArray(0);

    // Texture coordinate attribute
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
    glEnableVertexAttribArray(1);
  }

  public void cleanup() {
    glDeleteVertexArrays(VAO);
    glDeleteBuffers(VBO);
    glDeleteProgram(shaderProgram);
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

  public int getModelLocation() {
    return modelLocation;
  }

  private static int createShader(int type, String source) {
    int shader = glCreateShader(type);
    glShaderSource(shader, source);
    glCompileShader(shader);

    if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
      String info = glGetShaderInfoLog(shader);
      throw new RuntimeException("Shader compilation failed: " + info);
    }

    return shader;
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

  /////////////// Texture helpers

  public static int loadTexture(String path, boolean isResource) {
    int texture = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, texture);

    // Set texture wrapping/filtering options
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    // Load and generate texture
    try (MemoryStack stack = stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      IntBuffer channels = stack.mallocInt(1);

      // Flip image vertically for OpenGL
      stbi_set_flip_vertically_on_load(true);

      ByteBuffer image = null;
      if (isResource) {
        // Load resource as ByteBuffer
        var imageBuffer = loadResourceAsBuffer(path);
        if (imageBuffer == null) {
          System.err.println("Failed to load resource: " + path);
          return -1;
        }
        image = stbi_load_from_memory(imageBuffer, width, height, channels, 4);
      } else {
        image = stbi_load(path, width, height, channels, 4);
      }

      if (image != null) {
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            width.get(0),
            height.get(0),
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            image);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(image);
      } else {
        System.err.println("Failed to load texture: " + path);
        // Create a default white texture
        ByteBuffer defaultTexture = BufferUtils.createByteBuffer(4);
        defaultTexture.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255);
        defaultTexture.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, defaultTexture);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return texture;
  }

  /**
   * Returns an
   *
   * @param resourcePath
   * @return
   * @throws IOException
   */
  public static ByteBuffer loadResourceAsBuffer(String resourcePath) throws IOException {
    try (var inputStream = GLManager.class.getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IOException("Resource not found: " + resourcePath);
      }

      var data = inputStream.readAllBytes();

      // Create a ByteBuffer and copy the data
      ByteBuffer byteBuffer = memAlloc(data.length);
      byteBuffer.put(data);
      byteBuffer.flip();

      return byteBuffer;

    } catch (IOException e) {
      throw new IOException("Failed to read resource: " + resourcePath, e);
    }
  }
}
