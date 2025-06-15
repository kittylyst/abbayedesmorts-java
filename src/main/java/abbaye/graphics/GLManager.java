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

public class GLManager {

  private static Map<String, GLManager> managers = new HashMap<>();

  //  // Vertex shader source
  //  private static final String VERTEX_SHADER =
  //          """
  //          #version 330 core
  //          layout (location = 0) in vec3 aPos;
  //          layout (location = 1) in vec2 aTexCoord;
  //
  //          uniform mat4 projection;
  //
  //          out vec2 TexCoord;
  //
  //          void main() {
  //              gl_Position = projection * vec4(aPos, 1.0);
  //              TexCoord = aTexCoord;
  //          }
  //          """;
  //
  //  private static final String FRAGMENT_SHADER =
  //          """
  //          #version 330 core
  //          out vec4 FragColor;
  //
  //          in vec2 TexCoord;
  //
  //          uniform sampler2D logoTexture;
  //          uniform float alpha;
  //
  //          void main() {
  //            vec4 texColor = texture(logoTexture, TexCoord);
  //            FragColor = vec4(texColor.rgb, texColor.a * alpha);
  //          }
  //          """;

  //  private static final String VERTEX_SHADER =
  //      """
  //      #version 330 core
  //      layout (location = 0) in vec2 aPos;
  //      layout (location = 1) in vec2 aTexCoord;
  //
  //      uniform mat4 projection;
  //      uniform mat4 model;
  //
  //      out vec2 TexCoord;
  //
  //      void main() {
  //          gl_Position = projection * model * vec4(aPos, 0.0, 1.0);
  //          TexCoord = aTexCoord;
  //      }
  //      """;
  //
  //  private static final String FRAGMENT_SHADER =
  //      """
  //      #version 330 core
  //      out vec4 FragColor;
  //
  //      in vec2 TexCoord;
  //      uniform vec3 color;
  //
  //      void main() {
  //          FragColor = vec4(color, 1.0);
  //      }
  //      """;

  // Quad vertices (position + texture coordinates)
  private static final float[] VERTICES = {
    // positions        // texture coords
    -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, // top left
    0.5f, 0.5f, 0.0f, 1.0f, 1.0f, // top right
    0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
    -0.5f, -0.5f, 0.0f, 0.0f, 0.0f // bottom left
  };

  private static final int[] INDICES = {
    0, 1, 2,
    2, 3, 0
  };

  private static float[] PROJECTION_MATRIX = {
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

    // Load texture
    //        logoTexture = loadTexture("logo.png", false);
    //    logoTexture = loadTexture("/intro.png", true);

    // Setup projection matrix (orthographic)
    glUseProgram(shaderProgram);
    int projectionLoc = glGetUniformLocation(shaderProgram, "projection");

    glUniformMatrix4fv(projectionLoc, false, PROJECTION_MATRIX);

    // Set texture uniform
    glUniform1i(glGetUniformLocation(shaderProgram, "logoTexture"), 0);
  }

  private int createShader(int type, String source) {
    int shader = glCreateShader(type);
    glShaderSource(shader, source);
    glCompileShader(shader);

    if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
      String info = glGetShaderInfoLog(shader);
      throw new RuntimeException("Shader compilation failed: " + info);
    }

    return shader;
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
