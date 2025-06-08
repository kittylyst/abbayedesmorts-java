/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Room.ROOM_CHURCH;
import static org.lwjgl.opengl.GL20.glUseProgram;

import abbaye.AbbayeMain;
import abbaye.graphics.Textures;
import java.io.*;

/** The stage shows the layout of the furniture of the current screen */
public class Stage implements Tiles {
  public static final int SCREENS_X = 5;
  public static final int SCREENS_Y = 5;
  public static final int NUM_SCREENS = SCREENS_X * SCREENS_Y;
  public static final int NUM_COLUMNS = 32;
  public static final int NUM_ROWS = 22;

  private int[][][] stagedata = new int[NUM_SCREENS][NUM_ROWS][NUM_COLUMNS];
  private int roomx = 0;
  private int roomy = 1;

  private int tiles;
  private int shaderProgram;

  /** Loads stage screens from default location */
  public void load() {
    load("/map/map.txt");
    if (AbbayeMain.isGlEnabled()) {
      tiles = Textures.loadTexture("/tiles.png");
      shaderProgram = 1;
      glUseProgram(shaderProgram);
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

  public int[][] getScreen(int level) {
    return stagedata[level];
  }

  public void moveLeft() {
    roomx -= 1;
  }

  public void moveRight() {
    roomx += 1;
  }

  public void moveUp() {
    roomy -= 1;
  }

  public void moveDown() {
    roomy += 1;
  }

  public int getTileSize() {
    return 32;
  }

  public int getTile(int x, int y) {
    return stagedata[roomy * 5 + roomx][y][x];
  }

  public static class SDL_Rect {
    public int x;
    public int y;
    public int w;
    public int h;

    public SDL_Rect(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }
  }
}
