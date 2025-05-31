/* Copyright (C) The Authors 2025 */
package abbaye.model;

import abbaye.TextureMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.lwjgl.util.Renderable;

public class Layer {
  private final List<Renderable> misc = new ArrayList<>();

  private Optional<Player> oPlayer = Optional.empty();

  public void init() {}

  public void add(TextureMap texture) {}

  public void setPlayer(Player p) {
    oPlayer = Optional.of(p);
  }
}
