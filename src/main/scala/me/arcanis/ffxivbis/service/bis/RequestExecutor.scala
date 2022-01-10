/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.bis

import akka.actor.ClassicActorSystemProvider
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import akka.util.ByteString
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

trait RequestExecutor {

  def system: ClassicActorSystemProvider

  private val http = Http()(system)
  implicit val materializer: Materializer = Materializer.createMaterializer(system)
  implicit val executionContext: ExecutionContext =
    system.classicSystem.dispatchers.lookup("me.arcanis.ffxivbis.default-dispatcher")

  def sendRequest[T](uri: Uri, parser: JsObject => Future[T]): Future[T] =
    http
      .singleRequest(HttpRequest(uri = uri))
      .map {
        case r: HttpResponse if r.status.isRedirection() =>
          val location = r.header[Location].get.uri
          sendRequest(uri.withPath(location.path), parser)
        case HttpResponse(status, _, entity, _) if status.isSuccess() =>
          entity.dataBytes
            .fold(ByteString.empty)(_ ++ _)
            .map(_.utf8String)
            .map(result => parser(result.parseJson.asJsObject))
            .toMat(Sink.head)(Keep.right)
            .run()
            .flatten
        case other => Future.failed(new Error(s"Invalid response from server $other"))
      }
      .flatten

  def shutdown(): Unit = http.shutdownAllConnectionPools()
}
