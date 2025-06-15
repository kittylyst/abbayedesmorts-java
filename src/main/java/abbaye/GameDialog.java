/* Copyright (C) The Authors 2025 */
package abbaye;

import static abbaye.graphics.GLManager.PROJECTION_MATRIX;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import abbaye.graphics.GLManager;
import abbaye.graphics.OGLFont;
import abbaye.model.Player;

public class GameDialog {
  public enum State {
    INACTIVE,
    START,
    END
  }

  private final AbbayeMain mainClass;
  private final GLManager glManager;
  private final long window;

  private final int introSplashTexture;

  private OGLFont font;
  private State state;
  private Player player;

  //  private static final int SPLASH_SIZE_X = 256;
  //  private static final int SPLASH_SIZE_Y = 384;

  public GameDialog(Player pl, AbbayeMain main) {
    player = pl;
    mainClass = main;
    introSplashTexture = GLManager.loadTexture("/intro.png", true); // Needs to be mirrored?
    glManager = GLManager.get("dialog");
    state = State.INACTIVE;
    window = main.getWindow();
    reset();
  }

  public void render() {
    switch (state) {
      case INACTIVE -> {}
      case START -> {
        // True if we're testing
        if (mainClass == null) {
          return;
        }
        // Setup projection matrix (orthographic)
        //        if (glManager == null) {
        //          state = State.INACTIVE;
        //          return;
        //        }
        var shaderProgram = glManager.getShaderProgram();
        glUseProgram(shaderProgram);
        var projectionLocation = glGetUniformLocation(shaderProgram, "projection");

        glUniformMatrix4fv(projectionLocation, false, PROJECTION_MATRIX);

        // Set texture uniform
        glUniform1i(glGetUniformLocation(shaderProgram, "splashTexture"), 0);

        // Set alpha uniform for fade effect
        glUniform1f(glGetUniformLocation(glManager.getShaderProgram(), "alpha"), 1.0f);

        // Bind texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, introSplashTexture);

        // Draw quad
        glBindVertexArray(glManager.getVAO());
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
      }
      case END -> {
        // Used for testing
        if (mainClass == null) {
          return;
        }

        //        final var score = player.getScore();
        //        Config.config().setHighScore(score);

        //        font.print("Game Over", new Vector2(250, 150));
        //        font.print("Score: " + score, new Vector2(120, 200));
        //        font.print("High Score: " + Config.config().getHighScore(), new Vector2(330,
        // 200));
        //        font.print("Press TAB to play again", new Vector2(100, 260));

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
    glfwSetKeyCallback(window, mainClass.getStage().moveCallback());
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public void setFont(OGLFont font) {
    this.font = font;
  }

  public boolean isActive() {
    //    return false;
    return !(state == State.INACTIVE);
  }
}
