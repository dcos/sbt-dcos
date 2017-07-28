package com.mesosphere.cosmos

/**
 * Specifies how to wire a system property for the server under test.
 *
 * @param name - the short name of the system property. The full name has "com.mesosphere.cosmos"
 *             prepended.
 */
case class TestProperty(clientName: String, name: String) {

  def propertyName: String = s"com.mesosphere.cosmos.$name"

}
