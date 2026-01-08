/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v2

import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import me.arcanis.ffxivbis.http.AuthorizationProvider
import me.arcanis.ffxivbis.http.api.v2.json.JsonSupport
import me.arcanis.ffxivbis.messages.Message

class RootApiV2Endpoint(
  storage: ActorRef[Message],
  auth: AuthorizationProvider,
)(implicit
  timeout: Timeout,
  scheduler: Scheduler
) extends JsonSupport
  with HttpHandler {

  private val lootEndpoint = new LootEndpoint(storage, auth)

  def routes: Route =
    handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        lootEndpoint.routes
      }
    }
}
