/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import java.time.Instant

import com.typesafe.config.Config
import me.arcanis.ffxivbis.models.{Loot, Piece, PlayerId}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class DatabaseProfile(context: ExecutionContext, config: Config)
  extends BiSProfile with LootProfile with PartyProfile with PlayersProfile with UsersProfile {

  implicit val executionContext: ExecutionContext = context

  val dbConfig: DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig[JdbcProfile]("", DatabaseProfile.getSection(config))
  import dbConfig.profile.api._
  val db = dbConfig.db

  val bisTable: TableQuery[BiSPieces] = TableQuery[BiSPieces]
  val lootTable: TableQuery[LootPieces] = TableQuery[LootPieces]
  val partiesTable: TableQuery[Parties] = TableQuery[Parties]
  val playersTable: TableQuery[Players] = TableQuery[Players]
  val usersTable: TableQuery[Users] = TableQuery[Users]

  // generic bis api
  def deletePieceBiS(playerId: PlayerId, piece: Piece): Future[Int] =
    byPlayerId(playerId, deletePieceBiSById(piece))
  def getPiecesBiS(playerId: PlayerId): Future[Seq[Loot]] =
    byPlayerId(playerId, getPiecesBiSById)
  def getPiecesBiS(partyId: String): Future[Seq[Loot]] =
    byPartyId(partyId, getPiecesBiSById)
  def insertPieceBiS(playerId: PlayerId, piece: Piece): Future[Int] =
    byPlayerId(playerId, insertPieceBiSById(piece))

  // generic loot api
  def deletePiece(playerId: PlayerId, piece: Piece): Future[Int] = {
    // we don't really care here about loot
    val loot = Loot(-1, piece, Instant.now, isFreeLoot = false)
    byPlayerId(playerId, deletePieceById(loot))
  }
  def getPieces(playerId: PlayerId): Future[Seq[Loot]] =
    byPlayerId(playerId, getPiecesById)
  def getPieces(partyId: String): Future[Seq[Loot]] =
    byPartyId(partyId, getPiecesById)
  def insertPiece(playerId: PlayerId, loot: Loot): Future[Int] =
    byPlayerId(playerId, insertPieceById(loot))

  private def byPartyId[T](partyId: String, callback: Seq[Long] => Future[T]): Future[T] =
    getPlayers(partyId).map(callback).flatten
  private def byPlayerId[T](playerId: PlayerId, callback: Long => Future[T]): Future[T] =
    getPlayer(playerId).flatMap {
      case Some(id) => callback(id)
      case None => Future.failed(new Error(s"Could not find player $playerId"))
    }
}

object DatabaseProfile {
  def now: Long = Instant.now.toEpochMilli
  def getSection(config: Config): Config = {
    val section = config.getString("me.arcanis.ffxivbis.database.mode")
    config.getConfig("me.arcanis.ffxivbis.database").getConfig(section)
  }
}
