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
import me.arcanis.ffxivbis.http.{Authorization, LootHelper}
import me.arcanis.ffxivbis.models.{Piece, Player, PlayerId}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class LootView (override val storage: ActorRef)(implicit timeout: Timeout)
  extends LootHelper(storage) with Authorization {

  def route: Route = getLoot ~ modifyLoot

  def getLoot: Route =
    path("party" / Segment / "loot") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            complete {
              loot(partyId, None).map { players =>
                LootView.template(partyId, players, None)
              }.map { text =>
                (StatusCodes.OK, RootView.toHtml(text))
              }
            }
          }
        }
      }
    }

  def modifyLoot: Route =
    path("party" / Segment / "loot") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          post {
            formFields("player".as[String], "piece".as[String], "is_tome".as[String].?, "action".as[String]) {
              (player, maybePiece, maybeIsTome, action) =>
                onComplete(modifyLootCall(partyId, player, maybePiece, maybeIsTome, action)) {
                  case _ => redirect(s"/party/$partyId/loot", StatusCodes.Found)
                }
            }
          }
        }
      }
    }

  private def modifyLootCall(partyId: String, player: String,
                             maybePiece: String, maybeIsTome: Option[String],
                             action: String)
                            (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Unit] = {
    import me.arcanis.ffxivbis.utils.Implicits._

    def getPiece(playerId: PlayerId) =
      Try(Piece(maybePiece, maybeIsTome, playerId.job)).toOption

    PlayerId(partyId, player) match {
      case Some(playerId) => (getPiece(playerId), action) match {
        case (Some(piece), "add") => addPieceLoot(playerId, piece).map(_ => ())
        case (Some(piece), "remove") => removePieceLoot(playerId, piece).map(_ => ())
        case _ => Future.failed(new Error(s"Could not construct piece from `$maybePiece`"))
      }
      case _ => Future.failed(new Error(s"Could not construct player id from `$player`"))
    }
  }
}

object LootView {
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title => titleTag}

  def template(partyId: String, party: Seq[Player], error: Option[String]): String =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(lang:="en",
        head(
          titleTag("Loot"),
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          h2("Loot"),

          ErrorView.template(error),
          SearchLineView.template,

          form(action:=s"/party/$partyId/loot", method:="post")(
            select(name:="player", id:="player", title:="player")
                  (for (player <- party) yield option(player.playerId.toString)),
            select(name:="piece", id:="piece", title:="piece")
                  (for (piece <- Piece.available) yield option(piece)),
            input(name:="is_tome", id:="is_tome", title:="is tome", `type`:="checkbox"),
            label(`for`:="is_tome")("is tome gear"),
            input(name:="action", id:="action", `type`:="hidden", value:="add"),
            input(name:="add", id:="add", `type`:="submit", value:="add")
          ),

          table(id:="result")(
            tr(
              th("player"),
              th("piece"),
              th("is tome"),
              th("timestamp"),
              th("")
            ),
            for (player <- party; loot <- player.loot) yield tr(
              td(`class`:="include_search")(player.playerId.toString),
              td(`class`:="include_search")(loot.piece.piece),
              td(loot.piece.isTomeToString),
              td(loot.timestamp.toString),
              td(
                form(action:=s"/party/$partyId/loot", method:="post")(
                  input(name:="player", id:="player", `type`:="hidden", value:=player.playerId.toString),
                  input(name:="piece", id:="piece", `type`:="hidden", value:=loot.piece.piece),
                  input(name:="is_tome", id:="is_tome", `type`:="hidden", value:=loot.piece.isTomeToString),
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
