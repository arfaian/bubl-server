package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class UserCommand(
  val tick: Int,
  val forward: Boolean,
  val backward: Boolean,
  val left: Boolean,
  val right: Boolean,
  val mousedx: Float,
  val mousedy: Float)
