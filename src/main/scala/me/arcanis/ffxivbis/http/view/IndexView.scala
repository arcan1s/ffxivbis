/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.view

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import me.arcanis.ffxivbis.http.UserHelper
import me.arcanis.ffxivbis.models.{Party, Permission, User}

class IndexView(storage: ActorRef)(implicit timeout: Timeout)
  extends UserHelper(storage) {

  def route: Route = createParty ~ getIndex

  def createParty: Route =
    path("party" / Segment / "create") { partyId =>
      extractExecutionContext { implicit executionContext =>
        post {
          formFields("username".as[String], "password".as[String]) { (username, password) =>
            val user = User(partyId, username, password, Permission.admin)
            onComplete(addUser(user, isHashedPassword = false)) {
              case _ => redirect(s"/party/$partyId", StatusCodes.Found)
            }
          }
        }
      }
    }

  def getIndex: Route =
    pathEndOrSingleSlash {
      get {
        parameters("partyId".as[String].?) {
          case Some(partyId) => redirect(s"/party/$partyId", StatusCodes.Found)
          case _ => complete((StatusCodes.OK, RootView.toHtml(IndexView.template)))
        }
      }
    }
}

object IndexView {
  import scalatags.Text.all._

  def template: String =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(
        head(
          title:="FFXIV loot helper",
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          form(action:=s"party/${Party.randomPartyId}/create", method:="post")(
            label("create a new party"),
            input(name:="username", id:="username", placeholder:="username", title:="username", `type`:="text"),
            input(name:="password", id:="password", placeholder:="password", title:="password", `type`:="password"),
            input(name:="add", id:="add", `type`:="submit", value:="add")
          ),

          br,

          form(action:="/", method:="get")(
            label("already have party?"),
            input(name:="partyId", id:="partyId", placeholder:="party id", title:="party id", `type`:="text"),
            input(name:="go", id:="go", `type`:="submit", value:="go")
          )
        )
      )
}

