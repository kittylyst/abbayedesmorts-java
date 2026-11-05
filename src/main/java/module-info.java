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

  // ! Jackson reflects on the package-private records in TileAtlas to load tile-atlas.json.
  // ! Without this, any test constructing TileAtlas (e.g. abbaye.model.TestTileAtlas) fails
  // ! with an inaccessible-object exception when run on the module path.
  opens abbaye.model to
      com.fasterxml.jackson.databind;
}
