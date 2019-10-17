package me.arcanis.ffxivbis.http.view

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

class RootView(storage: ActorRef, ariyala: ActorRef)(implicit timeout: Timeout) {

  private val basePartyView = new BasePartyView(storage)
  private val indexView = new IndexView(storage)

  private val biSView = new BiSView(storage, ariyala)
  private val lootView = new LootView(storage)
  private val lootSuggestView = new LootSuggestView(storage)
  private val playerView = new PlayerView(storage, ariyala)
  private val userView = new UserView(storage)

  def route: Route =
    basePartyView.route ~ indexView.route ~
    biSView.route ~ lootView.route ~ lootSuggestView.route ~ playerView.route ~ userView.route
}

object RootView {
  def toHtml(template: String): HttpEntity.Strict =
    HttpEntity(ContentTypes.`text/html(UTF-8)`, template)
}
