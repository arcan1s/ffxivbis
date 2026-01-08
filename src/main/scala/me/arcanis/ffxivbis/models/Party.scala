/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.service.LootSelector

import scala.jdk.CollectionConverters._
import scala.util.Random
import scala.util.control.NonFatal

case class Party(partyDescription: PartyDescription, rules: Seq[String], players: Map[PlayerId, Player])
  extends StrictLogging {

  require(players.keys.forall(_.partyId == partyDescription.partyId), "party id must be same")

  def getPlayers: Seq[Player] = players.values.toSeq

  def player(playerId: PlayerId): Option[Player] = players.get(playerId)

  def withPlayer(player: Player): Party =
    try {
      require(player.partyId == partyDescription.partyId, "player must belong to this party")
      copy(players = players + (player.playerId -> player))
    } catch {
      case NonFatal(exception) =>
        logger.error("cannot add player", exception)
        this
    }

  def suggestLoot(pieces: Seq[Piece]): LootSelector.LootSelectorResult =
    LootSelector(getPlayers, pieces, rules)
}

object Party {

  def apply(
    party: PartyDescription,
    config: Config,
    players: Map[Long, Player],
    bis: Seq[Loot],
    loot: Seq[Loot]
  ): Party = {
    val bisByPlayer = bis.groupBy(_.playerId).view.mapValues(piece => BiS(piece.map(_.piece)))
    val lootByPlayer = loot.groupBy(_.playerId).view
    val playersWithItems = players.foldLeft(Map.empty[PlayerId, Player]) { case (acc, (playerId, player)) =>
      acc + (player.playerId -> player
        .withBiS(bisByPlayer.get(playerId))
        .withLoot(lootByPlayer.getOrElse(playerId, Seq.empty)))
    }
    Party(party, getRules(config), playersWithItems)
  }

  def getRules(config: Config): Seq[String] =
    config.getStringList("me.arcanis.ffxivbis.settings.priority").asScala.toSeq

  def randomPartyId: String = Random.alphanumeric.take(20).mkString
}
