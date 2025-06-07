module abbaye {
  requires java.logging;
  requires java.desktop;
  requires jdk.unsupported;
  requires org.lwjgl;
  requires org.lwjgl.glfw;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires org.lwjgl.opengl;

  exports abbaye;
}
