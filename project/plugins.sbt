import com.mesosphere.sbt.Deps

// Use the plugin in its own build
unmanagedSourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "scala"

// BuildPlugin needs to import definitions from these plugins
Deps.plugins.map(addSbtPlugin)
