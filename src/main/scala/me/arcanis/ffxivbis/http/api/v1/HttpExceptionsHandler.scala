package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.api.v1.json._

trait HttpExceptionsHandler extends StrictLogging { this: JsonSupport =>

  def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case other: Exception =>
      logger.error("exception during request completion", other)
      complete(StatusCodes.InternalServerError, ErrorResponse("unknown server error"))
  }
}
