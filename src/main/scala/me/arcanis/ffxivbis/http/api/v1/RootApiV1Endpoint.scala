/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.config.Config
import me.arcanis.ffxivbis.http.api.v1.json.JsonSupport

class RootApiV1Endpoint(storage: ActorRef, ariyala: ActorRef, config: Config)
                       (implicit timeout: Timeout)
  extends JsonSupport with HttpHandler {

  private val biSEndpoint = new BiSEndpoint(storage, ariyala)
  private val lootEndpoint = new LootEndpoint(storage)
  private val partyEndpoint = new PartyEndpoint(storage, ariyala)
  private val playerEndpoint = new PlayerEndpoint(storage, ariyala)
  private val typesEndpoint = new TypesEndpoint(config)
  private val userEndpoint = new UserEndpoint(storage)

  def route: Route =
    handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        biSEndpoint.route ~ lootEndpoint.route ~ partyEndpoint.route ~
          playerEndpoint.route ~ typesEndpoint.route ~ userEndpoint.route
      }
    }
}
