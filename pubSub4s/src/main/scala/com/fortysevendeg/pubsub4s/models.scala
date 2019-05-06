package com.fortysevendeg.pubsub4s

object models {

  type PubSubResult[A] = Either[CloudPubSubException, A]

  sealed abstract class CloudPubSubException(
      val message: String,
      val maybeCause: Option[Throwable] = None
  ) extends RuntimeException(message)
      with Product
      with Serializable {

    maybeCause foreach initCause

    override def toString: String =
      message ++ maybeCause.foldLeft("")((_, b) => b.getStackTrace.toString)
  }

  final case class PubSubAdminClientException(msg: String, cause: Option[Throwable] = None)
      extends CloudPubSubException(msg, cause)

  final case class PubSubTopicException(msg: String, cause: Option[Throwable] = None)
      extends CloudPubSubException(msg, cause)

  final case class PubSubPublisherException(msg: String, cause: Option[Throwable] = None)
      extends CloudPubSubException(msg, cause)

  final case class PubSubMessageException(msg: String, cause: Option[Throwable] = None)
      extends CloudPubSubException(msg, cause)
}
