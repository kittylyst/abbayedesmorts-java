/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.model.Room.ROOM_CHURCH;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;

import abbaye.AbbayeMain;
import abbaye.basic.Textures;
import java.io.*;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/** The stage shows the layout of the furniture of the current screen */
public class Stage {
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

  public void render(int[] counter, boolean changeflag, int changetiles) {
    var room = roomy * 5 + roomx;
    for (var coordy = 0; coordy <= 21; coordy++) {
      for (var coordx = 0; coordx <= 31; coordx++) {
        var data = stagedata[room][coordy][coordx];

        var srctiles = new SDL_Rect(0, 0, 8, 8);
        //        var destiles = new SDL_Rect(0,0,8,8);

        if ((data > 0) && (data != 99)) {
          //                  destiles.x = coordx * 8;
          //                  destiles.y = coordy * 8;
          if (data < 200) {
            srctiles.w = 8;
            srctiles.h = 8;
            if (data < 101) {
              srctiles.y = 0;
              if (data == 84) /* Cross brightness */
                srctiles.x = (data - 1) * 8 + (counter[0] / 8 * 8);
              else srctiles.x = (data - 1) * 8;
            } else {
              if (data == 154) {
                /* Door */
                srctiles.x = 600 + ((counter[0] / 8) * 16);
                srctiles.y = 0;
                srctiles.w = 16;
                srctiles.h = 24;
              } else {
                srctiles.y = 8;
                srctiles.x = (data - 101) * 8;
              }
            }
          }
          if ((data > 199) && (data < 300)) {
            srctiles.x = (data - 201) * 48;
            srctiles.y = 16;
            srctiles.w = 48;
            srctiles.h = 48;
          }
          if ((data > 299) && (data < 399)) {
            srctiles.x = 96 + ((data - 301) * 8);
            srctiles.y = 16;
            srctiles.w = 8;
            srctiles.h = 8;
            /* Door movement */
            if ((room == ROOM_CHURCH.ordinal()) && ((counter[1] > 59) && (counter[1] < 71))) {
              if ((data == 347) || (data == 348) || (data == 349) || (data == 350)) {
                //                        destiles.x += 2;
                //                        if ((data == 350) && (counter[1] == 70)) {
                //                          Mix_PlayChannel(-1, fx[3], 0); /* Sound of door */
                //                        }
              }
            }
          }
          /* Hearts */
          if ((data > 399) && (data < 405)) {
            srctiles.x = 96 + ((data - 401) * 8) + (32 * (counter[0] / 15));
            srctiles.y = 24;
            srctiles.w = 8;
            srctiles.h = 8;
          }
          /* Crosses */
          if ((data > 408) && (data < 429)) {
            srctiles.x = 96 + ((data - 401) * 8) + (32 * (counter[1] / 23));
            srctiles.y = 24;
            srctiles.w = 8;
            srctiles.h = 8;
          }

          if ((data > 499) && (data < 599)) {
            srctiles.x = 96 + ((data - 501) * 8);
            srctiles.y = 32;
            srctiles.w = 8;
            srctiles.h = 8;
          }
          if ((data > 599) && (data < 650)) {
            srctiles.x = 96 + ((data - 601) * 8);
            srctiles.y = 56;
            srctiles.w = 8;
            srctiles.h = 8;
          }
          if (data == 650) {
            /* Cup */
            srctiles.x = 584;
            srctiles.y = 87;
            srctiles.w = 16;
            srctiles.h = 16;
          }
          //                  destiles.w = srctiles.w;
          //                  destiles.h = srctiles.h;
          if ((data == 152) || (data == 137) || (data == 136)) {
            if (changeflag) {
              srctiles.y = srctiles.y + (changetiles * 120);
              SDL_RenderCopy(srctiles);
            }
          } else {
            srctiles.y = srctiles.y + (changetiles * 120);
            SDL_RenderCopy(srctiles);
          }
        }
      }
    }
  }

  private void SDL_RenderCopy(SDL_Rect srctiles) {
    //    glActiveTexture(GL_TEXTURE0);
    //    glBindTexture(GL_TEXTURE_2D, tiles);

    // Fixme
    GL11.glBegin(GL11.GL_QUADS);

    var model =
        new Matrix4f()
            .translate(new Vector3f(srctiles.x, srctiles.y, 0))
            .scale(new Vector3f(srctiles.w, srctiles.h, 1));
    int uniModel = glGetUniformLocation(shaderProgram, "model");
    var buffer = ByteBuffer.allocateDirect(16 * 4).asFloatBuffer();
    model.load(buffer);
    glUniformMatrix4(uniModel, false, buffer);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    // fixme
    GL11.glEnd();
  }

  static class SDL_Rect {
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
