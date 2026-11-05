/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

// ! Unit tests validating LegacyTileAtlasReference matches TileAtlas behavior, expected sanity
// ! checks, and validating that the generated tile-atlas.json resource is correctly structured.
// ! Tests covering this class:
// ! - abbaye.model.TestLegacyTileAtlasReference.matchesTileAtlasDefaultFlags
// ! - abbaye.model.TestLegacyTileAtlasReference.matchesSanityCheckExpectations
// ! - abbaye.model.TestLegacyTileAtlasReference.generatedResourceMatchesLegacyOracle
public class TestLegacyTileAtlasReference {

  @Test
  public void matchesTileAtlasDefaultFlags() {
    var atlas = new TileAtlas();
    for (int id = 0; id <= 650; id++) {
      var expected = atlas.getCorners(id);
      var actual = LegacyTileAtlasReference.computeCorners(id, false, false);
      assertEquals(expected, actual, "Corners mismatch for tile ID " + id);
    }
  }

  @Test
  public void matchesSanityCheckExpectations() {
    // ID 0 -> x=992, y=0, w=8, h=8
    var rect0 = LegacyTileAtlasReference.computePixelRect(0, false, false);
    assertEquals(new LegacyTileAtlasReference.PixelRect(992, 0, 8, 8), rect0);

    // ID 154 (door) -> 16x24 at x=600, y=0
    var rect154 = LegacyTileAtlasReference.computePixelRect(154, false, false);
    assertEquals(new LegacyTileAtlasReference.PixelRect(600, 0, 16, 24), rect154);

    // ID 650 (cup) -> 584, 87, 16, 16
    var rect650 = LegacyTileAtlasReference.computePixelRect(650, false, false);
    assertEquals(new LegacyTileAtlasReference.PixelRect(584, 87, 16, 16), rect650);

    // ID 99 (trap door) -> 0, 0, 8, 8
    var rect99 = LegacyTileAtlasReference.computePixelRect(99, false, false);
    assertEquals(new LegacyTileAtlasReference.PixelRect(0, 0, 8, 8), rect99);
  }

  @Test
  public void generatedResourceMatchesLegacyOracle() throws Exception {
    try (InputStream in = getClass().getResourceAsStream("/tiles/tile-atlas.json")) {
      assertNotNull(in, "/tiles/tile-atlas.json should be available on classpath");
      var mapper = new ObjectMapper();
      var root = mapper.readTree(in);

      assertEquals(1000, root.get("width").asInt());
      assertEquals(240, root.get("height").asInt());
      assertEquals(8, root.get("tileSize").asInt());

      var defaultNode = root.get("default");
      assertNotNull(defaultNode, "default rect must be present");
      assertEquals(0, defaultNode.get("x").asInt());
      assertEquals(0, defaultNode.get("y").asInt());
      assertEquals(8, defaultNode.get("w").asInt());
      assertEquals(8, defaultNode.get("h").asInt());

      var tilesNode = root.get("tiles");
      assertNotNull(tilesNode, "tiles object must be present");
      assertEquals(651, tilesNode.size());

      for (int id = 0; id <= 650; id++) {
        var expectedRect = LegacyTileAtlasReference.computePixelRect(id, false, false);
        var tileNode = tilesNode.get(String.valueOf(id));
        assertNotNull(tileNode, "Missing tile " + id);
        assertEquals(expectedRect.x(), tileNode.get("x").asInt(), "x mismatch for tile " + id);
        assertEquals(expectedRect.y(), tileNode.get("y").asInt(), "y mismatch for tile " + id);
        assertEquals(expectedRect.w(), tileNode.get("w").asInt(), "w mismatch for tile " + id);
        assertEquals(expectedRect.h(), tileNode.get("h").asInt(), "h mismatch for tile " + id);
      }
    }
  }
}
