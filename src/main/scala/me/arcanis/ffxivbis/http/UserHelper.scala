/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.models.User
import me.arcanis.ffxivbis.service.PartyService
import me.arcanis.ffxivbis.service.impl.DatabaseUserHandler

import scala.concurrent.{ExecutionContext, Future}

class UserHelper(storage: ActorRef) {

  def addUser(user: User, isHashedPassword: Boolean)
             (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabaseUserHandler.AddUser(user, isHashedPassword)).mapTo[Int]

  def newPartyId(implicit executionContext: ExecutionContext, timeout: Timeout): Future[String] =
    (storage ? PartyService.GetNewPartyId).mapTo[String]

  def user(partyId: String, username: String)
          (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Option[User]] =
    (storage ? DatabaseUserHandler.GetUser(partyId, username)).mapTo[Option[User]]

  def users(partyId: String)
           (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[User]] =
    (storage ? DatabaseUserHandler.GetUsers(partyId)).mapTo[Seq[User]]

  def removeUser(partyId: String, username: String)
                (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabaseUserHandler.DeleteUser(partyId, username)).mapTo[Int]
}
