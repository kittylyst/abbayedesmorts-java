/* Copyright (C) The Authors 2025 */
package abbaye.model;

import abbaye.GameDialog;
import abbaye.basic.Actor;
import abbaye.basic.BoundingBox2;
import abbaye.basic.OGLFont;
import abbaye.basic.Vector2;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public final class Player implements Actor {

  private OGLFont font;

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

  public static class PlayerSerializer extends JsonSerializer<Player> {
    @Override
    public void serialize(
        Player player, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
  }

  private Player() {}

  public static Player of(Layer layer, GameDialog gameDialog) {
    return new Player();
  }

  public void update() {}

  public void render() {}

  public void setFont(OGLFont font) {
    this.font = font;
  }
}
