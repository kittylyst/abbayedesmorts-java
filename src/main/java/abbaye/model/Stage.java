/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static org.lwjgl.glfw.GLFW.*;

import abbaye.AbbayeMain;
import abbaye.basic.Corners;
import abbaye.basic.Renderable;
import abbaye.graphics.StageRenderer;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.glfw.GLFWKeyCallbackI;

/** The stage shows the layout of the furniture of the current screen */
public class Stage implements Renderable {
  public static final int SCREENS_X = 5;
  public static final int SCREENS_Y = 5;
  public static final int NUM_SCREENS = SCREENS_X * SCREENS_Y;
  public static final int NUM_COLUMNS = 32;
  public static final int NUM_ROWS = 22;

  public static final int TILES_PER_ROW = 125; // Calculated tiles per row // atlasWidth / tileSize;
  public static final int TILES_PER_COL =
      30; // Calculated tiles per column // atlasHeight / tileSize;

  private int[][][] stagedata = new int[NUM_SCREENS][NUM_ROWS][NUM_COLUMNS];
  private int roomx = 0;
  private int roomy = 1;

  private Map<Integer, Corners> cache = new HashMap<>();

  private StageRenderer renderer;
  private boolean is16Bit = false;
  private boolean changeflag = false;

  public void load(long window) {
    this.renderer = new StageRenderer(window);
    load();
  }

  /** Loads stage screens from default location */
  public void load() {
    load("/map/map.txt");
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

      for (int i = 0; i < NUM_SCREENS; i++) {
        for (int j = 0; j < NUM_ROWS; j++) {
          line = br.readLine();
          for (int k = 0; k < NUM_COLUMNS; k++) {
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

  @Override
  public boolean render() {
    return renderer.render();
  }

  public int[][] getScreen(int level) {
    return stagedata[level];
  }

  public int getRoomX() {
    return roomx;
  }

  public int getRoomY() {
    return roomy;
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

  public GLFWKeyCallbackI moveCallback() {
    return (window, key, scancode, action, mods) -> {
      if (action == GLFW_RELEASE) {
        switch (key) {
          case GLFW_KEY_ESCAPE:
            {
              glfwSetWindowShouldClose(window, true);
              break;
            }
          case GLFW_KEY_RIGHT:
            {
              moveRight();
              break;
            }
          case GLFW_KEY_LEFT:
            {
              moveLeft();
              break;
            }
          case GLFW_KEY_DOWN:
            {
              moveDown();
              break;
            }
          case GLFW_KEY_UP:
            {
              moveUp();
              break;
            }
          default:
        }
      }
    };
  }

  /**
   * @return the size of the tile in display pixel, i.e. as it appears to the player
   */
  public static float getTileSize() {
    return 64.0f;
  }

  private static class SDLRect {
    public int x;
    public int y;
    public int w;
    public int h;

    public SDLRect(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }

    @Override
    public String toString() {
      return "SDLRect{" + "posX=" + 8 * x + ", posY=" + 8 * y + ", w=" + w + ", h=" + h + '}';
    }
  }

  public Corners getCorners(int tileType) {
    return cache.get(tileType);
  }

  public Corners getCorners(int x, int y) {
    var tileType = stagedata[roomy * SCREENS_X + roomx][y][x];
    if (cache.containsKey(tileType)) {
      return cache.get(tileType);
    }
    int[] counter = new int[2];

    // When we want to generalize this game, we can move this logic into a separate remapper.
    var srctiles = new SDLRect(0, 0, 8, 8);
    if (tileType == 0) {
      srctiles = new SDLRect(992, 0, 8, 8);
    } else if (tileType != 99) {
      if (tileType < 200) {
        srctiles.w = 8;
        srctiles.h = 8;
        if (tileType < 101) {
          srctiles.y = 0;
          if (tileType == 84) /* Cross brightness */
            srctiles.x = (tileType - 1) * 8 + (counter[0] / 8 * 8);
          else srctiles.x = (tileType - 1) * 8;
        } else {
          if (tileType == 154) {
            /* Door */
            srctiles.x = 600 + ((counter[0] / 8) * 16);
            srctiles.y = 0;
            srctiles.w = 16;
            srctiles.h = 24;
          } else {
            srctiles.y = 8;
            srctiles.x = (tileType - 101) * 8;
          }
        }
      }
      if ((tileType > 199) && (tileType < 300)) {
        srctiles.x = (tileType - 201) * 48;
        srctiles.y = 16;
        srctiles.w = 48;
        srctiles.h = 48;
      }
      if ((tileType > 299) && (tileType < 399)) {
        srctiles.x = 96 + ((tileType - 301) * 8);
        srctiles.y = 16;
        srctiles.w = 8;
        srctiles.h = 8;
        /* Door movement */
        //                        if ((room == ROOM_CHURCH) && ((counter[1] > 59) && (counter[1] <
        // 71))) {
        //                            if ((tileType == 347) || (tileType == 348) || (tileType ==
        // 349) || (tileType == 350)) {
        //                                destiles.x += 2;
        //                            }
        //                        }
      }
      /* Hearts */
      if ((tileType > 399) && (tileType < 405)) {
        srctiles.x = 96 + ((tileType - 401) * 8) + (32 * (counter[0] / 15));
        srctiles.y = 24;
        srctiles.w = 8;
        srctiles.h = 8;
      }
      /* Crosses */
      if ((tileType > 408) && (tileType < 429)) {
        srctiles.x = 96 + ((tileType - 401) * 8) + (32 * (counter[1] / 23));
        srctiles.y = 24;
        srctiles.w = 8;
        srctiles.h = 8;
      }

      if ((tileType > 499) && (tileType < 599)) {
        srctiles.x = 96 + ((tileType - 501) * 8);
        srctiles.y = 32;
        srctiles.w = 8;
        srctiles.h = 8;
      }
      if ((tileType > 599) && (tileType < 650)) {
        srctiles.x = 96 + ((tileType - 601) * 8);
        srctiles.y = 56;
        srctiles.w = 8;
        srctiles.h = 8;
      }
      if (tileType == 650) {
        /* Cup */
        srctiles.x = 584;
        srctiles.y = 87;
        srctiles.w = 16;
        srctiles.h = 16;
      }
      if ((tileType == 152) || (tileType == 137) || (tileType == 136)) {
        if (!changeflag) {
          srctiles.y = srctiles.y + (is16Bit ? 120 : 0);
        }
      } else {
        srctiles.y = srctiles.y + (is16Bit ? 120 : 0);
      }
    }

    float u1 = (float) srctiles.x / (8 * TILES_PER_ROW);
    float v1 = (float) srctiles.y / (8 * TILES_PER_COL);
    float u2 = (float) (srctiles.x + srctiles.w) / (8 * TILES_PER_ROW);
    float v2 = (float) (srctiles.y + srctiles.h) / (8 * TILES_PER_COL);

    var out = new Corners(u1, 1 - v1, u2, 1 - v2);
    cache.putIfAbsent(tileType, out);
    return out;
  }
}
