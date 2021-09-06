package com.fortysevendeg.smarthome.server.process

import cats.effect._
import cats.syntax.flatMap._
import com.fortysevendeg.smarthome.protocol.messages._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.duration._
import scala.math.BigDecimal.RoundingMode
import scala.util.Random
import cats.effect.Temporal

trait TemperatureReader[F[_]] {
  def sendSamples: Stream[F, Temperature]
}

object TemperatureReader {
  implicit def instance[F[_]: Sync: Logger: Temporal]: TemperatureReader[F] =
    new TemperatureReader[F] {
      val seed = Temperature(77d, Some(TemperatureUnit("Fahrenheit")))

      def readTemperature(current: Temperature): F[Temperature] =
        Temporal[F]
          .sleep(1.second)
          .flatMap(_ =>
            Sync[F].delay {
              val increment: Double = Random.nextDouble() / 2d
              val signal            = if (Random.nextBoolean()) 1 else -1
              val currentValue      = current.value

              current.copy(
                value = BigDecimal(currentValue + (signal * increment))
                  .setScale(2, RoundingMode.HALF_UP)
                  .doubleValue
              )
            }
          )

      override def sendSamples: Stream[F, Temperature] =
        Stream.iterateEval(seed) { t =>
          Logger[F].info(s"* New Temperature 👍  --> $t").flatMap(_ => readTemperature(t))
        }
    }

  def apply[F[_]](implicit ev: TemperatureReader[F]): TemperatureReader[F] = ev
}
