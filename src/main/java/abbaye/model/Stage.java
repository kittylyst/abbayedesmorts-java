/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static org.lwjgl.glfw.GLFW.*;

import abbaye.AbbayeMain;
import abbaye.basic.Corners;
import abbaye.basic.Renderable;
import abbaye.graphics.GLManager;
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

  private int[][][] stagedata = new int[NUM_SCREENS][NUM_ROWS][NUM_COLUMNS];
  private int roomx = 0;
  private int roomy = 1;

  private Map<Integer, Corners> cache = new HashMap<>();

  private StageRenderer renderer;
  private GLManager glm;

  public void load(long window) {
    this.renderer = new StageRenderer(window);
    load();
    // TEST
    //    srctiles.y = 8;
    //    srctiles.x = (data - 101) * 8;
    cache.put(110, new Corners(72, 8, 104, 40));
  }

  /** Loads stage screens from default location */
  public void load() {
    load("/map/map.txt");
    if (AbbayeMain.isGlEnabled()) {
      glm = GLManager.get("game");
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

  public void moveLeft() {
    if (roomx > 0) {
      roomx -= 1;
    }
  }

  public void moveRight() {
    if (roomx < SCREENS_X - 1) {
      roomx += 1;
    }
  }

  public void moveUp() {
    if (roomy > 0) {
      roomy -= 1;
    }
  }

  public void moveDown() {
    if (roomy < SCREENS_Y - 1) {
      roomy += 1;
    }
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

  // FIXME What does this represent?
  public float getTileSize() {
    return 64.0f;
  }

  //  public int getTile(int x, int y) {
  //    var tileType = stagedata[roomy * SCREENS_X + roomx][y][x];
  //    var out = tileType;
  //    return out;
  //  }

  public Corners getCorners(int x, int y) {
    var tileType = stagedata[roomy * SCREENS_X + roomx][y][x];
    if (cache.containsKey(tileType)) {
      return cache.get(tileType);
    }

    int w = 0, h = 0;
    if ((tileType > 0) && (tileType != 99)) {
      if (tileType < 200) {
        w = 8;
        h = 8;
        if (tileType < 101) {
          y = 0;
          //          if (tileType == 84) /* Cross brightness */
          //            x = (tileType - 1) * 8 + (counter[0] / 8 * 8);
          //          else
          x = (tileType - 1) * 8;
        } else {
          if (tileType == 154) {
            /* Door */
            x = 600 + 16; // ((counter[0] / 8) * 16);
            y = 0;
            w = 16;
            h = 24;
          } else {
            y = 8;
            x = (tileType - 101) * 8;
          }
        }
      }
    }
    var out = new Corners(x, y, x + w, y + h);
    cache.computeIfAbsent(
        tileType,
        t -> {
          System.out.println("Tile type: " + t + " has corners: " + out);
          return out;
        });

    return out;
  }
}
