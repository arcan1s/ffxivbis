/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.view

import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import me.arcanis.ffxivbis.messages.{BiSProviderMessage, Message}

class RootView(storage: ActorRef[Message], provider: ActorRef[BiSProviderMessage])(implicit
  timeout: Timeout,
  scheduler: Scheduler
) {

  private val basePartyView = new BasePartyView(storage, provider)
  private val indexView = new IndexView(storage, provider)

  private val biSView = new BiSView(storage, provider)
  private val lootView = new LootView(storage)
  private val lootSuggestView = new LootSuggestView(storage)
  private val playerView = new PlayerView(storage, provider)
  private val userView = new UserView(storage)

  def route: Route =
    basePartyView.route ~ indexView.route ~
      biSView.route ~ lootView.route ~ lootSuggestView.route ~ playerView.route ~ userView.route
}

object RootView {

  def toHtml(template: String): HttpEntity.Strict =
    HttpEntity(ContentTypes.`text/html(UTF-8)`, template)
}
