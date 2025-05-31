/* Copyright (C) The Authors 2025 */
package abbaye;

import abbaye.basic.OGLFont;
import abbaye.model.Player;

public class GameDialog {

  public enum State {
    INACTIVE,
    START,
    LEVEL_CHANGE,
    END
  }

  private OGLFont font;
  private Player player;
  private AbbayeMain mainClass;
  private State state;

  public GameDialog(Player pl, AbbayeMain main) {
    player = pl;
    mainClass = main;
    state = State.INACTIVE;
    reset();
  }

  public void reset() {}

  public void render() {}

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
