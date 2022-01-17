/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1

import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.config.Config
import me.arcanis.ffxivbis.http.AuthorizationProvider
import me.arcanis.ffxivbis.http.api.v1.json.JsonSupport
import me.arcanis.ffxivbis.messages.{BiSProviderMessage, Message}

class RootApiV1Endpoint(
  storage: ActorRef[Message],
  auth: AuthorizationProvider,
  provider: ActorRef[BiSProviderMessage],
  config: Config
)(implicit
  timeout: Timeout,
  scheduler: Scheduler
) extends JsonSupport
  with HttpHandler {

  private val biSEndpoint = new BiSEndpoint(storage, provider, auth)
  private val lootEndpoint = new LootEndpoint(storage, auth)
  private val partyEndpoint = new PartyEndpoint(storage, provider, auth)
  private val playerEndpoint = new PlayerEndpoint(storage, provider, auth)
  private val statusEndpoint = new StatusEndpoint
  private val typesEndpoint = new TypesEndpoint(config)
  private val userEndpoint = new UserEndpoint(storage, auth)

  def route: Route =
    handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        biSEndpoint.route ~ lootEndpoint.route ~ partyEndpoint.route ~ playerEndpoint.route ~
          statusEndpoint.route ~ typesEndpoint.route ~ userEndpoint.route
      }
    }
}
