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
import me.arcanis.ffxivbis.http.{Authorization, LootHelper}
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.models.PlayerId

@Path("api/v1")
class LootEndpoint(override val storage: ActorRef)(implicit timeout: Timeout)
  extends LootHelper(storage) with Authorization with JsonSupport {

  def route: Route = getLoot ~ modifyLoot

  @GET
  @Path("party/{partyId}/loot")
  @Produces(value = Array("application/json"))
  @Operation(summary = "get loot list", description = "Return the looted items",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
      new Parameter(name = "nick", in = ParameterIn.QUERY, description = "player nick name to filter", example = "Siuan Sanche"),
      new Parameter(name = "job", in = ParameterIn.QUERY, description = "player job to filter", example = "DNC"),
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Loot list",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[PlayerResponse]))
        ))),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("get"))),
    tags = Array("loot"),
  )
  def getLoot: Route =
    path("party" / Segment / "loot") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            parameters("nick".as[String].?, "job".as[String].?) { (maybeNick, maybeJob) =>
              val playerId = PlayerId(partyId, maybeNick, maybeJob)
              complete(loot(partyId, playerId).map(_.map(PlayerResponse.fromPlayer)))
            }
          }
        }
      }
    }

  @POST
  @Consumes(value = Array("application/json"))
  @Path("party/{partyId}/loot")
  @Operation(summary = "modify loot list", description = "Add or remove an item from the loot list",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    requestBody = new RequestBody(description = "action and piece description", required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[PieceActionResponse])))),
    responses = Array(
      new ApiResponse(responseCode = "202", description = "Loot list has been modified"),
      new ApiResponse(responseCode = "400", description = "Invalid parameters were supplied"),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("post"))),
    tags = Array("loot"),
  )
  def modifyLoot: Route =
    path("party" / Segment / "loot") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          post {
            entity(as[PieceActionResponse]) { action =>
              val playerId = action.playerIdResponse.withPartyId(partyId)
              complete {
                val result = action.action match {
                  case ApiAction.add => addPieceLoot(playerId, action.piece.toPiece)
                  case ApiAction.remove => removePieceLoot(playerId, action.piece.toPiece)
                }
                result.map(_ => (StatusCodes.Accepted, HttpEntity.Empty))
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
  @Operation(summary = "suggest loot", description = "Suggest loot piece to party",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    requestBody = new RequestBody(description = "piece description", required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[PieceResponse])))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Players with counters ordered by priority to get this item",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[PlayerIdWithCountersResponse])),
        ))),
      new ApiResponse(responseCode = "400", description = "Invalid parameters were supplied"),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("get"))),
    tags = Array("loot"),
  )
  def suggestLoot: Route =
    path("party" / Segment / "loot") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          put {
            entity(as[PieceResponse]) { piece =>
              complete {
                suggestPiece(partyId, piece.toPiece).map { players =>
                  players.map(PlayerIdWithCountersResponse.fromPlayerId)
                }
              }
            }
          }
        }
      }
    }
}
