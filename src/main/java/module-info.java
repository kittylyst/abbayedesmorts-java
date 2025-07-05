module abbaye {
  requires java.logging;
  requires java.desktop;
  requires jdk.unsupported;
  requires org.lwjgl;
  requires org.lwjgl.glfw;
  requires org.lwjgl.opengl;
  requires org.lwjgl.stb;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;

  exports abbaye;
  exports abbaye.graphics;
}
