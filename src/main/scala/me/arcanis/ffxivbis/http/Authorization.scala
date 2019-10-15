package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.AuthenticationFailedRejection._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.models.{Permission, User}
import me.arcanis.ffxivbis.service.impl.DatabaseUserHandler

import scala.concurrent.{ExecutionContext, Future}

// idea comes from https://synkre.com/bcrypt-for-akka-http-password-encryption/
trait Authorization {

  def storage: ActorRef

  def authenticateBasicBCrypt[T](realm: String,
                                 authenticate: (String, String) => Future[Option[T]]): Directive1[T] = {
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

  def authenticator(scope: Permission.Value)(partyId: String)
                   (username: String, password: String)
                   (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Option[String]] =
    (storage ? DatabaseUserHandler.GetUser(partyId, username)).mapTo[Option[User]].map {
      case Some(user) if user.verify(password) && user.verityScope(scope) => Some(username)
      case _ => None
    }

  def authAdmin(partyId: String)(username: String, password: String)
               (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Option[String]] =
    authenticator(Permission.admin)(partyId)(username, password)

  def authGet(partyId: String)(username: String, password: String)
             (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Option[String]] =
    authenticator(Permission.get)(partyId)(username, password)

  def authPost(partyId: String)(username: String, password: String)
              (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Option[String]] =
    authenticator(Permission.post)(partyId)(username, password)
}
