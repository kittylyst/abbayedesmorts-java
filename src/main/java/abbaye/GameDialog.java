/* Copyright (C) The Authors 2025 */
package abbaye;

import abbaye.basic.OGLFont;
import abbaye.basic.Textures;
import abbaye.model.Player;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GameDialog {
  public enum State {
    INACTIVE,
    START,
    END
  }

  private final AbbayeMain mainClass;

  private final int introSplash;

  private OGLFont font;
  private State state;
  private Player player;

  private static final int SPLASH_SIZE_X = 256;
  private static final int SPLASH_SIZE_Y = 384;

  public GameDialog(Player pl, AbbayeMain main) {
    player = pl;
    mainClass = main;
    state = State.INACTIVE;
    introSplash = Textures.loadTextureMirrored("/intro.png");
    reset();
  }

  public void reset() {
    state = State.START;
  }

  public void render() {
    switch (state) {
      case INACTIVE -> {}
      case START -> {
        // Used for testing
        if (mainClass == null) {
          return;
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        GL11.glPushMatrix();

        //          GL11.glColor3f(1.0f, hitColorFade, hitColorFade);
        //          GL11.glTranslatef(pos.x(), pos.y(), 0);
        //          GL11.glRotatef(v.x() * 50, 0, 1, 0);

        // draw the texture
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, introSplash);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(0, -1);
        GL11.glVertex2f(0, SPLASH_SIZE_Y);
        GL11.glTexCoord2f(-1, -1);
        GL11.glVertex2f(SPLASH_SIZE_X, SPLASH_SIZE_Y);
        GL11.glTexCoord2f(-1, 0);
        GL11.glVertex2f(SPLASH_SIZE_X, 0);
        GL11.glEnd();

        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_BLEND);

        Keyboard.poll();
        if (Keyboard.isKeyDown(Keyboard.KEY_TAB)) {
          mainClass.initLayer();
          state = State.INACTIVE;
        }
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

        Keyboard.poll();
        if (Keyboard.isKeyDown(Keyboard.KEY_TAB)) {
          reset();
          mainClass.initLayer();
        }
      }
    }
  }

  //////////////////

  public void setPlayer(Player player) {
    this.player = player;
  }

  public void setFont(OGLFont font) {
    this.font = font;
  }

  public boolean isActive() {
    return false;
  }
}
