/* Copyright (C) The Authors 2025-2026 */
package abbaye;

import static org.lwjgl.glfw.GLFW.*;

import abbaye.model.InputEvent;
import abbaye.model.Player;
import org.lwjgl.glfw.GLFWKeyCallbackI;

/**
 * Translates raw GLFW key events into platform-agnostic {@link InputEvent}s and forwards them to
 * the current player. Centralises all GLFW key handling so that no domain class needs a GLFW
 * import.
 */
public final class InputHandler {

  private final long window;
  private final GameDialog gameDialog;
  private Player player;

  public InputHandler(long window, GameDialog gameDialog, Player player) {
    this.window = window;
    this.gameDialog = gameDialog;
    this.player = player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  /** Returns a GLFW key callback that routes all key events through this handler. */
  public GLFWKeyCallbackI keyCallback() {
    return (w, key, scancode, action, mods) -> {
      // ESC always quits
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
        glfwSetWindowShouldClose(w, true);
        return;
      }

      // Dialog controls (TAB / SPACE advance the intro splash)
      if ((key == GLFW_KEY_TAB || key == GLFW_KEY_SPACE) && action == GLFW_RELEASE) {
        gameDialog.startTurn();
        return;
      }

      // Player movement — only dispatch when a player is present
      if (player == null) {
        return;
      }

      InputEvent event = toInputEvent(key, action);
      if (event != null) {
        player.handleInput(event);
      }
    };
  }

  private static InputEvent toInputEvent(int key, int action) {
    if (action == GLFW_PRESS) {
      return switch (key) {
        case GLFW_KEY_RIGHT -> InputEvent.MOVE_RIGHT_START;
        case GLFW_KEY_LEFT -> InputEvent.MOVE_LEFT_START;
        case GLFW_KEY_DOWN -> InputEvent.CROUCH_START;
        case GLFW_KEY_UP -> InputEvent.JUMP_START;
        default -> null;
      };
    }
    if (action == GLFW_RELEASE) {
      return switch (key) {
        case GLFW_KEY_RIGHT -> InputEvent.MOVE_RIGHT_END;
        case GLFW_KEY_LEFT -> InputEvent.MOVE_LEFT_END;
        case GLFW_KEY_DOWN -> InputEvent.CROUCH_END;
        case GLFW_KEY_UP -> InputEvent.JUMP_END;
        case GLFW_KEY_TAB -> InputEvent.DEBUG_DUMP;
        default -> null;
      };
    }
    return null;
  }
}
