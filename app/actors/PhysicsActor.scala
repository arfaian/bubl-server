package actors

import akka.actor.{Actor, Props}

import models._

class PhysicsActor() extends Actor {

  val SPEED = 200.0;
  val INV_MAX_FPS = 1 / 100;
  val inverseLook = new Vector3(-1, -1, -1)
  val mouseSensitivity = new Vector3(0.25, 0.25, 0.25)

  var inputVelocity = new Vector3()
  var inputQuaternion = new Quaternion()
  var previousRotation = new Vector3()
  var aggregateRotation = new Vector3()

  override def receive = {
    case ProcessCommand(userCommand) =>
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

      val rotation = aggregateRotation.multiply(inverseLook)
        .multiply(mouseSensitivity)
        .multiplyScalar(INV_MAX_FPS)
        .add(previousRotation)

      previousRotation = rotation;
      aggregateRotation = new Vector3(userCommand.mousedy, userCommand.mousedx)

      val euler = new Euler(rotation.x, rotation.y, rotation.z)
      inputQuaternion = inputQuaternion.setFromEuler(euler)
      inputVelocity = inputVelocity.applyQuaternion(inputQuaternion)
          .multiplyScalar(INV_MAX_FPS)

      sender ! PlayerUpdate(inputVelocity, rotation)
  }
}

sealed trait PhysicsActorMessage
case class ProcessCommand(userCommand: UserCommand) extends PhysicsActorMessage
case class PlayerUpdate(velocity: Vector3, rotation: Vector3) extends PhysicsActorMessage
