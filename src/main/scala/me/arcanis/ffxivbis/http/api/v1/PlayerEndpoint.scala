/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1

import akka.actor.ActorRef
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
import javax.ws.rs._
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.http.{Authorization, PlayerHelper}
import me.arcanis.ffxivbis.models.PlayerId

import scala.util.{Failure, Success}

@Path("api/v1")
class PlayerEndpoint(override val storage: ActorRef, ariyala: ActorRef)(implicit timeout: Timeout)
  extends PlayerHelper(storage, ariyala) with Authorization with JsonSupport with HttpHandler {

  def route: Route = getParty ~ modifyParty

  @GET
  @Path("party/{partyId}")
  @Produces(value = Array("application/json"))
  @Operation(summary = "get party", description = "Return the players who belong to the party",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
      new Parameter(name = "nick", in = ParameterIn.QUERY, description = "player nick name to filter", example = "Siuan Sanche"),
      new Parameter(name = "job", in = ParameterIn.QUERY, description = "player job to filter", example = "DNC"),
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Players list",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[PlayerResponse])),
        ))),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("get"))),
    tags = Array("party"),
  )
  def getParty: Route =
    path("party" / Segment) { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            parameters("nick".as[String].?, "job".as[String].?) { (maybeNick, maybeJob) =>
              val playerId = PlayerId(partyId, maybeNick, maybeJob)
              onComplete(getPlayers(partyId, playerId)) {
                case Success(response) => complete(response.map(PlayerResponse.fromPlayer))
                case Failure(exception) => throw exception
              }
            }
          }
        }
      }
    }

  @POST
  @Path("party/{partyId}")
  @Consumes(value = Array("application/json"))
  @Operation(summary = "modify party", description = "Add or remove a player from party list",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    requestBody = new RequestBody(description = "player description", required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[PlayerActionResponse])))),
    responses = Array(
      new ApiResponse(responseCode = "202", description = "Party has been modified"),
      new ApiResponse(responseCode = "400", description = "Invalid parameters were supplied"),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("post"))),
    tags = Array("party"),
  )
  def modifyParty: Route =
    path("party" / Segment) { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          entity(as[PlayerActionResponse]) { action =>
            val player = action.playerIdResponse.toPlayer.copy(partyId = partyId)
            onComplete(doModifyPlayer(action.action, player)) {
              case Success(_) => complete(StatusCodes.Accepted, HttpEntity.Empty)
              case Failure(exception) => throw exception
            }
          }
        }
      }
    }
}
