package com.fortysevendeg.smarthome.server.app

import cats.effect._
import cats.effect.ExitCode
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.fortysevendeg.smarthome.config.ConfigService
import com.fortysevendeg.smarthome.server.common._
import com.permutive.pubsub.producer.{Model, PubsubProducer}
import com.permutive.pubsub.producer.encoder.MessageEncoder
import com.permutive.pubsub.producer.grpc.{GooglePubsubProducer, PubsubProducerConfig}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto._, io.circe.syntax._
import pureconfig.generic.auto._

import scala.concurrent.duration._


abstract class ServerBoot[F[_]: ConcurrentEffect] {

  implicit val encoder: MessageEncoder[Row] = (row: Row) => Right(row.asJson.noSpaces.getBytes)

  def runProgram(args: List[String]): F[ExitCode] =
    for {
      config  <- ConfigService[F].serviceConfig[SmartHomeServerConfig]
      logger  <- Slf4jLogger.fromName[F](config.name)
      pubsub  =
        GooglePubsubProducer.of[F, Row](
          Model.ProjectId(config.project),
          Model.Topic(config.topic),
          config = PubsubProducerConfig[F](
            batchSize = config.batchSize,
            delayThreshold = FiniteDuration(config.delayThreshold, MILLISECONDS),
            onFailedTerminate = e => logger.error(s"Got error $e")
          )
        )
      exitCode <- serverProgram(config)(logger, pubsub)
    } yield exitCode

  def serverProgram(config: SmartHomeServerConfig)(implicit L: Logger[F], topicPubSubClient: Resource[F, PubsubProducer[F, Row]]): F[ExitCode]
}
