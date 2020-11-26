/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.RootEndpoint
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.impl.DatabaseImpl
import me.arcanis.ffxivbis.service.PartyService
import me.arcanis.ffxivbis.storage.Migration

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

class Application extends Actor with StrictLogging {
  implicit private val executionContext: ExecutionContext = context.system.dispatcher
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  private val config = context.system.settings.config
  private val host = config.getString("me.arcanis.ffxivbis.web.host")
  private val port = config.getInt("me.arcanis.ffxivbis.web.port")

  override def receive: Receive = Actor.emptyBehavior

  Migration(config).onComplete {
    case Success(_) =>
      val bisProvider = context.system.actorOf(BisProvider.props, "bis-provider")
      val storage = context.system.actorOf(DatabaseImpl.props, "storage")
      val party = context.system.actorOf(PartyService.props(storage), "party")
      val http = new RootEndpoint(context.system, party, bisProvider)

      logger.info(s"start server at $host:$port")
      val bind = Http()(context.system).bindAndHandle(http.route, host, port)
      Await.result(context.system.whenTerminated, Duration.Inf)
      bind.foreach(_.unbind())

    case Failure(exception) => throw exception
  }
}

object Application {
  def props: Props = Props(new Application)
}
