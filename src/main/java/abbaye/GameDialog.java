/* Copyright (C) The Authors 2025 */
package abbaye;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import abbaye.graphics.OGLFont;
import abbaye.graphics.Textures;
import abbaye.model.Player;

public class GameDialog {
  private final long window;

  public enum State {
    INACTIVE,
    START,
    END
  }

  private final AbbayeMain mainClass;

  private final int introSplashTexture;

  private OGLFont font;
  private State state;
  private Player player;

  //  private static final int SPLASH_SIZE_X = 256;
  //  private static final int SPLASH_SIZE_Y = 384;

  public GameDialog(Player pl, AbbayeMain main) {
    player = pl;
    mainClass = main;
    state = State.INACTIVE;
    introSplashTexture = Textures.loadTexture("/intro.png", true); // Needs to be mirrored?
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
        glClear(GL_COLOR_BUFFER_BIT);

        //        if (glfwGetKey(window, GLFW_KEY_TAB) == GLFW_PRESS) {
        //          mainClass.initLayer();
        //          state = State.INACTIVE;
        //        }

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
