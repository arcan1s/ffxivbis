/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import me.arcanis.ffxivbis.models.{Job, Loot, Piece, PieceType}
import slick.lifted.{ForeignKeyQuery, Index}

import java.time.Instant
import scala.concurrent.Future

trait LootProfile { this: DatabaseProfile =>
  import dbConfig.profile.api._

  case class LootRep(
    lootId: Option[Long],
    playerId: Long,
    created: Long,
    piece: String,
    pieceType: String,
    job: String,
    isFreeLoot: Int
  ) {

    def toLoot: Loot = Loot(
      playerId,
      Piece(piece, PieceType.withName(pieceType), Job.withName(job)),
      Instant.ofEpochMilli(created),
      isFreeLoot == 1
    )
  }

  object LootRep {
    def fromLoot(playerId: Long, loot: Loot): LootRep =
      LootRep(
        None,
        playerId,
        loot.timestamp.toEpochMilli,
        loot.piece.piece,
        loot.piece.pieceType.toString,
        loot.piece.job.toString,
        if (loot.isFreeLoot) 1 else 0
      )
  }

  class LootPieces(tag: Tag) extends Table[LootRep](tag, "loot") {
    def lootId: Rep[Long] = column[Long]("loot_id", O.AutoInc, O.PrimaryKey)
    def playerId: Rep[Long] = column[Long]("player_id")
    def created: Rep[Long] = column[Long]("created")
    def piece: Rep[String] = column[String]("piece")
    def pieceType: Rep[String] = column[String]("piece_type")
    def job: Rep[String] = column[String]("job")
    def isFreeLoot: Rep[Int] = column[Int]("is_free_loot")

    def * =
      (lootId.?, playerId, created, piece, pieceType, job, isFreeLoot) <> ((LootRep.apply _).tupled, LootRep.unapply)

    def fkPlayerId: ForeignKeyQuery[Players, PlayerRep] =
      foreignKey("player_id", playerId, playersTable)(_.playerId, onDelete = ForeignKeyAction.Cascade)
    def lootOwnerIdx: Index =
      index("loot_owner_idx", playerId, unique = false)
  }

  def deletePieceById(loot: Loot)(playerId: Long): Future[Int] =
    db.run(pieceLoot(LootRep.fromLoot(playerId, loot)).map(_.lootId).max.result).flatMap {
      case Some(id) => db.run(lootTable.filter(_.lootId === id).delete)
      case _ => throw new IllegalArgumentException(s"Could not find piece $loot belong to $playerId")
    }

  def getPiecesById(playerId: Long): Future[Seq[Loot]] = getPiecesById(Seq(playerId))

  def getPiecesById(playerIds: Seq[Long]): Future[Seq[Loot]] =
    db.run(piecesLoot(playerIds).result).map(_.map(_.toLoot))

  def insertPieceById(loot: Loot)(playerId: Long): Future[Int] =
    db.run(lootTable.insertOrUpdate(LootRep.fromLoot(playerId, loot)))

  private def pieceLoot(piece: LootRep) =
    piecesLoot(Seq(piece.playerId)).filter(_.piece === piece.piece)

  private def piecesLoot(playerIds: Seq[Long]) =
    lootTable.filter(_.playerId.inSet(playerIds.toSet))
}
