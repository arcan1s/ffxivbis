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
import me.arcanis.ffxivbis.http.{PlayerHelper, UserHelper}
import me.arcanis.ffxivbis.models.{PartyDescription, Permission, User}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class IndexView(override val storage: ActorRef, override val ariyala: ActorRef)(implicit timeout: Timeout)
  extends PlayerHelper with UserHelper {

  def route: Route = createParty ~ getIndex

  def createParty: Route =
    path("party") {
      extractExecutionContext { implicit executionContext =>
        post {
          formFields("username".as[String], "password".as[String], "alias".as[String].?) { (username, password, maybeAlias) =>
            onComplete {
              newPartyId.flatMap { partyId =>
                val user = User(partyId, username, password, Permission.admin)
                addUser(user, isHashedPassword = false).flatMap { _ =>
                  if (maybeAlias.getOrElse("").isEmpty) Future.successful(partyId)
                  else updateDescription(PartyDescription(partyId, maybeAlias)).map(_ => partyId)
                }
              }
            } {
              case Success(partyId) => redirect(s"/party/$partyId", StatusCodes.Found)
              case Failure(exception) => throw exception
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
          case _ => complete(StatusCodes.OK, RootView.toHtml(IndexView.template))
        }
      }
    }
}

object IndexView {
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title => titleTag}

  def template: String =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(
        head(
          titleTag("FFXIV loot helper"),
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          form(action:=s"party", method:="post")(
            label("create a new party"),
            input(name:="alias", id:="alias", placeholder:="party alias", title:="alias", `type`:="text"),
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

