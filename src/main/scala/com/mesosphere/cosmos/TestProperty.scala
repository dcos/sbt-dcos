package com.mesosphere.cosmos

/**
 * Specifies how to wire a system property into both the server under test and the test client.
 *
 * @param name - the short name of the system property. The full name has "com.mesosphere.cosmos"
 *             prepended.
 * @param clientName - the name of the test client that makes use of the property value.
 * @param clientKey - the unique name that the test client will use to reference this property.
 */
case class TestProperty(name: String, clientName: String, clientKey: String) {

  def propertyName: String = s"com.mesosphere.cosmos.$name"

}
