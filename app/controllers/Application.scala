package controllers

object Application extends Controller {

  def index = WebSocket.async[Array[Byte]] { request =>
    TickActor.join.map { (iteratee, enumerator) =>
      (iteratee, enumerator)
    }
  }

}
