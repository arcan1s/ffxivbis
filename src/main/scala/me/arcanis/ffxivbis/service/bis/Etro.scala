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
import me.arcanis.ffxivbis.models.Job
import spray.json.{JsNumber, JsObject, deserializationError}

import scala.concurrent.{ExecutionContext, Future}

object Etro extends IdParser {

  override def parse(job: Job.Job, js: JsObject)
                    (implicit executionContext: ExecutionContext): Future[Map[String, Long]] =
    Future {
      js.fields.foldLeft(Map.empty[String, Long]) {
        case (acc, (key, JsNumber(id))) => BisProvider.remapKey(key).map(k => acc + (k -> id.toLong)).getOrElse(acc)
        case (acc, _) => acc
      }
    }

  override def uri(root: Uri, id: String): Uri =
    root.withPath(Uri.Path / "api" / "gearsets" / id)
}
