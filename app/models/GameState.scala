package models

import java.util.concurrent.atomic.AtomicInteger

class GameState() {
}

object GameState {
  val uidGenerator: AtomicInteger = new AtomicInteger(0);
  val tickGenerator: AtomicInteger = new AtomicInteger(0);

  def generateUid = uidGenerator.getAndIncrement
  def getAndIncrementTickCount = tickGenerator.getAndIncrement
}
