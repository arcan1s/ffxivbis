/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
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
import spray.json.{deserializationError, JsNumber, JsObject, JsString}

import scala.concurrent.{ExecutionContext, Future}

object Ariyala extends Parser {

  override def parse(job: Job, js: JsObject)(implicit
    executionContext: ExecutionContext
  ): Future[Map[String, Long]] =
    Future {
      val apiJob = js.fields.get("content") match {
        case Some(JsString(value)) => value
        case other => throw deserializationError(s"Invalid job name $other")
      }
      js.fields.get("datasets") match {
        case Some(datasets: JsObject) =>
          val fields = datasets.fields
          fields
            .getOrElse(apiJob, fields(job.toString))
            .asJsObject
            .fields("normal")
            .asJsObject
            .fields("items")
            .asJsObject
            .fields
            .foldLeft(Map.empty[String, Long]) {
              case (acc, (key, JsNumber(id))) =>
                BisProvider.remapKey(key).map(k => acc + (k -> id.toLong)).getOrElse(acc)
              case (acc, _) => acc
            }
        case other => throw deserializationError(s"Invalid json $other")
      }
    }

  override def uri(root: Uri, id: String): Uri =
    root
      .withPath(Uri.Path / "store.app")
      .withQuery(Uri.Query(Map("identifier" -> id)))
}
