/* Copyright (C) The Authors 2025 */
package abbaye;

import abbaye.model.Enemy;
import abbaye.model.Player;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Optional;

public class AbbayeMain {
  private static ObjectMapper mapper;
  private static volatile boolean glEnabled = true;

  private boolean fullscreen = false;
  private final String windowTitle = "Abbaye Des Mortes";
  //    private Layer layers[] = new Layer[2];
  private GameDialog gameDialog;

  public static boolean isGlEnabled() {
    return glEnabled;
  }

  public static void setGlEnabled(boolean glEnabled) {
    AbbayeMain.glEnabled = glEnabled;
  }

  public static synchronized ObjectMapper getMapper() {
    if (mapper != null) {
      return mapper;
    }
    final ObjectMapper newMapper = new ObjectMapper();
    newMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    SimpleModule simpleModule =
        new SimpleModule("SimpleModule", new Version(1, 0, 0, null, "abbaye", "game-state"));
    simpleModule.addSerializer(Player.class, new Player.PlayerSerializer());
    simpleModule.addSerializer(Enemy.class, new Enemy.EnemySerializer());

    newMapper.registerModule(simpleModule);
    mapper = newMapper;
    return mapper;
  }

  public static void main(String[] args) {
    Optional<String> oPath = Optional.empty();
    if (args.length > 0) {
      oPath = Optional.of(args[0]);
    }

    var main = new AbbayeMain();
    // Currently, should always be false but smaller devices may want
    // fullscreen to be true
    //        main.fullscreen = Config.config(oPath).getFullscreen();
    main.init();
    main.run();
  }

  void run() {}

  void init() {}
}
