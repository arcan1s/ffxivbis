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

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model._
import akka.pattern.pipe
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.models.{BiS, Job, Piece, PieceType}
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

abstract class BisProvider extends Actor with XivApi with StrictLogging {

  def idParser(job: Job.Job, js: JsObject)
              (implicit executionContext: ExecutionContext): Future[Map[String, Long]]
  def uri(root: Uri, id: String): Uri

  override def receive: Receive = {
    case BisProvider.GetBiS(link, job) =>
      val client = sender()
      get(link, job).map(BiS(_)).pipeTo(client)
  }

  override def postStop(): Unit = {
    shutdown()
    super.postStop()
  }

  private def get(link: String, job: Job.Job): Future[Seq[Piece]] = {
    val url = Uri(link)
    val id = Paths.get(link).normalize.getFileName.toString

    sendRequest(uri(url, id), BisProvider.parseBisJsonToPieces(job, idParser, getPieceType))
  }
}

object BisProvider {

  def props(useEtro: Boolean): Props =
    if (useEtro) Props(new BisProvider with Etro)
    else Props(new BisProvider with Ariyala)

  case class GetBiS(link: String, job: Job.Job)

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
    case "weapon" | "head" | "body" | "hands" | "waist" | "legs" | "feet" | "ears" | "neck" | "wrist" | "wrists" => Some(key)
    case _ => None
  }
}
