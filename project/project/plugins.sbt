//val repo = "s3://downloads.mesosphere.io"
// val repo = "s3://kjoshi-dev.s3.us-east-1.amazonaws.com"
// resolvers ++= Seq(
//     ("Mesosphere Public Snapshot Repo (S3)" at s"${repo}/maven-snapshots"),
//     ("Mesosphere Public Releases Repo (S3)" at s"${repo}/maven"))
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.19.0")
