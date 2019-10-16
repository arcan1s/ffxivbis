package me.arcanis.ffxivbis.http.view

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import me.arcanis.ffxivbis.http.{Authorization, BiSHelper}
import me.arcanis.ffxivbis.models.{Piece, Player, PlayerId}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BiSView(override val storage: ActorRef, ariyala: ActorRef)(implicit timeout: Timeout)
  extends BiSHelper(storage, ariyala) with Authorization {

  def route: Route = getBiS ~ modifyBiS

  def getBiS: Route =
    path("party" / Segment / "bis") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          get {
            complete {
              bis(partyId, None).map { players =>
                BiSView.template(partyId, players, Piece.available, None)
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
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { _ =>
          post {
            formFields("player".as[String], "piece".as[String].?, "is_tome".as[String].?, "link".as[String].?, "action".as[String]) {
              (player, maybePiece, maybeIsTome, maybeLink, action) =>
                onComplete(modifyBiSCall(partyId, player, maybePiece, maybeIsTome, maybeLink, action)) {
                  case _ => redirect(s"/party/$partyId/bis", StatusCodes.Found)
                }
            }
          }
        }
      }
    }

  private def modifyBiSCall(partyId: String, player: String,
                            maybePiece: Option[String], maybeIsTome: Option[String],
                            maybeLink: Option[String], action: String)
                           (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Unit] = {
    def getPiece(playerId: PlayerId, piece: String) =
      Try(Piece(piece, maybeIsTome.isDefined, playerId.job)).toOption

    PlayerId(partyId, player) match {
      case Some(playerId) => (maybePiece, action, maybeLink) match {
        case (Some(piece), "add", _) => getPiece(playerId, piece) match {
          case Some(item) => addPieceBiS(playerId, item).map(_ => ())
          case _ => Future.failed(new Error(s"Could not construct piece from `$piece`"))
        }
        case (Some(piece), "remove", _) => getPiece(playerId, piece) match {
          case Some(item) => removePieceBiS(playerId, item).map(_ => ())
          case _ => Future.failed(new Error(s"Could not construct piece from `$piece`"))
        }
        case (_, "create", Some(link)) => putBiS(playerId, link).map(_ => ())
        case _ => Future.failed(new Error(s"Could not perform $action"))
      }
      case _ => Future.failed(new Error(s"Could not construct player id from `$player`"))
    }
  }
}

object BiSView {
  import scalatags.Text.all._

  def template(partyId: String, party: Seq[Player], pieces: Seq[String], error: Option[String]): String = {
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
    html(lang:="en",
      head(
        title:="Best in slot",
        link(rel:="stylesheet", `type`:="text/css", href:="/static/styles.css")
      ),

      body(
        h2("best in slot"),

        ErrorView.template(error),
        SearchLineView.template,

        form(action:=s"/party/$partyId/bis", method:="post")(
          select(name:="player", id:="player", title:="player")
                (for (player <- party) yield option(player.playerId.toString)),
          select(name:="piece", id:="piece", title:="piece")
                (for (piece <- pieces) yield option(piece)),
          input(name:="is_tome", id:="is_tome", title:="is tome", `type`:="checkbox"),
          label(`for`:="is_tome")("is tome gear"),
          input(name:="action", id:="action", `type`:="hidden", value:="add"),
          input(name:="add", id:="add", `type`:="submit", value:="add")
        ),

        form(action:="/bis", method:="post")(
          select(name:="player", id:="player", title:="player")
                (for (player <- party) yield option(player.playerId.toString)),
          input(name:="link", id:="link", placeholder:="player bis link", title:="link", `type`:="text"),
          input(name:="action", id:="action", `type`:="hidden", value:="create"),
          input(name:="add", id:="add", `type`:="submit", value:="add")
        ),

        table(
          tr(
            th("player"),
            th("piece"),
            th("is tome"),
            th("")
            //td(`class`:="include_search")
          ),
          for (player <- party; piece <- player.bis.pieces) yield tr(
            td(`class`:="include_search")(player.playerId.toString),
            td(`class`:="include_search")(piece.piece),
            td(piece.isTomeToString),
            td(
              form(action:=s"/party/$partyId/bis", method:="post")(
                input(name:="player", id:="player", `type`:="hidden", value:=player.playerId.toString),
                input(name:="piece", id:="piece", `type`:="hidden", value:=piece.piece),
                input(name:="is_tome", id:="is_tome", `type`:="hidden", value:=piece.isTomeToString),
                input(name:="action", id:="action", `type`:="hidden", value:="remove"),
                input(name:="remove", id:="remove", `type`:="submit", value:="x")
              )
            )
          )
        ),

        ExportToCSVView.template,
        script(src:="/static/table_search.js", `type`:="text/javascript")
      )
    )
  }
}
