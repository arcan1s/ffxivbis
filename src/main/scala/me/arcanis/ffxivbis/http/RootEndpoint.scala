package me.arcanis.ffxivbis.http

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.api.v1.ApiV1Endpoint

class RootEndpoint(system: ActorSystem, storage: ActorRef, ariyala: ActorRef)
  extends StrictLogging {
  import me.arcanis.ffxivbis.utils.Implicits._

  private val config = system.settings.config

  implicit val timeout: Timeout =
    config.getDuration("me.arcanis.ffxivbis.settings.request-timeout")

  private val apiV1Endpoint: ApiV1Endpoint = new ApiV1Endpoint(storage, ariyala)

  def route: Route = apiRoute ~ htmlRoute ~ Swagger.routes ~ swaggerUIRoute

  private def apiRoute: Route =
    ignoreTrailingSlash {
      pathPrefix("api") {
        pathPrefix(Segment) {
          case "v1" => apiV1Endpoint.route
          case _ => reject
        }
      }
    }

  private def htmlRoute: Route =
    ignoreTrailingSlash {
      pathEndOrSingleSlash {
        complete(StatusCodes.OK)
      }
    }

  private def swaggerUIRoute: Route =
    path("swagger") {
      getFromResource("swagger/index.html")
    } ~ getFromResourceDirectory("swagger")
}
