package com.fortysevendeg.smarthome.server.process

import cats.effect.Sync
import com.fortysevendeg.smarthome.protocol.messages._
import io.chrisdavenport.log4cats.Logger

trait SmartHomeSupervisor[F[_]] {
  def performAction(location: Location): F[List[SmartHomeAction]]
}

object SmartHomeSupervisor {
  implicit def instance[F[_]: Sync: Logger]: SmartHomeSupervisor[F] = new SmartHomeSupervisor[F] {
    val connectAlexa            = "👩 - Connect Alexa"
    val disableIrrigationSystem = "💦 - Disable irrigation system"
    val enableSecurityCameras   = "👮 - Enable security cameras"
    val fireplace               = "🔥 - Fireplace in ambient mode"
    val hotWaterHeater          = "💧 - Increase the power of the hot water heater"
    val livingRoom              = "🛋 - Start heating the living room"
    val lowBlinds               = "😎 - Low the blinds"
    val newsSummary             = "🗞 - Get news summary"
    val rumba                   = "🔌 - Send Rumba to the charging dock"
    val turnOnExternalLights    = "🔦 - Turn exterior lights on"
    val turnOnLights            = "💡 - Turn on the lights"
    val turnOnTowelHeater       = "🛁 - Turn the towel heaters on"
    val turnOnTV                = "📺 - Turn on the TV"
    val unlockDoors             = "🚪 - Unlock doors"
    val waiting                 = "👀 - Waiting for a new location..."
    val welcomeHome             = "🏡 - Welcome Home"

    override def performAction(location: Location): F[List[SmartHomeAction]] = Sync[F].delay(
      if (location.distanceToDestination < 6.0d && location.distanceToDestination > 5.85d)
        List(SmartHomeAction(enableSecurityCameras))
      else if (location.distanceToDestination < 5.0d && location.distanceToDestination > 4.85d)
        List(disableIrrigationSystem, rumba, livingRoom).map(SmartHomeAction)
      else if (location.distanceToDestination < 4.2d && location.distanceToDestination > 4.05d)
        List(fireplace, newsSummary).map(SmartHomeAction)
      else if (location.distanceToDestination < 3.3d && location.distanceToDestination > 3.15d)
        List(hotWaterHeater, turnOnTowelHeater).map(SmartHomeAction)
      else if (location.distanceToDestination < 2.4d && location.distanceToDestination > 2.25d)
        List(lowBlinds, turnOnLights).map(SmartHomeAction)
      else if (location.distanceToDestination < 1.5d && location.distanceToDestination > 1.35d)
        List(connectAlexa, turnOnTV).map(SmartHomeAction)
      else if (location.distanceToDestination < 0.6d && location.distanceToDestination > 0.45d)
        List(SmartHomeAction(turnOnExternalLights))
      else if (location.distanceToDestination == 0.0d)
        List(SmartHomeAction(unlockDoors), SmartHomeAction(welcomeHome))
      else
        List(SmartHomeAction(waiting))
    )
  }

  def apply[F[_]](implicit ev: SmartHomeSupervisor[F]): SmartHomeSupervisor[F] = ev
}
