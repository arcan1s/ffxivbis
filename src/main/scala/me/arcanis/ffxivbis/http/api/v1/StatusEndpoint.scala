/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs._
import me.arcanis.ffxivbis.http.api.v1.json._

@Path("/api/v1")
class StatusEndpoint extends JsonSupport {

  def route: Route = getServerStatus

  @GET
  @Path("status")
  @Produces(value = Array("application/json"))
  @Operation(
    summary = "server status",
    description = "Returns the server status descriptor",
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Service status descriptor",
        content = Array(new Content(schema = new Schema(implementation = classOf[StatusModel])))
      ),
      new ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorModel])))
      ),
    ),
    tags = Array("status"),
  )
  def getServerStatus: Route =
    path("status") {
      get {
        complete {
          StatusModel(
            version = Option(getClass.getPackage.getImplementationVersion),
          )
        }
      }
    }
}
