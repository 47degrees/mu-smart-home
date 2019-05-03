package com.fortysevendeg.smarthome.server.app

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.fortysevendeg.smarthome.config.ConfigService
import com.fortysevendeg.smarthome.server.common._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import pureconfig.generic.auto._

abstract class ServerBoot[F[_]: ConcurrentEffect] {

  def runProgram(args: List[String]): F[ExitCode] =
    for {
      config   <- ConfigService[F].serviceConfig[SmartHomeServerConfig]
      logger   <- Slf4jLogger.fromName[F](config.name)
      exitCode <- serverProgram(config)(logger)
    } yield exitCode

  def serverProgram(config: SmartHomeServerConfig)(implicit L: Logger[F]): F[ExitCode]
}
