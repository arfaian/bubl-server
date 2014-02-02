package actors

import akka.actor.{Actor, Props}

import java.nio.ByteBuffer

import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api.Play.current

import scala.collection.mutable.ListBuffer
import scala.util.Random

import models._

object PlayerActor {
  def props(id: Int, channel: Channel[Array[Byte]]):Props = Props(new PlayerActor(id, channel))
}

class PlayerActor(id: Int,
  channel: Channel[Array[Byte]],
  position: Vector3 = new Vector3,
  velocity: Vector3 = new Vector3,
  rotation: Vector3 = new Vector3,
  quaternion: Quaternion = new Quaternion) extends Actor {

  lazy val log = Logger("application." + this.getClass.getName)

  val physicsActor = Akka.system.actorOf(Props[PhysicsActor], name = s"/user/physicsActor$id")

  override def receive = {
    case ReceiveCommand(bytes) =>
      val len = bytes.length
      log debug s"received message from $id with byte array length $len"
      val buffer = ByteBuffer.wrap(bytes)
      val userCommand = new UserCommand(
        buffer.getInt(0),
        buffer.getChar(32) == 1,
        buffer.getChar(40) == 1,
        buffer.getChar(48) == 1,
        buffer.getChar(56) == 1,
        buffer.getFloat(64),
        buffer.getFloat(96))
      physicsActor ! ProcessCommand(position, rotation, quaternion, userCommand)

  }
}

sealed trait PlayerActorMessage
case class ReceiveCommand(bytes: Array[Byte]) extends PlayerActorMessage
