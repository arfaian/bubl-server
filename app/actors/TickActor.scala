package actors

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import models._

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Logger

import play.api.Play.current

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object TickActor {

  lazy val log = Logger("application." + this.getClass.getName)

  implicit val timeout = Timeout(Duration(3, TimeUnit.SECONDS))

  lazy val default = {
    log debug "create tickActor"
    Akka.system.actorOf(Props[TickActor], name = "tickActor")
  }

  def join:Future[(Iteratee[Array[Byte], _], Enumerator[Array[Byte]])] = {
    log debug "join"
    (default ? SocketConnect()).asInstanceOf[Future[(Iteratee[Array[Byte], _], Enumerator[Array[Byte]])]]
  }
}

class TickActor extends Actor {

  lazy val log = Logger("application." + this.getClass.getName)

  implicit val timeout = Timeout(Duration(3, TimeUnit.SECONDS))

  var webSockets = TrieMap[Int, PlayerChannel]()
  var playerTicks = TrieMap[Int, PlayerTick]()
  val cancellable = context.system.scheduler.schedule(500 milliseconds, 15 milliseconds, self, SendTick())
  val worldActor = Akka.system.actorSelection("/user/worldActor")

  override def receive = {

    case SocketConnect() =>
      log debug "SocketConnect"

      val id = GameState.generateUid

      val playerChannel: PlayerChannel = webSockets.get(id) getOrElse {
        val broadcast: (Enumerator[Array[Byte]], Channel[Array[Byte]]) = Concurrent.broadcast[Array[Byte]]
        PlayerChannel(id, 0, broadcast._1, broadcast._2)
      }

      val playerActor = context.actorOf(PlayerActor.props(id, playerChannel.channel), name = s"playerActor$id")

      val iteratee = Iteratee.foreach[Array[Byte]] { bytes =>
        playerActor ! ReceiveCommand(bytes)
      }.map { _ =>
        self ! SocketDisconnect(id)
      }

      log debug s"socket connected for player $id"

      playerChannel.channelsCount = playerChannel.channelsCount + 1
      webSockets += (id -> playerChannel)

      log debug s"channel for player: $id count: ${playerChannel.channelsCount}"
      log debug s"channel count: ${webSockets.size}"

      self ? SendSession(id)

      sender ! (iteratee, playerChannel.enumerator)

    case PlayerUpdate(id, tick, position, rotation, velocity) =>
      val t = new PlayerTick(
        tick,
        position.x.toFloat,
        position.y.toFloat,
        position.z.toFloat,
        rotation.x.toFloat,
        rotation.y.toFloat,
        rotation.z.toFloat
      )
      playerTicks += (id -> t)

    case SendSession(id) =>
      log debug "SendSession"
      val playerChannel = webSockets.get(id).get

      (worldActor ? WorldState()).onSuccess { case result =>
        val gameObjects = result.asInstanceOf[List[GameObject]]
        val bufferSize = (gameObjects.size * 30) + 10
        val byteBuffer = ByteBuffer.allocate(bufferSize);
        byteBuffer.putChar('a');
        byteBuffer.putInt(id);
        byteBuffer.putInt(GameState.getAndIncrementTickCount);

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
      var bufferLength = (playerTicks.size * 28) + 14
      //log debug s"bufferLength: $bufferLength"
      val byteBuffer = ByteBuffer.allocate(bufferLength);
      byteBuffer.putChar('b');
      byteBuffer.putInt(GameState.getAndIncrementTickCount);
      val time = TimeUnit.MILLISECONDS.convert(System.nanoTime, TimeUnit.NANOSECONDS);
      val lowBits = time.toInt;
      val highBits = (time >> 32).toInt;
      byteBuffer.putInt(lowBits);
      byteBuffer.putInt(highBits);
      //log debug s"playerTicks: $playerTicks"
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
      //log debug s"data: $byteBuffer"

      webSockets.foreach {
        case (id, playerChannel) =>
          playerChannel.channel.push(byteBuffer.array)
      }

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

case class SocketConnect() extends SocketMessage
case class SocketConnected(enumerator: Enumerator[Array[Byte]], id: Int) extends SocketMessage
case class SocketDisconnect(id: Int) extends SocketMessage
case class SendSession(id: Int) extends SocketMessage
case class SendTick() extends SocketMessage
case class PlayerLeave(id: Int) extends SocketMessage
case class ReceiveTick(id: Int, event: Array[Byte]) extends SocketMessage
