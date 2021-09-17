package com.fortysevendeg.smarthome.client.common

import cats.effect.{Async, Timer}
import cats.syntax.flatMap._
import com.fortysevendeg.smarthome.protocol.messages._
import fs2.Stream
import org.typelevel.log4cats.Logger

import scala.concurrent.duration._
import scala.math.BigDecimal.RoundingMode
import scala.util.Random

trait GeoCalculator {
  private val AVERAGE_RADIUS_OF_EARTH_KM: Double    = 6371d
  private val AVERAGE_RADIUS_OF_EARTH_MILES: Double = 3959d

  private[this] def calculateDistance(startingPoint: Point, destination: Point): Double = {
    val latDistance = Math.toRadians(startingPoint.lat - destination.lat)
    val lngDistance = Math.toRadians(startingPoint.long - destination.long)
    val sinLat      = Math.sin(latDistance / 2)
    val sinLng      = Math.sin(lngDistance / 2)
    val a = sinLat * sinLat +
      (Math.cos(Math.toRadians(startingPoint.lat)) *
        Math.cos(Math.toRadians(destination.lat)) *
        sinLng * sinLng)
    2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  }

  def calculateDistanceInKilometer(startingPoint: Point, destination: Point): Double =
    BigDecimal(calculateDistance(startingPoint, destination) * AVERAGE_RADIUS_OF_EARTH_KM)
      .setScale(2, RoundingMode.HALF_UP)
      .toDouble

  def calculateDistanceInMiles(startingPoint: Point, destination: Point): Double =
    BigDecimal(calculateDistance(startingPoint, destination) * AVERAGE_RADIUS_OF_EARTH_MILES)
      .setScale(2, RoundingMode.HALF_UP)
      .toDouble
}

object LocationsGenerator extends GeoCalculator {
  val startingPoint = Point(47.582678d, -122.334617d)
  val destination   = Point(47.616187d, -122.203689d)
  val startingLocation = Location(
    Some(startingPoint),
    Some(destination),
    calculateDistanceInMiles(startingPoint, destination)
  )

  def get[F[_]: Async: Logger: Timer]: Stream[F, Location] =
    Stream
      .iterateEval(startingLocation)(location => nextLocation(location))
      .takeWhile(location =>
        destination.lat - location.currentLocation
          .map(_.lat)
          .getOrElse(0d) > 0.0001d && destination.long - location.currentLocation
          .map(_.long)
          .getOrElse(0d) > 0.0001d
      )
      .append(Stream.emit(Location(Some(destination), Some(destination), 0d)).covary[F])

  def nextLocation[F[_]: Async: Timer](location: Location): F[Location] =
    Timer[F]
      .sleep(1.seconds)
      .flatMap(_ =>
        Async[F].delay {
          val nextPoint =
            Point(
              closeLocation(
                location.currentLocation
                  .map(_.lat)
                  .getOrElse(0d),
                0.00067018d
              ),
              closeLocation(
                location.currentLocation
                  .map(_.long)
                  .getOrElse(0d),
                0.00261856d
              )
            )
          Location(
            Some(nextPoint),
            Some(destination),
            calculateDistanceInMiles(nextPoint, destination)
          )
        }
      )

  def closeLocation(point: Double, delta: Double): Double = {
    val increment: Double = Random.nextDouble() / 1000000d
    val signal            = if (Random.nextBoolean()) 1 else -1
    point + delta + signal * increment
  }
}
