/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis

import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.RootEndpoint
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.{Database, PartyService}
import me.arcanis.ffxivbis.storage.Migration

import scala.concurrent.ExecutionContext

class Application(context: ActorContext[Nothing])
  extends AbstractBehavior[Nothing](context) with StrictLogging {

  logger.info("root supervisor started")
  startApplication()

  override def onMessage(msg: Nothing): Behavior[Nothing] = Behaviors.unhandled

  override def onSignal: PartialFunction[Signal, Behavior[Nothing]] = {
    case PostStop =>
      logger.info("root supervisor stopped")
      Behaviors.same
  }

  private def startApplication(): Unit = {
    val config = context.system.settings.config
    val host = config.getString("me.arcanis.ffxivbis.web.host")
    val port = config.getInt("me.arcanis.ffxivbis.web.port")

    implicit val executionContext: ExecutionContext = context.system.executionContext
    implicit val materializer: Materializer = Materializer(context)

    Migration(config)

    val bisProvider = context.spawn(BisProvider(), "bis-provider")
    val storage = context.spawn(Database(), "storage")
    val party = context.spawn(PartyService(storage), "party")
    val http = new RootEndpoint(context.system, party, bisProvider)

    Http()(context.system).newServerAt(host, port).bindFlow(http.route)
  }
}

object Application {

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => new Application(context))
}
