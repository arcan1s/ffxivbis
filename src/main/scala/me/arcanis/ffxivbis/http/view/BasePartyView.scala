package me.arcanis.ffxivbis.http.view

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import me.arcanis.ffxivbis.http.Authorization

class BasePartyView(override val storage: ActorRef)(implicit timeout: Timeout)
  extends Authorization {

  def route: Route = getIndex

  def getIndex: Route =
    path("party" / Segment) { partyId =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            complete {
              (StatusCodes.OK, RootView.toHtml(BasePartyView.template(partyId)))
            }
          }
        }
      }
    }
}

object BasePartyView {
  import scalatags.Text
  import scalatags.Text.all._

  def root(partyId: String): Text.TypedTag[String] =
    a(href:=s"/party/$partyId", title:="root")("root")

  def template(partyId: String): String =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(lang:="en",
        head(
          title:=s"Party $partyId",
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          h2(s"Party $partyId"),
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
