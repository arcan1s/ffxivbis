/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import java.time.Instant

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import com.typesafe.scalalogging.{Logger, StrictLogging}
import me.arcanis.ffxivbis.http.api.v1.RootApiV1Endpoint
import me.arcanis.ffxivbis.http.view.RootView

class RootEndpoint(system: ActorSystem, storage: ActorRef, ariyala: ActorRef)
  extends StrictLogging {
  import me.arcanis.ffxivbis.utils.Implicits._

  private val config = system.settings.config

  implicit val timeout: Timeout =
    config.getDuration("me.arcanis.ffxivbis.settings.request-timeout")

  private val rootApiV1Endpoint: RootApiV1Endpoint = new RootApiV1Endpoint(storage, ariyala, config)
  private val rootView: RootView = new RootView(storage, ariyala)
  private val swagger: Swagger = new Swagger(config)
  private val httpLogger = Logger("http")

  private val withHttpLog: Directive0 =
    extractRequestContext.flatMap { context =>
      val start = Instant.now.toEpochMilli
      mapResponse { response =>
        val time = (Instant.now.toEpochMilli - start) / 1000.0
        httpLogger.debug(s"""- - [${Instant.now}] "${context.request.method.name()} ${context.request.uri.path}" ${response.status.intValue()} ${response.entity.getContentLengthOption.getAsLong} $time""")
        response
      }
    }

  def route: Route =
    withHttpLog {
      apiRoute ~ htmlRoute ~ swagger.routes ~ swaggerUIRoute
    }

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
