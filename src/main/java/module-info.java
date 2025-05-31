module abbaye {
  requires java.logging;
  requires java.desktop;
  requires jdk.unsupported;
  requires lwjgl;
  requires lwjgl.util;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;

  exports abbaye;
}
