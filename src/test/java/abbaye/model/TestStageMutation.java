/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.Stage.*;
import static abbaye.model.TileAtlas.*;
import static org.junit.jupiter.api.Assertions.*;

import abbaye.AbbayeMain;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Headless tests for Stage.clearTile() and Stage.clearTilesWhere(). */
public class TestStageMutation {

  private Stage stage;
  private int screen;

  @BeforeAll
  public static void setUpBeforeClass() {
    AbbayeMain.setGlEnabled(false);
  }

  @BeforeEach
  public void setUp() {
    stage = Stage.of();
    screen = stage.getRoom(); // use the actual initial screen (roomx=2, roomy=0 → screen 2)
    Utils.setTile(stage, 5, 3, 42);
    Utils.setTile(stage, 6, 3, 42);
    Utils.setTile(stage, 7, 3, 99);
  }

  // ── clearTile ──────────────────────────────────────────────────────────────

  @Test
  public void clearTileSetsTileToEmpty() {
    stage.clearTile(screen, 3, 5);
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[3][5]);
  }

  @Test
  public void clearTileDoesNotAffectOtherCells() {
    stage.clearTile(screen, 3, 5);
    assertEquals(42, stage.getScreen(screen)[3][6]); // col 6 untouched
    assertEquals(99, stage.getScreen(screen)[3][7]); // col 7 untouched
  }

  @Test
  public void clearTileIgnoresOutOfBoundsRow() {
    assertDoesNotThrow(() -> stage.clearTile(screen, NUM_ROWS, 0));
    assertDoesNotThrow(() -> stage.clearTile(screen, -1, 0));
  }

  @Test
  public void clearTileIgnoresOutOfBoundsCol() {
    assertDoesNotThrow(() -> stage.clearTile(screen, 0, NUM_COLUMNS));
    assertDoesNotThrow(() -> stage.clearTile(screen, 0, -1));
  }

  @Test
  public void clearTileIgnoresOutOfBoundsScreen() {
    assertDoesNotThrow(() -> stage.clearTile(NUM_SCREENS, 0, 0));
    assertDoesNotThrow(() -> stage.clearTile(-1, 0, 0));
  }

  // ── clearTilesWhere ────────────────────────────────────────────────────────

  @Test
  public void clearTilesWhereRemovesMatchingTiles() {
    stage.clearTilesWhere(screen, t -> t == 42);
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[3][5]);
    assertEquals(TILE_EMPTY, stage.getScreen(screen)[3][6]);
  }

  @Test
  public void clearTilesWhereDoesNotRemoveNonMatchingTiles() {
    stage.clearTilesWhere(screen, t -> t == 42);
    assertEquals(99, stage.getScreen(screen)[3][7]); // value 99 should survive
  }

  @Test
  public void clearTilesWhereOnOtherScreenLeavesThisScreenUntouched() {
    int otherScreen = (screen + 1) % NUM_SCREENS;
    stage.clearTilesWhere(otherScreen, t -> t == 42);
    assertEquals(42, stage.getScreen(screen)[3][5]); // seeded screen unchanged
    assertEquals(42, stage.getScreen(screen)[3][6]);
  }

  @Test
  public void clearTilesWhereWithNoMatchIsNoop() {
    assertDoesNotThrow(() -> stage.clearTilesWhere(screen, t -> t == 999));
    assertEquals(42, stage.getScreen(screen)[3][5]); // unchanged
  }
}
