/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.database.impl

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.actor.typed.{Behavior, DispatcherSelector}
import com.typesafe.config.Config
import me.arcanis.ffxivbis.messages.DatabaseMessage
import me.arcanis.ffxivbis.messages.DatabaseMessage.{BisDatabaseMessage, LootDatabaseMessage, PartyDatabaseMessage, UserDatabaseMessage}
import me.arcanis.ffxivbis.service.database.Database
import me.arcanis.ffxivbis.storage.DatabaseProfile

import scala.concurrent.ExecutionContext

class DatabaseImpl(context: ActorContext[DatabaseMessage])
  extends AbstractBehavior[DatabaseMessage](context)
  with Database
  with DatabaseBiSHandler
  with DatabaseLootHandler
  with DatabasePartyHandler
  with DatabaseUserHandler {

  implicit override val executionContext: ExecutionContext = {
    val selector = DispatcherSelector.fromConfig("me.arcanis.ffxivbis.default-dispatcher")
    context.system.dispatchers.lookup(selector)
  }
  override val config: Config = context.system.settings.config
  override val profile: DatabaseProfile = new DatabaseProfile(executionContext, config)

  override def onMessage(msg: DatabaseMessage): Behavior[DatabaseMessage] =
    msg match {
      case msg: BisDatabaseMessage => bisHandler(msg)
      case msg: LootDatabaseMessage => lootHandler(msg)
      case msg: PartyDatabaseMessage => partyHandler(msg)
      case msg: UserDatabaseMessage => userHandler(msg)
    }

}
