package models

class Player(
  val id: Int = -1,
  val position: Position,
  val rotation: Rotation,
  val properties: Map[String, Object]) {
}
