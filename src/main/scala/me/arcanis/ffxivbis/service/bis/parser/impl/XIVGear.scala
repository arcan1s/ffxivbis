/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.bis.parser.impl

import akka.http.scaladsl.model.Uri
import me.arcanis.ffxivbis.models.Job
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.bis.parser.Parser
import spray.json.{deserializationError, JsNumber, JsObject}

import scala.concurrent.{ExecutionContext, Future}

object XIVGear extends Parser {

  override def parse(job: Job, js: JsObject)(implicit executionContext: ExecutionContext): Future[Map[String, Long]] =
    Future {
      val set = js.fields.get("items") match {
        case Some(JsObject(items)) => items
        case other => throw deserializationError(s"Invalid job name $other")
      }
      set.foldLeft(Map.empty[String, Long]) {
        case (acc, (key, JsObject(properties))) =>
          val pieceId = properties.get("id").collect { case JsNumber(id) =>
            id.toLong
          }
          (for (
            piece <- BisProvider.remapKey(key);
            id <- pieceId
          ) yield (piece, id)).map(acc + _).getOrElse(acc)
        case (acc, _) => acc
      }
    }

  override def uri(root: Uri, id: String): Uri = {
    val gearSet = Uri(id).query().get("page").map(_.replace("sl|", "")).getOrElse(id)
    root.withHost(s"api.${root.authority.host.address()}").withPath(Uri.Path / "shortlink" / gearSet)
  }
}
