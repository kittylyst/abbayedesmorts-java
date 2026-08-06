/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.TileAtlas.*;

import abbaye.AbbayeMain;
import abbaye.basic.Corners;
import abbaye.basic.Renderable;
import abbaye.graphics.StageRenderer;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

/** The stage shows the layout of the furniture of the current screen */
public final class Stage implements Renderable {
  public static final int SCREENS_X = 5;
  public static final int SCREENS_Y = 5;
  public static final int NUM_SCREENS = SCREENS_X * SCREENS_Y;
  public static final int NUM_COLUMNS = 32;
  public static final int NUM_ROWS = 22;

  public static final int LEFT_EDGE = 0;
  public static final int TOP_EDGE = 0;

  // Room-specific collision constants (screen/room geometry — stay on Stage)
  static final int INVISIBLE_WALL_CROUCH_ROW = 5;
  static final int INVISIBLE_GROUND_ROW_THRESHOLD = 19;
  static final int INVISIBLE_GROUND_COLUMN = 2;
  static final int ROOM_BEAST_INVISIBLE_WALL_START = 27;
  static final int ROOM_BEAST_INVISIBLE_WALL_END = 32;
  static final int SCREEN_BOTTOM_ROW_THRESHOLD = 21;

  /** Number of enemy slots per screen, matching {@code struct enem} array size in the C source. */
  public static final int ENEMY_SLOTS = 7;

  private int[][][] stagedata = new int[NUM_SCREENS][NUM_ROWS][NUM_COLUMNS];

  /**
   * Enemy data indexed by [screen][slot]. Populated by {@link #loadEnemies()} or {@link
   * #loadEnemies(String)}.
   */
  private final List<List<EnemyData>> enemydata = new ArrayList<>(NUM_SCREENS);

  // Initial room coordinates
  private int roomx = 2; // 0
  private int roomy = 0; // 1

  private final TileAtlas atlas = new TileAtlas();

  private StageRenderer renderer;

  public Stage() {
    for (int i = 0; i < NUM_SCREENS; i++) {
      enemydata.add(new ArrayList<>(ENEMY_SLOTS));
    }
  }

  public void load(long window) {
    this.renderer = new StageRenderer(window);
    load();
  }

  /** Loads stage screens from default location */
  public void load() {
    load("/map/map.txt");
    loadEnemies();
    if (AbbayeMain.isGlEnabled()) {
      renderer.init(this);
    }
  }

