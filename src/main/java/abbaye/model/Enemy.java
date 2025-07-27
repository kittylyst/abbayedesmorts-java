/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Facing.RIGHT;

import abbaye.basic.Actor;
import abbaye.basic.BoundingBox2;
import abbaye.basic.Vector2;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public final class Enemy implements Actor {

  // Physicality
  private Vector2 pos = new Vector2(0, 0);
  private Vector2 v = new Vector2(0, 0);
  private Facing direction = RIGHT;

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
  public Vector2 getPos() {
    return pos;
  }

  @Override
  public Vector2 getV() {
    return v;
  }

  @Override
  public Facing getDirection() {
    return direction;
  }

  @Override
  public Vector2 getSize() {
    return null;
  }

  @Override
  public boolean render() {
    return false;
  }
}
