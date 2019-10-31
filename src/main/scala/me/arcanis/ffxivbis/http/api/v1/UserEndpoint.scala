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
import javax.ws.rs.{Consumes, DELETE, GET, POST, PUT, Path, Produces}
import me.arcanis.ffxivbis.http.{Authorization, UserHelper}
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.models.Permission

import scala.util.{Failure, Success}

@Path("api/v1")
class UserEndpoint(override val storage: ActorRef)(implicit timeout: Timeout)
  extends UserHelper(storage) with Authorization with JsonSupport with HttpHandler {

  def route: Route = createParty ~ createUser ~ deleteUser ~ getUsers

  @PUT
  @Path("party/{partyId}/create")
  @Consumes(value = Array("application/json"))
  @Operation(summary = "create new party", description = "Create new party with specified ID",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    requestBody = new RequestBody(description = "party administrator description", required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[UserResponse])))),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "Party has been created"),
      new ApiResponse(responseCode = "400", description = "Invalid parameters were supplied"),
      new ApiResponse(responseCode = "406", description = "Party with the specified ID already exists"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    tags = Array("party"),
  )
  def createParty: Route =
    path("party" / Segment / "create") { partyId =>
      handleExceptions(exceptionHandler) {
        handleRejections(rejectionHandler) {
          extractExecutionContext { implicit executionContext =>
            put {
              entity(as[UserResponse]) { user =>
                val admin = user.toUser.copy(partyId = partyId, permission = Permission.admin)
                onComplete(addUser(admin, isHashedPassword = false)) {
                  case Success(_) => complete(StatusCodes.Created, HttpEntity.Empty)
                  case Failure(exception) => throw exception
                }
              }
            }
          }
        }
      }
    }

  @POST
  @Path("party/{partyId}/users")
  @Consumes(value = Array("application/json"))
  @Operation(summary = "create new user", description = "Add an user to the specified party",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    requestBody = new RequestBody(description = "user description", required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[UserResponse])))),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "User has been created"),
      new ApiResponse(responseCode = "400", description = "Invalid parameters were supplied"),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("admin"))),
    tags = Array("users"),
  )
  def createUser: Route =
    path("party" / Segment / "users") { partyId =>
      handleExceptions(exceptionHandler) {
        handleRejections(rejectionHandler) {
          extractExecutionContext { implicit executionContext =>
            authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { _ =>
              post {
                entity(as[UserResponse]) { user =>
                  val withPartyId = user.toUser.copy(partyId = partyId)
                  onComplete(addUser(withPartyId, isHashedPassword = false)) {
                    case Success(_) => complete(StatusCodes.Accepted, HttpEntity.Empty)
                    case Failure(exception) => throw exception
                  }
                }
              }
            }
          }
        }
      }
    }

  @DELETE
  @Path("party/{partyId}/users/{username}")
  @Operation(summary = "remove user", description = "Remove an user from the specified party",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
      new Parameter(name = "username", in = ParameterIn.PATH, description = "username to remove", example = "siuan"),
    ),
    responses = Array(
      new ApiResponse(responseCode = "202", description = "User has been removed"),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("admin"))),
    tags = Array("users"),
  )
  def deleteUser: Route =
    path("party" / Segment / "users" / Segment) { (partyId, username) =>
      handleExceptions(exceptionHandler) {
        handleRejections(rejectionHandler) {
          extractExecutionContext { implicit executionContext =>
            authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { _ =>
              delete {
                onComplete(removeUser(partyId, username)) {
                  case Success(_) => complete(StatusCodes.Accepted, HttpEntity.Empty)
                  case Failure(exception) => throw exception
                }
              }
            }
          }
        }
      }
    }

  @GET
  @Path("party/{partyId}/users")
  @Produces(value = Array("application/json"))
  @Operation(summary = "get users", description = "Return the list of users belong to party",
    parameters = Array(
      new Parameter(name = "partyId", in = ParameterIn.PATH, description = "unique party ID", example = "abcdefgh"),
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Users list",
        content = Array(new Content(
          array = new ArraySchema(schema = new Schema(implementation = classOf[UserResponse])),
        ))),
      new ApiResponse(responseCode = "401", description = "Supplied authorization is invalid"),
      new ApiResponse(responseCode = "403", description = "Access is forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ),
    security = Array(new SecurityRequirement(name = "basic auth", scopes = Array("admin"))),
    tags = Array("users"),
  )
  def getUsers: Route =
    path("party" / Segment / "users") { partyId =>
      handleExceptions(exceptionHandler) {
        handleRejections(rejectionHandler) {
          extractExecutionContext { implicit executionContext =>
            authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { _ =>
              get {
                onComplete(users(partyId)) {
                  case Success(response) => complete(response.map(UserResponse.fromUser))
                  case Failure(exception) => throw exception
                }
              }
            }
          }
        }
      }
    }
}
