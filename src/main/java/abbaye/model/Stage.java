/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

import abbaye.AbbayeMain;
import abbaye.basic.Renderable;
import abbaye.graphics.GLManager;
import abbaye.graphics.StageRenderer;
import java.io.*;
import org.lwjgl.glfw.GLFWKeyCallbackI;

/** The stage shows the layout of the furniture of the current screen */
public class Stage implements Tiles, Renderable {
  public static final int SCREENS_X = 5;
  public static final int SCREENS_Y = 5;
  public static final int NUM_SCREENS = SCREENS_X * SCREENS_Y;
  public static final int NUM_COLUMNS = 32;
  public static final int NUM_ROWS = 22;

  private int[][][] stagedata = new int[NUM_SCREENS][NUM_ROWS][NUM_COLUMNS];
  private int roomx = 0;
  private int roomy = 1;

  private int tilesTexture;
  private int shaderProgram;
  private StageRenderer renderer;
  private GLManager glm;

  /** Loads stage screens from default location */
  public void load() {
    load("/map/map.txt");
    if (AbbayeMain.isGlEnabled()) {
      tilesTexture = GLManager.loadTexture("/tiles.png", true);
      glm = GLManager.get("game");
      renderer.init(this);
    }
  }

  public void load(long window) {
    this.renderer = new StageRenderer(window);
    load();
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
      //      br.readLine();

      for (int i = 0; i < NUM_SCREENS; i++) {
        for (int j = 0; j < NUM_ROWS; j++) {
          line = br.readLine();
          for (int k = 0; k < NUM_COLUMNS; k++) {
            // Extract 3 characters, parse as int
            String temp = line.substring(k * 4, k * 4 + 3);
            stagedata[i][j][k] = Integer.parseInt(temp.trim());
          }
        }
        line = br.readLine(); // Skip separator line
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
    if (roomx < 4) {
      roomx += 1;
    }
  }

  public void moveUp() {
    if (roomy > 0) {
      roomy -= 1;
    }
  }

  public void moveDown() {
    if (roomy < 4) {
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

  public int getTileSize() {
    return 32;
  }

  public int getTile(int x, int y) {
    return stagedata[roomy * 5 + roomx][y][x];
  }
}
