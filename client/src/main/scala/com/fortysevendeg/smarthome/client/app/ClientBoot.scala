package com.fortysevendeg.smarthome.client.app

import cats.effect._
import cats.syntax.functor._
import com.fortysevendeg.smarthome.client.common._
import com.fortysevendeg.smarthome.client.process.SmartHomeServiceApi
import com.fortysevendeg.smarthome.config.ConfigService
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import pureconfig.generic.auto._

abstract class ClientBoot[F[_]: ConcurrentEffect: ContextShift: Timer] {

  def smartHomeServiceApi(host: String, port: Int)(
      implicit L: Logger[F]
  ): Stream[F, SmartHomeServiceApi[F]] =
    SmartHomeServiceApi.createInstance(host, port, sslEnabled = false)

  def runProgram(args: List[String]): Stream[F, ExitCode] = {

    def setupConfig: F[SmartHomeClientConfig] =
      ConfigService[F]
        .serviceConfig[ClientConfig]
        .map(SmartHomeClientConfig)

    for {
      config   <- Stream.eval(setupConfig)
      logger   <- Stream.eval(Slf4jLogger.fromName[F](config.client.name))
      exitCode <- clientProgram(config)(logger)
    } yield exitCode
  }

  def clientProgram(config: SmartHomeClientConfig)(implicit L: Logger[F]): Stream[F, ExitCode]

}
