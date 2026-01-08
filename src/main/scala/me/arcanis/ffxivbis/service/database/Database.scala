/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.database

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.messages.DatabaseMessage
import me.arcanis.ffxivbis.models.{Party, Player, PlayerId}
import me.arcanis.ffxivbis.service.database.impl.DatabaseImpl
import me.arcanis.ffxivbis.storage.DatabaseProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait Database extends StrictLogging {

  implicit def executionContext: ExecutionContext

  def config: Config

  def profile: DatabaseProfile

  def filterParty(party: Party, maybePlayerId: Option[PlayerId]): Seq[Player] =
    maybePlayerId match {
      case Some(playerId) => party.player(playerId).map(Seq(_)).getOrElse(Seq.empty)
      case _ => party.getPlayers
    }

  def getParty(partyId: String, withBiS: Boolean, withLoot: Boolean): Future[Party] =
    for {
      partyDescription <- profile.getPartyDescription(partyId)
      players <- profile.getParty(partyId)
      bis <- if (withBiS) profile.getPiecesBiS(partyId) else Future.successful(Seq.empty)
      loot <- if (withLoot) profile.getPieces(partyId) else Future.successful(Seq.empty)
    } yield Party(partyDescription, config, players, bis, loot)

  protected def run[T](fn: => Future[T])(onSuccess: T => Unit): Unit =
    fn.onComplete {
      case Success(value) => onSuccess(value)
      case Failure(exception) => logger.error("exception during performing database request", exception)
    }
}

object Database {

  def apply(): Behavior[DatabaseMessage] =
    Behaviors.setup[DatabaseMessage](context => new DatabaseImpl(context))
}
