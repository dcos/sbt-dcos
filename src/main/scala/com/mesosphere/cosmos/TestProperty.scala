package com.mesosphere.cosmos

/**
 * Specifies how to wire a system property into both the server under test and the test client.
 *
 * @param clientName - the name of the test client that makes use of the property value.
 * @param name - the short name of the system property. The full name has "com.mesosphere.cosmos"
 *             prepended.
 */
case class TestProperty(clientName: String, name: String) {

  def propertyName: String = s"com.mesosphere.cosmos.$name"

}
