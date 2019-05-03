package com.fortysevendeg.smarthome.client.common

import cats.Show
import cats.syntax.show._
import com.fortysevendeg.smarthome.protocol.messages._

object Implicits {
  implicit val catsShowInstanceForSmartHomeAction: Show[SmartHomeAction] =
    new Show[SmartHomeAction] {
      override def show(action: SmartHomeAction): String = s"${action.value}"
    }
  implicit val catsShowInstanceForComingBackModeResponse: Show[ComingBackModeResponse] =
    new Show[ComingBackModeResponse] {
      override def show(comingBack: ComingBackModeResponse): String =
        comingBack.actions.map(_.show).mkString("\n")
    }
}
