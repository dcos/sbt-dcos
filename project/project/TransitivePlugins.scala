package com.mesosphere.sbt

import sbt._

object TransitivePlugins {

  // The plugins that BuildPlugin will provide to builds where it is used
  val moduleIds: Seq[ModuleID] = Seq(
    "org.scala-sbt.plugins" % "sbt-onejar" % "0.8",
    "org.scoverage" % "sbt-scoverage" % "1.5.0"
  )

}
