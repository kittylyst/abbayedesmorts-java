/* Copyright (C) The Authors 2025 */
package abbaye;

import static abbaye.graphics.GLManager.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryStack.stackPush;

import abbaye.basic.Corners;
import abbaye.graphics.GLManager;
import abbaye.model.Player;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

public class GameDialog {
  public enum State {
    INACTIVE,
    START,
    END
  }

  private final AbbayeMain mainClass;
  private final GLManager glManager;
  private final long window;

  private State state;
  private Player player;

  public GameDialog(Player pl, AbbayeMain main) {
    player = pl;
    mainClass = main;
    glManager = GLManager.get("dialog");
    state = State.INACTIVE;
    window = main.getWindow();
    reset();
  }

  private static float[] PROJECTION_MATRIX = {
    6.0f, 0.0f, Z_ZERO, 0.0f,
    0.0f, 6.0f, Z_ZERO, 0.0f,
    0.0f, 0.0f, Z_ZERO, 0.0f,
    -1.0f, -1.0f, Z_ZERO, 1.0f
  };

  public void render() {
    switch (state) {
      case INACTIVE -> {}
      case START -> {
        // True if we're testing
        if (mainClass == null) {
          return;
        }

        try (MemoryStack stack = stackPush()) {
          IntBuffer width = stack.mallocInt(1);
          IntBuffer height = stack.mallocInt(1);
          glfwGetFramebufferSize(window, width, height);
          glViewport(0, 0, width.get(0), height.get(0));
        }

        var shaderProgram = glManager.getShaderProgram();
        glUseProgram(shaderProgram);
        // Set texture uniform
        glUniform1i(glGetUniformLocation(shaderProgram, "splashTexture"), 0);

        // Set alpha uniform for fade effect
        glUniform1f(glGetUniformLocation(shaderProgram, "alpha"), 1.0f);

        // Bind texture
        glActiveTexture(GL_TEXTURE0);
        glManager.bindTexture("introSplash");
        glBindVertexArray(glManager.getVAO());

        glManager.renderTile(new Corners(-1.0f, -1.0f, 2.0f, 0.5f), PROJECTION_MATRIX);
      }
      case END -> {
        // Used for testing
        if (mainClass == null) {
          return;
        }

        //  FIXME Game Over screen

        if (glfwGetKey(window, GLFW_KEY_TAB) == GLFW_PRESS) {
          reset();
          mainClass.initLayer();
        }
      }
    }
  }

  //////////////////

  public void reset() {
    state = State.START;
  }

  public void startTurn() {
    state = State.INACTIVE;
    glfwSetKeyCallback(window, mainClass.getLayer().moveCallback());
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public boolean isActive() {
    return !(state == State.INACTIVE);
  }
}
