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

  val physicsActor = context.actorOf(PhysicsActor.props(id), name = s"physicsActor$id")

  override def receive = {
    case ReceiveCommand(bytes) =>
      val len = bytes.length
      val buffer = ByteBuffer.wrap(bytes)
      val userCommand = new UserCommand(
        buffer.getInt(0),
        buffer.get(4) == 1,
        buffer.get(5) == 1,
        buffer.get(6) == 1,
        buffer.get(7) == 1,
        buffer.getFloat(8),
        buffer.getFloat(40))
      log debug userCommand.toString
      physicsActor ! ProcessCommand(position, rotation, quaternion, userCommand)

    case PlayerUpdate(id, tick, position, rotation, velocity) =>
      val buffer = ByteBuffer.allocate(30);
      buffer.putChar('e');
      buffer.putInt(tick);
      buffer.putFloat(position.x.toFloat)
      buffer.putFloat(position.y.toFloat)
      buffer.putFloat(position.z.toFloat)
      buffer.putFloat(rotation.x.toFloat)
      buffer.putFloat(rotation.y.toFloat)
      buffer.putFloat(rotation.z.toFloat)
      buffer.flip
      channel.push(buffer.array)
  }
}

sealed trait PlayerActorMessage
case class ReceiveCommand(bytes: Array[Byte]) extends PlayerActorMessage
