package com.mesosphere.sbt

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.nio.charset.StandardCharsets
import org.scalastyle.Directory
import org.scalastyle.EndFile
import org.scalastyle.EndWork
import org.scalastyle.ErrorLevel
import org.scalastyle.FileSpec
import org.scalastyle.InfoLevel
import org.scalastyle.Message
import org.scalastyle.MessageHelper
import org.scalastyle.Output
import org.scalastyle.ScalastyleChecker
import org.scalastyle.ScalastyleConfiguration
import org.scalastyle.StartFile
import org.scalastyle.StartWork
import org.scalastyle.StyleError
import org.scalastyle.StyleException
import org.scalastyle.WarningLevel
import org.scalastyle.XmlOutput
import sbt._
import sbt.Keys._

object Scalastyle {

  val scalastyle: TaskKey[Unit] = taskKey("Check sources with Scalastyle")

  val scalastyleConfig: SettingKey[Option[File]] =
    settingKey(
      "The location of the Scalastyle configuration file. Set to None to use the default config."
    )

  private val genericSettings: Seq[Def.Setting[_]] = Seq(
    scalastyle := {
      val configString = scalastyleConfig.value match {
        case Some(configFile) =>
          IO.read(configFile, StandardCharsets.UTF_8)
        case _ =>
          val configFileName = "scalastyle-config.xml"
          val resourceName = s"/$configFileName"  // scalastyle:ignore multiple.string.literals
          val configInputStream = Option(getClass.getResourceAsStream(resourceName))

          configInputStream match {
            case Some(is) => IO.readStream(is, StandardCharsets.UTF_8)
            case _ => sys.error(s"Scalastyle config file is not on the classpath: $configFileName")
          }
      }

      val checker = new ScalastyleChecker[FileSpec]()
      val config = ScalastyleConfiguration.readFromString(configString)
      val sourceFiles = Directory.getFiles(files = sourceDirectories.value, encoding = None)
      val messages = checker.checkFiles(config, sourceFiles)

      val messageConfig = ConfigFactory.load(checker.getClass.getClassLoader)
      val outputFile = target.value / s"scalastyle-${configuration.value.name}-result.xml"
      XmlOutput.save(messageConfig, outputFile, StandardCharsets.UTF_8.toString, messages)

      val logger = streams.value.log
      val output = new ScalastyleOutput(messageConfig, logger)
      val result = output.output(messages)

      logger.info(s"Processed ${result.files} files")
      logger.info(
        s"Found ${result.errors} errors, ${result.warnings} warnings, and ${result.infos} infos"
      )
      logger.success(s"Created output: $outputFile")

      if (result.errors > 0 || result.warnings > 0) {
        sys.error("Errors and/or warnings exist")
      }
    }
  )

  val settings: Seq[Def.Setting[_]] = Seq(
//    scalastyle in (This, Global, This) := {
//      val ignore1 = (scalastyle in Compile).value
//      val ignore2 = (scalastyle in Test).value
//      (scalastyle in IntegrationTest).value
//    },
    scalastyleConfig in Global := None
  ) ++ Seq(Compile, Test, IntegrationTest).flatMap(inConfig(_)(genericSettings))

}

private final class ScalastyleOutput(config: Config, logger: Logger) extends Output[FileSpec] {

  private val messageHelper: MessageHelper = new MessageHelper(config)

  override def message(m: Message[FileSpec]): Unit = {
    m match {
      case StartWork() =>
        logger.verbose("Starting scalastyle")
      case EndWork() =>
        // Do nothing
      case StartFile(file) =>
        logger.verbose(s"Start file $file")
      case EndFile(file) =>
        logger.verbose(s"End file $file")
      case StyleError(file, clazz, key, level, args, line, column, customMessage) =>
        val message = Output.findMessage(messageHelper, key, args, customMessage)
        val fullMessage = s"${location(file, line, column)}: $message"

        level match {
          case ErrorLevel => logger.error(fullMessage)
          case WarningLevel => logger.warn(fullMessage)
          case InfoLevel => logger.info(fullMessage)
        }
      case StyleException(file, clazz, message, stacktrace, line, column) =>
        logger.error(s"${location(file, line, column)}: $message")
    }
  }

  private def location(file: FileSpec, line: Option[Int], column: Option[Int]): String = {
    val columnString = column.fold("")(":" + _)
    val lineString = line.fold("")(n => s":$n$columnString")
    file.name + lineString
  }

}
