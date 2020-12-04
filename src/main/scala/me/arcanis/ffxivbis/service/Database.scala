/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.messages.{DatabaseMessage}
import me.arcanis.ffxivbis.models.{Party, Player, PlayerId}
import me.arcanis.ffxivbis.service.impl.DatabaseImpl
import me.arcanis.ffxivbis.storage.DatabaseProfile

import scala.concurrent.{ExecutionContext, Future}

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
      bis <- if (withBiS) profile.getPiecesBiS(partyId) else Future(Seq.empty)
      loot <- if (withLoot) profile.getPieces(partyId) else Future(Seq.empty)
    } yield Party(partyDescription, config, players, bis, loot)
}

object Database {

  def apply(): Behavior[DatabaseMessage] =
    Behaviors.setup[DatabaseMessage](context => new DatabaseImpl(context))
}
