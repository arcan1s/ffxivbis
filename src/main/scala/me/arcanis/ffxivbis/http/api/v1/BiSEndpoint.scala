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
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import javax.ws.rs.{Consumes, GET, POST, PUT, Path, Produces}
import me.arcanis.ffxivbis.http.{Authorization, BiSHelper}
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.models.PlayerId

@Path("api/v1")
class BiSEndpoint(override val storage: ActorRef, ariyala: ActorRef)(implicit timeout: Timeout)
  extends BiSHelper(storage, ariyala) with Authorization with JsonSupport {

  def route: Route = createBiS ~ getBiS ~ modifyBiS

  @PUT
  @Path("party/{partyId}/bis")
  @Consumes(value = Array("application/json"))
  @Operation(summary = "create best in slot", description = "Create the best in slot set",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    requestBody = new RequestBody(description = "player best in slot description", required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[PlayerBiSLinkResponse])))),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "Best in slot set has been created"),
      new ApiResponse(responseCode = "400", description = "Invalid parameters were supplied"),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("post"))),
    tags = Array("best in slot"),
  )
  def createBiS: Route =
    path("party" / Segment / "bis") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          put {
            entity(as[PlayerBiSLinkResponse]) { bisLink =>
              val playerId = bisLink.playerId.withPartyId(partyId)
              complete(putBiS(playerId, bisLink.link).map(_ => (StatusCodes.Created, HttpEntity.Empty)))
            }
          }
        }
      }
    }

  @GET
  @Path("party/{partyId}/bis")
  @Produces(value = Array("application/json"))
  @Operation(summary = "get best in slot", description = "Return the best in slot items",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
      new Parameter(name = "nick", in = ParameterIn.QUERY, description = "player nick name to filter", example = "Siuan Sanche"),
      new Parameter(name = "job", in = ParameterIn.QUERY, description = "player job to filter", example = "DNC"),
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Best in slot",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[PlayerResponse]))
        ))),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("get"))),
    tags = Array("best in slot"),
  )
  def getBiS: Route =
    path("party" / Segment / "bis") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            parameters("nick".as[String].?, "job".as[String].?) { (maybeNick, maybeJob) =>
              val playerId = PlayerId(partyId, maybeNick, maybeJob)
              complete(bis(partyId, playerId).map(_.map(PlayerResponse.fromPlayer)))
            }
          }
        }
      }
    }

  @POST
  @Path("party/{partyId}/bis")
  @Consumes(value = Array("application/json"))
  @Operation(summary = "modify best in slot", description = "Add or remove an item from the best in slot",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    requestBody = new RequestBody(description = "action and piece description", required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[PieceActionResponse])))),
    responses = Array(
      new ApiResponse(responseCode = "202", description = "Best in slot set has been modified"),
      new ApiResponse(responseCode = "400", description = "Invalid parameters were supplied"),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("post"))),
    tags = Array("best in slot"),
  )
  def modifyBiS: Route =
    path("party" / Segment / "bis") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          post {
            entity(as[PieceActionResponse]) { action =>
              val playerId = action.playerIdResponse.withPartyId(partyId)
              complete {
                val result = action.action match {
                  case ApiAction.add => addPieceBiS(playerId, action.piece.toPiece)
                  case ApiAction.remove => removePieceBiS(playerId, action.piece.toPiece)
                }
                result.map(_ => (StatusCodes.Accepted, HttpEntity.Empty))
              }
            }
          }
        }
      }
    }
}
