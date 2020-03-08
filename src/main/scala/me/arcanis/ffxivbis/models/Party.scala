/*
 * Copyright (c) 2019 Evgeniy Alekseev.
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

case class Party(partyId: String, rules: Seq[String], players: Map[PlayerId, Player])
  extends StrictLogging {
  require(players.keys.forall(_.partyId == partyId), "party id must be same")

  def getPlayers: Seq[Player] = players.values.toSeq
  def player(playerId: PlayerId): Option[Player] = players.get(playerId)
  def withPlayer(player: Player): Party =
    try {
      require(player.partyId == partyId, "player must belong to this party")
      copy(players = players + (player.playerId -> player))
    } catch {
      case exception: Exception =>
        logger.error("cannot add player", exception)
        this
    }

  def suggestLoot(piece: Piece): LootSelector.LootSelectorResult =
    LootSelector(getPlayers, piece, rules)
}

object Party {
  def apply(partyId: Option[String], config: Config): Party =
    new Party(partyId.getOrElse(randomPartyId), getRules(config), Map.empty)

  def apply(partyId: String, config: Config,
            players: Map[Long, Player], bis: Seq[Loot], loot: Seq[Loot]): Party = {
    val bisByPlayer = bis.groupBy(_.playerId).view.mapValues(piece => BiS(piece.map(_.piece)))
    val lootByPlayer = loot.groupBy(_.playerId).view
    val playersWithItems = players.foldLeft(Map.empty[PlayerId, Player]) {
      case (acc, (playerId, player)) =>
        acc + (player.playerId -> player
          .withBiS(bisByPlayer.get(playerId))
          .withLoot(lootByPlayer.getOrElse(playerId, Seq.empty)))
    }
    Party(partyId, getRules(config), playersWithItems)
  }

  def getRules(config: Config): Seq[String] =
    config.getStringList("me.arcanis.ffxivbis.settings.priority").asScala.toSeq

  def randomPartyId: String = Random.alphanumeric.take(20).mkString
}
