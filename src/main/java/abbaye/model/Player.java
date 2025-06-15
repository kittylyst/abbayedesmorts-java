/* Copyright (C) The Authors 2025 */
package abbaye.model;

import abbaye.Config;
import abbaye.GameDialog;
import abbaye.basic.*;
import abbaye.graphics.OGLFont;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public final class Player implements Actor {

  // GL fields
  private int texture;
  private int mask;
  private OGLFont font;

  // Physicality
  private Vector2 pos = new Vector2(0, 0);

  @Override
  public void destroy() {
    Actor.super.destroy();
  }

  @Override
  public BoundingBox2 getBB() {
    return new BoundingBox2(Vector2.ORIGIN, Vector2.ORIGIN); // pos, size);
  }

  public boolean checkHit() {
    return false;
  }

  @Override
  public boolean render() {
    return false;
  }

  @Override
  public boolean update() {
    return Actor.super.update();
  }

  public static class PlayerSerializer extends JsonSerializer<Player> {
    @Override
    public void serialize(
        Player player, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
  }

  private Player() {
    //    texture = Textures.loadTexture("/duke-ship.png");
    //    mask = Textures.loadTexture("/starshipmask.png");
    pos = new Vector2(Config.config().getScreenWidth() / 2, Config.config().getScreenHeight() / 2);
  }

  public static Player of(Layer layer, GameDialog gameDialog) {
    return new Player();
  }

  public void setFont(OGLFont font) {
    this.font = font;
  }
}
