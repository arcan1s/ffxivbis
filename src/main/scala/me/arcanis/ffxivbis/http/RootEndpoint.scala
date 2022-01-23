/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.api.v1.RootApiV1Endpoint
import me.arcanis.ffxivbis.http.view.RootView
import me.arcanis.ffxivbis.messages.{BiSProviderMessage, Message}

class RootEndpoint(system: ActorSystem[Nothing], storage: ActorRef[Message], provider: ActorRef[BiSProviderMessage])
  extends StrictLogging
  with HttpLog {
  import me.arcanis.ffxivbis.utils.Implicits._

  private val config = system.settings.config

  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = config.getTimeout("me.arcanis.ffxivbis.settings.request-timeout")

  private val auth = AuthorizationProvider(config, storage)

  private val rootApiV1Endpoint = new RootApiV1Endpoint(storage, auth, provider, config)
  private val rootView = new RootView(auth)
  private val swagger = new Swagger(config)

  def routes: Route =
    withHttpLog {
      ignoreTrailingSlash {
        cors() {
          apiRoutes ~ htmlRoutes ~ swagger.routes ~ swaggerUIRoutes
        }
      }
    }

  private def apiRoutes: Route =
    pathPrefix("api") {
      pathPrefix(Segment) {
        case "v1" => rootApiV1Endpoint.routes
        case _ => reject
      }
    }

  private def htmlRoutes: Route =
    pathPrefix("static") {
      getFromResourceDirectory("static")
    } ~ rootView.routes

  private def swaggerUIRoutes: Route =
    path("api-docs") {
      getFromResource("html/api.html")
    }
}
