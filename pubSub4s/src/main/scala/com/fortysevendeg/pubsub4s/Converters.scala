package com.fortysevendeg.pubsub4s

import java.util.concurrent.{Executor => JavaExecutor}

import com.google.api.core.{ApiFuture, ApiFutureToListenableFuture}
import cats.effect.{ContextShift, Effect}
import cats.syntax.apply._
import com.google.common.util.concurrent.{FutureCallback, Futures}

object Converters {

  def to[F[_], A](
      fa: => ApiFuture[A]
  )(implicit E: Effect[F], CS: ContextShift[F]): F[A] =
    E.async { cb =>
      Futures.addCallback(
        new ApiFutureToListenableFuture(fa),
        new FutureCallback[A] {
          override def onSuccess(result: A): Unit = cb(Right(result))

          override def onFailure(t: Throwable): Unit = cb(Left(t))
        },
        new JavaExecutor {
          override def execute(command: Runnable): Unit =
            E.toIO(CS.shift *> E.delay(command.run())).unsafeRunSync
        }
      )
    }

}
