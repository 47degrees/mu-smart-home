package com.fortysevendeg.pubsub4s

import Converters._
import models._
import cats.data._
import cats.effect.{ContextShift, Effect}
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.functor._
import com.google.cloud.pubsub.v1._
import io.chrisdavenport.log4cats.Logger
import com.google.protobuf.ByteString
import com.google.pubsub.v1._

class TopicPubSubClient[F[_]](projectId: String, topic: String)(
    implicit E: Effect[F],
    CS: ContextShift[F],
    L: Logger[F]
) {

  def getOrCreateTopic: F[PubSubResult[Topic]] = {
    def createOrReturn(maybeTopic: Option[Topic]): F[PubSubResult[Topic]] = maybeTopic match {
      case Some(t) => L.info(s"Topic $topic exists, skipping the creation...").as(t.asRight)
      case _       => L.info(s"Topic $topic doesn't exist, creating it...") *> createTopic
    }

    (for {
      admin      <- EitherT(topicAdminClient)
      topicName  <- EitherT(topicName)
      maybeTopic <- EitherT(getTopic(admin, topicName))
      r          <- EitherT(createOrReturn(maybeTopic))
    } yield r).value
  }

  def getTopic: F[PubSubResult[Option[Topic]]] =
    (for {
      admin     <- EitherT(topicAdminClient)
      topicName <- EitherT(topicName)
      topic     <- EitherT(getTopic(admin, topicName))
    } yield topic).value

  def createTopic: F[PubSubResult[Topic]] =
    (for {
      admin     <- EitherT(topicAdminClient)
      topicName <- EitherT(topicName)
      topic     <- EitherT(createTopic(admin, topicName))
    } yield topic).value

  def publishMessage(message: String): F[PubSubResult[String]] =
    publishMessage(ByteString.copyFromUtf8(message))

  def publishMessage(bytes: Array[Byte]): F[PubSubResult[String]] =
    publishMessage(ByteString.copyFrom(bytes))

  def shutdown: F[PubSubResult[Unit]] = publisher.map(_.map(_.shutdown()))

  private[this] def getTopic(
      admin: TopicAdminClient,
      topicName: ProjectTopicName
  ): F[PubSubResult[Option[Topic]]] =
    E.delay(Option(admin.getTopic(topicName)).asRight[CloudPubSubException])
      .handleError(
        e => PubSubTopicException(e.getMessage, Some(e.getCause)).asLeft[Option[Topic]]
      )

  private[this] def createTopic(
      admin: TopicAdminClient,
      topicName: ProjectTopicName
  ): F[PubSubResult[Topic]] =
    E.delay(admin.createTopic(topicName).asRight[CloudPubSubException])
      .handleError(
        e => PubSubTopicException(e.getMessage, Some(e.getCause)).asLeft[Topic]
      )

  private[this] def publisherDefaultBuilder(
      topicName: ProjectTopicName
  ): F[PubSubResult[Publisher.Builder]] =
    E.delay(Publisher.newBuilder(topicName).asRight[CloudPubSubException])
      .handleError(
        e => PubSubPublisherException(e.getMessage, Some(e.getCause)).asLeft[Publisher.Builder]
      )

  private[this] def createMessage(data: ByteString): F[PubSubResult[PubsubMessage]] =
    E.delay(PubsubMessage.newBuilder().setData(data).build().asRight[CloudPubSubException])
      .handleError(
        e => PubSubMessageException(e.getMessage, Some(e.getCause)).asLeft[PubsubMessage]
      )

  private[this] def publishMessage(msg: ByteString): F[PubSubResult[String]] =
    (for {
      pub     <- EitherT[F, CloudPubSubException, Publisher](publisher)
      message <- EitherT[F, CloudPubSubException, PubsubMessage](createMessage(msg))
      r       <- EitherT[F, CloudPubSubException, String](publishMessageF(pub, message))
    } yield r).value

  private[this] def publishMessageF(
      publisher: Publisher,
      message: PubsubMessage
  ): F[PubSubResult[String]] =
    to[F, String](publisher.publish(message))
      .map(_.asRight[CloudPubSubException])
      .handleError(
        e => PubSubMessageException(e.getMessage, Some(e.getCause)).asLeft[String]
      )

  private[TopicPubSubClient] val topicAdminClient: F[PubSubResult[TopicAdminClient]] =
    E.delay(TopicAdminClient.create().asRight[CloudPubSubException])
      .handleError(
        e => PubSubAdminClientException(e.getMessage, Some(e.getCause)).asLeft[TopicAdminClient]
      )

  private[this] val topicName: F[PubSubResult[ProjectTopicName]] =
    E.delay(ProjectTopicName.of(projectId, topic).asRight[CloudPubSubException])
      .handleError(
        e => PubSubTopicException(e.getMessage, Some(e.getCause)).asLeft[ProjectTopicName]
      )

  private[this] val publisher: F[PubSubResult[Publisher]] = {
    L.info("Creating publisher...") *> (for {
      tn      <- EitherT(topicName)
      builder <- EitherT(publisherDefaultBuilder(tn))
    } yield builder.build()).value
  }
}
