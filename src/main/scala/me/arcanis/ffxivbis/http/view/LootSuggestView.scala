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
import me.arcanis.ffxivbis.models.{Job, Piece, PieceType, PlayerIdWithCounters}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class LootSuggestView(override val storage: ActorRef)(implicit timeout: Timeout)
  extends LootHelper with Authorization {

  def route: Route = getIndex ~ suggestLoot

  def getIndex: Route =
    path("party" / Segment / "suggest") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            complete {
              val text = LootSuggestView.template(partyId, Seq.empty, None, false, None)
              (StatusCodes.OK, RootView.toHtml(text))
            }
          }
        }
      }
    }

  def suggestLoot: Route =
    path("party" / Segment / "suggest") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          post {
            formFields("piece".as[String], "job".as[String], "piece_type".as[String], "free_loot".as[String].?) {
              (piece, job, pieceType, maybeFreeLoot) =>
                import me.arcanis.ffxivbis.utils.Implicits._

                val maybePiece = Try(Piece(piece, PieceType.withName(pieceType), Job.withName(job))).toOption

                onComplete(suggestLootCall(partyId, maybePiece)) {
                  case Success(players) =>
                    val text = LootSuggestView.template(partyId, players, maybePiece, maybeFreeLoot, None)
                    complete(StatusCodes.OK, RootView.toHtml(text))
                  case Failure(exception) =>
                    val text = LootSuggestView.template(partyId, Seq.empty, None, false, Some(exception.getMessage))
                    complete(StatusCodes.OK, RootView.toHtml(text))
                }
            }
          }
        }
      }
    }

  private def suggestLootCall(partyId: String, maybePiece: Option[Piece])
                             (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[PlayerIdWithCounters]] =
    maybePiece match {
      case Some(piece) => suggestPiece(partyId, piece)
      case _ => Future.failed(new Error(s"Could not construct piece from `$maybePiece`"))
    }
}

object LootSuggestView {
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title => titleTag}

  def template(partyId: String, party: Seq[PlayerIdWithCounters], piece: Option[Piece],
               isFreeLoot: Boolean, error: Option[String]): String =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(lang:="en",
        head(
          titleTag("Suggest loot"),
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          h2("Suggest loot"),

          ErrorView.template(error),
          SearchLineView.template,

          form(action:=s"/party/$partyId/suggest", method:="post")(
            select(name:="piece", id:="piece", title:="piece")
                  (for (piece <- Piece.available) yield option(piece)),
            select(name:="job", id:="job", title:="job")
                  (for (job <- Job.availableWithAnyJob) yield option(job.toString)),
            select(name:="piece_type", id:="piece_type", title:="piece type")
                  (for (pieceType <- PieceType.available) yield option(pieceType.toString)),
            input(name:="free_loot", id:="free_loot", title:="is free loot", `type`:="checkbox"),
            label(`for`:="free_loot")("is free loot"),
            input(name:="suggest", id:="suggest", `type`:="submit", value:="suggest")
          ),

          table(id:="result")(
            tr(
              th("player"),
              th("is required"),
              th("these pieces looted"),
              th("total bis pieces looted"),
              th("total pieces looted"),
              th("")
            ),
            for (player <- party) yield tr(
              td(`class`:="include_search")(player.playerId.toString),
              td(player.isRequiredToString),
              td(player.lootCount),
              td(player.lootCountBiS),
              td(player.lootCountTotal),
              td(
                form(action:=s"/party/$partyId/loot", method:="post")(
                  input(name:="player", id:="player", `type`:="hidden", value:=player.playerId.toString),
                  input(name:="piece", id:="piece", `type`:="hidden", value:=piece.map(_.piece).getOrElse("")),
                  input(name:="piece_type", id:="piece_type", `type`:="hidden", value:=piece.map(_.pieceType.toString).getOrElse("")),
                  input(name:="free_loot", id:="free_loot", `type`:="hidden", value:=(if (isFreeLoot) "yes" else "no")),
                  input(name:="action", id:="action", `type`:="hidden", value:="add"),
                  input(name:="add", id:="add", `type`:="submit", value:="add")
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
