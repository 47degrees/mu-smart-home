package com.fortysevendeg.smarthome
package config

import cats.effect.Effect
import cats.syntax.either._
import pureconfig.{ConfigReader, Derivation}

trait ConfigService[F[_]] {

  def serviceConfig[Config](implicit reader: Derivation[ConfigReader[Config]]): F[Config]

}

object ConfigService {
  def apply[F[_]: Effect]: ConfigService[F] = new ConfigService[F] {

    override def serviceConfig[Config](
        implicit reader: Derivation[ConfigReader[Config]]
    ): F[Config] =
      Effect[F].fromEither(
        pureconfig
          .loadConfig[Config]
          .leftMap(e => new IllegalStateException(s"Error loading configuration: $e"))
      )

  }
}
