/* Copyright (C) The Authors 2025 */
package abbaye.model;

import abbaye.basic.Actor;
import abbaye.basic.BoundingBox2;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public final class Enemy implements Actor {

  public static class EnemySerializer extends JsonSerializer<Enemy> {
    @Override
    public void serialize(
        Enemy enemy, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
  }

  @Override
  public BoundingBox2 getBB() {
    return null;
  }

  @Override
  public void init() {
    Actor.super.init();
  }

  @Override
  public boolean render() {
    return false;
  }

  @Override
  public boolean update() {
    return Actor.super.update();
  }
}
