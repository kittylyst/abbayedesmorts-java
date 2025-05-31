/* Copyright (C) The Authors 2025 */
package abbaye.model;

import abbaye.GameDialog;
import abbaye.basic.OGLFont;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class Player {

  private OGLFont font;

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

  public void setFont(OGLFont font) {
    this.font = font;
  }
}
