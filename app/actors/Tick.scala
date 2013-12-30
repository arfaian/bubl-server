package actor

import akka.actor.Actor
import play.api.libs.concurrent.Akka

class TickActor extends Actor {
  def receive = { channel =>
    // do something
  }
}

val tickActor = Akka.system.actorOf(Props[TickActor])

val cancellable =
  system.scheduler.schedule(0 milliseconds,
    50 milliseconds,
    tickActor,
    Tick)

//This cancels further Ticks to be sent
cancellable.cancel()
