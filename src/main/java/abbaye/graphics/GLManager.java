/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static abbaye.graphics.Textures.loadTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class GLManager {

  // Vertex shader source
  private static final String VERTEX_SHADER =
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

  private static final String FRAGMENT_SHADER =
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

  private int shaderProgram;
  private int VAO, VBO, EBO;
  private int projectionLocation, modelLocation;

  public void init() {
    // Enable blending for transparency
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Create and compile shaders
    int vertexShader = createShader(GL_VERTEX_SHADER, VERTEX_SHADER);
    int fragmentShader = createShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

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

  public void init2() {
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
}
