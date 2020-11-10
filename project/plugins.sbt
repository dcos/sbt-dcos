import com.mesosphere.sbt.Deps

// Use the plugin in its own build
unmanagedSourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "scala"

// Needed for running our Scalastyle task on ourselves
unmanagedResourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "resources"

// BuildPlugin needs to import definitions from these plugins
Deps.plugins.map(addSbtPlugin)

//val repo = "s3://downloads.mesosphere.io"
// val repo = "s3://kjoshi-dev.s3.us-east-1.amazonaws.com"
// resolvers ++= Seq(
//     ("Mesosphere Public Snapshot Repo (S3)" at s"${repo}/maven-snapshots"),
//     ("Mesosphere Public Releases Repo (S3)" at s"${repo}/maven"))
