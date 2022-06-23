/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import anorm.SqlParser._
import anorm._
import me.arcanis.ffxivbis.models.{Job, Loot, Piece, PieceType}

import java.time.Instant
import scala.concurrent.Future

trait LootProfile extends DatabaseConnection {

  private val loot: RowParser[Loot] =
    (long("player_id") ~ str("piece") ~ str("piece_type")
      ~ str("job") ~ long("created") ~ int("is_free_loot"))
      .map { case playerId ~ piece ~ pieceType ~ job ~ created ~ isFreeLoot =>
        Loot(
          playerId = playerId,
          piece = Piece(
            piece = piece,
            pieceType = PieceType.withName(pieceType),
            job = Job.withName(job)
          ),
          timestamp = Instant.ofEpochMilli(created),
          isFreeLoot = isFreeLoot == 1,
        )
      }

  def deletePieceById(loot: Loot)(playerId: Long): Future[Int] =
    withConnection { implicit conn =>
      SQL("""delete from loot
          | where loot_id in
          | (
          |   select loot_id from loot
          |    where player_id = {player_id}
          |      and piece = {piece}
          |      and piece_type = {piece_type}
          |      and job = {job}
          |      and is_free_loot = {is_free_loot}
          |    limit 1
          | )""".stripMargin)
        .on(
          "player_id" -> playerId,
          "piece" -> loot.piece.piece,
          "piece_type" -> loot.piece.pieceType.toString,
          "job" -> loot.piece.job.toString,
          "is_free_loot" -> loot.isFreeLootToInt
        )
        .executeUpdate()
    }

  def getPiecesById(playerId: Long): Future[Seq[Loot]] = getPiecesById(Seq(playerId))

  def getPiecesById(playerIds: Seq[Long]): Future[Seq[Loot]] =
    if (playerIds.isEmpty) Future.successful(Seq.empty)
    else
      withConnection { implicit conn =>
        SQL("""select * from loot where player_id in ({player_ids})""")
          .on("player_ids" -> playerIds)
          .executeQuery()
          .as(loot.*)
      }

  def insertPieceById(loot: Loot)(playerId: Long): Future[Int] =
    withConnection { implicit conn =>
      SQL("""insert into loot
          |  (player_id, piece, piece_type, job, created, is_free_loot)
          | values
          |  ({player_id}, {piece}, {piece_type}, {job}, {created}, {is_free_loot})""".stripMargin)
        .on(
          "player_id" -> playerId,
          "piece" -> loot.piece.piece,
          "piece_type" -> loot.piece.pieceType.toString,
          "job" -> loot.piece.job.toString,
          "created" -> DatabaseProfile.now,
          "is_free_loot" -> loot.isFreeLootToInt
        )
        .executeUpdate()
    }
}
