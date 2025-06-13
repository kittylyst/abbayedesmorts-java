/* Copyright (C) The Authors 2024-2025 */
package abbaye.basic;

/** The fundamental interface for game objects that will be rendered via OpenGL */
public interface Renderable {
  default void init() {}

  /**
   * The main render method.
   *
   * @return false if the object is to be deleted
   */
  // FIXME: Check the "return false to delete" contract is honoured
  boolean render();

  /**
   * @return false if object is no longer active
   */
  // FIXME: Check the "return false to delete" contract is honoured
  default boolean update() {
    return true;
  }

  default void cleanup() {}
}
