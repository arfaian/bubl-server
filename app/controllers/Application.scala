package controllers

import play.api._
import play.api.mvc._


import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent._

object Application extends Controller {

  def index = WebSocket.async[JsValue] { request =>
    // Concurrent.broadcast returns (Enumerator, Concurrent.Channel)
    val (out, channel) = Concurrent.broadcast[JsValue]

    //log the message to stdout and send response back to client
    val in = Iteratee.foreach[JsValue] { event =>
        println(event)
        //the channel will push to the Enumerator
        //channel push("RESPONSE: " + msg)
    }

    future { (in, out) }
  }

}
