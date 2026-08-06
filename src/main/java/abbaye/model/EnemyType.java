/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import abbaye.basic.Vector2;

/**
 * The type of an enemy, carrying its sprite dimensions for collision detection. Populated with
 * placeholder values until the C-side parsing logic is ported.
 */
public enum EnemyType {
  UNKNOWN(new Vector2(16, 16));

  // TODO: replace UNKNOWN with concrete types (e.g. GHOST, SPIDER, …) once
  //       enemy parsing from enemies.txt is implemented.

  private final Vector2 size;

  EnemyType(Vector2 size) {
    this.size = size;
  }

  public Vector2 getSize() {
    return size;
  }
}
