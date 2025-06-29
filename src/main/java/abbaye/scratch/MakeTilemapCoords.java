/* Copyright (C) The Authors 2025 */
package abbaye.scratch;

import abbaye.AbbayeMain;
import abbaye.basic.Corners;
import abbaye.model.Stage;

public class MakeTilemapCoords {
  public static final int SCREENS_X = 5;
  public static final int SCREENS_Y = 5;
  public static final int NUM_SCREENS = SCREENS_X * SCREENS_Y;
  public static final int NUM_COLUMNS = 32;
  public static final int NUM_ROWS = 22;

  private Stage stage;

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

    @Override
    public String toString() {
      return "SDL_Rect{" + "posX=" + 8 * x + ", posY=" + 8 * y + ", w=" + w + ", h=" + h + '}';
    }
  }

  /**
   * @param room Tile index
   * @param counter Unused
   * @param changeflag Unused
   * @param changetiles 0 for 8-bit colour, 1 for 16-bit
   * @return
   */
  //    void computeScreen(int room, int[] counter, int changeflag, int changetiles) {
  //
  ////        SDL_Rect destiles = new SDL_Rect(0,0,8,8);
  //        var stagedata = stage.getScreen(room);
  //
  //        for (var coordy = 0; coordy <= 21; coordy++) {
  //            for (var coordx = 0; coordx <= 31; coordx++) {
  //                var data = stagedata[coordy][coordx];
  //                return computeTextureCoords(data, counter, changeflag, changetiles);
  //            }
  //        }
  //    }

  Corners computeTextureCoords(int data, int[] counter, int changeflag, int changetiles) {
    SDL_Rect srctiles = new SDL_Rect(0, 0, 8, 8);
    if ((data > 0) && (data != 99)) {
      //                   destiles.x = coordx * 8;
      //                    destiles.y = coordy * 8;
      if (data < 200) {
        srctiles.w = 8;
        srctiles.h = 8;
        if (data < 101) {
          srctiles.y = 0;
          if (data == 84) /* Cross brightness */ srctiles.x = (data - 1) * 8 + (counter[0] / 8 * 8);
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
        //                        if ((room == ROOM_CHURCH) && ((counter[1] > 59) && (counter[1] <
        // 71))) {
        //                            if ((data == 347) || (data == 348) || (data == 349) || (data
        // == 350)) {
        //                                destiles.x += 2;
        //                            }
        //                        }
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
      if ((data == 152) || (data == 137) || (data == 136)) {
        if (changeflag == 0) {
          srctiles.y = srctiles.y + (changetiles * 120);
          //                            SDL_RenderCopy(renderer,tiles,&srctiles,&destiles);
        }
      } else {
        srctiles.y = srctiles.y + (changetiles * 120);
        //                        SDL_RenderCopy(renderer,tiles,&srctiles,&destiles);
      }
    }
    return sdlToCoords(data, srctiles);
  }

  private Corners sdlToCoords(int tileIndex, SDL_Rect srctiles) {
    var tilesPerRow = 125;
    var tilesPerCol = 30;
    // Calculate texture coordinates for this tile in the atlas
    int tileX = tileIndex % tilesPerRow;
    int tileY = tileIndex / tilesPerRow;

    float u1 = (float) tileX / tilesPerRow;
    float v1 = (float) tileY / tilesPerCol;
    float u2 = (float) (tileX + 1) / tilesPerRow;
    float v2 = (float) (tileY + 1) / tilesPerCol;

    // Update texture coordinates in vertex buffer
    return new Corners(u1, 1 - v1, u2, 1 - v2);
  }

  private void run() {
    stage = new Stage();
    stage.load();
    //        Tile type: 7 has corners: Corners[u1=0.21875, v1=1.0, u2=0.25, v2=0.875]
    //        Tile type: 8 has corners: Corners[u1=0.25, v1=1.0, u2=0.28125, v2=0.875]
    //        Tile type: 0 has corners: Corners[u1=0.0, v1=1.0, u2=0.03125, v2=0.875]
    //        Tile type: 131 has corners: Corners[u1=0.09375, v1=0.5, u2=0.125, v2=0.375]
    //        Tile type: 132 has corners: Corners[u1=0.125, v1=0.5, u2=0.15625, v2=0.375]
    System.out.println(computeTextureCoords(7, new int[2], 0, 0));
    System.out.println(computeTextureCoords(8, new int[2], 0, 0));
    System.out.println(computeTextureCoords(0, new int[2], 0, 0));
    System.out.println(computeTextureCoords(131, new int[2], 0, 0));
    System.out.println(computeTextureCoords(132, new int[2], 0, 0));
  }

  public static void main(String[] args) {
    AbbayeMain.setGlEnabled(false);
    new MakeTilemapCoords().run();
  }
}
