/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.AuthenticationFailedRejection._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import me.arcanis.ffxivbis.messages.{GetUser, Message}
import me.arcanis.ffxivbis.models.Permission

import scala.concurrent.{ExecutionContext, Future}

// idea comes from https://synkre.com/bcrypt-for-akka-http-password-encryption/
trait Authorization {

  def storage: ActorRef[Message]

  def authenticateBasicBCrypt[T](realm: String, authenticate: (String, String) => Future[Option[T]]): Directive1[T] = {
    def challenge = HttpChallenges.basic(realm)

    extractCredentials.flatMap {
      case Some(BasicHttpCredentials(username, password)) =>
        onSuccess(authenticate(username, password)).flatMap {
          case Some(client) => provide(client)
          case None => reject(AuthenticationFailedRejection(CredentialsRejected, challenge))
        }
      case _ => reject(AuthenticationFailedRejection(CredentialsMissing, challenge))
    }
  }

  def authenticator(scope: Permission.Value, partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Option[String]] =
    storage.ask(GetUser(partyId, username, _)).map {
      case Some(user) if user.verify(password) && user.verityScope(scope) => Some(username)
      case _ => None
    }

  def authAdmin(partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Option[String]] =
    authenticator(Permission.admin, partyId)(username, password)

  def authGet(partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Option[String]] =
    authenticator(Permission.get, partyId)(username, password)

  def authPost(partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Option[String]] =
    authenticator(Permission.post, partyId)(username, password)
}
