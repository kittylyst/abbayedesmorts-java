/* Copyright (C) The Authors 2025 */
package abbaye.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class Enemy {
  public static class EnemySerializer extends JsonSerializer<Enemy> {
    @Override
    public void serialize(
        Enemy enemy, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
  }
}
