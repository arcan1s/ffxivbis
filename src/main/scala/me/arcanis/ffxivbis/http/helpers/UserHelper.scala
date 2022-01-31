/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.helpers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import me.arcanis.ffxivbis.messages.ControlMessage.GetNewPartyId
import me.arcanis.ffxivbis.messages.DatabaseMessage._
import me.arcanis.ffxivbis.messages.Message
import me.arcanis.ffxivbis.models.User

import scala.concurrent.Future

trait UserHelper {

  def storage: ActorRef[Message]

  def addUser(user: User, isHashedPassword: Boolean)(implicit timeout: Timeout, scheduler: Scheduler): Future[Unit] =
    storage.ask(AddUser(user, isHashedPassword, _))

  def newPartyId(implicit timeout: Timeout, scheduler: Scheduler): Future[String] =
    storage.ask(GetNewPartyId)

  def user(partyId: String, username: String)(implicit timeout: Timeout, scheduler: Scheduler): Future[Option[User]] =
    storage.ask(GetUser(partyId, username, _))

  def users(partyId: String)(implicit timeout: Timeout, scheduler: Scheduler): Future[Seq[User]] =
    storage.ask(GetUsers(partyId, _))

  def removeUser(partyId: String, username: String)(implicit timeout: Timeout, scheduler: Scheduler): Future[Unit] =
    storage.ask(DeleteUser(partyId, username, _))
}
