package me.arcanis.ffxivbis.service.impl

import akka.pattern.pipe
import me.arcanis.ffxivbis.models.User
import me.arcanis.ffxivbis.service.Database

trait DatabaseUserHandler { this: Database =>
  import DatabaseUserHandler._

  def userHandler: Receive = {
    case DeleteUser(partyId, username) =>
      val client = sender()
      profile.deleteUser(partyId, username).pipeTo(client)

    case GetUser(partyId, username) =>
      val client = sender()
      profile.getUser(partyId, username).pipeTo(client)

    case GetUsers(partyId) =>
      val client = sender()
      profile.getUsers(partyId).pipeTo(client)

    case InsertUser(user, isHashedPassword) =>
      val client = sender()
      val toInsert = if (isHashedPassword) user else user.copy(password = user.hash)
      profile.insertUser(toInsert).pipeTo(client)
  }
}

object DatabaseUserHandler {
  case class DeleteUser(partyId: String, username: String)
  case class GetUser(partyId: String, username: String)
  case class GetUsers(partyId: String)
  case class InsertUser(user: User, isHashedPassword: Boolean)
}
