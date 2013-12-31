package models

import java.util.concurrent.atomic.AtomicInteger

class GameState() {
}

object GameState {
  val atomicInteger: AtomicInteger = new AtomicInteger(0);

  def generateUid = atomicInteger.getAndIncrement()
}
