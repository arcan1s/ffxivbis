/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import anorm.SqlParser._
import anorm._
import me.arcanis.ffxivbis.models.{BiS, Job, Player, PlayerId}

import scala.concurrent.Future

trait PlayersProfile extends DatabaseConnection {

  private val player: RowParser[Player] =
    (long("player_id") ~ str("party_id") ~ str("job")
      ~ str("nick") ~ str("bis_link").? ~ int("priority").?)
      .map { case playerId ~ partyId ~ job ~ nick ~ link ~ priority =>
        Player(
          id = playerId,
          partyId = partyId,
          job = Job.withName(job),
          nick = nick,
          bis = BiS.empty,
          loot = Seq.empty,
          link = link,
          priority = priority.getOrElse(0),
        )
      }

  def deletePlayer(playerId: PlayerId): Future[Int] =
    withConnection { implicit conn =>
      SQL("""delete from players
          | where party_id = {party_id}
          |   and nick = {nick}
          |   and job = {job}""".stripMargin)
        .on("party_id" -> playerId.partyId, "nick" -> playerId.nick, "job" -> playerId.job.toString)
        .executeUpdate()
    }

  def getParty(partyId: String): Future[Map[Long, Player]] =
    withConnection { implicit conn =>
      SQL("""select * from players where party_id = {party_id}""")
        .on("party_id" -> partyId)
        .executeQuery()
        .as(player.*)
        .map(p => p.id -> p)
        .toMap
    }

  def getPlayer(playerId: PlayerId): Future[Option[Long]] =
    withConnection { implicit conn =>
      SQL("""select player_id from players
          | where party_id = {party_id}
          |   and nick = {nick}
          |   and job = {job}""".stripMargin)
        .on("party_id" -> playerId.partyId, "nick" -> playerId.nick, "job" -> playerId.job.toString)
        .executeQuery()
        .as(scalar[Long].singleOpt)
    }

  def getPlayerFull(playerId: PlayerId): Future[Option[Player]] =
    withConnection { implicit conn =>
      SQL("""select * from players
          | where party_id = {party_id}
          |   and nick = {nick}
          |   and job = {job}""".stripMargin)
        .on("party_id" -> playerId.partyId, "nick" -> playerId.nick, "job" -> playerId.job.toString)
        .executeQuery()
        .as(player.singleOpt)
    }

  def getPlayers(partyId: String): Future[Seq[Long]] =
    withConnection { implicit conn =>
      SQL("""select player_id from players where party_id = {party_id}""")
        .on("party_id" -> partyId)
        .executeQuery()
        .as(scalar[Long].*)
    }

  def insertPlayer(player: Player): Future[Int] =
    withConnection { implicit conn =>
      SQL("""insert into players
          |  (party_id, created, job, nick, bis_link, priority)
          | values
          |  ({party_id}, {created}, {job}, {nick}, {link}, {priority})
          | on conflict (party_id, nick, job) do update set
          |  bis_link = {link}, priority = {priority}""".stripMargin)
        .on(
          "party_id" -> player.partyId,
          "created" -> DatabaseProfile.now,
          "job" -> player.job.toString,
          "nick" -> player.nick,
          "link" -> player.link,
          "priority" -> player.priority
        )
        .executeUpdate()
    }

  def updateBiSLink(playerId: PlayerId, link: String): Future[Int] =
    withConnection { implicit conn =>
      SQL("""update players
          |  set bis_link = {link}
          | where party_id = {party_id} and nick = {nick} and job = {job}""".stripMargin)
        .on(
          "link" -> link,
          "party_id" -> playerId.partyId,
          "nick" -> playerId.nick,
          "job" -> playerId.job.toString
        )
        .executeUpdate()
    }

}
