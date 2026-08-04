/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

/**
 * Platform-agnostic input events delivered to game entities. Decouples domain logic from GLFW so
 * that Player.handleInput() can be exercised in headless tests without a GL context.
 */
public enum InputEvent {
  MOVE_LEFT_START,
  MOVE_LEFT_END,
  MOVE_RIGHT_START,
  MOVE_RIGHT_END,
  CROUCH_START,
  CROUCH_END,
  JUMP_START,
  JUMP_END,
  DEBUG_DUMP
}
