package com.mesosphere.sbt

import sbt._

object Build {

  type EncodedModuleID = (String, (String, String))

  def buildInfoDecode(encodedId: EncodedModuleID): ModuleID = {
    val (organization, (name, revision)) = encodedId
    organization % name % revision
  }

}