  /**
   * Loads stage from supplied resource
   *
   * @param mapResource
   */
  public void load(String mapResource) {
    var input = Stage.class.getResourceAsStream(mapResource);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
      String line;

      // Skip two header lines
      br.readLine();

      for (int i = 0; i < NUM_SCREENS; i += 1) {
        for (int j = 0; j < NUM_ROWS; j += 1) {
          line = br.readLine();
          for (int k = 0; k < NUM_COLUMNS; k += 1) {
            // Extract 3 characters, parse as int
            String temp = line.substring(k * 4, k * 4 + 3);
            stagedata[i][j][k] = Integer.parseInt(temp.trim());
          }
        }
        br.readLine(); // Skip separator line
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Loads enemy data from the default resource location. */
  public void loadEnemies() {
    loadEnemies("/map/enemies.txt");
  }

  /**
   * Loads enemy data from the supplied classpath resource into {@link #enemydata}.
   *
   * <p>Format: 25 sections separated by {@code X-Y} header lines, 7 data lines each, 15
   * space-separated 3-digit fields per line. Matches {@code int enemydata[25][7][15]} in the C
   * source.
   *
   * @param resource classpath resource path (e.g. {@code "/map/enemies.txt"})
   */
  public void loadEnemies(String resource) {
    var input = Stage.class.getResourceAsStream(resource);
    try (var br = new BufferedReader(new InputStreamReader(input))) {
      for (int i = 0; i < NUM_SCREENS; i++) {
        br.readLine(); // consume "X-Y" header line
        var slots = enemydata.get(i);
        slots.clear();
        for (int j = 0; j < ENEMY_SLOTS; j++) {
          String line = br.readLine();
          int[] fields = new int[EnemyData.FIELD_COUNT];
          for (int k = 0; k < EnemyData.FIELD_COUNT; k++) {
            fields[k] = Integer.parseInt(line.substring(k * 4, k * 4 + 3).trim());
          }
          slots.add(EnemyData.fromFields(fields));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns an unmodifiable view of the raw enemy slots for the given screen index. Only slots
   * where {@link EnemyData#isPresent()} is {@code true} are meaningful.
   *
   * @param screen screen index (0 .. NUM_SCREENS-1)
   */
  List<EnemyData> getEnemySlots(int screen) {
    return Collections.unmodifiableList(enemydata.get(screen));
  }

  /**
   * Builds and returns a list of live {@link Enemy} instances for the given screen, one per present
   * slot (type code != 0). Positions are converted to Java world pixels.
   *
   * @param screen screen index (0 .. NUM_SCREENS-1)
   */
  public List<Enemy> buildEnemies(int screen) {
    return enemydata.get(screen).stream()
        .filter(EnemyData::isPresent)
        .map(Enemy::of)
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  public boolean render() {
    var out = renderer.render();
    return out;
  }

  /**
   * @param level
   * @return 2d array of tile ids in [x][y] order
   */
  public int[][] getScreen(int level) {
    return stagedata[level];
  }

  /**
   * Clears a single tile in the given screen by setting it to {@code TILE_EMPTY}. Prefer this over
   * direct array writes so that Stage remains the sole mutator of tile data.
   *
   * @param screen screen index (0 .. NUM_SCREENS-1)
   * @param row row index (0 .. NUM_ROWS-1)
   * @param col column index (0 .. NUM_COLUMNS-1)
   */
  public void clearTile(int screen, int row, int col) {
    if (screen >= 0
        && screen < NUM_SCREENS
        && row >= 0
        && row < NUM_ROWS
        && col >= 0
        && col < NUM_COLUMNS) {
      stagedata[screen][row][col] = TILE_EMPTY;
    }
  }

  /**
   * Clears every tile in the given screen that satisfies {@code predicate}, setting matching cells
   * to {@code TILE_EMPTY}. Used for sweep-clear operations (e.g. collecting all hearts or crosses).
   *
   * @param screen screen index (0 .. NUM_SCREENS-1)
   * @param predicate test applied to the current tile-type value; matching tiles are cleared
   */
  public void clearTilesWhere(int screen, IntPredicate predicate) {
    if (screen < 0 || screen >= NUM_SCREENS) {
      return;
    }
    var screenData = stagedata[screen];
    for (int row = 0; row < NUM_ROWS; row++) {
      for (int col = 0; col < NUM_COLUMNS; col++) {
        if (predicate.test(screenData[row][col])) {
          screenData[row][col] = TILE_EMPTY;
        }
      }
    }
  }

  public int getRoom() {
    return roomy * SCREENS_X + roomx;
  }

  public boolean moveLeft() {
    if (roomx > 0) {
      roomx -= 1;
      return true;
    }
    return false;
  }

  public boolean moveRight() {
    if (roomx < SCREENS_X - 1) {
      roomx += 1;
      return true;
    }
    return false;
  }

  public boolean moveUp() {
    if (roomy > 0) {
      roomy -= 1;
      return true;
    }
    return false;
  }

  public boolean moveDown() {
    if (roomy < SCREENS_Y - 1) {
      roomy += 1;
      return true;
    }
    return false;
  }

  /**
   * @return the size of the tile in display pixel, i.e. as it appears to the player
   */
  public static float getTileSize() {
    return 64.0f;
  }

  /** Converts a pixel x-coordinate to a tile column index. */
  public static int toTileX(float px) {
    return (int) (px / getTileSize());
  }

  /** Converts a pixel y-coordinate to a tile row index. */
  public static int toTileY(float px) {
    return (int) (px / getTileSize());
  }

  public int getRoomX() {
    return roomx;
  }

  public int getRoomY() {
    return roomy;
  }

  public void toWaypoint(Player.Waypoint waypoint) {
    roomx = waypoint.roomX();
    roomy = waypoint.roomY();
  }

  public Map<Integer, Corners> getCache() {
    return atlas.getCache();
  }

  public Corners getCorners(int x, int y) {
    var tileType = stagedata[roomy * SCREENS_X + roomx][y][x];
    return atlas.getCorners(tileType);
  }

  public Corners getCorners(int tileType) {
    return atlas.getCorners(tileType);
  }

  /** Exposes the atlas for testing and future animation wiring. */
  TileAtlas getAtlas() {
    return atlas;
  }
}
