package actors

import play.api.libs.json._
import play.api.libs.json.Json._

import akka.actor.Actor

import play.api.libs.iteratee.{Concurrent, Enumerator}

import play.api.libs.iteratee.Concurrent.Channel
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._

class TickActor extends Actor {

  lazy val log = Logger("application." + this.getClass.getName)

  val cancellable = context.system.scheduler.schedule(0 milliseconds, 30 milliseconds, self, Tick())
  var webSockets = Map[Int, PlayerChannel]()
  var playerTicks = Map[Int, Int]()

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

    case Tick() =>
      webSockets.foreach {
        case (id, channel) =>
          val data = Map("event" -> "", "data" -> toJson(GameState.newInstance(playerTicks)))
          channel push Json.toJson(data)
      }

    case SocketDisconnect(id) =>

      log debug s"closed socket for $id"

      val playerChannel = webSockets.get(id).get

      if (playerChannel.channelsCount > 1) {
        playerChannel.channelsCount -= 1
        webSockets += (id -> playerChannel)
        log debug s"channel for player: $id count: ${playerChannel.channelsCount}"
      } else {
        removePlayerChannel(id)
        log debug s"removed channel and for player $id"
      }

  }

  def removePlayerChannel(id: Int) = webSockets -= id

  case class PlayerChannel(id: Int, var channelsCount: Int, enumerator: Enumerator[JsValue], channel: Channel[JsValue])

}


sealed trait SocketMessage

case class SocketConnect(id: Int) extends SocketMessage
case class SocketDisconnect(id: Int) extends SocketMessage
case class Tick() extends SocketMessage
case class Start(id: Int) extends SocketMessage
case class Stop(id: Int) extends SocketMessage

