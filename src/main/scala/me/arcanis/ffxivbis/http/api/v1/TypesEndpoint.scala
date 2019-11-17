/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.config.Config
import io.swagger.v3.oas.annotations.media.{ArraySchema, Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import javax.ws.rs._
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.models.{Job, Party, Permission, Piece}

@Path("api/v1")
class TypesEndpoint(config: Config) extends JsonSupport {

  def route: Route = getJobs ~ getPermissions ~ getPieces ~ getPriority

  @GET
  @Path("types/jobs")
  @Produces(value = Array("application/json"))
  @Operation(summary = "jobs list", description = "Returns the available jobs",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of available jobs",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[String]))
        ))),
      new ApiResponse(responseCode = "500", description = "Internal server error",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorResponse])))),
    ),
    tags = Array("types"),
  )
  def getJobs: Route =
    path("types" / "jobs") {
      get {
        complete(Job.available.map(_.toString))
      }
    }

  @GET
  @Path("types/permissions")
  @Produces(value = Array("application/json"))
  @Operation(summary = "permissions list", description = "Returns the available permissions",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of available permissions",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[String]))
        ))),
      new ApiResponse(responseCode = "500", description = "Internal server error",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorResponse])))),
    ),
    tags = Array("types"),
  )
  def getPermissions: Route =
    path("types" / "permissions") {
      get {
        complete(Permission.values.toSeq.sorted.map(_.toString))
      }
    }

  @GET
  @Path("types/pieces")
  @Produces(value = Array("application/json"))
  @Operation(summary = "pieces list", description = "Returns the available pieces",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of available pieces",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[String]))
        ))),
      new ApiResponse(responseCode = "500", description = "Internal server error",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorResponse])))),
    ),
    tags = Array("types"),
  )
  def getPieces: Route =
    path("types" / "pieces") {
      get {
        complete(Piece.available)
      }
    }

  @GET
  @Path("types/priority")
  @Produces(value = Array("application/json"))
  @Operation(summary = "priority list", description = "Returns the current priority list",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Priority order",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[String]))
        ))),
      new ApiResponse(responseCode = "500", description = "Internal server error",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorResponse])))),
    ),
    tags = Array("types"),
  )
  def getPriority: Route =
    path("types" / "priority") {
      get {
        complete(Party.getRules(config))
      }
    }
}
