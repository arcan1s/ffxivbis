/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import me.arcanis.ffxivbis.models.{Job, Loot, Piece}
import slick.lifted.{ForeignKeyQuery, Index}

import scala.concurrent.Future

trait LootProfile { this: DatabaseProfile =>
  import dbConfig.profile.api._

  case class LootRep(lootId: Option[Long], playerId: Long, created: Long, piece: String,
                     isTome: Int, job: String) {
    def toLoot: Loot = Loot(playerId, Piece(piece, isTome == 1, Job.fromString(job)))
  }
  object LootRep {
    def fromPiece(playerId: Long, piece: Piece) =
      LootRep(None, playerId, DatabaseProfile.now, piece.piece, if (piece.isTome) 1 else 0,
        piece.job.toString)
  }

  class LootPieces(tag: Tag) extends Table[LootRep](tag, "loot") {
    def lootId: Rep[Long] = column[Long]("loot_id", O.AutoInc, O.PrimaryKey)
    def playerId: Rep[Long] = column[Long]("player_id")
    def created: Rep[Long] = column[Long]("created")
    def piece: Rep[String] = column[String]("piece")
    def isTome: Rep[Int] = column[Int]("is_tome")
    def job: Rep[String] = column[String]("job")

    def * =
      (lootId.?, playerId, created, piece, isTome, job) <> ((LootRep.apply _).tupled, LootRep.unapply)

    def fkPlayerId: ForeignKeyQuery[Players, PlayerRep] =
      foreignKey("player_id", playerId, playersTable)(_.playerId, onDelete = ForeignKeyAction.Cascade)
    def lootOwnerIdx: Index =
      index("loot_owner_idx", (playerId), unique = false)
  }

  def deletePieceById(piece: Piece)(playerId: Long): Future[Int] =
    db.run(pieceLoot(LootRep.fromPiece(playerId, piece)).map(_.lootId).max.result).flatMap {
      case Some(id) => db.run(lootTable.filter(_.lootId === id).delete)
      case _ => throw new IllegalArgumentException(s"Could not find piece $piece belong to $playerId")
    }
  def getPiecesById(playerId: Long): Future[Seq[Loot]] = getPiecesById(Seq(playerId))
  def getPiecesById(playerIds: Seq[Long]): Future[Seq[Loot]] =
    db.run(piecesLoot(playerIds).result).map(_.map(_.toLoot))
  def insertPieceById(piece: Piece)(playerId: Long): Future[Int] =
    db.run(lootTable.insertOrUpdate(LootRep.fromPiece(playerId, piece)))

  private def pieceLoot(piece: LootRep) =
    piecesLoot(Seq(piece.playerId)).filter(_.piece === piece.piece)
  private def piecesLoot(playerIds: Seq[Long]) =
    lootTable.filter(_.playerId.inSet(playerIds.toSet))
}
