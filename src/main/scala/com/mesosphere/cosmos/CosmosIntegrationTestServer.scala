package com.mesosphere.cosmos

import java.io.OutputStream
import java.io.PrintStream
import java.net.URL
import java.util.Properties
import javassist.CannotCompileException
import org.apache.curator.test.TestingCluster
import sbt._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.TimeoutException
import scala.concurrent.duration._
import scala.util.Random

final class CosmosIntegrationTestServer(
  javaHome: Option[String],
  classpathPrefix: Seq[File],
  oneJarPath: File,
  additionalProperties: List[TestProperty]
) {
  private val originalProperties: Properties = System.getProperties
  private var process: Option[Process] = None           // scalastyle:ignore var.field
  private var zkCluster: Option[TestingCluster] = None  // scalastyle:ignore var.field

  def setup(logger: Logger): Unit = {
    initZkCluster(logger)
    val cmd = createServerCommand()
    runProcess(logger, cmd)
  }

  def runProcess(logger: Logger, cmd: Seq[String]): Unit = {
    logger.info("Starting cosmos with command: " + cmd.mkString(" "))

    val prefix = "<<cosmos-server>> "
    val run = Process(cmd).run(new ProcessLogger() {
      override def buffer[T](f: => T): T = logger.buffer(f)

      override def error(s: => String): Unit = logger.info(prefix + s)

      override def info(s: => String): Unit = logger.info(prefix + s)
    })
    val fExitValue = Future(run.exitValue())
    process = Some(run)
    try {
      waitUntilTrue(60.seconds) {
        if (fExitValue.isCompleted) {
          throw new IllegalStateException("Cosmos Server has terminated.")
        }
        canConnectTo(logger, new URL("http://localhost:9990/admin/ping"))
      }
    } catch {
      case t: Throwable =>
        cleanup()
        throw t
    }
  }

  def createServerCommand(): Seq[String] = {
    val zkUri = zkCluster.map { c =>
      val connectString = c.getConnectString
      val zNodeNameSize = 10
      val baseZNode = Random.alphanumeric.take(zNodeNameSize).mkString
      s"zk://$connectString/$baseZNode"  // scalastyle:ignore multiple.string.literals
    }.get

    val java = javaHome
      .map(_ + "/bin/java")
      .orElse(systemProperty("java.home").map(jre => s"$jre/bin/java"))
      .getOrElse("java")

    val dcosUri = systemProperty("com.mesosphere.cosmos.dcosUri").get
    val propertiesMap = additionalProperties.map { testProperty =>
      testProperty -> systemProperty(testProperty.propertyName).get
    }

    val uriKey = "uri"
    setClientProperty("CosmosClient", uriKey, "http://localhost:7070")
    setClientProperty("ZooKeeperClient", uriKey, zkUri)

    propertiesMap.foreach { case (testProperty, value) =>
      setClientProperty(testProperty.clientName, testProperty.clientKey, value)
    }

    val pathSeparator = System.getProperty("path.separator")
    val classpath =
      s"${classpathPrefix.map(_.getCanonicalPath).mkString("", pathSeparator, pathSeparator)}" +
        s"${oneJarPath.getCanonicalPath}"

    Seq(
      java,
      "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
      "-classpath",
      classpath,
      "com.simontuffs.onejar.Boot",
      s"-com.mesosphere.cosmos.zookeeperUri=$zkUri",
      s"-com.mesosphere.cosmos.dcosUri=$dcosUri"
    ) ++ propertiesMap.map { case (testProperty, value) => s"-${testProperty.propertyName}=$value" }
  }

  def initZkCluster(logger: Logger): Unit = {
    try {
      initCuratorTestJavassist()
      val cluster = new TestingCluster(1)
      cluster.start()
      zkCluster = Some(cluster)
    } catch {
      case _: CannotCompileException =>
      // ignore, this appears to be thrown by some runtime bytecode stuff that
      // doesn't actually seem to break things
      case t: Throwable =>
        logger.info(s"caught throwable: ${t.toString}")
    }
  }

  def cleanup(): Unit = {
    System.setProperties(originalProperties)
    process.foreach(_.destroy())
    zkCluster.foreach(_.close())
  }

  private[this] def setClientProperty(clientName: String, key: String, value: String): Unit = {
    val property = s"com.mesosphere.cosmos.test.CosmosIntegrationTestClient.$clientName.$key"
    val ignored = System.setProperty(property, value)
  }

  private[this] def systemProperty(key: String): Option[String] = {
    Option(System.getProperty(key))
  }

  /**
   * True if a connection could be made to the given URL
   */
  private[this] def canConnectTo(logger: Logger, url: URL): Boolean = {
    try {
      logger.info("waiting for url: " + url)
      url.openConnection()
        .getInputStream
        .close()
      true
    } catch {
      case _: Exception => false
    }
  }

  /**
   * Polls the given action until it returns true, or throws a TimeoutException
   * if it does not do so within 'timeout'
   */
  private[this] def waitUntilTrue(timeout: Duration)(action: => Boolean): Unit = {
    val startTimeMillis = System.currentTimeMillis()

    @annotation.tailrec
    def loop(): Unit = {
      if (!action) {
        val elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
        if (elapsedTimeMillis.millis > timeout) {
          throw new TimeoutException()
        }
        val sleepTime = 1000L
        Thread.sleep(sleepTime)
        loop()
      }
    }

    loop()
  }

  /**
   * This is being done to work around the fact that ByteCodeRewrite has a static {} block in its
   * class that logs an exception to stdout via e.printStackTrace
   */
  private[this] def initCuratorTestJavassist(): Unit = {
    val origStdOut = System.out
    val origStdErr = System.err
    // noinspection ConvertExpressionToSAM
    val devNull = new PrintStream(new OutputStream {
      override def write(b: Int): Unit = { /* do Nothing */ }
    })
    System.setOut(devNull)
    System.setErr(devNull)
    org.apache.curator.test.ByteCodeRewrite.apply()
    System.setOut(origStdOut)
    System.setErr(origStdErr)
  }
}
