import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import java.util.concurrent.TimeUnit

import play.api._
import play.api.libs.concurrent._
import play.api.Play.current

import scala.concurrent.duration._

import actors.WorldActor
import actors.GenerateWorld

object BublGlobal extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("bubl-server has started...")
    Logger.info("generating world...")
    val worldActor = Akka.system.actorOf(Props[WorldActor], name = "worldActor")
    implicit val timeout = Timeout(Duration(1, TimeUnit.SECONDS))
    worldActor ? GenerateWorld()
    Logger.info("world generation finished...")
  }

  override def onStop(app: Application) {
    Logger.info("bubl-server has shutdown...")
  }
}
