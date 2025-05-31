/* Copyright (C) The Authors 2025 */
package abbaye.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class Player {
  public static class PlayerSerializer extends JsonSerializer<Player> {
    @Override
    public void serialize(
        Player player, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
  }
}
