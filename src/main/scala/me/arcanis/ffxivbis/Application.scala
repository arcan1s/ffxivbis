/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.RootEndpoint
import me.arcanis.ffxivbis.service.PartyService
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.database.{Database, Migration}

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class Application(context: ActorContext[Nothing]) extends AbstractBehavior[Nothing](context) with StrictLogging {

  logger.info("root supervisor started")
  startApplication()

  override def onMessage(msg: Nothing): Behavior[Nothing] = Behaviors.unhandled

  override def onSignal: PartialFunction[Signal, Behavior[Nothing]] = { case PostStop =>
    logger.info("root supervisor stopped")
    Behaviors.same
  }

  private def startApplication(): Unit = {
    val config = context.system.settings.config
    val host = config.getString("me.arcanis.ffxivbis.web.host")
    val port = config.getInt("me.arcanis.ffxivbis.web.port")

    implicit val executionContext: ExecutionContext = context.system.executionContext
    implicit val materializer: Materializer = Materializer(context)

    Migration(config) match {
      case Success(result) if result.success =>
        val bisProvider = context.spawn(BisProvider(), "bis-provider")
        val storage = context.spawn(Database(), "storage")
        val party = context.spawn(PartyService(storage), "party")
        val http = new RootEndpoint(context.system, party, bisProvider)

        val flow = Route.toFlow(http.routes)(context.system)
        Http(context.system).newServerAt(host, port).bindFlow(flow)

      case Success(result) =>
        logger.error(s"migration completed with error, executed ${result.migrationsExecuted}")
        result.migrations.asScala.foreach(o => logger.info(s"=> ${o.description} (${o.executionTime})"))
        context.system.terminate()

      case Failure(exception) =>
        logger.error("exception during migration", exception)
        context.system.terminate()
    }
  }
}

object Application {

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => new Application(context))
}
