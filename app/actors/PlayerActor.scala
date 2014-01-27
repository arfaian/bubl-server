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

class PlayerActor(id: Int) extends Actor {

  override def receive = {
    case Create() => {
    }
  }
}

sealed trait PlayerActorMessage
case class Create() extends PlayerActorMessage
