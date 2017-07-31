package com

package object mesosphere {
  def loadSystemProperty(key: String): Option[String] = {
    Option(System.getProperty(key))
  }

  def saveSystemProperty(key: String, value: String): Unit = {
    val ignored = System.setProperty(key, value)
  }

  def loadDcosUriSystemProperty(): String = {
    loadSystemProperty("com.mesosphere.cosmos.dcosUri").get
  }

  def bootCosmos(): Boolean = {
    loadSystemProperty("com.mesosphere.cosmos.boot").map(_.toBoolean).getOrElse(false)
  }
}
