/* Copyright (C) The Authors 2025 */
package abbaye.scratch;

import static abbaye.graphics.GLManager.loadTexture;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

public class SplashScreenResource {

  private long window;
  private int shaderProgram;
  private int VAO, VBO, EBO;
  private int logoTexture;

  // Vertex shader source
  private static final String VERTEX_SHADER_SOURCE =
      """
      #version 330 core
      layout (location = 0) in vec3 aPos;
      layout (location = 1) in vec2 aTexCoord;

      uniform mat4 projection;

      out vec2 TexCoord;

      void main() {
          gl_Position = projection * vec4(aPos, 1.0);
          TexCoord = aTexCoord;
      }
      """;

  private static final String FRAGMENT_SHADER_SOURCE =
      """
      #version 330 core
      out vec4 FragColor;

      in vec2 TexCoord;

      uniform sampler2D logoTexture;
      uniform float alpha;

      void main() {
        vec4 texColor = texture(logoTexture, TexCoord);
        FragColor = vec4(texColor.rgb, texColor.a * alpha);
      }
      """;

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

  public void run() {
    System.out.println("LWJGL Version: " + Version.getVersion());

    init();
    loop();
    cleanup();
  }

  private void init() {
    // Setup error callback
    GLFWErrorCallback.createPrint(System.err).set();

    // Initialize GLFW
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    // Configure GLFW
    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

    // Create window
    window = glfwCreateWindow(800, 600, "Game Splash Screen", NULL, NULL);
    if (window == NULL) {
      throw new RuntimeException("Failed to create GLFW window");
    }

    // Setup key callback
    glfwSetKeyCallback(
        window,
        (window, key, scancode, action, mods) -> {
          if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(window, true);
          }
        });

    // Get thread stack and push new frame
    try (MemoryStack stack = stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);

      // Get window size
      glfwGetWindowSize(window, pWidth, pHeight);

      // Get resolution of primary monitor
      GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

      // Center window
      glfwSetWindowPos(
          window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
    }

    // Make OpenGL context current
    glfwMakeContextCurrent(window);

    // Enable v-sync
    glfwSwapInterval(1);

    // Make window visible
    glfwShowWindow(window);

    // Create OpenGL capabilities
    GL.createCapabilities();

    // Setup OpenGL
    setupOpenGL();
  }

  private void setupOpenGL() {
    // Enable blending for transparency
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Create and compile shaders
    int vertexShader = createShader(GL_VERTEX_SHADER, VERTEX_SHADER_SOURCE);
    int fragmentShader = createShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER_SOURCE);

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
    logoTexture = loadTexture("/intro.png", true);

    // Setup projection matrix (orthographic)
    glUseProgram(shaderProgram);
    int projectionLoc = glGetUniformLocation(shaderProgram, "projection");

    // Create orthographic projection matrix
    float[] projectionMatrix = {
      2.0f, 0.0f, 0.0f, 0.0f,
      0.0f, 2.0f, 0.0f, 0.0f,
      0.0f, 0.0f, -1.0f, 0.0f,
      0.0f, 0.0f, 0.0f, 1.0f
    };

    glUniformMatrix4fv(projectionLoc, false, projectionMatrix);

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

  private void loop() {
    // Set clear color (dark background)
    glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    float startTime = (float) glfwGetTime();
    float fadeDuration = 2.0f;

    while (!glfwWindowShouldClose(window)) {
      float currentTime = (float) glfwGetTime();
      float elapsed = currentTime - startTime;

      // Calculate fade-in alpha
      float alpha = Math.min(elapsed / fadeDuration, 1.0f);

      // Clear screen
      glClear(GL_COLOR_BUFFER_BIT);

      // Use shader program
      glUseProgram(shaderProgram);

      // Set alpha uniform for fade effect
      glUniform1f(glGetUniformLocation(shaderProgram, "alpha"), alpha);

      // Bind texture
      glActiveTexture(GL_TEXTURE0);
      glBindTexture(GL_TEXTURE_2D, logoTexture);

      // Draw quad
      glBindVertexArray(VAO);
      glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

      // Swap buffers and poll events
      glfwSwapBuffers(window);
      glfwPollEvents();
    }
  }

  private void cleanup() {
    // Cleanup OpenGL resources
    glDeleteVertexArrays(VAO);
    glDeleteBuffers(VBO);
    glDeleteBuffers(EBO);
    glDeleteProgram(shaderProgram);
    glDeleteTextures(logoTexture);

    // Free window callbacks and destroy window
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);

    // Terminate GLFW and free error callback
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }

  public static void main(String[] args) {
    new SplashScreenResource().run();
  }
}
