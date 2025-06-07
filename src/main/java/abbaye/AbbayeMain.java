/* Copyright (C) The Authors 2025 */
package abbaye;

import abbaye.basic.Clock;
import abbaye.basic.OGLFont;
import abbaye.model.Enemy;
import abbaye.model.Layer;
import abbaye.model.Player;
import abbaye.model.Stage;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Optional;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class AbbayeMain {
  private static ObjectMapper mapper;
  private static volatile boolean glEnabled = true;

  private volatile boolean done = false;
  private boolean fullscreen = false;
  private final String windowTitle = "Abbaye Des Mortes";
  private Stage stage = new Stage();
  private Layer layer = new Layer();
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

  /** Main game loop method */
  public void run() {
    try {
      while (!done) {
        Keyboard.poll();

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
          done = true;
        }
        if (Display.isCloseRequested()) {
          done = true;
        }
        Clock.updateTimer();

        // Update Layers
        if (!gameDialog.isActive()) {
          layer.update();
        }

        // Now Render
        render();
        Display.update();
      }
      cleanup();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
  }

  void init() {
    try {
      createWindow();
      Keyboard.create();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    initGL();
    Clock.init();
    Clock.updateTimer();

    gameDialog = new GameDialog(null, this);
    initLayer();
    stage.load();
  }

  void initLayer() {
    var font = new OGLFont();
    font.buildFont("Courier New", 24);

    // Layer 0 is the background starfield
    //    layer[0] = new Layer();
    //    layer[0].add(new TextureMap());
    //    layer[0].init();

    Player p = Player.of(layer, gameDialog);
    p.setFont(font);
    layer.setPlayer(p);
    layer.init();

    gameDialog.setPlayer(p);
    gameDialog.setFont(font);
    //    gameDialog.reset();
  }

  private void initGL() {
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glShadeModel(GL11.GL_SMOOTH);
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
    GL11.glClearDepth(1.0f);

    var config = Config.config();
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glLoadIdentity();
    GL11.glOrtho(0, config.getScreenWidth(), config.getScreenHeight(), 0, -100, 100);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glDisable(GL11.GL_DEPTH_TEST);
  }

  private void createWindow() throws Exception {
    Display.setTitle(windowTitle);
    Display.setLocation(0, 0);
    Display.setFullscreen(false);
    Display.create(); // windowTitle, 10, 10, 640, 480, 16, 0, 8, 0);
  }

  private void render() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glLoadIdentity();
    int[] counters = {0, 0, 0};

    stage.render(counters, false, 0);
    layer.render();

    gameDialog.render();
  }

  private static void cleanup() {
    Keyboard.destroy();
    Display.destroy();
  }
}
