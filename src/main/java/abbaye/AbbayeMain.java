/* Copyright (C) The Authors 2025 */
package abbaye;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import abbaye.basic.Clock;
import abbaye.graphics.OGLFont;
import abbaye.graphics.StageRenderer;
import abbaye.model.Enemy;
import abbaye.model.Layer;
import abbaye.model.Player;
import abbaye.model.Stage;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.nio.IntBuffer;
import java.util.Optional;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

public class AbbayeMain {
  private static ObjectMapper mapper;
  private static volatile boolean glEnabled = true;

  private boolean fullscreen = false;
  private final String windowTitle = "Abbaye Des Mortes";
  private Stage stage = new Stage();
  private Layer layer = new Layer();
  private GameDialog gameDialog;
  private long window;
  private StageRenderer renderer;

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
      GLFWErrorCallback.createPrint(System.err).set();

      // Initialize GLFW. Most GLFW functions will not work before doing this.
      if (!glfwInit()) {
        throw new IllegalStateException("Unable to initialize GLFW");
      }

      var config = Config.config();

      glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
      glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
      glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
      glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
      window =
          glfwCreateWindow(config.getScreenWidth(), config.getScreenHeight(), windowTitle, 0, 0);

      glfwMakeContextCurrent(window);
      if (window == NULL) {
        throw new RuntimeException("Failed to create the GLFW window");
      }

      glfwSetKeyCallback(
          window,
          (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
              System.out.println("ESC Released");
              glfwSetWindowShouldClose(window, true);
            }
          });

      spawnInitialWindow();

      glfwMakeContextCurrent(window);
      glfwSwapInterval(1);
      glfwShowWindow(window);

      GL.createCapabilities();

      glEnable(GL_BLEND);
      glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    stage.load();
    renderer = new StageRenderer();
    renderer.init();

    Clock.init();
    Clock.updateTimer();

    gameDialog = new GameDialog(null, this);
    initLayer();
  }

  private void spawnInitialWindow() {
    try (MemoryStack stack = stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);

      glfwGetWindowSize(window, pWidth, pHeight);

      GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

      glfwSetWindowPos(
          window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
    }
  }

  /** Main game loop method */
  public void run() {
    try {
      while (!glfwWindowShouldClose(window)) {
        Clock.updateTimer();

        // Update Layers
        if (!gameDialog.isActive()) {
          layer.update();
        }

        // Now Render
        render();

        glfwSwapBuffers(window);
        glfwPollEvents();
      }
      cleanup();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
  }

  void initLayer() {
    var font = new OGLFont();
    //    font.buildFont("Courier New", 24);

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

  public void render() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Update viewport
    try (MemoryStack stack = stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      glfwGetFramebufferSize(window, width, height);
      glViewport(0, 0, width.get(0), height.get(0));

      renderer.render(stage, width.get(0), height.get(0));
    }
    //    layer.render();
    //
    //    gameDialog.render();
  }

  private void cleanup() {
    renderer.cleanup();
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }

  public long getWindow() {
    return window;
  }
}
