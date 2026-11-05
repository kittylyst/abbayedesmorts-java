/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import abbaye.model.LegacyTileAtlasReference.PixelRect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// ! Test-scope generator that writes src/main/resources/tiles/tile-atlas.json using
// ! the legacy reference oracle.
// ! Manual execution only — disabled from normal test runs so the resource is not mutated during
// builds.
// ! Tested by: abbaye.model.TestLegacyTileAtlasReference (validates oracle output)
public class TileAtlasJsonGenerator {

  /** Root model for tile atlas JSON resource. */
  public record TileAtlasFile(
      int width,
      int height,
      int tileSize,
      @JsonProperty("default") PixelRect defaultRect,
      Map<String, PixelRect> tiles) {}

  public static final int ATLAS_WIDTH = 1000;
  public static final int ATLAS_HEIGHT = 240;
  public static final int BASE_TILE_SIZE = 8;
  public static final int MAX_TILE_ID = 650;

  public static TileAtlasFile buildTileAtlasData() {
    var defaultRect = new PixelRect(0, 0, 8, 8);
    Map<String, PixelRect> tiles = new LinkedHashMap<>();

    for (int id = 0; id <= MAX_TILE_ID; id++) {
      tiles.put(String.valueOf(id), LegacyTileAtlasReference.computePixelRect(id, false, false));
    }

    return new TileAtlasFile(ATLAS_WIDTH, ATLAS_HEIGHT, BASE_TILE_SIZE, defaultRect, tiles);
  }

  public static void generate(Path targetPath) throws IOException {
    Files.createDirectories(targetPath.getParent());
    var fileData = buildTileAtlasData();

    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
    writer.writeValue(targetPath.toFile(), fileData);
  }

  public static void main(String[] args) throws IOException {
    Path targetPath = Path.of("src/main/resources/tiles/tile-atlas.json");
    generate(targetPath);
    System.out.println("Generated " + targetPath.toAbsolutePath());
  }

  @Test
  @Disabled(
      "Manual generator - run explicitly to regenerate src/main/resources/tiles/tile-atlas.json")
  public void runGenerator() throws IOException {
    main(new String[0]);
  }
}
