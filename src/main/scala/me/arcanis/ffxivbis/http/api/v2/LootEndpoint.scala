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
import akka.http.scaladsl.server._
import akka.util.Timeout
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{ArraySchema, Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs._
import me.arcanis.ffxivbis.http.api.v1.json.{ErrorModel, PieceModel, PlayerIdWithCountersModel}
import me.arcanis.ffxivbis.http.api.v2.json._
import me.arcanis.ffxivbis.http.helpers.LootHelper
import me.arcanis.ffxivbis.http.{Authorization, AuthorizationProvider}
import me.arcanis.ffxivbis.messages.Message

@Path("/api/v2")
class LootEndpoint(override val storage: ActorRef[Message], override val auth: AuthorizationProvider)(implicit
  timeout: Timeout,
  scheduler: Scheduler
) extends LootHelper
  with Authorization
  with JsonSupport
  with HttpHandler {

  def routes: Route = suggestLoot

  @PUT
  @Path("party/{partyId}/loot")
  @Consumes(value = Array("application/json"))
  @Produces(value = Array("application/json"))
  @Operation(
    summary = "suggest loot",
    description = "Suggest loot pieces to party",
    parameters = Array(
      new Parameter(
        name = "partyId",
        in = ParameterIn.PATH,
        description = "unique party ID",
        example = "o3KicHQPW5b0JcOm5yI3"
      ),
    ),
    requestBody = new RequestBody(
      description = "piece description",
      required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[PieceModel])))
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Players with counters ordered by priority to get this item",
        content = Array(
          new Content(
            array = new ArraySchema(schema = new Schema(implementation = classOf[PlayerIdWithCountersModel])),
          )
        )
      ),
      new ApiResponse(
        responseCode = "400",
        description = "Invalid parameters were supplied",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorModel])))
      ),
      new ApiResponse(
        responseCode = "401",
        description = "Supplied authorization is invalid",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorModel])))
      ),
      new ApiResponse(
        responseCode = "403",
        description = "Access is forbidden",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorModel])))
      ),
      new ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorModel])))
      ),
    ),
    security = Array(new SecurityRequirement(name = "basic", scopes = Array("get"))),
    tags = Array("loot"),
  )
  def suggestLoot: Route =
    path("party" / Segment / "loot") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          put {
            entity(as[PiecesModel]) { piece =>
              onSuccess(suggestPiece(partyId, piece.toPiece)) { response =>
                complete(response.map(PlayerIdWithCountersModel.fromPlayerId))
              }
            }
          }
        }
      }
    }
}
