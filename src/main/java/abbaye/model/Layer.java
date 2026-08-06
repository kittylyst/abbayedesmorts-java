/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import abbaye.Config;
import abbaye.basic.Actor;
import abbaye.basic.Renderable;
import abbaye.logs.GameLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Layer {
  private final List<Renderable> misc = new ArrayList<>();

  private Optional<Player> oPlayer = Optional.empty();
  private Optional<Stage> oStage = Optional.empty();
  private Optional<StatusDisplay> oStatus = Optional.empty();
  private List<Enemy> enemies = new ArrayList<>();

  private GameLogger logger = Config.config().getLogger();

  public void init() {
    // Init order shouldn't matter (where render order does)
    for (var gObj : getRenderables()) {
      gObj.init();
    }
  }

  private List<Renderable> getRenderables() {
    var renderables = new ArrayList<Renderable>();
    renderables.addAll(misc);
    oPlayer.ifPresent(renderables::add);
    oStage.ifPresent(renderables::add);

    return renderables;
  }

  public void render() {
    // Order matters!
    oStage.ifPresent(Stage::render);
    oPlayer.ifPresent(Player::render);
    for (var enemy : enemies) {
      enemy.render();
    }
    // Status display needs to bind a different texture map (the fonts)
    oStatus.ifPresent(StatusDisplay::render);

    // Render other stuff
    for (var gObj : misc) {
      gObj.render();
    }
  }

  /**
   * Do the position update and collision detection of different object types
   *
   * @return
   */
  public void update() {
    oPlayer.ifPresent(Player::update);

    for (var enemy : enemies) {
      enemy.update();
    }

    debugLogState();

    // Collision detection — check if destroyable objects have been hit
    try {
      oPlayer.filter(Player::checkHit).ifPresent(Player::destroy);
    } catch (Throwable t) {
      oPlayer.ifPresent(p -> logger.error("Player threw: " + p, t));
    }

    // Enemy–player contact: any overlap kills the player (decrements lives, respawns at waypoint)
    oPlayer.ifPresent(this::checkEnemyContact);
  }

  /**
   * Checks whether any live enemy's hit box overlaps the player's hit box. On contact, delegates to
   * {@link Player#onEnemyContact()} which mirrors the C {@code jean.death = 1} path.
   */
  private void checkEnemyContact(Player player) {
    var playerBox = player.hitBox();
    for (var enemy : enemies) {
      if (enemy.hitBox().overlaps(playerBox)) {
        player.onEnemyContact();
        return; // one contact per tick is sufficient
      }
    }
  }

  private void debugLogState() {
    var logger = Config.config().getLogger();
    if (logger.getMinLevel().ordinal() == 0) {
      var jsonList =
          getRenderables().stream().filter(o -> o instanceof Actor).map(x -> x.toString()).toList();
      if (jsonList.size() > 0) {
        logger.debug(jsonList.toString());
      }
    }
  }

  ///////////////////////////

  public void setPlayer(Player p) {
    oPlayer = Optional.of(p);
  }

  public void setStage(Stage stage) {
    oStage = Optional.of(stage);
  }

  public void setStatus(StatusDisplay status) {
    oStatus = Optional.of(status);
  }

  /** Replaces the active enemy list. Called by {@code AbbayeMain.initLayer()} after stage load. */
  public void setEnemies(List<Enemy> enemies) {
    this.enemies = enemies;
  }

  public void cleanup() {
    oStage.ifPresent(Stage::cleanup);
  }
}
