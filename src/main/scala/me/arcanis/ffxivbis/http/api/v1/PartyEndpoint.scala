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
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs._
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.http.helpers.PlayerHelper
import me.arcanis.ffxivbis.http.{Authorization, AuthorizationProvider}
import me.arcanis.ffxivbis.messages.{BiSProviderMessage, Message}

import scala.util.{Failure, Success}

@Path("/api/v1")
class PartyEndpoint(
  override val storage: ActorRef[Message],
  override val provider: ActorRef[BiSProviderMessage],
  override val auth: AuthorizationProvider
)(implicit
  timeout: Timeout,
  scheduler: Scheduler
) extends PlayerHelper
  with Authorization
  with JsonSupport
  with HttpHandler {

  def routes: Route = getPartyDescription ~ modifyPartyDescription

  @GET
  @Path("party/{partyId}/description")
  @Produces(value = Array("application/json"))
  @Operation(
    summary = "get party description",
    description = "Return the party description",
    parameters = Array(
      new Parameter(
        name = "partyId",
        in = ParameterIn.PATH,
        description = "unique party ID",
        example = "o3KicHQPW5b0JcOm5yI3"
      ),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Party description",
        content = Array(new Content(schema = new Schema(implementation = classOf[PartyDescriptionModel])))
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
    tags = Array("party"),
  )
  def getPartyDescription: Route =
    path("party" / Segment / "description") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            onSuccess(getPartyDescription(partyId)) { response =>
              complete(PartyDescriptionModel.fromDescription(response))
            }
          }
        }
      }
    }

  @POST
  @Consumes(value = Array("application/json"))
  @Path("party/{partyId}/description")
  @Operation(
    summary = "modify party description",
    description = "Edit party description",
    parameters = Array(
      new Parameter(
        name = "partyId",
        in = ParameterIn.PATH,
        description = "unique party ID",
        example = "o3KicHQPW5b0JcOm5yI3"
      ),
    ),
    requestBody = new RequestBody(
      description = "new party description",
      required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[PartyDescriptionModel])))
    ),
    responses = Array(
      new ApiResponse(responseCode = "202", description = "Party description has been modified"),
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
    tags = Array("party"),
  )
  def modifyPartyDescription: Route =
    path("party" / Segment / "description") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          post {
            entity(as[PartyDescriptionModel]) { partyDescription =>
              val description = partyDescription.copy(partyId = partyId)
              onSuccess(updateDescription(description.toDescription)) {
                complete(StatusCodes.Accepted, HttpEntity.Empty)
              }
            }
          }
        }
      }
    }
}
