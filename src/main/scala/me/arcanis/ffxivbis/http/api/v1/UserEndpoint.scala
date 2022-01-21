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
import me.arcanis.ffxivbis.http.helpers.UserHelper
import me.arcanis.ffxivbis.http.{Authorization, AuthorizationProvider}
import me.arcanis.ffxivbis.messages.Message
import me.arcanis.ffxivbis.models.Permission

import scala.util.{Failure, Success}

@Path("/api/v1")
class UserEndpoint(override val storage: ActorRef[Message], override val auth: AuthorizationProvider)(implicit
  timeout: Timeout,
  scheduler: Scheduler
) extends UserHelper
  with Authorization
  with JsonSupport {

  def routes: Route = createParty ~ createUser ~ deleteUser ~ getUsers ~ getUsersCurrent

  @POST
  @Path("party")
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "create new party",
    description = "Create new party with specified ID",
    requestBody = new RequestBody(
      description = "party administrator description",
      required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[UserModel])))
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Party has been created",
        content = Array(new Content(schema = new Schema(implementation = classOf[PartyIdModel])))
      ),
      new ApiResponse(
        responseCode = "400",
        description = "Invalid parameters were supplied",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorModel])))
      ),
      new ApiResponse(
        responseCode = "406",
        description = "Party with the specified ID already exists",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorModel])))
      ),
      new ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = Array(new Content(schema = new Schema(implementation = classOf[ErrorModel])))
      ),
    ),
    tags = Array("party"),
  )
  def createParty: Route =
    path("party") {
      extractExecutionContext { implicit executionContext =>
        post {
          entity(as[UserModel]) { user =>
            onSuccess(newPartyId) { partyId =>
              val admin = user.toUser.copy(partyId = partyId, permission = Permission.admin)
              onSuccess(addUser(admin, isHashedPassword = false)) {
                complete(PartyIdModel(partyId))
              }
            }
          }
        }
      }
    }

  @POST
  @Path("party/{partyId}/users")
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "create new user",
    description = "Add an user to the specified party",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    requestBody = new RequestBody(
      description = "user description",
      required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[UserModel])))
    ),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "User has been created"),
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
    security = Array(new SecurityRequirement(name = "basic", scopes = Array("admin"))),
    tags = Array("users"),
  )
  def createUser: Route =
    path("party" / Segment / "users") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { _ =>
          post {
            entity(as[UserModel]) { user =>
              val withPartyId = user.toUser.copy(partyId = partyId)
              onSuccess(addUser(withPartyId, isHashedPassword = false)) {
                complete(StatusCodes.Accepted, HttpEntity.Empty)
              }
            }
          }
        }
      }
    }

  @DELETE
  @Path("party/{partyId}/users/{username}")
  @Operation(
    summary = "remove user",
    description = "Remove an user from the specified party",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
      new Parameter(name = "username", in = ParameterIn.PATH, description = "username to remove", example = "siuan"),
    ),
    responses = Array(
      new ApiResponse(responseCode = "202", description = "User has been removed"),
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
    security = Array(new SecurityRequirement(name = "basic", scopes = Array("admin"))),
    tags = Array("users"),
  )
  def deleteUser: Route =
    path("party" / Segment / "users" / Segment) { (partyId, username) =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { _ =>
          delete {
            onSuccess(removeUser(partyId, username)) {
              complete(StatusCodes.Accepted, HttpEntity.Empty)
            }
          }
        }
      }
    }

  @GET
  @Path("party/{partyId}/users")
  @Produces(value = Array("application/json"))
  @Operation(
    summary = "get users",
    description = "Return the list of users belong to party",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Users list",
        content = Array(
          new Content(
            array = new ArraySchema(schema = new Schema(implementation = classOf[UserModel])),
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
    tags = Array("users"),
  )
  def getUsers: Route =
    path("party" / Segment / "users") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { _ =>
          get {
            onSuccess(users(partyId)) { response =>
              complete(response.map(UserModel.fromUser))
            }
          }
        }
      }
    }

  @GET
  @Path("party/{partyId}/users/current")
  @Produces(value = Array("application/json"))
  @Operation(
    summary = "get current user",
    description = "Return the current user descriptor",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "User descriptor",
        content = Array(new Content(schema = new Schema(implementation = classOf[UserModel])))
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
    security = Array(new SecurityRequirement(name = "basic", scopes = Array("admin"))),
    tags = Array("users"),
  )
  def getUsersCurrent: Route =
    path("party" / Segment / "users" / "current") { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { user =>
          get {
            complete(UserModel.fromUser(user))
          }
        }
      }
    }
}
