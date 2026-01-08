/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.database.impl

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import me.arcanis.ffxivbis.messages.DatabaseMessage
import me.arcanis.ffxivbis.messages.DatabaseMessage._
import me.arcanis.ffxivbis.service.database.Database

trait DatabaseUserHandler { this: Database =>

  def userHandler(msg: UserDatabaseMessage): Behavior[DatabaseMessage] =
    msg match {
      case AddUser(user, isHashedPassword, client) =>
        val toInsert = if (isHashedPassword) user else user.withHashedPassword
        run(profile.insertUser(toInsert))(_ => client ! ())
        Behaviors.same

      case DeleteUser(partyId, username, client) =>
        run(profile.deleteUser(partyId, username))(_ => client ! ())
        Behaviors.same

      case Exists(partyId, client) =>
        run(profile.exists(partyId))(client ! _)
        Behaviors.same

      case GetUser(partyId, username, client) =>
        run(profile.getUser(partyId, username))(client ! _)
        Behaviors.same

      case GetUsers(partyId, client) =>
        run(profile.getUsers(partyId))(client ! _)
        Behaviors.same
    }
}
