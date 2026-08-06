/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.Stage.*;
import static abbaye.model.TileAtlas.*;
import static org.junit.jupiter.api.Assertions.*;

import abbaye.basic.Corners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Headless tests for TileAtlas.getCorners(). No GL/GLFW context required. */
public class TestTileAtlas {

  private TileAtlas atlas;

  @BeforeEach
  public void setUp() {
    atlas = new TileAtlas();
  }

  // ── Basic UV range ─────────────────────────────────────────────────────────

  @Test
  public void allCornersAreInUnitRange() {
    // Spot-check a representative spread of tile IDs
    int[] tileIds = {
      TILE_EMPTY,
      1,
      TILE_PASSABLE,
      TILE_PLATFORM,
      TILE_BEDROCK1,
      TILE_DOOR,
      TILE_TORCH_LIT,
      TILE_TORCH_DIM,
      TILE_FLAME,
      TILE_CUP,
      401, // heart
      409, // cross
      500, // 500-range tile
      600 // 600-range tile
    };
    for (int id : tileIds) {
      var c = atlas.getCorners(id);
      assertNotNull(c, "null corners for tile " + id);
      assertTrue(c.u1() >= 0f && c.u1() <= 1f, "u1 out of range for tile " + id);
      assertTrue(c.u2() >= 0f && c.u2() <= 1f, "u2 out of range for tile " + id);
      assertTrue(c.v1() >= 0f && c.v1() <= 1f, "v1 out of range for tile " + id);
      assertTrue(c.v2() >= 0f && c.v2() <= 1f, "v2 out of range for tile " + id);
    }
  }

  // ── Caching ───────────────────────────────────────────────────────────────

  @Test
  public void sameTileTypeReturnsSameCornerInstance() {
    var first = atlas.getCorners(TILE_BEDROCK1);
    var second = atlas.getCorners(TILE_BEDROCK1);
    assertSame(first, second, "expected cached instance to be returned on second call");
  }

  @Test
  public void cacheIsPopulatedAfterLookup() {
    assertTrue(atlas.getCache().isEmpty());
    atlas.getCorners(TILE_PASSABLE);
    assertTrue(atlas.getCache().containsKey(TILE_PASSABLE));
  }

  // ── Specific mappings ─────────────────────────────────────────────────────

  @Test
  public void tileEmptyMapsToFarRightOfAtlas() {
    // TILE_EMPTY is mapped to x=992 in the atlas (off to the right, invisible)
    var c = atlas.getCorners(TILE_EMPTY);
    float expectedU1 = 992f / (8f * TILES_PER_ROW);
    assertEquals(expectedU1, c.u1(), 1e-6f);
  }

  @Test
  public void distinctTileTypesProduceDifferentCorners() {
    var solidCorners = atlas.getCorners(TILE_BEDROCK1);
    var emptyCorners = atlas.getCorners(TILE_EMPTY);
    assertNotEquals(solidCorners.u1(), emptyCorners.u1(), "different tiles must not share u1");
  }

  @Test
  public void trapDoorProducesDefaultCorners() {
    // TILE_TRAP_DOOR falls through to the default SDLRect(0,0,8,8) path
    var c = atlas.getCorners(TILE_TRAP_DOOR);
    assertNotNull(c);
    assertEquals(0f, c.u1(), 1e-6f);
  }

  @Test
  public void cupTileHasNonZeroWidth() {
    // Cup tile uses w=16, h=16 — the UV extent must be larger than a standard 8px tile
    Corners standard = atlas.getCorners(1); // smallest regular tile, 8×8
    Corners cup = atlas.getCorners(TILE_CUP);
    float standardWidth = Math.abs(standard.u2() - standard.u1());
    float cupWidth = Math.abs(cup.u2() - cup.u1());
    assertTrue(cupWidth > standardWidth, "cup tile should be wider than a standard 8px tile");
  }
}
