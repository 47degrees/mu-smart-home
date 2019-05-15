package com.fortysevendeg.smarthome.server
package common

import java.time.Instant

final case class SmartHomeServerConfig(name: String,
                                       host: String,
                                       port: Int,
                                       pubsub: PubSubConfig
                                      )

case class PubSubConfig(topic: String,
                        project: String,
                        batchSize: Int,
                        delayThreshold: Int
                       )

case class Row(timestamp: Instant, lat: Double, long: Double)
