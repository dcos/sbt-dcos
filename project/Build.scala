package com.mesosphere.sbt

import sbt._

object Build {

  type EncodedArtifactID = (String, Boolean)
  type EncodedModuleID = (String, (EncodedArtifactID, String))

  def buildInfoDecode(encodedId: EncodedModuleID): ModuleID = {
    val (organization, (artifactId, revision)) = encodedId
    val (name, isBinaryCrossVersion) = artifactId
    if (isBinaryCrossVersion) organization %% name % revision else organization % name % revision
  }

}
