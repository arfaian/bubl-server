package actors

import akka.actor.{Actor, Props}

import play.api.Logger

import scala.collection.mutable.ListBuffer
import scala.util.Random

import models.GameObject
import models.GameState
import models.Vector3

object PlayerActor {
  def props(id: Int):Props = Props(new PlayerActor(id))
}

class PlayerActor(id: Int,
  position: Vector3 = new Vector3(0, 0, 0),
  velocity: Vector3 = new Vector3(0, 0, 0),
  rotation: Vector3 = new Vector3(0, 0, 0)) extends Actor {

  val physicsActor = Akka.system.actorSelection("/user/physicsActor")

  override def receive = {
    case ReceiveCommand(id, bytes) =>
      val len = bytes.length
      log debug s"received message from $id with byte array length $len"
      val buffer = ByteBuffer.wrap(bytes)
      val userCommand = new UserCommand(
        buffer.getInt(0),
        buffer.get(32),
        buffer.get(40),
        buffer.get(48),
        buffer.get(56),
        buffer.getFloat(64),
        buffer.getFloat(96))
      physicsActor ! ProcessCommand(userCommand)

    case PlayerUpdate(position, velocity, rotation) =>
      
  }
}

sealed trait PlayerActorMessage
case class ReceiveCommand() extends PlayerActorMessage
