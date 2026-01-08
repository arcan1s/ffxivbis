/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
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
import scala.jdk.CollectionConverters._
import scala.util.Try

trait XivApi extends RequestExecutor {

  private val config = system.classicSystem.settings.config
  private val xivapiUrl = config.getString("me.arcanis.ffxivbis.bis-provider.xivapi-url")
  private val xivapiKey = Try(config.getString("me.arcanis.ffxivbis.bis-provider.xivapi-key")).toOption

  private val preloadedItems: Map[Long, PieceType] =
    config
      .getConfigList("me.arcanis.ffxivbis.bis-provider.cached-items")
      .asScala
      .map { item =>
        item.getLong("id") -> PieceType.withName(item.getString("source"))
      }
      .toMap

  def getPieceType(itemIds: Seq[Long]): Future[Map[Long, PieceType]] = {
    val (local, remote) = itemIds.foldLeft((Map.empty[Long, PieceType], Seq.empty[Long])) { case ((l, r), id) =>
      if (preloadedItems.contains(id)) (l.updated(id, preloadedItems(id)), r)
      else (l, r :+ id)
    }
    if (remote.isEmpty) Future.successful(local)
    else remotePieceType(remote).map(_ ++ local)
  }

  private def remotePieceType(itemIds: Seq[Long]): Future[Map[Long, PieceType]] =
    Future
      .traverse(itemIds) { id =>
        val uriForItem = Uri(xivapiUrl)
          .withPath(Uri.Path / "api" / "sheet" / "Item" / id.toString)
          .withQuery(
            Uri.Query(
              Map(
                "fields" -> Seq("Lot").mkString(","),
                "private_key" -> xivapiKey.getOrElse("")
              )
            )
          )

        sendRequest(uriForItem, XivApi.parseXivapiJsonToLot).map(id -> _)
      }
      .map(_.toMap)
}

object XivApi {

  private def parseXivapiJsonToLot(js: JsObject)(implicit executionContext: ExecutionContext): Future[PieceType] =
    Future {
      js.fields("fields") match {
        case JsObject(fields) =>
          fields
            .get("Lot")
            .collect {
              case JsBoolean(true) => PieceType.Savage
              case JsBoolean(false) => PieceType.Tome
            }
            .getOrElse(throw deserializationError(s"Could not find lot field in $fields"))
        case other => throw deserializationError(s"Could not read fields as object from $other")
      }
    }
}
