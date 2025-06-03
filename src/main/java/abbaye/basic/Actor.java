/* Copyright (C) The Authors 2004-2025 */
package abbaye.basic;

import abbaye.AbbayeMain;
import abbaye.model.Enemy;
import abbaye.model.Player;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The interface defining core game actors that can interact with each other and participate in the
 * game lifecycle and main loop
 */
public sealed interface Actor extends Renderable permits Player, Enemy {

  default void destroy() {}

  BoundingBox2 getBB();

  /**
   * More useful than toString() - this method enables the dump of a complete game state for
   * debugging and deterministic testing.
   *
   * <p>Relies on a "shonky typeclass" pattern, whereby a Jackson serializer needs to be registered
   * for each type that will participate here.
   *
   * @param o - the object to be dumped.
   * @return
   */
  static String toJSon(Object o) {
    var mapper = AbbayeMain.getMapper();
    try {
      return mapper.writer().writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
