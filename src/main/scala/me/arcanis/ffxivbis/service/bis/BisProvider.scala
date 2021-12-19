/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.bis

import java.nio.file.Paths

import akka.actor.ClassicActorSystemProvider
import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.model._
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.messages.{BiSProviderMessage, DownloadBiS}
import me.arcanis.ffxivbis.models.{BiS, Job, Piece, PieceType}
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class BisProvider(context: ActorContext[BiSProviderMessage])
  extends AbstractBehavior[BiSProviderMessage](context) with XivApi with StrictLogging {

  override def system: ClassicActorSystemProvider = context.system

  override def onMessage(msg: BiSProviderMessage): Behavior[BiSProviderMessage] =
    msg match {
      case DownloadBiS(link, job, client) =>
        get(link, job).map(BiS(_)).foreach(client ! _)
        Behaviors.same
    }

  override def onSignal: PartialFunction[Signal, Behavior[BiSProviderMessage]] = {
    case PostStop =>
      shutdown()
      Behaviors.same
  }

  private def get(link: String, job: Job.Job): Future[Seq[Piece]] = {
    val url = Uri(link)
    val id = Paths.get(link).normalize.getFileName.toString

    val (idParser, uri) =
      if (url.authority.host.address().contains("etro")) {
        (Etro.idParser(_, _), Etro.uri(url, id))
      } else {
        (Ariyala.idParser(_, _), Ariyala.uri(url, id))
      }

    sendRequest(uri, BisProvider.parseBisJsonToPieces(job, idParser, getPieceType))
  }
}

object BisProvider {

  def apply(): Behavior[BiSProviderMessage] =
    Behaviors.setup[BiSProviderMessage](context => new BisProvider(context))

  private def parseBisJsonToPieces(job: Job.Job,
                                   idParser: (Job.Job, JsObject) => Future[Map[String, Long]],
                                   pieceTypes: Seq[Long] => Future[Map[Long, PieceType.PieceType]])
                                   (js: JsObject)
                                   (implicit executionContext: ExecutionContext): Future[Seq[Piece]] =
    idParser(job, js).flatMap { pieces =>
      pieceTypes(pieces.values.toSeq).map { types =>
        pieces.view.mapValues(types).map {
          case (piece, pieceType) => Piece(piece, pieceType, job)
        }.toSeq
      }
    }

  def remapKey(key: String): Option[String] = key match {
    case "mainhand" => Some("weapon")
    case "chest" => Some("body")
    case "ringLeft" | "fingerL" => Some("left ring")
    case "ringRight" | "fingerR" => Some("right ring")
    case "weapon" | "head" | "body" | "hands" | "legs" | "feet" | "ears" | "neck" | "wrist" | "wrists" => Some(key)
    case _ => None
  }
}
