/* Copyright (C) The Authors 2025 */
package abbaye;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import abbaye.graphics.OGLFont;
import abbaye.graphics.Textures;
import abbaye.model.Player;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

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

  public void reset() {
    state = State.START;
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
        glLoadIdentity();

        int logoWidth = 0, logoHeight = 0;
        try (MemoryStack stack = stackPush()) {
          IntBuffer width = stack.mallocInt(1);
          IntBuffer height = stack.mallocInt(1);
          glfwGetFramebufferSize(window, width, height);
          // Assume logo is 512x256, scale it appropriately
          logoWidth = width.get(0); // 512 * logoScale;
          logoHeight = height.get(0); // 256 * logoScale;
        } catch (Exception e) {
          e.printStackTrace();
        }
        displayScreen(logoWidth, logoHeight);

        if (glfwGetKey(window, GLFW_KEY_TAB) == GLFW_PRESS) {
          mainClass.initLayer();
          state = State.INACTIVE;
        }

        //          glViewport(0, 0, logoWidth, logoHeight);
        //
        //
        //          float centerX = logoWidth / 2.0f;
        //          float centerY = logoHeight / 2.0f;
        //
        //          float x = centerX - logoWidth / 2;
        //          float y = centerY - logoHeight / 2;
        //
        ////        glColor4f(1.0f, 1.0f, 1.0f, fadeAlpha);
        //          glBindTexture(GL_TEXTURE_2D, introSplashTexture);

        //          glBegin(GL_QUADS);
        //          glTexCoord2f(0, 1);
        //          glVertex2f(x, y);
        //          glTexCoord2f(1, 1);
        //          glVertex2f(x + logoWidth, y);
        //          glTexCoord2f(1, 0);
        //          glVertex2f(x + logoWidth, y + logoHeight);
        //          glTexCoord2f(0, 0);
        //          glVertex2f(x, y + logoHeight);
        //          glEnd();

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

  private void displayScreen(int width, int height) {
    //    glViewport(0, 0, width, height);
    //    glClear(GL_COLOR_BUFFER_BIT);
    //    glLoadIdentity();

    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glBindTexture(GL_TEXTURE_2D, introSplashTexture);

    glBegin(GL_QUADS);
    glTexCoord2f(0, 1);
    glVertex2f(0, 0);
    glTexCoord2f(1, 1);
    glVertex2f(width, 0);
    glTexCoord2f(1, 0);
    glVertex2f(width, height);
    glTexCoord2f(0, 0);
    glVertex2f(0, height);
    glEnd();
  }

  //////////////////

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
