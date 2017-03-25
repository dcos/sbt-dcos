import com.mesosphere.sbt.Deps

lazy val metaroot = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](
      // Encode the plugin and library module IDs in BuildInfo to avoid duplication
      "plugins" -> Deps.plugins.map(Deps.buildInfoEncode),
      "libraries" -> Deps.libraries.map(Deps.buildInfoEncode)
    ),
    buildInfoPackage := "metabuild",

    libraryDependencies ++= Deps.libraries
  )
