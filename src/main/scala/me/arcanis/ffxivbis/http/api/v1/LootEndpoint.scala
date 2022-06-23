/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1

import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
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
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.http.helpers.LootHelper
import me.arcanis.ffxivbis.http.{Authorization, AuthorizationProvider}
import me.arcanis.ffxivbis.messages.Message
import me.arcanis.ffxivbis.models.PlayerId

import scala.util.{Failure, Success}

@Path("/api/v1")
class LootEndpoint(override val storage: ActorRef[Message], override val auth: AuthorizationProvider)(implicit
  timeout: Timeout,
  scheduler: Scheduler
) extends LootHelper
  with Authorization
  with JsonSupport
  with HttpHandler {

  def routes: Route = getLoot ~ modifyLoot ~ suggestLoot

  @GET
  @Path("party/{partyId}/loot")
  @Produces(value = Array("application/json"))
  @Operation(
    summary = "get loot list",
    description = "Return the looted items",
    parameters = Array(
      new Parameter(
        name = "partyId",
        in = ParameterIn.PATH,
        description = "unique party ID",
        example = "o3KicHQPW5b0JcOm5yI3"
      ),
      new Parameter(
        name = "nick",
        in = ParameterIn.QUERY,
        description = "player nick name to filter",
        example = "Siuan Sanche"
      ),
      new Parameter(name = "job", in = ParameterIn.QUERY, description = "player job to filter", example = "DNC"),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Loot list",
        content = Array(
          new Content(
            array = new ArraySchema(schema = new Schema(implementation = classOf[PlayerModel]))
          )
        )
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
  def getLoot: Route =
    path("party" / Segment / "loot") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            parameters("nick".as[String].?, "job".as[String].?) { (maybeNick, maybeJob) =>
              val playerId = PlayerId(partyId, maybeNick, maybeJob)
              onSuccess(loot(partyId, playerId)) { response =>
                complete(response.map(PlayerModel.fromPlayer))
              }
            }
          }
        }
      }
    }

  @POST
  @Consumes(value = Array("application/json"))
  @Path("party/{partyId}/loot")
  @Operation(
    summary = "modify loot list",
    description = "Add or remove an item from the loot list",
    parameters = Array(
      new Parameter(
        name = "partyId",
        in = ParameterIn.PATH,
        description = "unique party ID",
        example = "o3KicHQPW5b0JcOm5yI3"
      ),
    ),
    requestBody = new RequestBody(
      description = "action and piece description",
      required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[PieceActionModel])))
    ),
    responses = Array(
      new ApiResponse(responseCode = "202", description = "Loot list has been modified"),
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
    security = Array(new SecurityRequirement(name = "basic", scopes = Array("post"))),
    tags = Array("loot"),
  )
  def modifyLoot: Route =
    path("party" / Segment / "loot") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          post {
            entity(as[PieceActionModel]) { action =>
              val playerId = action.playerId.withPartyId(partyId)
              onSuccess(doModifyLoot(action.action, playerId, action.piece.toPiece, action.isFreeLoot)) {
                complete(StatusCodes.Accepted, HttpEntity.Empty)
              }
            }
          }
        }
      }
    }

  @PUT
  @Path("party/{partyId}/loot")
  @Consumes(value = Array("application/json"))
  @Produces(value = Array("application/json"))
  @Operation(
    summary = "suggest loot",
    description = "Suggest loot piece to party",
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
            entity(as[PieceModel]) { piece =>
              onSuccess(suggestPiece(partyId, piece.toPiece)) { response =>
                complete(response.map(PlayerIdWithCountersModel.fromPlayerId))
              }
            }
          }
        }
      }
    }
}
