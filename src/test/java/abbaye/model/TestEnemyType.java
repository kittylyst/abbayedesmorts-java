/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.*;

import abbaye.basic.Vector2;
import org.junit.jupiter.api.Test;

class TestEnemyType {

  // ── fromCode() ──────────────────────────────────────────────────────────────

  @Test
  void knownCodesMapToCorrectValues() {
    assertEquals(EnemyType.WALKER, EnemyType.fromCode(1));
    assertEquals(EnemyType.WALKER_NO_FLIP, EnemyType.fromCode(2));
    assertEquals(EnemyType.TALL_WALKER, EnemyType.fromCode(3));
    assertEquals(EnemyType.FLOATER_V, EnemyType.fromCode(4));
    assertEquals(EnemyType.TALL_FLOATER_V, EnemyType.fromCode(5));
    assertEquals(EnemyType.WIDE_WALKER, EnemyType.fromCode(6));
    assertEquals(EnemyType.SHOOTER, EnemyType.fromCode(11));
    assertEquals(EnemyType.WATER_DROP, EnemyType.fromCode(12));
    assertEquals(EnemyType.FIREBALL, EnemyType.fromCode(13));
    assertEquals(EnemyType.FISH, EnemyType.fromCode(14));
    assertEquals(EnemyType.AIMING_SHOOTER, EnemyType.fromCode(15));
    assertEquals(EnemyType.CRUSADER, EnemyType.fromCode(16));
    assertEquals(EnemyType.CRUSADER_SPAWN, EnemyType.fromCode(17));
    assertEquals(EnemyType.SATAN, EnemyType.fromCode(18));
    assertEquals(EnemyType.SMOKE, EnemyType.fromCode(88));
  }

  @Test
  void codeZeroMapsToUnknown() {
    assertEquals(EnemyType.UNKNOWN, EnemyType.fromCode(0));
  }

  @Test
  void unrecognisedCodeMapsToUnknown() {
    assertEquals(EnemyType.UNKNOWN, EnemyType.fromCode(99));
    assertEquals(EnemyType.UNKNOWN, EnemyType.fromCode(-1));
  }

  // ── code field round-trips ───────────────────────────────────────────────────

  @Test
  void codeFieldMatchesFileValue() {
    assertEquals(1, EnemyType.WALKER.code);
    assertEquals(3, EnemyType.TALL_WALKER.code);
    assertEquals(17, EnemyType.CRUSADER_SPAWN.code);
    assertEquals(88, EnemyType.SMOKE.code);
  }

  // ── sprite sizes from C source ───────────────────────────────────────────────

  @Test
  void standardEnemiesAre16x16() {
    assertEquals(new Vector2(16, 16), EnemyType.WALKER.getSize());
    assertEquals(new Vector2(16, 16), EnemyType.WALKER_NO_FLIP.getSize());
    assertEquals(new Vector2(16, 16), EnemyType.FLOATER_V.getSize());
    assertEquals(new Vector2(16, 16), EnemyType.WATER_DROP.getSize());
    assertEquals(new Vector2(16, 16), EnemyType.FIREBALL.getSize());
    assertEquals(new Vector2(16, 16), EnemyType.FISH.getSize());
  }

  @Test
  void tallEnemiesAre16x24() {
    assertEquals(new Vector2(16, 24), EnemyType.TALL_WALKER.getSize());
    assertEquals(new Vector2(16, 24), EnemyType.TALL_FLOATER_V.getSize());
    assertEquals(new Vector2(16, 24), EnemyType.SHOOTER.getSize());
    assertEquals(new Vector2(16, 24), EnemyType.AIMING_SHOOTER.getSize());
    assertEquals(new Vector2(16, 24), EnemyType.CRUSADER.getSize());
    assertEquals(new Vector2(16, 24), EnemyType.CRUSADER_SPAWN.getSize());
  }

  @Test
  void wideWalkerIs24x16() {
    assertEquals(new Vector2(24, 16), EnemyType.WIDE_WALKER.getSize());
  }

  @Test
  void satanIs32x24() {
    assertEquals(new Vector2(32, 24), EnemyType.SATAN.getSize());
  }

  @Test
  void smokeIs32x48() {
    assertEquals(new Vector2(32, 48), EnemyType.SMOKE.getSize());
  }
}
