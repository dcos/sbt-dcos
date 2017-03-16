import com.mesosphere.sbt.TransitivePlugins

lazy val metaroot = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](
      // Encode the plugin module ID in BuildInfo to avoid duplication
      "pluginModuleInfo" -> TransitivePlugins.moduleIds.map { id =>
        // Need to use nested Tuple2s; sbt-buildinfo doesn't handle arbitrary TupleNs
        (id.organization, (id.name, id.revision))
      }
    ),
    buildInfoPackage := "metabuild"
  )
