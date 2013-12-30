package models

import java.util.concurrent.atomic.AtomicInteger

class GameState(val tick: Int = -1, val players: List[Player]) {
}

object GameState {
  val atomicInteger: AtomicInteger = new AtomicInteger(0);

  def newInstance(players: List[Player]) = new GameState(atomicInteger.getAndIncrement(), players)
}
