/* Copyright (C) The Authors 2025 */
package abbaye.model;

public class MapUtils {

  private MapUtils() {}

  private static Stage stage;

  public static synchronized Stage getStage() {
    if (stage == null) {
      stage = new Stage();
      stage.load();
    }
    return stage;
  }

  //    void loaddata(uint stagedata[][22][32],int enemydata[][7][15]) {
  //
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
  //
  //        /* Loading enemies data file */
  //        datafile = fopen(DATADIR "/data/enemies.txt", "r");
  //        fgets (line2, 61, datafile);  /* skip header */
  //        fgets (line2, 61, datafile);
  //
  //        /* Loading data into the array */
  //        for (int i=0; i<=24; i++) {
  //            for (int j=0; j<7; j++) {
  //                for (int k=0; k<15; k++) {
  //                    temp[0] = line2[k*4];
  //                    temp[1] = line2[(k*4) + 1];
  //                    temp[2] = line2[(k*4) + 2];
  //                    sscanf (temp, "%d", &enemydata[i][j][k]);
  //                }
  //                fgets (line2, 61, datafile);
  //            }
  //            fgets (line2, 61, datafile);
  //        }
  //
  //        /* Closing file */
  //        fclose (datafile);
  //
  //    }

}
