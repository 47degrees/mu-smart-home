package com.fortysevendeg.smarthome.server
package common

import java.sql.Timestamp

final case class SmartHomeServerConfig(name: String, host: String, port: Int, topic: String, project: String)

case class Row(timestamp: Timestamp, lat: Double, lon: Double)
