/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.impl

import akka.actor.typed.scaladsl.Behaviors
import me.arcanis.ffxivbis.messages.{AddUser, DatabaseMessage, DeleteUser, Exists, GetUser, GetUsers}
import me.arcanis.ffxivbis.service.Database

trait DatabaseUserHandler { this: Database =>

  def userHandler: DatabaseMessage.Handler = {
    case AddUser(user, isHashedPassword, client) =>
      val toInsert = if (isHashedPassword) user else user.withHashedPassword
      profile.insertUser(toInsert).foreach(_ => client ! ())
      Behaviors.same

    case DeleteUser(partyId, username, client) =>
      profile.deleteUser(partyId, username).foreach(_ => client ! ())
      Behaviors.same

    case Exists(partyId, client) =>
      profile.exists(partyId).foreach(client ! _)
      Behaviors.same

    case GetUser(partyId, username, client) =>
      profile.getUser(partyId, username).foreach(client ! _)
      Behaviors.same

    case GetUsers(partyId, client) =>
      profile.getUsers(partyId).foreach(client ! _)
      Behaviors.same
  }
}
