package controllers

import play.api.mvc._

import actors.TickActor

object Application extends Controller {

  def index = WebSocket.async[Array[Byte]] { request =>
    TickActor.join.map { (iteratee, enumerator) =>
      (iteratee, enumerator)
    }
  }

}
