package actors

import akka.actor.{Actor, Props}

import play.api.Logger

import scala.collection.mutable.ListBuffer
import scala.util.Random

import models.GameObject
import models.GameState
import models.Vector3

class WorldActor extends Actor {

  lazy val log = Logger("application." + this.getClass.getName)

  var boxes: List[GameObject] = _

  override def receive = {
    case GenerateWorld() => {
      val buffer = new ListBuffer[GameObject]
      for (i <- 0 until 10) {
        val x = (Random.nextFloat - 0.5) * 1000;
        val y = 40;
        val z = (Random.nextFloat - 0.5) * 1000;
        buffer += new GameObject(
            GameState.generateUid,
            'b',
            new Vector3(x, y, z),
            new Vector3(0, 0, 0))
      }
      boxes = buffer.toList
    }

    case WorldState() => {
      log debug boxes.toString
      sender ! boxes
    }
  }
}

sealed trait WorldActorMessage
case class GenerateWorld() extends WorldActorMessage
case class WorldState() extends WorldActorMessage
