/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.bis

import akka.http.scaladsl.model.Uri
import me.arcanis.ffxivbis.models.PieceType
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait XivApi extends RequestExecutor {

  private val config = context.system.settings.config
  private val xivapiUrl = config.getString("me.arcanis.ffxivbis.bis-provider.xivapi-url")
  private val xivapiKey = Try(config.getString("me.arcanis.ffxivbis.bis-provider.xivapi-key")).toOption

  def getPieceType(itemIds: Seq[Long]): Future[Map[Long, PieceType.PieceType]] = {
    val uriForItems = Uri(xivapiUrl)
      .withPath(Uri.Path / "item")
      .withQuery(Uri.Query(Map(
        "columns" -> Seq("ID", "GameContentLinks").mkString(","),
        "ids" -> itemIds.mkString(","),
        "private_key" -> xivapiKey.getOrElse("")
      )))

    sendRequest(uriForItems, XivApi.parseXivapiJsonToShop).flatMap { shops =>
      val shopIds = shops.values.map(_._2).toSet
      val columns = shops.values.map(pair => s"ItemCost${pair._1}").toSet
      val uriForShops = Uri(xivapiUrl)
        .withPath(Uri.Path / "specialshop")
        .withQuery(Uri.Query(Map(
          "columns" -> (columns + "ID").mkString(","),
          "ids" -> shopIds.mkString(","),
          "private_key" -> xivapiKey.getOrElse("")
        )))

      sendRequest(uriForShops, XivApi.parseXivapiJsonToType(shops))
    }
  }
}

object XivApi {

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
}
