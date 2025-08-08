/* Copyright (C) The Authors 2025 */
package abbaye.model;

import abbaye.Config;
import abbaye.basic.Renderable;
import abbaye.logs.GameLogger;

public class StatusDisplay implements Renderable {

  private final Player player;

  private GameLogger logger = Config.config().getLogger();

  private StatusDisplay(Player player) {
    this.player = player;
  }

  public static StatusDisplay of(Player p) {
    return new StatusDisplay(p);
  }

  @Override
  public boolean render() {
    logger.info("Lives: " + player.getLives());
    return false;
  }
}
