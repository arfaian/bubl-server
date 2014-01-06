package models

case class GameObject(
  val id: Int = -1,
  val objectType: Char,
  val position: Vector3,
  val rotation: Vector3)

sealed trait Type { def objectType: String }
case class Player() extends Type { def objectType = "player" }
case class Box() extends Type { def objectType = "box" }

