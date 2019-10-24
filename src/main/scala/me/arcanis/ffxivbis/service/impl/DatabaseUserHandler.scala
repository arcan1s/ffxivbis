/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.impl

import akka.pattern.pipe
import me.arcanis.ffxivbis.models.User
import me.arcanis.ffxivbis.service.Database

trait DatabaseUserHandler { this: Database =>
  import DatabaseUserHandler._

  def userHandler: Receive = {
    case AddUser(user, isHashedPassword) =>
      val client = sender()
      val toInsert = if (isHashedPassword) user else user.withHashedPassword
      profile.insertUser(toInsert).pipeTo(client)

    case DeleteUser(partyId, username) =>
      val client = sender()
      profile.deleteUser(partyId, username).pipeTo(client)

    case GetUser(partyId, username) =>
      val client = sender()
      profile.getUser(partyId, username).pipeTo(client)

    case GetUsers(partyId) =>
      val client = sender()
      profile.getUsers(partyId).pipeTo(client)
  }
}

object DatabaseUserHandler {
  case class AddUser(user: User, isHashedPassword: Boolean)
  case class DeleteUser(partyId: String, username: String)
  case class GetUser(partyId: String, username: String)
  case class GetUsers(partyId: String)
}
