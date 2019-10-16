package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.models.User
import me.arcanis.ffxivbis.service.impl.DatabaseUserHandler

import scala.concurrent.{ExecutionContext, Future}

class UserHelper(storage: ActorRef) {

  def addUser(user: User, isHashedPassword: Boolean)
             (implicit executionContext: ExecutionContext): Future[Unit] =
    Future { storage ! DatabaseUserHandler.InsertUser(user, isHashedPassword) }

  def user(partyId: String, username: String)
          (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Option[User]] =
    (storage ? DatabaseUserHandler.GetUser(partyId, username)).mapTo[Option[User]]

  def users(partyId: String)
           (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[User]] =
    (storage ? DatabaseUserHandler.GetUsers(partyId)).mapTo[Seq[User]]

  def removeUser(partyId: String, username: String)
                (implicit executionContext: ExecutionContext): Future[Unit] =
    Future { storage ! DatabaseUserHandler.DeleteUser(partyId, username) }
}
