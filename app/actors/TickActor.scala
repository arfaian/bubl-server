package actors

import akka.actor.Actor

import models.PlayerTick

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Logger

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

class TickActor extends Actor {

  lazy val log = Logger("application." + this.getClass.getName)

  var webSockets = TrieMap[Int, PlayerChannel]()
  var playerTicks = TrieMap[Int, PlayerTick]()
  val cancellable = context.system.scheduler.schedule(500 milliseconds, 30 milliseconds, self, SendTick())

  override def receive = {

    case SocketConnect(id) =>
      log debug s"socket connected for player $id"

      val playerChannel: PlayerChannel = webSockets.get(id) getOrElse {
        val broadcast: (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
        PlayerChannel(id, 0, broadcast._1, broadcast._2)
      }

      playerChannel.channelsCount = playerChannel.channelsCount + 1
      webSockets += (id -> playerChannel)

      log debug s"channel for player: $id count: ${playerChannel.channelsCount}"
      log debug s"channel count: ${webSockets.size}"

      sender ! playerChannel.enumerator

    case SendSession(id) =>
      val playerChannel = webSockets.get(id).get

      val sessionStart = Json.obj(
        "event" -> "session:start",
        "data" -> Json.obj(
          "session" -> Json.obj("id" -> id)
        )
      )

      log debug s"sessionStart: $sessionStart"
      playerChannel.channel.push(sessionStart)

    case SendTick() =>
      log debug s"playerTicks: $playerTicks"
      var jsObject = Json.obj()
      playerTicks.foreach {
        case (i, pT) =>
          jsObject = jsObject ++ Json.obj(i.toString -> Json.toJson(pT))
      }
      log debug s"jsObject: $jsObject"
      val msg = Json.obj("event" -> "incoming.tick", "data" -> jsObject)
      val data = Json.toJson(msg)
      log debug s"data: $data"

      webSockets.foreach {
        case (id, playerChannel) =>
          playerChannel.channel.push(data)
      }

    case ReceiveTick(id, event) =>
      log debug s"received message from $id: $event"
      playerTicks += ((id, Json.fromJson[PlayerTick](event \ "data").get))

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
      }

  }

  case class PlayerChannel(id: Int, var channelsCount: Int, enumerator: Enumerator[JsValue], channel: Channel[JsValue])

}

sealed trait SocketMessage

case class SocketConnect(id: Int) extends SocketMessage
case class SocketDisconnect(id: Int) extends SocketMessage
case class SendSession(id: Int) extends SocketMessage
case class SendTick() extends SocketMessage
case class ReceiveTick(id: Int, event: JsValue) extends SocketMessage
