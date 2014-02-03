package actors

import akka.actor.{Actor, Props}

import models._

import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api.Play.current

object PhysicsActor {
  def props(id: Int):Props = Props(new PhysicsActor(id))
}

class PhysicsActor(id: Int) extends Actor {

  lazy val log = Logger("application." + this.getClass.getName)

  val SPEED = 200.0;
  val INV_MAX_FPS = 1.0 / 100.0;
  val inverseLook = new Vector3(-1, -1, -1)
  val mouseSensitivity = new Vector3(0.25, 0.25, 0.25)

  var inputVelocity = new Vector3()
  var inputQuaternion = new Quaternion()

  val tickActor = Akka.system.actorSelection("/user/tickActor")

  override def receive = {
    case ProcessCommand(position, rotation, quaternion, userCommand) =>
      var x = 0.0
      var z = 0.0

      if (userCommand.forward == true) {
        z = z - SPEED
      }

      if (userCommand.backward == true) {
        x = x - SPEED
      }

      if (userCommand.left == true) {
        z = z + SPEED
      }

      if (userCommand.right == true) {
        x = x + SPEED
      }

      log debug s"x: $x"
      log debug s"z: $z"

      inputVelocity = new Vector3(x = x, z = z)
      log debug s"inputVelocity: $inputVelocity"

      val aggregateRotation = new Vector3(userCommand.mousedy, userCommand.mousedx)

      log debug s"aggregateRotation: $aggregateRotation"

      var test = aggregateRotation * inverseLook * mouseSensitivity * INV_MAX_FPS

      log debug s"test: $test"

      val r = aggregateRotation * inverseLook * mouseSensitivity * INV_MAX_FPS + rotation

      log debug s"r: $r"

      val euler = new Euler(r.x, r.y, r.z)
      inputQuaternion = inputQuaternion.setFromEuler(euler)
      inputVelocity = inputVelocity.applyQuaternion(inputQuaternion)  * INV_MAX_FPS

      val updatedPosition = translate(position, inputVelocity, quaternion)

      log debug inputVelocity.toString
      log debug updatedPosition.toString

      tickActor ! PlayerUpdate(id, userCommand.tick, updatedPosition, r, inputVelocity)
      sender ! PlayerUpdate(id, userCommand.tick, updatedPosition, r, inputVelocity)
  }

  def translate(position: Vector3, velocity: Vector3, quaternion: Quaternion) = {
    val vx = Vector3.X.applyQuaternion(quaternion) * velocity.x
    val vy = Vector3.Y.applyQuaternion(quaternion) * velocity.y
    val vz = Vector3.Z.applyQuaternion(quaternion) * velocity.z

    position + vx + vy + vz
  }
}

sealed trait PhysicsActorMessage
case class ProcessCommand(position: Vector3, rotation: Vector3, quaternion: Quaternion, userCommand: UserCommand) extends PhysicsActorMessage
case class PlayerUpdate(id: Int, tick: Int, position: Vector3, rotation: Vector3, velocity: Vector3) extends PhysicsActorMessage
