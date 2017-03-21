package com.mesosphere.sbt

import com.github.retronym.SbtOneJar._
import com.mesosphere.cosmos.CosmosIntegrationTestServer
import sbt._
import sbt.Keys._
import scala.Ordering.Implicits._
import scoverage.ScoverageKeys._

object BuildPlugin extends AutoPlugin {

  private val teamcityVersion: Option[String] = sys.env.get("TEAMCITY_VERSION")

  private val parsedScalaVersion: SettingKey[List[Int]] =
    settingKey("The project's Scala version, parsed into a list of version numbers")

  private val supportedJvmVersion: SettingKey[String] =
    settingKey("The JVM version required by this project")

  override def trigger: PluginTrigger = allRequirements

  override def projectConfigurations: Seq[Configuration] = Seq(IntegrationTest extend Test)

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    parsedScalaVersion := scalaVersion.value.split('.').toList.map(_.toInt),
    supportedJvmVersion := { if (parsedScalaVersion.value < List(2, 11, 5)) "1.7" else "1.8" },

    javacOptions in Compile ++= Seq(
      "-source", supportedJvmVersion.value,
      "-target", supportedJvmVersion.value,
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),

    scalacOptions ++= {
      val targetJvm = s"-target:jvm-${supportedJvmVersion.value}"

      val commonOptions = Seq(
        "-deprecation",            // Emit warning and location for usages of deprecated APIs.
        "-encoding", "UTF-8",      // Specify character encoding used by source files.
        "-explaintypes",           // Explain type errors in more detail.
        "-feature",                // Emit warning for usages of features that should be imported explicitly.
        targetJvm,                 // Target platform for object files.
        "-unchecked",              // Enable additional warnings where generated code depends on assumptions.
        "-Xfatal-warnings",        // Fail the compilation if there are any warnings.
        "-Xfuture",                // Turn on future language features.
        "-Xlint",                  // Enable or disable specific warnings
        "-Ywarn-adapted-args",     // Warn if an argument list is modified to match the receiver.
        "-Ywarn-dead-code",        // Warn when dead code is identified.
        "-Ywarn-inaccessible",     // Warn about inaccessible types in method signatures.
        "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
        "-Ywarn-nullary-unit",     // Warn when nullary methods return Unit.
        "-Ywarn-numeric-widen",    // Warn when numerics are widened.
        "-Ywarn-value-discard"     // Warn when non-Unit expression results are unused.
      )

      val twoElevenOptions = Seq(
        "-Ywarn-infer-any",        // Warn when a type argument is inferred to be `Any`.
        "-Ywarn-unused",           // Warn when local and private vals, vars, defs, and types are unused.
        "-Ywarn-unused-import"     // Warn when imports are unused.
      )

      commonOptions ++ (if (parsedScalaVersion.value < List(2, 11)) Seq.empty else twoElevenOptions)
    },

    scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
    scalacOptions in (Test, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
    scalacOptions in (IntegrationTest, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
    scalacOptions in (Compile, doc) += "-no-link-warnings",

    coverageOutputTeamCity := teamcityVersion.isDefined,
    cancelable in Global := true,

    initialize in LocalRootProject := {
      val _ = initialize.value
      teamcityVersion.foreach { _ =>
        // add some info into the teamcity build context so that they can be used by later steps
        reportTeamCityParameter("SCALA_VERSION", scalaVersion.value)
        reportTeamCityParameter("PROJECT_VERSION", version.value)
      }
    }
  ) ++ Defaults.itSettings ++ Scalastyle.settings

  /** These should be added to the subproject containing the main class. */
  def itSettings(mainClassName: String): Seq[Def.Setting[_]] = {
    oneJarSettings ++ Seq(
      mainClass in oneJar := Some(mainClassName),
      testOptions in IntegrationTest ++= {
        lazy val itServer = new CosmosIntegrationTestServer(
          (javaHome in run).value.map(_.getCanonicalPath),
          (resourceDirectories in IntegrationTest).value,
          oneJar.value
        )

        Seq(
          Tests.Setup(() => itServer.setup((streams in runMain).value.log)),
          Tests.Cleanup(() => itServer.cleanup())
        )
      }
    )
  }

  lazy val publishSettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value) {
        Some("snapshots" at nexus + "content/repositories/snapshots")
      } else {
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
      }
    }
  )

  private def reportTeamCityParameter(key: String, value: String): Unit = {
    println(s"##teamcity[setParameter name='env.SBT_$key' value='$value']")
    println(s"##teamcity[setParameter name='system.sbt.$key' value='$value']")
  }

}
