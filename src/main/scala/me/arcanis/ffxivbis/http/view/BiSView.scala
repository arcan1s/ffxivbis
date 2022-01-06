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
import me.arcanis.ffxivbis.http.{Authorization, BiSHelper}
import me.arcanis.ffxivbis.messages.{BiSProviderMessage, Message}
import me.arcanis.ffxivbis.models.{Piece, PieceType, Player, PlayerId}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BiSView(override val storage: ActorRef[Message],
              override val provider: ActorRef[BiSProviderMessage])
             (implicit timeout: Timeout, scheduler: Scheduler)
  extends BiSHelper with Authorization {

  def route: Route = getBiS ~ modifyBiS

  def getBiS: Route =
    path("party" / Segment / "bis") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            complete {
              bis(partyId, None).map { players =>
                BiSView.template(partyId, players, None)
              }.map { text =>
                (StatusCodes.OK, RootView.toHtml(text))
              }
            }
          }
        }
      }
    }

  def modifyBiS: Route =
    path("party" / Segment / "bis") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authPost(partyId)) { _ =>
          post {
            formFields("player".as[String], "piece".as[String].?, "piece_type".as[String].?, "link".as[String].?, "action".as[String]) {
              (player, maybePiece, maybePieceType, maybeLink, action) =>
                onComplete(modifyBiSCall(partyId, player, maybePiece, maybePieceType, maybeLink, action)) { _ =>
                  redirect(s"/party/$partyId/bis", StatusCodes.Found)
                }
            }
          }
        }
      }
    }

  private def modifyBiSCall(partyId: String, player: String,
                            maybePiece: Option[String], maybePieceType: Option[String],
                            maybeLink: Option[String], action: String)
                           (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Unit] = {
    def getPiece(playerId: PlayerId, piece: String, pieceType: String) =
      Try(Piece(piece, PieceType.withName(pieceType), playerId.job)).toOption

    def bisAction(playerId: PlayerId, piece: String, pieceType: String)(fn: Piece => Future[Unit]) =
      getPiece(playerId, piece, pieceType) match {
        case Some(item) => fn(item)
        case _ => Future.failed(new Error(s"Could not construct piece from `$piece ($pieceType)`"))
      }

    PlayerId(partyId, player) match {
      case Some(playerId) => (maybePiece, maybePieceType, action, maybeLink.map(_.trim).filter(_.nonEmpty)) match {
        case (Some(piece), Some(pieceType), "add", _) =>
          bisAction(playerId, piece, pieceType)(addPieceBiS(playerId, _))
        case (Some(piece), Some(pieceType), "remove", _) =>
          bisAction(playerId, piece, pieceType)(removePieceBiS(playerId, _))
        case (_, _, "create", Some(link)) => putBiS(playerId, link)
        case _ => Future.failed(new Error(s"Could not perform $action"))
      }
      case _ => Future.failed(new Error(s"Could not construct player id from `$player`"))
    }
  }
}

object BiSView {
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title => titleTag}

  def template(partyId: String, party: Seq[Player], error: Option[String]): String =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
      html(lang:="en",
        head(
          titleTag("Best in slot"),
          link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
        ),

        body(
          h2("Best in slot"),

          ErrorView.template(error),
          SearchLineView.template,

          form(action:=s"/party/$partyId/bis", method:="post")(
            select(name:="player", id:="player", title:="player")
                  (for (player <- party) yield option(player.playerId.toString)),
            select(name:="piece", id:="piece", title:="piece")
                  (for (piece <- Piece.available) yield option(piece)),
            select(name:="piece_type", id:="piece_type", title:="piece type")
                  (for (pieceType <- PieceType.available) yield option(pieceType.toString)),
            input(name:="action", id:="action", `type`:="hidden", value:="add"),
            input(name:="add", id:="add", `type`:="submit", value:="add")
          ),

          form(action:=s"/party/$partyId/bis", method:="post")(
            select(name:="player", id:="player", title:="player")
                  (for (player <- party) yield option(player.playerId.toString)),
            input(name:="link", id:="link", placeholder:="player bis link", title:="link", `type`:="text"),
            input(name:="action", id:="action", `type`:="hidden", value:="create"),
            input(name:="add", id:="add", `type`:="submit", value:="add")
          ),

          table(id:="result")(
            tr(
              th("player"),
              th("piece"),
              th("piece type"),
              th("")
            ),
            for (player <- party; piece <- player.bis.pieces) yield tr(
              td(`class`:="include_search")(player.playerId.toString),
              td(`class`:="include_search")(piece.piece),
              td(piece.pieceType.toString),
              td(
                form(action:=s"/party/$partyId/bis", method:="post")(
                  input(name:="player", id:="player", `type`:="hidden", value:=player.playerId.toString),
                  input(name:="piece", id:="piece", `type`:="hidden", value:=piece.piece),
                  input(name:="piece_type", id:="piece_type", `type`:="hidden", value:=piece.pieceType.toString),
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
