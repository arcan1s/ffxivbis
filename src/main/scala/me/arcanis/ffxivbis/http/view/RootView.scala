package me.arcanis.ffxivbis.http.view

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

class RootView(storage: ActorRef, ariyala: ActorRef)(implicit timeout: Timeout) {

  private val biSView = new BiSView(storage, ariyala)

  def route: Route =
    biSView.route
}

object RootView {
  def toHtml(template: String): HttpEntity.Strict =
    HttpEntity(ContentTypes.`text/html(UTF-8)`, template)
}
