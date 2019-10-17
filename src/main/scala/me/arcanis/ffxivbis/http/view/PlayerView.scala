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
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import me.arcanis.ffxivbis.http.{Authorization, PlayerHelper}
import me.arcanis.ffxivbis.models.{BiS, Job, Player, PlayerId, PlayerIdWithCounters}

import scala.concurrent.{ExecutionContext, Future}

class PlayerView(override val storage: ActorRef, ariyala: ActorRef)(implicit timeout: Timeout)
  extends PlayerHelper(storage, ariyala) with Authorization {

  def route: Route = getParty ~ modifyParty

  def getParty: Route =
    path("party" / Segment / "players") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            complete {
              getPlayers(partyId, None).map { players =>
                PlayerView.template(partyId, players.map(_.withCounters(None)), None)
              }.map { text =>
                (StatusCodes.OK, RootView.toHtml(text))
              }
            }
          }
        }
      }
    }

  def modifyParty: Route =
    path("party" / Segment / "players") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          post {
            formFields("nick".as[String], "job".as[String], "priority".as[Int].?, "link".as[String].?, "action".as[String]) {
              (nick, job, maybePriority, maybeLink, action) =>
                onComplete(modifyPartyCall(partyId, nick, job, maybePriority, maybeLink, action)) {
                  case _ => redirect(s"/party/$partyId/players", StatusCodes.Found)
                }
            }
          }
        }
      }
    }

  private def modifyPartyCall(partyId: String, nick: String, job: String,
                              maybePriority: Option[Int], maybeLink: Option[String],
                              action: String)
                             (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Unit] = {
    def maybePlayerId = PlayerId(partyId, Some(nick), Some(job))
    def player(playerId: PlayerId) =
      Player(partyId, playerId.job, playerId.nick, BiS(), Seq.empty, maybeLink, maybePriority.getOrElse(0))

    (action, maybePlayerId) match {
      case ("add", Some(playerId)) => addPlayer(player(playerId)).map(_ => ())
      case ("remove", Some(playerId)) => removePlayer(playerId).map(_ => ())
      case _ => Future.failed(new Error(s"Could not perform $action with $nick ($job)"))
    }
  }
}

object PlayerView {
  import scalatags.Text.all._

  def template(partyId: String, party: Seq[PlayerIdWithCounters], error: Option[String]): String =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(lang:="en",
        head(
          title:="Party",
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          h2("Party"),

          ErrorView.template(error),
          SearchLineView.template,

          form(action:=s"/party/$partyId/players", method:="post")(
            input(name:="nick", id:="nick", placeholder:="nick", title:="nick", `type`:="nick"),
            select(name:="job", id:="job", title:="job")
                  (for (job <- Job.groupAll) yield option(job.toString)),
            input(name:="link", id:="link", placeholder:="player bis link", title:="link", `type`:="text"),
            input(name:="prioiry", id:="priority", placeholder:="priority", title:="priority", `type`:="number", value:="0"),
            input(name:="action", id:="action", `type`:="hidden", value:="add"),
            input(name:="add", id:="add", `type`:="submit", value:="add")
          ),

          table(id:="result")(
            tr(
              th("nick"),
              th("job"),
              th("total bis pieces looted"),
              th("total pieces looted"),
              th("priority"),
              th("")
            ),
            for (player <- party) yield tr(
              td(`class`:="include_search")(player.nick),
              td(`class`:="include_search")(player.job.toString),
              td(player.lootCountBiS),
              td(player.lootCountTotal),
              td(player.priority),
              td(
                form(action:=s"/party/$partyId/players", method:="post")(
                  input(name:="nick", id:="nick", `type`:="hidden", value:=player.nick),
                  input(name:="job", id:="job", `type`:="hidden", value:=player.job.toString),
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
