/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.typesafe.config.Config
import me.arcanis.ffxivbis.messages.DatabaseMessage.GetUser
import me.arcanis.ffxivbis.messages.Message
import me.arcanis.ffxivbis.models.{Permission, User}

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}

trait AuthorizationProvider {

  def get(partyId: String, username: String): Future[Option[User]]

  def authenticator[T](scope: Permission.Value, partyId: String)(username: String, password: String)(implicit
    executionContext: ExecutionContext,
    extractor: User => T
  ): Future[Option[T]] =
    get(partyId, username).map {
      case Some(user) if user.verify(password) && user.verityScope(scope) => Some(extractor(user))
      case _ => None
    }
}

object AuthorizationProvider {

  def apply(config: Config, storage: ActorRef[Message])(implicit
    timeout: Timeout,
    scheduler: Scheduler
  ): AuthorizationProvider =
    new AuthorizationProvider {
      private val cacheSize = config.getInt("me.arcanis.ffxivbis.web.authorization-cache.cache-size")
      private val cacheTimeout =
        config.getDuration("me.arcanis.ffxivbis.web.authorization-cache.cache-timeout", TimeUnit.MILLISECONDS)

      private val cache: LoadingCache[(String, String), Future[Option[User]]] = CacheBuilder
        .newBuilder()
        .expireAfterWrite(cacheTimeout, TimeUnit.MILLISECONDS)
        .maximumSize(cacheSize)
        .build(
          new CacheLoader[(String, String), Future[Option[User]]] {
            override def load(key: (String, String)): Future[Option[User]] = {
              val (partyId, username) = key
              storage.ask(GetUser(partyId, username, _))(timeout, scheduler)
            }
          }
        )

      override def get(partyId: String, username: String): Future[Option[User]] =
        cache.get((partyId, username))
    }
}
