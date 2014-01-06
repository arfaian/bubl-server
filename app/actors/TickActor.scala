package actors

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import models.PlayerTick
import models.GameObject

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Logger

import play.api.Play.current

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class TickActor extends Actor {

  lazy val log = Logger("application." + this.getClass.getName)

  var webSockets = TrieMap[Int, PlayerChannel]()
  var playerTicks = TrieMap[Int, PlayerTick]()
  val cancellable = context.system.scheduler.schedule(500 milliseconds, 30 milliseconds, self, SendTick())
  val worldActor = Akka.system.actorSelection("/user/worldActor")

  override def receive = {

    case SocketConnect(id) =>
      log debug s"socket connected for player $id"

      val playerChannel: PlayerChannel = webSockets.get(id) getOrElse {
        val broadcast: (Enumerator[Array[Byte]], Channel[Array[Byte]]) = Concurrent.broadcast[Array[Byte]]
        PlayerChannel(id, 0, broadcast._1, broadcast._2)
      }

      playerChannel.channelsCount = playerChannel.channelsCount + 1
      webSockets += (id -> playerChannel)

      log debug s"channel for player: $id count: ${playerChannel.channelsCount}"
      log debug s"channel count: ${webSockets.size}"

      sender ! playerChannel.enumerator

    case SendSession(id) =>
      val playerChannel = webSockets.get(id).get

      implicit val timeout = Timeout(Duration(3, TimeUnit.SECONDS))

      (worldActor ? WorldState()).onSuccess { case result =>
        val gameObjects = result.asInstanceOf[List[GameObject]]
        val bufferSize = (gameObjects.size * 30) + 6
        val byteBuffer = ByteBuffer.allocate(bufferSize);
        byteBuffer.putChar('a');
        byteBuffer.putInt(id);

        gameObjects.foreach { entity =>
          byteBuffer.putInt(entity.id)
          byteBuffer.putChar(entity.objectType)
          byteBuffer.putFloat(entity.position.x.toFloat)
          byteBuffer.putFloat(entity.position.y.toFloat)
          byteBuffer.putFloat(entity.position.z.toFloat)
          byteBuffer.putFloat(entity.rotation.x.toFloat)
          byteBuffer.putFloat(entity.rotation.y.toFloat)
          byteBuffer.putFloat(entity.rotation.z.toFloat)
        }
        byteBuffer.flip

        log debug s"sessionStart: $byteBuffer"
        playerChannel.channel.push(byteBuffer.array)
    }

    case SendTick() =>
      var bufferLength = (playerTicks.size * 28) + 2
      log debug s"bufferLength: $bufferLength"
      val byteBuffer = ByteBuffer.allocate(bufferLength);
      byteBuffer.putChar('b');
      log debug s"playerTicks: $playerTicks"
      playerTicks.foreach {
        case (id, playerTick) =>
          byteBuffer.putInt(id)
          byteBuffer.putFloat(playerTick.px)
          byteBuffer.putFloat(playerTick.py)
          byteBuffer.putFloat(playerTick.pz)
          byteBuffer.putFloat(playerTick.rx)
          byteBuffer.putFloat(playerTick.ry)
          byteBuffer.putFloat(playerTick.rz)
      }
      log debug s"data: $byteBuffer"

      webSockets.foreach {
        case (id, playerChannel) =>
          playerChannel.channel.push(byteBuffer.array)
      }

    case ReceiveTick(id, bytes) =>
      val len = bytes.length
      log debug s"received message from $id with byte array length $len"
      val buffer = ByteBuffer.wrap(bytes)
      val playerTick = new PlayerTick(
        buffer.getFloat(0),
        buffer.getFloat(4),
        buffer.getFloat(8),
        buffer.getFloat(12),
        buffer.getFloat(16),
        buffer.getFloat(20))
      playerTicks += ((id, playerTick))

    case SocketDisconnect(id) =>

      log debug s"closed socket for $id"

      val playerChannel = webSockets.get(id).get

      if (playerChannel.channelsCount > 1) {
        playerChannel.channelsCount -= 1
        webSockets += (id -> playerChannel)
        log debug s"channel for player: $id count: ${playerChannel.channelsCount}"
      } else {
        webSockets -= id
        playerTicks -= id
        log debug s"removed channel for player $id"
        self ! PlayerLeave(id)
      }

    case PlayerLeave(id) =>
      val byteBuffer = ByteBuffer.allocate(6);
      byteBuffer.putChar('d');
      byteBuffer.putInt(id)

      webSockets.foreach {
        case (id, playerChannel) =>
          playerChannel.channel.push(byteBuffer.array)
      }

  }

  case class PlayerChannel(id: Int, var channelsCount: Int, enumerator: Enumerator[Array[Byte]], channel: Channel[Array[Byte]])

}

sealed trait SocketMessage

case class SocketConnect(id: Int) extends SocketMessage
case class SocketDisconnect(id: Int) extends SocketMessage
case class SendSession(id: Int) extends SocketMessage
case class SendTick() extends SocketMessage
case class PlayerLeave(id: Int) extends SocketMessage
case class ReceiveTick(id: Int, event: Array[Byte]) extends SocketMessage
