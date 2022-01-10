/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.AuthenticationFailedRejection._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import me.arcanis.ffxivbis.models.{Permission, User}

import scala.concurrent.{ExecutionContext, Future}

// idea comes from https://synkre.com/bcrypt-for-akka-http-password-encryption/
trait Authorization {

  def auth: AuthorizationProvider

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

  def authAdmin(partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext
  ): Future[Option[User]] =
    authenticator(Permission.admin, partyId)(username, password)

  def authGet(partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext
  ): Future[Option[User]] =
    authenticator(Permission.get, partyId)(username, password)

  def authPost(partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext
  ): Future[Option[User]] =
    authenticator(Permission.post, partyId)(username, password)

  private def authenticator(scope: Permission.Value, partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext
  ): Future[Option[User]] =
    auth.get(partyId, username).map {
      case Some(user) if user.verify(password) && user.verityScope(scope) => Some(user)
      case _ => None
    }
}
