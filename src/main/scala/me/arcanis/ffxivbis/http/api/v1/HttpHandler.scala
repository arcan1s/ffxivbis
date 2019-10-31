package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.api.v1.json._
import spray.json._

trait HttpHandler extends StrictLogging { this: JsonSupport =>

  implicit def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case other: Exception =>
      logger.error("exception during request completion", other)
      complete(StatusCodes.InternalServerError, ErrorResponse("unknown server error"))
  }

  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler.default
      .mapRejectionResponse {
        case response @ HttpResponse(_, _, entity: HttpEntity.Strict, _) =>
          val message = ErrorResponse(entity.data.utf8String).toJson
          response.copy(entity = HttpEntity(ContentTypes.`application/json`, message.compactPrint))
        case other => other
      }
}
