package me.arcanis.ffxivbis.service.bis.parser.impl

import akka.http.scaladsl.model.Uri
import me.arcanis.ffxivbis.models.Job
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.bis.parser.Parser
import spray.json.{JsNumber, JsObject}

import scala.concurrent.{ExecutionContext, Future}

object Etro extends Parser {

  override def parse(job: Job.Job, js: JsObject)(implicit
    executionContext: ExecutionContext
  ): Future[Map[String, Long]] =
    Future {
      js.fields.foldLeft(Map.empty[String, Long]) {
        case (acc, (key, JsNumber(id))) => BisProvider.remapKey(key).map(k => acc + (k -> id.toLong)).getOrElse(acc)
        case (acc, _) => acc
      }
    }

  override def uri(root: Uri, id: String): Uri =
    root.withPath(Uri.Path / "api" / "gearsets" / id)
}
