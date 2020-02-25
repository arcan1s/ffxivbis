/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service

import java.nio.file.Paths

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.models.{BiS, Job, Piece}
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class Ariyala extends Actor with StrictLogging {
  import Ariyala._

  private val settings = context.system.settings.config
  private val ariyalaUrl = settings.getString("me.arcanis.ffxivbis.ariyala.ariyala-url")
  private val xivapiUrl = settings.getString("me.arcanis.ffxivbis.ariyala.xivapi-url")
  private val xivapiKey = Try(settings.getString("me.arcanis.ffxivbis.ariyala.xivapi-key")).toOption

  private val http = Http()(context.system)
  implicit private val materializer: ActorMaterializer = ActorMaterializer()
  implicit private val executionContext: ExecutionContext =
    context.system.dispatchers.lookup("me.arcanis.ffxivbis.default-dispatcher")

  override def receive: Receive = {
    case GetBiS(link, job) =>
      val client = sender()
      get(link, job).map(BiS(_)).pipeTo(client)
  }

  override def postStop(): Unit = {
    http.shutdownAllConnectionPools()
    super.postStop()
  }

  private def get(link: String, job: Job.Job): Future[Seq[Piece]] = {
    val id = Paths.get(link).normalize.getFileName.toString
    val uri = Uri(ariyalaUrl)
      .withPath(Uri.Path / "store.app")
      .withQuery(Uri.Query(Map("identifier" -> id)))

    sendRequest(uri, Ariyala.parseAriyalaJsonToPieces(job, getIsTome))
  }

  private def getIsTome(itemIds: Seq[Long]): Future[Map[Long, Boolean]] = {
    val uri = Uri(xivapiUrl)
      .withPath(Uri.Path / "item")
      .withQuery(Uri.Query(Map(
        "columns" -> Seq("ID", "Lot").mkString(","),
        "ids" -> itemIds.mkString(","),
        "private_key" -> xivapiKey.getOrElse("")
      )))

    sendRequest(uri, Ariyala.parseXivapiJson)
  }

  private def sendRequest[T](uri: Uri, parser: JsObject => Future[T]): Future[T] =
    http.singleRequest(HttpRequest(uri = uri)).map {
      case HttpResponse(status, _, entity, _) if status.isSuccess() =>
        entity.dataBytes
          .fold(ByteString.empty)(_ ++ _)
          .map(_.utf8String)
          .map(result => parser(result.parseJson.asJsObject))
          .toMat(Sink.head)(Keep.right)
          .run().flatten
      case other => Future.failed(new Error(s"Invalid response from server $other"))
    }.flatten
}

object Ariyala {
  def props: Props = Props(new Ariyala)

  case class GetBiS(link: String, job: Job.Job)

  private def parseAriyalaJson(job: Job.Job)(js: JsObject)
                              (implicit executionContext: ExecutionContext): Future[Map[String, Long]] =
    Future {
      val apiJob = js.fields.get("content") match {
        case Some(JsString(value)) => value
        case other => throw deserializationError(s"Invalid job name $other")
      }
      js.fields.get("datasets") match {
        case Some(datasets: JsObject) =>
          val fields = datasets.fields
          fields.getOrElse(apiJob, fields(job.toString)).asJsObject
            .fields("normal").asJsObject
            .fields("items").asJsObject
            .fields.foldLeft(Map.empty[String, Long]) {
            case (acc, (key, JsNumber(id))) => remapKey(key).map(k => acc + (k -> id.toLong)).getOrElse(acc)
            case (acc, _) => acc
          }
        case other => throw deserializationError(s"Invalid json $other")
      }
    }

  private def parseAriyalaJsonToPieces(job: Job.Job, isTome: Seq[Long] => Future[Map[Long, Boolean]])(js: JsObject)
                                      (implicit executionContext: ExecutionContext): Future[Seq[Piece]] =
    parseAriyalaJson(job)(js).flatMap { pieces =>
      isTome(pieces.values.toSeq).map { tomePieces =>
        pieces.view.mapValues(tomePieces).map {
          case (piece, isTomePiece) => Piece(piece, isTomePiece, job)
        }.toSeq
      }
    }

  private def parseXivapiJson(js: JsObject)
                             (implicit executionContext: ExecutionContext): Future[Map[Long, Boolean]] =
    Future {
      js.fields("Results") match {
        case array: JsArray =>
          array.elements.map(_.asJsObject.getFields("ID", "Lot") match {
            case Seq(JsNumber(id), JsNumber(isTome)) => id.toLong -> (isTome == 0)
            case other => throw deserializationError(s"Could not parse $other")
          }).toMap
        case other => throw deserializationError(s"Could not parse $other")
      }
    }

  private def remapKey(key: String): Option[String] = key match {
    case "mainhand" => Some("weapon")
    case "chest" => Some("body")
    case "ringLeft" => Some("left ring")
    case "ringRight" => Some("right ring")
    case "head" | "hands" | "waist" | "legs" | "feet" | "ears" | "neck" | "wrist" => Some(key)
    case _ => None
  }
}
