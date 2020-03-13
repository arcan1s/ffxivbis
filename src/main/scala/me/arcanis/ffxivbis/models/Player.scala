/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

case class Player(id: Long,
                  partyId: String,
                  job: Job.Job,
                  nick: String,
                  bis: BiS,
                  loot: Seq[Loot],
                  link: Option[String] = None,
                  priority: Int = 0) {
  require(job ne Job.AnyJob, "AnyJob is not allowed")

  val playerId: PlayerId = PlayerId(partyId, job, nick)
  def withBiS(set: Option[BiS]): Player = set match {
    case Some(value) => copy(bis = value)
    case None => this
  }
  def withCounters(piece: Option[Piece]): PlayerIdWithCounters =
    PlayerIdWithCounters(
      partyId, job, nick, isRequired(piece), priority,
      bisCountTotal(piece), lootCount(piece),
      lootCountBiS(piece), lootCountTotal(piece))
  def withLoot(piece: Loot): Player = withLoot(Seq(piece))
  def withLoot(list: Seq[Loot]): Player = {
    require(loot.forall(_.playerId == id), "player id must be same")
    list match {
      case Nil => this
      case _ => copy(loot = list)
    }
  }

  def isRequired(piece: Option[Piece]): Boolean = {
    piece match {
      case None => false
      case Some(p) if !bis.hasPiece(p) => false
      case Some(p: PieceUpgrade) => bis.upgrades(p) > lootCount(piece)
      case Some(_) => lootCount(piece) == 0
    }
  }

  def bisCountTotal(piece: Option[Piece]): Int = bis.pieces.count(_.pieceType == PieceType.Savage)
  def lootCount(piece: Option[Piece]): Int = piece match {
    case Some(p) => loot.count(_.piece == p)
    case None => lootCountTotal(piece)
  }
  def lootCountBiS(piece: Option[Piece]): Int = loot.map(_.piece).count(bis.hasPiece)
  def lootCountTotal(piece: Option[Piece]): Int = loot.length
  def lootPriority(piece: Piece): Int = priority
}
