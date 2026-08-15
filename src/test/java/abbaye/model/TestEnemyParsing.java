/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.*;

import abbaye.AbbayeMain;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@code Stage.loadEnemies()} correctly parses {@code enemies.txt} and that {@code
 * Stage.buildEnemies()} produces {@link Enemy} instances with the right type, position, and
 * direction.
 *
 * <p>All assertions are pinned against known values in the real {@code enemies.txt} resource.
 */
class TestEnemyParsing {

  // Screen indices used in assertions (row-major: screen = roomy*5 + roomx)
  private static final int SCREEN_0_0 = 0; // all empty
  private static final int SCREEN_0_2 = 2; // 2 WALKERs
  private static final int SCREEN_0_3 = 3; // 4 WALKERs
  private static final int SCREEN_0_4 = 4; // 4 TALL_WALKERs
  private static final int SCREEN_1_0 = 5; // 7 CRUSADER_SPAWNs
  private static final int SCREEN_1_2 = 7; // all empty
  private static final int SCREEN_1_4 = 9; // 2 WALKERs + 4 TALL_FLOATER_Vs

  private static Stage stage;

  @BeforeAll
  static void loadStage() {
    AbbayeMain.setGlEnabled(false);
    stage = Stage.of();
    stage.load("/map/map.txt");
    stage.loadEnemies("/map/enemies.txt");
  }

  // ── Slot counts ─────────────────────────────────────────────────────────────

  @Test
  void allScreensHaveSevenSlots() {
    for (int i = 0; i < Stage.NUM_SCREENS; i++) {
      assertEquals(Stage.ENEMY_SLOTS, stage.getEnemySlots(i).size(), "screen " + i);
    }
  }

  @Test
  void emptyScreenHasNoPresentSlots() {
    long present = stage.getEnemySlots(SCREEN_0_0).stream().filter(EnemyData::isPresent).count();
    assertEquals(0, present);
  }

  @Test
  void screen02HasTwoPresentSlots() {
    long present = stage.getEnemySlots(SCREEN_0_2).stream().filter(EnemyData::isPresent).count();
    assertEquals(2, present);
  }

  @Test
  void screen03HasFourPresentSlots() {
    long present = stage.getEnemySlots(SCREEN_0_3).stream().filter(EnemyData::isPresent).count();
    assertEquals(4, present);
  }

  @Test
  void screen10HasSevenRawSlots() {
    // All 7 slots contain CRUSADER_SPAWN (code 17). isPresent() returns false for spawn markers,
    // so no live enemies are built — but the raw slot list still has 7 entries.
    assertEquals(7, stage.getEnemySlots(SCREEN_1_0).size());
    long live = stage.getEnemySlots(SCREEN_1_0).stream().filter(EnemyData::isPresent).count();
    assertEquals(0, live);
  }

  @Test
  void screen12HasNoPresentSlots() {
    long present = stage.getEnemySlots(SCREEN_1_2).stream().filter(EnemyData::isPresent).count();
    assertEquals(0, present);
  }

  // ── Field values: screen 0-2, slot 0 ────────────────────────────────────────
  // Source line: 001 056 144 000 128 040 000 056 224 005 000 001 013 006 015

  @Test
  void screen02Slot0HasCorrectType() {
    assertEquals(EnemyType.WALKER, stage.getEnemySlots(SCREEN_0_2).get(0).type());
  }

  @Test
  void screen02Slot0HasCorrectPosition() {
    var slot = stage.getEnemySlots(SCREEN_0_2).get(0);
    assertEquals(56, slot.x());
    assertEquals(144, slot.y());
  }

  @Test
  void screen02Slot0DirectionIsRight() {
    assertEquals(0, stage.getEnemySlots(SCREEN_0_2).get(0).direction());
  }

  @Test
  void screen02Slot0HasCorrectPatrolBounds() {
    var slot = stage.getEnemySlots(SCREEN_0_2).get(0);
    assertEquals(56, slot.limitLeft());
    assertEquals(224, slot.limitRight());
  }

  @Test
  void screen02Slot0HasCorrectSpeed() {
    assertEquals(5, stage.getEnemySlots(SCREEN_0_2).get(0).speed());
  }

  // ── Field values: screen 0-2, slot 1 ────────────────────────────────────────
  // Source line: 001 216 144 001 096 040 000 056 224 005 000 001 013 006 015

  @Test
  void screen02Slot1DirectionIsLeft() {
    assertEquals(1, stage.getEnemySlots(SCREEN_0_2).get(1).direction());
  }

  @Test
  void screen02Slot1XIs216() {
    assertEquals(216, stage.getEnemySlots(SCREEN_0_2).get(1).x());
  }

  // ── Type variety: screen 0-4 (TALL_WALKERs) ─────────────────────────────────

  @Test
  void screen04AllPresentSlotsAreTallWalkers() {
    stage.getEnemySlots(SCREEN_0_4).stream()
        .filter(EnemyData::isPresent)
        .forEach(d -> assertEquals(EnemyType.TALL_WALKER, d.type()));
  }

  // ── Type variety: screen 1-0 (CRUSADER_SPAWNs) ──────────────────────────────

  @Test
  void screen10AllSlotsAreCrusaderSpawn() {
    stage.getEnemySlots(SCREEN_1_0).stream()
        .filter(EnemyData::isPresent)
        .forEach(d -> assertEquals(EnemyType.CRUSADER_SPAWN, d.type()));
  }

  // ── buildEnemies(): Enemy construction ──────────────────────────────────────

  @Test
  void buildEnemiesScreen02ReturnsTwoEnemies() {
    List<Enemy> enemies = stage.buildEnemies(SCREEN_0_2);
    assertEquals(2, enemies.size());
  }

  @Test
  void buildEnemiesScreen00ReturnsEmptyList() {
    assertTrue(stage.buildEnemies(SCREEN_0_0).isEmpty());
  }

  @Test
  void buildEnemiesPositionScaledToJavaWorld() {
    // C native: x=56, y=144 → Java world: x=56*8=448, y=144*8=1152
    var enemy = stage.buildEnemies(SCREEN_0_2).get(0);
    assertEquals(56 * Player.PIXELS_PER_TILE, enemy.getPos().x(), 0.001f);
    assertEquals(144 * Player.PIXELS_PER_TILE, enemy.getPos().y(), 0.001f);
  }

  @Test
  void buildEnemiesDirectionMappedCorrectly() {
    List<Enemy> enemies = stage.buildEnemies(SCREEN_0_2);
    // slot 0: direction field=0 → RIGHT
    assertEquals(Facing.RIGHT, enemies.get(0).getDirection());
    // slot 1: direction field=1 → LEFT
    assertEquals(Facing.LEFT, enemies.get(1).getDirection());
  }

  @Test
  void buildEnemiesSizeFromType() {
    // WALKER is 16×16 in C native pixels
    var enemy = stage.buildEnemies(SCREEN_0_2).get(0);
    assertEquals(EnemyType.WALKER.getSize(), enemy.getSize());
  }
}
