package com.fortysevendeg.smarthome.client.process

import java.net.InetAddress

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.fortysevendeg.smarthome.client.common._
import com.fortysevendeg.smarthome.protocol.messages._
import com.fortysevendeg.smarthome.protocol.services._
import fs2.Stream
import org.typelevel.log4cats.Logger
import higherkindness.mu.rpc.ChannelForAddress
import higherkindness.mu.rpc.channel.{ManagedChannelInterpreter, UsePlaintext}
import io.grpc.{CallOptions, ManagedChannel}

trait SmartHomeServiceApi[F[_]] {

  def isEmpty: F[Boolean]

  def getTemperature: Stream[F, TemperaturesSummary]

  def comingBackMode(locations: Stream[F, Location]): Stream[F, ComingBackModeResponse]
}

object SmartHomeServiceApi {

  def apply[F[_]: Effect](clientRPC: SmartHomeService[F])(implicit
      L: Logger[F]
  ): SmartHomeServiceApi[F] =
    new SmartHomeServiceApi[F] {
      override def isEmpty: F[Boolean] =
        for {
          result <- clientRPC.isEmpty(IsEmptyRequest())
          _      <- L.info(s"Result: $result")
        } yield result.result

      def getTemperature: Stream[F, TemperaturesSummary] = {
        for {
          temperature <- clientRPC.getTemperature(IsEmptyRequest())
          _           <- Stream.eval(L.info(s"* Received new temperature: 👍 --> $temperature"))
        } yield temperature
      }.fold(TemperaturesSummary.empty)((summary, temperature) => summary.append(temperature))

      def comingBackMode(locations: Stream[F, Location]): Stream[F, ComingBackModeResponse] =
        clientRPC.comingBackMode(locations)
    }

  def createInstance[F[_]: ContextShift: Logger: Timer](
      hostname: String,
      port: Int,
      sslEnabled: Boolean = true
  )(implicit F: ConcurrentEffect[F]): fs2.Stream[F, SmartHomeServiceApi[F]] = {

    val channel: F[ManagedChannel] =
      F.delay(InetAddress.getByName(hostname).getHostAddress).flatMap { ip =>
        val channelFor    = ChannelForAddress(ip, port)
        val channelConfig = if (!sslEnabled) List(UsePlaintext()) else Nil
        new ManagedChannelInterpreter[F](channelFor, channelConfig).build
      }

    def clientFromChannel: Resource[F, SmartHomeService[F]] =
      SmartHomeService.clientFromChannel(channel, CallOptions.DEFAULT)

    fs2.Stream.resource(clientFromChannel).map(SmartHomeServiceApi(_))
  }
}
