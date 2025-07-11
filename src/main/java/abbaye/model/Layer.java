/* Copyright (C) The Authors 2025 */
package abbaye.model;

import abbaye.AbbayeMain;
import abbaye.Config;
import abbaye.basic.Actor;
import abbaye.basic.Renderable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public class Layer {
  private final List<Renderable> misc = new ArrayList<>();

  private Optional<Player> oPlayer = Optional.empty();

  public void init() {
    for (var gObj : getRenderables()) {
      gObj.init();
    }
  }

  private List<Renderable> getRenderables() {
    var renderables = new ArrayList<Renderable>();
    renderables.addAll(misc);
    oPlayer.ifPresent(renderables::add);
    //    for (var formation : formations) {
    //      renderables.addAll(formation.getEnemies());
    //    }
    return renderables;
  }

  public void render() {
    oPlayer.ifPresent(Player::render);
    for (var gObj : getRenderables()) {
      gObj.render();
    }
  }

  public GLFWKeyCallbackI moveCallback() {
    return oPlayer.map(Player::moveCallback).orElse(AbbayeMain.ESC_QUITS_GAME);
  }

  /**
   * Do the position update and collision detection of different object types
   *
   * @return
   */
  public void update() {
    // FIXME Check for removals first

    oPlayer.ifPresent(Player::update);
    //    oPlayer.ifPresent(p -> System.out.println(p.getPos()));

    // FIXME Update enemies

    debugLogState();

    // Now do collision detection - check if destroyable objects have been hit

    // Player first
    oPlayer.filter(Player::checkHit).ifPresent(Player::destroy);

    // FIXME Now enemies

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
}
