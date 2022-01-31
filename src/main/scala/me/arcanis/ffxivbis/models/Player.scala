/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

case class Player(
  id: Long,
  partyId: String,
  job: Job,
  nick: String,
  bis: BiS,
  loot: Seq[Loot],
  link: Option[String] = None,
  priority: Int = 0
) {
  require(job ne Job.AnyJob, "AnyJob is not allowed")

  val playerId: PlayerId = PlayerId(partyId, job, nick)

  def withBiS(set: Option[BiS]): Player = set match {
    case Some(value) => copy(bis = value)
    case None => this
  }

  def withCounters(piece: Option[Piece]): PlayerIdWithCounters =
    PlayerIdWithCounters(
      partyId,
      job,
      nick,
      isRequired(piece),
      priority,
      bisCountTotal,
      lootCount(piece),
      lootCountBiS,
      lootCountTotal
    )

  def withLoot(piece: Loot): Player = withLoot(Seq(piece))

  def withLoot(list: Seq[Loot]): Player = {
    require(loot.forall(_.playerId == id), "player id must be same")
    copy(loot = loot ++ list)
  }

  def isRequired(piece: Option[Piece]): Boolean =
    piece match {
      case None => false
      case Some(p) if !bis.hasPiece(p) => false
      case Some(p: Piece.PieceUpgrade) => bis.upgrades(p) > lootCount(piece)
      case Some(_) => lootCount(piece) == 0
    }

  def bisCountTotal: Int = bis.pieces.count(_.pieceType == PieceType.Savage)

  def lootCount(piece: Option[Piece]): Int = piece match {
    case Some(p) => loot.count(item => !item.isFreeLoot && item.piece == p)
    case None => lootCountTotal
  }

  def lootCountBiS: Int = loot.map(_.piece).count(bis.hasPiece)

  def lootCountTotal: Int = loot.count(!_.isFreeLoot)

  def lootPriority: Int = priority
}
