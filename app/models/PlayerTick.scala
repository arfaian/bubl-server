package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class PlayerTick(
  val position: Position,
  val rotation: Rotation)

object PlayerTick {

  implicit val positionFormat = (
    (__(0)).format[Long] and
    (__(1)).format[Long] and
    (__(2)).format[Long]
  )(Position.apply, unlift(Position.unapply))

  implicit val rotationFormat = (
    (__(0)).format[Float] and
    (__(1)).format[Float]
  )(Rotation.apply, unlift(Rotation.unapply))

  implicit val playerTickFormat = (
    (__ \ "position").format[Position] and
    (__ \ "rotation").format[Rotation]
  )(PlayerTick.apply, unlift(PlayerTick.unapply))
}
