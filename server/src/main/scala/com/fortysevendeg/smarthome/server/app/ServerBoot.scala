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
import pureconfig.generic.auto._

import scala.concurrent.duration._


abstract class ServerBoot[F[_]: ConcurrentEffect] {

  implicit val encoder: MessageEncoder[Row] = (a: Row) => Right(a.toString.getBytes)

  def runProgram(args: List[String]): F[ExitCode] =
    for {
      config  <- ConfigService[F].serviceConfig[SmartHomeServerConfig]
      logger  <- Slf4jLogger.fromName[F](config.name)
      pubsub  =
        GooglePubsubProducer.of[F, Row](
          Model.ProjectId(config.project),
          Model.Topic(config.topic),
          config = PubsubProducerConfig[F](
            batchSize = 100,
            delayThreshold = 100.millis,
            onFailedTerminate = e => Sync[F].delay(println(s"Got error $e")) >> Sync[F].unit
          )
        )
      exitCode <- serverProgram(config)(logger, pubsub)
    } yield exitCode

  def serverProgram(config: SmartHomeServerConfig)(implicit L: Logger[F], topicPubSubClient: Resource[F, PubsubProducer[F, Row]]): F[ExitCode]
}
