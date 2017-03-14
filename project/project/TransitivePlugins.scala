package com.mesosphere.sbt

import sbt._

object TransitivePlugins {

  // The plugins that BuildPlugin will provide to builds where it is used
  val moduleIds: Seq[ModuleID] = Seq(
    "org.scoverage" % "sbt-scoverage" % "1.5.0"
  )

}
