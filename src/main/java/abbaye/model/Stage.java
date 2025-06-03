/* Copyright (C) The Authors 2025 */
package abbaye.model;

import java.io.*;

/** The stage shows the layout of the furniture of the current screen */
public class Stage {
  private int[][][] stagedata;

  /** Loads stage screens from default location */
  public void load() {
    load("/data/map.txt");
  }

  /**
   * Loads stage from supplied resource
   *
   * @param mapResource
   */
  public void load(String mapResource) {
    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(Stage.class.getClassLoader().getResourceAsStream(mapResource)))) {
      String line;

      // Skip two header lines
      br.readLine();
      br.readLine();

      for (int i = 0; i <= 24; i++) {
        for (int j = 0; j <= 21; j++) {
          line = br.readLine();
          for (int k = 0; k <= 31; k++) {
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

  //        FILE *datafile = NULL;
  //        char line[129],temp[4],line2[61];
  //        temp[3] = 0;
  //
  //        /* Loading stage data file */
  //        datafile = fopen(DATADIR "/data/map.txt", "r");
  //        fgets (line, 129, datafile); /* skip header */
  //        fgets (line, 129, datafile);
  //
  //        /* Loading data into the array */
  //        for (int i=0; i<=24; i++) {
  //            for (int j=0; j<=21; j++) {
  //                for (int k=0; k<=31; k++) {
  //                    temp[0] = line[k*4];
  //                    temp[1] = line[(k*4) + 1];
  //                    temp[2] = line[(k*4) + 2];
  //                    sscanf (temp, "%d", &stagedata[i][j][k]);
  //                }
  //                fgets (line, 129, datafile);
  //            }
  //            fgets (line, 129, datafile);
  //        }
  //
  //        /* Closing file */
  //        fclose (datafile);
}
