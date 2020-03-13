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
import me.arcanis.ffxivbis.models.{BiS, Job, Piece, PieceType}
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

    sendRequest(uri, Ariyala.parseAriyalaJsonToPieces(job, getPieceType))
  }

  private def getPieceType(itemIds: Seq[Long]): Future[Map[Long, PieceType.PieceType]] = {
    val uriForItems = Uri(xivapiUrl)
      .withPath(Uri.Path / "item")
      .withQuery(Uri.Query(Map(
        "columns" -> Seq("ID", "GameContentLinks").mkString(","),
        "ids" -> itemIds.mkString(","),
        "private_key" -> xivapiKey.getOrElse("")
      )))

    sendRequest(uriForItems, Ariyala.parseXivapiJsonToShop).flatMap { shops =>
      val shopIds = shops.values.map(_._2).toSet
      val columns = shops.values.map(pair => s"ItemCost${pair._1}").toSet
      val uriForShops = Uri(xivapiUrl)
        .withPath(Uri.Path / "specialshop")
        .withQuery(Uri.Query(Map(
          "columns" -> (columns + "ID").mkString(","),
          "ids" -> shopIds.mkString(","),
          "private_key" -> xivapiKey.getOrElse("")
        )))

      sendRequest(uriForShops, Ariyala.parseXivapiJsonToType(shops))
    }
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

  private def parseAriyalaJsonToPieces(job: Job.Job, pieceTypes: Seq[Long] => Future[Map[Long, PieceType.PieceType]])
                                      (js: JsObject)
                                      (implicit executionContext: ExecutionContext): Future[Seq[Piece]] =
    parseAriyalaJson(job)(js).flatMap { pieces =>
      pieceTypes(pieces.values.toSeq).map { types =>
        pieces.view.mapValues(types).map {
          case (piece, pieceType) => Piece(piece, pieceType, job)
        }.toSeq
      }
    }

  private def parseXivapiJsonToShop(js: JsObject)
                                   (implicit executionContext: ExecutionContext): Future[Map[Long, (String, Long)]] = {
    def extractTraderId(js: JsObject) = {
      js.fields
        .get("Recipe").map(_ => "crafted" -> -1L) // you can craft this item
        .orElse {  // lets try shop items
          js.fields("SpecialShop").asJsObject
            .fields.collectFirst {
            case (shopName, JsArray(array)) if shopName.startsWith("ItemReceive") =>
              val shopId = array.head match {
                case JsNumber(id) => id.toLong
                case other => throw deserializationError(s"Could not parse $other")
              }
              shopName.replace("ItemReceive", "") -> shopId
          }
        }.getOrElse(throw deserializationError(s"Could not parse $js"))
    }

    Future {
      js.fields("Results") match {
        case array: JsArray =>
          array.elements.map(_.asJsObject.getFields("ID", "GameContentLinks") match {
            case Seq(JsNumber(id), shop) => id.toLong -> extractTraderId(shop.asJsObject)
            case other => throw deserializationError(s"Could not parse $other")
          }).toMap
        case other => throw deserializationError(s"Could not parse $other")
      }
    }
  }

  private def parseXivapiJsonToType(shops: Map[Long, (String, Long)])(js: JsObject)
                                   (implicit executionContext: ExecutionContext): Future[Map[Long, PieceType.PieceType]] =
    Future {
      val shopMap = js.fields("Results") match {
        case array: JsArray =>
          array.elements.map { shop =>
            shop.asJsObject.fields("ID") match {
              case JsNumber(id) => id.toLong -> shop.asJsObject
              case other => throw deserializationError(s"Could not parse $other")
            }
          }.toMap
        case other => throw deserializationError(s"Could not parse $other")
      }

      shops.map { case (itemId, (index, shopId)) =>
        val pieceType =
          if (index == "crafted" && shopId == -1) PieceType.Crafted
          else {
            Try(shopMap(shopId).fields(s"ItemCost$index").asJsObject)
              .toOption
              .getOrElse(throw new Exception(s"${shopMap(shopId).fields(s"ItemCost$index")}, $index"))
              .getFields("IsUnique", "StackSize") match {
              case Seq(JsNumber(isUnique), JsNumber(stackSize)) =>
                if (isUnique == 1 || stackSize.toLong == 2000) PieceType.Tome // either upgraded gear or tomes found
                else PieceType.Savage
              case other => throw deserializationError(s"Could not parse $other")
            }
          }
        itemId -> pieceType
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
