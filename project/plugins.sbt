// Use the plugin in its own build
unmanagedSourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "scala"
