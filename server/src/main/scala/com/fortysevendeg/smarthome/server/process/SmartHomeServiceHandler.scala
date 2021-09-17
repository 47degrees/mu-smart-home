package com.fortysevendeg.smarthome.server.process

import cats.effect._
import cats.syntax.apply._
import cats.syntax.functor._
import com.fortysevendeg.smarthome.protocol.messages._
import com.fortysevendeg.smarthome.protocol.services._
import fs2._
import org.typelevel.log4cats.Logger

class SmartHomeServiceHandler[F[_]: Async: Logger: Timer: TemperatureReader: SmartHomeSupervisor]
    extends SmartHomeService[F] {
  val serviceName = "SmartHomeService"

  override def isEmpty(request: IsEmptyRequest): F[IsEmptyResponse] =
    Logger[F].info(s"$serviceName - Request: $request").as(IsEmptyResponse(true))

  override def getTemperature(empty: IsEmptyRequest): Stream[F, Temperature] =
    for {
      _            <- Stream.eval(Logger[F].info(s"$serviceName - getTemperature Request"))
      temperatures <- TemperatureReader[F].sendSamples.take(20)
    } yield temperatures

  override def comingBackMode(request: Stream[F, Location]): Stream[F, ComingBackModeResponse] =
    for {
      _        <- Stream.eval(Logger[F].info(s"$serviceName - Enabling Coming Back Home mode"))
      location <- request
      response <- Stream.eval(
        (if (location.distanceToDestination > 0.0d)
           Logger[F]
             .info(s"$serviceName - Distance to destination: ${location.distanceToDestination} mi")
         else
           Logger[F]
             .info(s"$serviceName - You have reached your destination 🏡")) *> SmartHomeSupervisor[
          F
        ].performAction(location).map(ComingBackModeResponse)
      )
    } yield response
}
