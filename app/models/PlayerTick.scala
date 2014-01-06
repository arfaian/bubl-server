package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class PlayerTick(
  val px: Float,
  val py: Float,
  val pz: Float,
  val rx: Float,
  val ry: Float,
  val rz: Float)

object PlayerTick {
  implicit val playerTickFormat = Json.format[PlayerTick]
}
