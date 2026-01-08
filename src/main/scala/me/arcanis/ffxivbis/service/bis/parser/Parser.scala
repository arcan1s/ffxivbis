/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.bis.parser

import akka.http.scaladsl.model.Uri
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.models.Job
import spray.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

trait Parser extends StrictLogging {

  def parse(job: Job, js: JsObject)(implicit executionContext: ExecutionContext): Future[Map[String, Long]]

  def uri(root: Uri, id: String): Uri
}
