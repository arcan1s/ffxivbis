package me.arcanis.ffxivbis.service

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.models.{BiS, Loot, Piece, Player, PlayerId}

import scala.jdk.CollectionConverters._
import scala.util.Random

case class Party(partyId: String, config: Config, players: Map[PlayerId, Player])
  extends StrictLogging {

  private val rules =
    config.getStringList("me.arcanis.ffxivbis.settings.priority").asScala.toSeq

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
    new Party(partyId.getOrElse(Random.alphanumeric.take(20).mkString), config, Map.empty)

  def apply(partyId: String, config: Config,
            players: Map[Long, Player], bis: Seq[Loot], loot: Seq[Loot]): Party = {
    val bisByPlayer = bis.groupBy(_.playerId).view.mapValues(piece => BiS(piece.map(_.piece)))
    val lootByPlayer = loot.groupBy(_.playerId).view.mapValues(_.map(_.piece))
    val playersWithItems = players.foldLeft(Map.empty[PlayerId, Player]) {
      case (acc, (playerId, player)) =>
        acc + (player.playerId -> player
          .withBiS(bisByPlayer.get(playerId))
          .withLoot(lootByPlayer.get(playerId)))
    }
    Party(partyId, config, playersWithItems)
  }
}
