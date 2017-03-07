package com.mesosphere.sbt

import sbt._
import sbt.Keys._

object BuildPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    scalacOptions ++= Seq(
      "-deprecation",            // Emit warning and location for usages of deprecated APIs.
      "-encoding", "UTF-8",      // Specify character encoding used by source files.
      "-explaintypes",           // Explain type errors in more detail.
      "-feature",                // Emit warning for usages of features that should be imported explicitly.
      "-target:jvm-1.8",         // Target platform for object files.
      "-unchecked",              // Enable additional warnings where generated code depends on assumptions.
      "-Xfatal-warnings",        // Fail the compilation if there are any warnings.
      "-Xfuture",                // Turn on future language features.
      "-Xlint",                  // Enable or disable specific warnings
      "-Ywarn-adapted-args",     // Warn if an argument list is modified to match the receiver.
      "-Ywarn-dead-code",        // Warn when dead code is identified.
      "-Ywarn-inaccessible",     // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any",        // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit",     // Warn when nullary methods return Unit.
      "-Ywarn-numeric-widen",    // Warn when numerics are widened.
      "-Ywarn-unused",           // Warn when local and private vals, vars, defs, and types are unused.
      "-Ywarn-unused-import",    // Warn when imports are unused.
      "-Ywarn-value-discard"     // Warn when non-Unit expression results are unused.
    ),
    scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
    scalacOptions in (Test, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
    scalacOptions in (Compile, doc) += "-no-link-warnings"
  )

}
