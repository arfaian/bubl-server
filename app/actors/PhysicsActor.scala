package actors

import akka.actor.{Actor, Props}

import models.UserCommand

class PhysicsActor() extends Actor {

  val inputVelocity = new Vector3()
  val inputQuaternion = new Quaternion()
  val SPEED = 200;
  val INV_MAX_FPS = 1 / 100;

  override def receive = {
    case ProcessCommand(userCommand) =>
      inputVelocity.set(0, 0, 0);

      if (userCommand.forward) inputVelocity.z -= SPEED
      if (userCommand.backward) inputVelocity.x -= SPEED
      if (userCommand.left) inputVelocity.z += SPEED
      if (userCommand.right) inputVelocity.x += SPEED

      val rotation = aggregateRotation.multiply(inverseLook)
        .multiply(mouseSensitivity)
        .multiplyScalar(INV_MAX_FPS)
        .add(previousRotation)

      previousRotation = rotation;
      aggregateRotation.set(0, 0, 0)
      aggregateRotation.x += userCommand.mousedy
      aggregateRotation.y += userCommand.mousedx

      val euler = new Euler(rotation.x, rotation.y, rotation.z)
      inputQuaternion.setFromEuler(euler)
      inputVelocity.applyQuaternion(inputQuaternion)
      inputVelocity.multiplyScalar(INV_MAX_FPS)

      sender ! PlayerUpdate(inputVelocity, rotation)
  }
}

sealed trait PhysicsActorMessage
case class ProcessCommand(userCommand: UserCommand) extends PhysicsActorMessage
case class PlayerUpdate(velocity: Vector3, rotation: Vector3) extends PhysicsActorMessage
