/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.bis

import akka.actor.ClassicActorSystemProvider
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.http.scaladsl.model._
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.messages.BiSProviderMessage
import me.arcanis.ffxivbis.models.{BiS, Job, Piece, PieceType}
import me.arcanis.ffxivbis.service.bis.parser.Parser
import me.arcanis.ffxivbis.service.bis.parser.impl.{Ariyala, Etro, XIVGear}
import spray.json._

import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class BisProvider(context: ActorContext[BiSProviderMessage])
  extends AbstractBehavior[BiSProviderMessage](context)
  with XivApi
  with StrictLogging {

  override def system: ClassicActorSystemProvider = context.system

  override def onMessage(msg: BiSProviderMessage): Behavior[BiSProviderMessage] =
    msg match {
      case BiSProviderMessage.DownloadBiS(link, job, client) =>
        get(link, job).onComplete {
          case Success(items) => client ! BiS(items)
          case Failure(exception) =>
            logger.error("received exception while getting items", exception)
        }
        Behaviors.same
    }

  override def onSignal: PartialFunction[Signal, Behavior[BiSProviderMessage]] = { case PostStop =>
    shutdown()
    Behaviors.same
  }

  private def get(link: String, job: Job): Future[Seq[Piece]] =
    try {
      val url = Uri(link)
      val id = Paths.get(link).normalize.getFileName.toString

      val parser =
        if (url.authority.host.address().contains("etro")) Etro
        else if (url.authority.host.address().contains("xivgear.app")) XIVGear
        else Ariyala
      val uri = parser.uri(url, id)
      sendRequest(uri, BisProvider.parseBisJsonToPieces(job, parser, getPieceType))
    } catch {
      case NonFatal(exception) => Future.failed(exception)
    }
}

object BisProvider {

  def apply(): Behavior[BiSProviderMessage] =
    Behaviors.setup[BiSProviderMessage](context => new BisProvider(context))

  private def parseBisJsonToPieces(
    job: Job,
    idParser: Parser,
    pieceTypes: Seq[Long] => Future[Map[Long, PieceType]]
  )(js: JsObject)(implicit executionContext: ExecutionContext): Future[Seq[Piece]] =
    idParser.parse(job, js).flatMap { pieces =>
      pieceTypes(pieces.values.toSeq).map { types =>
        pieces.view
          .mapValues(types)
          .map { case (piece, pieceType) =>
            Piece(piece, pieceType, job)
          }
          .toSeq
      }
    }

  def remapKey(key: String): Option[String] = Some(key.toLowerCase).collect {
    case "mainhand" => "weapon"
    case "chest" => "body"
    case "ringleft" | "fingerl" => "left ring"
    case "ringright" | "fingerr" => "right ring"
    case "weapon" | "head" | "body" | "hand" | "hands" | "legs" | "feet" | "ears" | "neck" | "wrist" | "wrists" => key
  }
}
