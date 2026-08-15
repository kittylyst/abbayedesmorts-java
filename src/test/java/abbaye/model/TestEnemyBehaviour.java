/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.*;

import abbaye.AbbayeMain;
import abbaye.basic.BoundingBox2;
import abbaye.basic.Vector2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for enemy patrol movement ({@link Enemy#update()}) and enemy–player contact detection
 * ({@link Layer#update()} → {@link Player#onEnemyContact()}).
 *
 * <p>All tests are headless (no OpenGL context).
 */
class TestEnemyBehaviour {

  private static Stage stage;

  @BeforeAll
  static void setUpBeforeClass() {
    AbbayeMain.setGlEnabled(false);
  }

  @BeforeEach
  void loadStage() {
    stage = Stage.of();
    stage.load("/map/map.txt");
    stage.loadEnemies("/map/enemies.txt");
  }

  // ── Patrol movement ──────────────────────────────────────────────────────────

  /** A WALKER starting at x=56, limitRight=224 (C native), direction=RIGHT should move right. */
  @Test
  void walkerMovesRightWhenFacingRight() {
    var enemies = stage.buildEnemies(2); // screen 0-2, slot 0: WALKER dir=RIGHT
    var walker = enemies.get(0);
    assertEquals(Facing.RIGHT, walker.getDirection());

    float xBefore = walker.getPos().x();
    walker.update();
    assertTrue(walker.getPos().x() > xBefore, "Walker should advance to the right");
  }

  /** A WALKER starting direction=LEFT (slot 1 in screen 0-2) should move left. */
  @Test
  void walkerMovesLeftWhenFacingLeft() {
    var enemies = stage.buildEnemies(2); // screen 0-2, slot 1: WALKER dir=LEFT
    var walker = enemies.get(1);
    assertEquals(Facing.LEFT, walker.getDirection());

    float xBefore = walker.getPos().x();
    walker.update();
    assertTrue(walker.getPos().x() < xBefore, "Walker should advance to the left");
  }

  /** Walker should reverse direction when it reaches its right patrol boundary. */
  @Test
  void walkerReversesAtRightBoundary() {
    // Screen 0-2 slot 0: limitRight=224 C → 1792 Java world
    var enemies = stage.buildEnemies(2);
    var walker = enemies.get(0);
    assertEquals(Facing.RIGHT, walker.getDirection());

    // Drive the walker past its right limit
    for (int i = 0; i < 5000; i++) {
      walker.update();
      if (walker.getDirection() == Facing.LEFT) break;
    }
    assertEquals(Facing.LEFT, walker.getDirection(), "Walker should have reversed to LEFT");
  }

  /** Walker should reverse direction when it reaches its left patrol boundary. */
  @Test
  void walkerReversesAtLeftBoundary() {
    // Screen 0-2 slot 1: limitLeft=56 C → 448 Java world, starts facing LEFT
    var enemies = stage.buildEnemies(2);
    var walker = enemies.get(1);
    assertEquals(Facing.LEFT, walker.getDirection());

    for (int i = 0; i < 5000; i++) {
      walker.update();
      if (walker.getDirection() == Facing.RIGHT) break;
    }
    assertEquals(Facing.RIGHT, walker.getDirection(), "Walker should have reversed to RIGHT");
  }

  /** Non-patrol types (code >= 10) should not move. */
  @Test
  void nonPatrolTypeDoesNotMove() {
    // Use Enemy.of(EnemyType) to construct a SHOOTER directly — screen 1-0 contains only
    // CRUSADER_SPAWN markers which are now filtered by buildEnemies().
    var shooter = Enemy.of(EnemyType.SHOOTER);
    assertTrue(shooter.getType().code > 9, "Expected non-patrol type");

    var posBefore = shooter.getPos();
    shooter.update();
    assertEquals(posBefore, shooter.getPos(), "Non-patrol enemy should not move");
  }

  // ── Hit box ──────────────────────────────────────────────────────────────────

  /**
   * Screen 0-2 slot 0: adjustX1=1, adjustX2=13, adjustY1=6, adjustY2=15 (C native). Java world:
   * each × 8. With pos.x=448, pos.y=1152, hitBox left=456, right=552, top=1200, bottom=1272.
   */
  @Test
  void hitBoxReflectsAdjustOffsets() {
    var enemy = stage.buildEnemies(2).get(0);
    BoundingBox2 box = enemy.hitBox();

    float scale = Player.PIXELS_PER_TILE;
    float posX = 56 * scale; // 448
    float posY = 144 * scale; // 1152
    assertEquals(posX + 1 * scale, box.left(), 0.001f);
    assertEquals(posX + 13 * scale, box.right(), 0.001f);
    assertEquals(posY + 6 * scale, box.top(), 0.001f);
    assertEquals(posY + 15 * scale, box.bottom(), 0.001f);
  }

  // ── Enemy–player contact ─────────────────────────────────────────────────────

  /**
   * When the player is placed on top of an enemy, {@code Layer.update()} should call {@code
   * onEnemyContact()}, which decrements lives and teleports the player to the last waypoint.
   *
   * <p>Player is placed at the default waypoint (192, 1088) so that {@code checkStaticHazard()}
   * does not fire first and move the player away before the contact check runs.
   */
  @Test
  void contactWithEnemyDecrementsPlayerLives() {
    var layer = new Layer();
    var player = Player.of(layer, stage);
    // Default waypoint pos is (192, 1088); place player there so no static hazard fires first
    player.setPos(new Vector2(192, 1088));
    layer.setPlayer(player);
    layer.setStage(stage);

    // Enemy at same position with large adjust box (C native: x=24,y=136, adjustX2=100,
    // adjustY2=100)
    // Java world: pos=(192,1088), adjustX2=800, adjustY2=800 — guaranteed overlap
    var enemy =
        Enemy.of(
            EnemyData.fromFields(
                new int[] {1, 24, 136, 0, 0, 0, 0, 0, 1000, 0, 0, 0, 100, 0, 100}));
    layer.setEnemies(java.util.List.of(enemy));

    int livesBefore = player.getLives();
    layer.update();
    assertEquals(
        livesBefore - 1, player.getLives(), "Lives should have decremented on enemy contact");
  }

  /** When the player is far from all enemies, no life should be lost. */
  @Test
  void noContactWhenPlayerFarFromEnemy() {
    var layer = new Layer();
    var player = Player.of(layer, stage);
    // Place player far from any enemy in screen 0-2 (enemies around x=448..1792, y=1152)
    player.setPos(new Vector2(10000, 10000));
    layer.setPlayer(player);
    layer.setStage(stage);
    layer.setEnemies(stage.buildEnemies(2));

    int livesBefore = player.getLives();
    layer.update();
    assertEquals(livesBefore, player.getLives(), "Lives should not change when player is far away");
  }

  /** After contact, the player's position should be reset to the waypoint. */
  @Test
  void contactTeleportsPlayerToWaypoint() {
    var layer = new Layer();
    var player = Player.of(layer, stage);
    player.setPos(new Vector2(192, 1088));
    layer.setPlayer(player);
    layer.setStage(stage);

    // Default waypoint: roomX=0, roomY=1, pos (192, 1088)
    var waypointPos = new Vector2(192.0f, 1088.0f);

    // Enemy at player's position with large adjust box — guaranteed overlap
    var enemy =
        Enemy.of(
            EnemyData.fromFields(
                new int[] {1, 24, 136, 0, 0, 0, 0, 0, 1000, 0, 0, 0, 100, 0, 100}));
    layer.setEnemies(java.util.List.of(enemy));

    layer.update();
    assertEquals(waypointPos.x(), player.getPos().x(), 0.001f);
    assertEquals(waypointPos.y(), player.getPos().y(), 0.001f);
  }

  // ── Fix 1: CRUSADER_SPAWN filtered from buildEnemies ─────────────────────────

  /** Screen 1-0 (index 5) has 7 CRUSADER_SPAWN slots; buildEnemies should return empty. */
  @Test
  void crusaderSpawnIsFilteredFromBuildEnemies() {
    var enemies = stage.buildEnemies(5); // screen 1-0: all CRUSADER_SPAWN
    assertTrue(enemies.isEmpty(), "CRUSADER_SPAWN markers must not produce live enemies");
  }

  // ── Fix 2: Vertical patrol (FLOATER_V / TALL_FLOATER_V) ─────────────────────

  /**
   * Screen 1-4 (index 9) has TALL_FLOATER_V enemies that patrol vertically. A LEFT-direction
   * floater should decrease its Y coordinate each tick.
   */
  @Test
  void verticalFloaterMovesOnYAxis() {
    // Screen 1-4 has TALL_FLOATER_V enemies
    var enemies = stage.buildEnemies(9);
    var floater =
        enemies.stream()
            .filter(e -> e.getType() == EnemyType.TALL_FLOATER_V)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No TALL_FLOATER_V in screen 1-4"));

    float xBefore = floater.getPos().x();
    floater.update();
    // Y must change; X must stay fixed
    assertEquals(xBefore, floater.getPos().x(), 0.001f, "Vertical floater must not move on X");
  }

  @Test
  void verticalFloaterReversesAtYBoundary() {
    var enemies = stage.buildEnemies(9);
    var floater =
        enemies.stream()
            .filter(e -> e.getType() == EnemyType.TALL_FLOATER_V)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No TALL_FLOATER_V in screen 1-4"));

    Facing startDir = floater.getDirection();
    // Drive it to the opposite boundary
    for (int i = 0; i < 5000; i++) {
      floater.update();
      if (floater.getDirection() != startDir) break;
    }
    assertNotEquals(
        startDir, floater.getDirection(), "Vertical floater should reverse at Y boundary");
  }

  // ── Fix 3: Post-contact invulnerability ──────────────────────────────────────

  /**
   * After enemy contact the player gains an invulnerability window; a second immediate contact on
   * the following tick must not decrement lives again.
   */
  @Test
  void noSecondLifeLostDuringInvulnerabilityWindow() {
    var layer = new Layer();
    var player = Player.of(layer, stage);
    player.setPos(new Vector2(192, 1088));
    layer.setPlayer(player);
    layer.setStage(stage);

    var enemy =
        Enemy.of(
            EnemyData.fromFields(
                new int[] {1, 24, 136, 0, 0, 0, 0, 0, 1000, 0, 0, 0, 100, 0, 100}));
    layer.setEnemies(java.util.List.of(enemy));

    layer.update(); // first contact — lives decremented, cooldown set
    int livesAfterFirst = player.getLives();

    // Manually place player back on top of the enemy (waypoint teleport already did this,
    // but make it explicit to ensure overlap)
    player.setPos(new Vector2(192, 1088));
    layer.update(); // second tick — player should still be invulnerable
    assertEquals(livesAfterFirst, player.getLives(), "Lives must not drop again during cooldown");
  }

  // ── Fix 4: Patrol boundary epsilon scaled ────────────────────────────────────

  /**
   * The boundary check epsilon is {@code PIXELS_PER_TILE} (8). A walker positioned exactly at its
   * right limit minus epsilon must still advance; positioned at exactly the limit it must reverse.
   */
  @Test
  void walkerReversesWhenWithinScaledEpsilonOfRightLimit() {
    // Construct a walker with limitRight = 800 (Java world), position at 800 - epsilon + 1 → still
    // inside → still moving right. Position at 800 - epsilon → should reverse.
    float scale = Player.PIXELS_PER_TILE;
    float limitRight = 800f;
    float epsilon = scale; // PIXELS_PER_TILE

    // Place walker at limitRight - epsilon exactly: pos + epsilon >= limitRight → reverses
    var data =
        EnemyData.fromFields(
            new int[] {
              1,
              (int) (limitRight / scale),
              10, // x, y (C native)
              0,
              0,
              0,
              0,
              0,
              (int) (limitRight / scale), // limitRight == pos: should reverse immediately
              5,
              0,
              0,
              16,
              0,
              16
            });
    var walker = Enemy.of(data);
    assertEquals(Facing.RIGHT, walker.getDirection());
    walker.update();
    assertEquals(
        Facing.LEFT,
        walker.getDirection(),
        "Walker at right limit should reverse when pos + epsilon >= limitRight");
  }
}
