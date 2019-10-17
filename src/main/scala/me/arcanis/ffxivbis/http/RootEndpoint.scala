/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.api.v1.RootApiV1Endpoint
import me.arcanis.ffxivbis.http.view.RootView

class RootEndpoint(system: ActorSystem, storage: ActorRef, ariyala: ActorRef)
  extends StrictLogging {
  import me.arcanis.ffxivbis.utils.Implicits._

  private val config = system.settings.config

  implicit val timeout: Timeout =
    config.getDuration("me.arcanis.ffxivbis.settings.request-timeout")

  private val rootApiV1Endpoint: RootApiV1Endpoint = new RootApiV1Endpoint(storage, ariyala)
  private val rootView: RootView = new RootView(storage, ariyala)

  def route: Route = apiRoute ~ htmlRoute ~ Swagger.routes ~ swaggerUIRoute

  private def apiRoute: Route =
    ignoreTrailingSlash {
      pathPrefix("api") {
        pathPrefix(Segment) {
          case "v1" => rootApiV1Endpoint.route
          case _ => reject
        }
      }
    }

  private def htmlRoute: Route =
    ignoreTrailingSlash {
      pathPrefix("static") {
        getFromResourceDirectory("static")
      } ~ rootView.route
    }

  private def swaggerUIRoute: Route =
    path("swagger") {
      getFromResource("swagger/index.html")
    } ~ getFromResourceDirectory("swagger")
}
