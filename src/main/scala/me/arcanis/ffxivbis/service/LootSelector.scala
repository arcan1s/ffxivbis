package me.arcanis.ffxivbis.service

import me.arcanis.ffxivbis.models.{Piece, Player, PlayerIdWithCounters}

class LootSelector(players: Seq[Player], piece: Piece, orderBy: Seq[String]) {

  val counters: Seq[PlayerIdWithCounters] = players.map(_.withCounters(Some(piece)))

  def suggest: LootSelector.LootSelectorResult =
    LootSelector.LootSelectorResult {
      counters.sortWith { case (left, right) => left.gt(right, orderBy) }
    }
}

object LootSelector {
  def apply(players: Seq[Player], piece: Piece, orderBy: Seq[String]): LootSelectorResult =
    new LootSelector(players, piece, orderBy).suggest

  case class LootSelectorResult(result: Seq[PlayerIdWithCounters])
}
