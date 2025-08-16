/* Copyright (C) The Authors 2025 */
package abbaye;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import abbaye.basic.Clock;
import abbaye.model.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.nio.IntBuffer;
import java.util.Optional;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

// Must be run with -Djava.awt.headless=true
public class AbbayeMain {
  private static ObjectMapper mapper;
  private static volatile boolean glEnabled = true;

  private boolean fullscreen = false;
  private final String windowTitle = "Abbaye Des Mortes";
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

  public static final GLFWKeyCallbackI ESC_QUITS_GAME =
      (w, key, scancode, action, mods) -> {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
          glfwSetWindowShouldClose(w, true);
        }
      };

  public static void main(String[] args) {
    Optional<String> oPath = Optional.empty();
    if (args.length > 0) {
      oPath = Optional.of(args[0]);
    }

    var main = new AbbayeMain();
    // Currently, should always be false but smaller devices may want
    // fullscreen to be true
    //        main.fullscreen = Config.config(oPath).getFullscreen();
    main.run();
  }

  public void run() {
    init();
    loop();
    cleanup();
  }

  void init() {
    try {
      glStaticInit();
      glInit();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    gameDialog = new GameDialog(null, this);
    glfwSetKeyCallback(
        window,
        (w, key, scancode, action, mods) -> {
          if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(w, true);
          }
          if ((key == GLFW_KEY_TAB || key == GLFW_KEY_SPACE) && action == GLFW_RELEASE) {
            gameDialog.startTurn();
          }
        });

    initLayer();
    Clock.init();
    Clock.updateTimer();
  }

  public static void glStaticInit() {
    GLFWErrorCallback.createPrint(System.err).set();

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
  }

  public void glInit() {
    var config = Config.config();
    var width = config.getScreenWidth();
    var height = config.getScreenHeight();
    window = glfwCreateWindow(width, height, windowTitle, NULL, NULL);
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    glfwSetKeyCallback(
        window,
        (window, key, scancode, action, mods) -> {
          if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(window, true);
          }
        });

    try (MemoryStack stack = stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);

      glfwGetWindowSize(window, pWidth, pHeight);

      GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

      glfwSetWindowPos(
          window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
    } catch (Exception e) {
      e.printStackTrace();
    }

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);

    GL.createCapabilities();

    // Set viewport
    glViewport(0, 0, width, height);

    // Enable textures and blending
    glEnable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
  }

  /** Main game loop method */
  public void loop() {
    try {
      while (!glfwWindowShouldClose(window)) {
        Clock.updateTimer();

        if (!gameDialog.isActive()) {
          layer.update();
        }
        glClear(GL_COLOR_BUFFER_BIT);

        if (gameDialog.isActive()) {
          gameDialog.render();
        } else {
          layer.render();
        }

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
    var stage = new Stage();
    stage.load(window);

    var p = Player.of(layer, stage);
    p.init();

    var status = StatusDisplay.of(p);
    status.init();

    layer.setPlayer(p);
    layer.setStage(stage);
    layer.setStatus(status);
    layer.init();

    gameDialog.setPlayer(p);
  }

  private void cleanup() {
    layer.cleanup();
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);
    glfwTerminate();
    glfwSetErrorCallback(null);
  }

  public long getWindow() {
    return window;
  }

  //  public Stage getStage() {
  //    return stage;
  //  }

  public Layer getLayer() {
    return layer;
  }
}
