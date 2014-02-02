package actors

import akka.actor.{Actor, Props}

import models._

import play.api.libs.concurrent.Akka
import play.api.Play.current

object PhysicsActor {
  def props(id: Int):Props = Props(new PhysicsActor(id))
}

class PhysicsActor(id: Int) extends Actor {

  val SPEED = 200.0;
  val INV_MAX_FPS = 1 / 100;
  val inverseLook = new Vector3(-1, -1, -1)
  val mouseSensitivity = new Vector3(0.25, 0.25, 0.25)

  var inputVelocity = new Vector3()
  var inputQuaternion = new Quaternion()

  val tickActor = Akka.system.actorSelection("/user/tickActor")

  override def receive = {
    case ProcessCommand(position, rotation, quaternion, userCommand) =>
      var x = 0.0
      var z = 0.0

      if (userCommand.forward) {
        z = z - SPEED
      }

      if (userCommand.backward) {
        x = x - SPEED
      }

      if (userCommand.left) {
        z = z + SPEED
      }

      if (userCommand.right) {
        x = x + SPEED
      }

      inputVelocity = new Vector3(x = x, z = z)

      val aggregateRotation = new Vector3(userCommand.mousedy, userCommand.mousedx)

      val r = aggregateRotation.multiply(inverseLook)
        .multiply(mouseSensitivity)
        .multiplyScalar(INV_MAX_FPS)
        .add(rotation)

      val euler = new Euler(rotation.x, rotation.y, rotation.z)
      inputQuaternion = inputQuaternion.setFromEuler(euler)
      inputVelocity = inputVelocity.applyQuaternion(inputQuaternion)
          .multiplyScalar(INV_MAX_FPS)

      val updatedPosition = translate(position, inputVelocity, quaternion)

      tickActor ! PlayerUpdate(id, userCommand.tick, updatedPosition, r, inputVelocity)
  }

  def translate(position: Vector3, velocity: Vector3, quaternion: Quaternion) = {
    val vx = new Vector3(x = 1).applyQuaternion(quaternion).multiplyScalar(velocity.x)
    val vy = new Vector3(y = 1).applyQuaternion(quaternion).multiplyScalar(velocity.y)
    val vz = new Vector3(z = 1).applyQuaternion(quaternion).multiplyScalar(velocity.z)

    position.add(vx).add(vy).add(vz)
  }
}

sealed trait PhysicsActorMessage
case class ProcessCommand(position: Vector3, rotation: Vector3, quaternion: Quaternion, userCommand: UserCommand) extends PhysicsActorMessage
case class PlayerUpdate(id: Int, tick: Int, position: Vector3, rotation: Vector3, velocity: Vector3) extends PhysicsActorMessage
