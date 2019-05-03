package com.fortysevendeg.smarthome.server.app

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.fortysevendeg.smarthome.server.common._
import com.fortysevendeg.smarthome.server.process._
import com.fortysevendeg.smarthome.protocol.services._
import higherkindness.mu.rpc.server.{AddService, GrpcServer}
import io.chrisdavenport.log4cats.Logger

class ServerProgram[F[_]: ConcurrentEffect: Timer] extends ServerBoot[F] {

  def serverProgram(config: SmartHomeServerConfig)(implicit L: Logger[F]): F[ExitCode] = {

    implicit val PS: SmartHomeService[F] = new SmartHomeServiceHandler[F]

    for {
      shService <- SmartHomeService.bindService[F]
      server    <- GrpcServer.default[F](config.port, List(AddService(shService)))
      _         <- L.info(s"${config.name} - Starting server at ${config.host}:${config.port}")
      exitCode  <- GrpcServer.server(server).as(ExitCode.Success)
    } yield exitCode

  }
}

object ServerApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = new ServerProgram[IO].runProgram(args)
}
