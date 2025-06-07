/* Copyright (C) The Authors 2025 */
package abbaye;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;

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
import org.lwjgl.opengl.GL;
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
  private long window;

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

  void init() {
    try {
      //      GLFWErrorCallback.createPrint(System.err).set();

      // Initialize GLFW. Most GLFW functions will not work before doing this.
      if (!glfwInit()) {
        throw new IllegalStateException("Unable to initialize GLFW");
      }

      createWindow();
      glfwShowWindow(window);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    //    initGL();
    Clock.init();
    Clock.updateTimer();

    gameDialog = new GameDialog(null, this);
    initLayer();
    stage.load();
  }

  /** Main game loop method */
  public void run() {
    try {
      while (!done) {
        //        Keyboard.poll();

        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
          done = true;
        }
        if (glfwWindowShouldClose(window)) {
          done = true;
        }
        Clock.updateTimer();

        // Update Layers
        if (!gameDialog.isActive()) {
          layer.update();
        }

        // Now Render
        render();
        //        Display.update();
        glfwPollEvents();
        glfwSwapBuffers(window);
      }
      cleanup();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
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

  //  private void initGL() {
  //    GL11.glEnable(GL11.GL_TEXTURE_2D);
  //    GL11.glShadeModel(GL11.GL_SMOOTH);
  //    GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
  //    GL11.glClearDepth(1.0f);
  //
  //    var config = Config.config();
  //    GL11.glMatrixMode(GL11.GL_PROJECTION);
  //    GL11.glLoadIdentity();
  //    GL11.glOrtho(0, config.getScreenWidth(), config.getScreenHeight(), 0, -100, 100);
  //    GL11.glMatrixMode(GL11.GL_MODELVIEW);
  //    GL11.glDisable(GL11.GL_DEPTH_TEST);
  //  }

  private void createWindow() throws Exception {
    var config = Config.config();

    glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    window = glfwCreateWindow(config.getScreenWidth(), config.getScreenHeight(), windowTitle, 0, 0);

    glfwMakeContextCurrent(window);
    GL.createCapabilities();
  }

  private void render() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glLoadIdentity();
    int[] counters = {0, 0, 0};

    stage.render(counters, false, 0);
    layer.render();

    gameDialog.render();
  }

  private void cleanup() {
    //    Keyboard.destroy();
    //    Display.destroy();
    glfwDestroyWindow(window);
  }

  public long getWindow() {
    return window;
  }
}
