package com.fortysevendeg.smarthome
package config

import cats.effect.Effect
import cats.syntax.either._
import pureconfig._

trait ConfigService[F[_]] {

  def serviceConfig[Config](implicit reader: Derivation[ConfigReader[Config]]): F[Config]

}

object ConfigService {
  def apply[F[_]: Effect]: ConfigService[F] = new ConfigService[F] {

    override def serviceConfig[Config](
        implicit reader: Derivation[ConfigReader[Config]]
    ): F[Config] =
      Effect[F].fromEither(
        ConfigSource.default
          .load[Config]
          .leftMap(e => new IllegalStateException(s"Error loading configuration: $e"))
      )

  }
}
