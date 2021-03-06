/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.view

import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import me.arcanis.ffxivbis.http.{Authorization, UserHelper}
import me.arcanis.ffxivbis.messages.Message
import me.arcanis.ffxivbis.models.{Permission, User}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class UserView(override val storage: ActorRef[Message])
              (implicit timeout: Timeout, scheduler: Scheduler)
  extends UserHelper with Authorization {

  def route: Route = getUsers ~ modifyUsers

  def getUsers: Route =
    path("party" / Segment / "users") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { _ =>
          get {
            complete {
              users(partyId).map { users =>
                UserView.template(partyId, users, None)
              }.map { text =>
                (StatusCodes.OK, RootView.toHtml(text))
              }
            }
          }
        }
      }
    }

  def modifyUsers: Route =
    path("party" / Segment / "users") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { _ =>
          post {
            formFields("username".as[String], "password".as[String].?, "permission".as[String].?, "action".as[String]) {
              (username, maybePassword, maybePermission, action) =>
                onComplete(modifyUsersCall(partyId, username, maybePassword, maybePermission, action)) {
                  case _ => redirect(s"/party/$partyId/users", StatusCodes.Found)
                }
            }
          }
        }
      }
    }

  private def modifyUsersCall(partyId: String, username: String,
                              maybePassword: Option[String], maybePermission: Option[String],
                              action: String)
                             (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Unit] = {
    def permission: Option[Permission.Value] =
      maybePermission.flatMap(p => Try(Permission.withName(p)).toOption)

    action match {
      case "add" => (maybePassword, permission) match {
        case (Some(password), Some(permission)) => addUser(User(partyId, username, password, permission), isHashedPassword = false)
        case _ => Future.failed(new Error(s"Could not construct permission/password from `$maybePermission`/`$maybePassword`"))
      }
      case "remove" => removeUser(partyId, username)
      case _ => Future.failed(new Error(s"Could not perform $action"))
    }
  }
}

object UserView {
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title => titleTag}

  def template(partyId: String, users: Seq[User], error: Option[String]) =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(lang:="en",
        head(
          titleTag("Users"),
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          h2("Users"),

          ErrorView.template(error),
          SearchLineView.template,

          form(action:=s"/party/$partyId/users", method:="post")(
            input(name:="username", id:="username", placeholder:="username", title:="username", `type`:="text"),
            input(name:="password", id:="password", placeholder:="password", title:="password", `type`:="password"),
            select(name:="permission", id:="permission", title:="permission")(option("get"), option("post")),
            input(name:="action", id:="action", `type`:="hidden", value:="add"),
            input(name:="add", id:="add", `type`:="submit", value:="add")
          ),

          table(id:="result")(
            tr(
              th("username"),
              th("permission"),
              th("")
            ),
            for (user <- users) yield tr(
              td(`class`:="include_search")(user.username),
              td(user.permission.toString),
              td(
                form(action:=s"/party/$partyId/users", method:="post")(
                  input(name:="username", id:="username", `type`:="hidden", value:=user.username.toString),
                  input(name:="action", id:="action", `type`:="hidden", value:="remove"),
                  input(name:="remove", id:="remove", `type`:="submit", value:="x")
                )
              )
            )
          ),

          ExportToCSVView.template,
          BasePartyView.root(partyId),
          script(src:="/static/table_search.js", `type`:="text/javascript")
        )
      )
}
