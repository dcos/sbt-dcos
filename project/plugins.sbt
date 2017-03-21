import com.mesosphere.sbt.Deps

// Use the plugin in its own build
unmanagedSourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "scala"

// Needed for running our Scalastyle task on ourselves
unmanagedResourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "resources"

// BuildPlugin needs to import definitions from these plugins
Deps.plugins.map(addSbtPlugin)
