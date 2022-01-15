/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.api.v1.json._
import spray.json._

trait HttpHandler extends StrictLogging { this: JsonSupport =>

  def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case ex: IllegalArgumentException =>
      complete(StatusCodes.BadRequest, ErrorModel(ex.getMessage))

    case other: Exception =>
      logger.error("exception during request completion", other)
      complete(StatusCodes.InternalServerError, ErrorModel("unknown server error"))
  }

  def rejectionHandler: RejectionHandler =
    RejectionHandler.default
      .mapRejectionResponse {
        case response @ HttpResponse(_, _, entity: HttpEntity.Strict, _) =>
          val message = ErrorModel(entity.data.utf8String).toJson
          response.withEntity(HttpEntity(ContentTypes.`application/json`, message.compactPrint))
        case other => other
      }
}
