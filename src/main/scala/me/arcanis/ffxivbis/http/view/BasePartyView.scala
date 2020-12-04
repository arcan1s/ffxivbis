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
import akka.http.scaladsl.server._
import akka.util.Timeout
import me.arcanis.ffxivbis.http.{Authorization, PlayerHelper}
import me.arcanis.ffxivbis.messages.{BiSProviderMessage, Message}

import scala.util.{Failure, Success}

class BasePartyView(override val storage: ActorRef[Message],
                    override val provider: ActorRef[BiSProviderMessage])
                   (implicit timeout: Timeout, scheduler: Scheduler)
  extends PlayerHelper with Authorization {

  def route: Route = getIndex

  def getIndex: Route =
    path("party" / Segment) { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            onComplete(getPartyDescription(partyId)) {
              case Success(description) =>
                complete(StatusCodes.OK, RootView.toHtml(BasePartyView.template(partyId, description.alias)))
              case Failure(exception) => throw exception
            }
          }
        }
      }
    }
}

object BasePartyView {
  import scalatags.Text
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title => titleTag}

  def root(partyId: String): Text.TypedTag[String] =
    a(href:=s"/party/$partyId", title:="root")("root")

  def template(partyId: String, alias: String): String =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(lang:="en",
        head(
          titleTag(s"Party $alias"),
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          h2(s"Party $alias"),
          br,
          h2(a(href:=s"/party/$partyId/players", title:="party")("party")),
          h2(a(href:=s"/party/$partyId/bis", title:="bis management")("best in slot")),
          h2(a(href:=s"/party/$partyId/loot", title:="loot management")("loot")),
          h2(a(href:=s"/party/$partyId/suggest", title:="suggest loot")("suggest")),
          hr,
          h2(a(href:=s"/party/$partyId/users", title:="user management")("users"))
        )
      )
}
