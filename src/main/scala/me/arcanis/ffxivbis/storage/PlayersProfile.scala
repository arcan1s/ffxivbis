package me.arcanis.ffxivbis.storage

import me.arcanis.ffxivbis.models.{BiS, Job, Player, PlayerId}
import slick.lifted.Index

import scala.concurrent.Future

trait PlayersProfile { this: DatabaseProfile =>
  import dbConfig.profile.api._

  case class PlayerRep(partyId: String, playerId: Option[Long], created: Long, nick: String,
                       job: String, link: Option[String], priority: Int) {
    def toPlayer: Player =
      Player(partyId, Job.fromString(job), nick, BiS(Seq.empty), List.empty, link, priority)
  }
  object PlayerRep {
    def fromPlayer(player: Player): PlayerRep =
      PlayerRep(player.partyId, None, DatabaseProfile.now, player.nick,
        player.job.toString, player.link, player.priority)
  }

  class Players(tag: Tag) extends Table[PlayerRep](tag, "players") {
    def partyId: Rep[String] = column[String]("party_id")
    def playerId: Rep[Long] = column[Long]("player_id", O.AutoInc, O.PrimaryKey)
    def created: Rep[Long] = column[Long]("created")
    def nick: Rep[String] = column[String]("nick")
    def job: Rep[String] = column[String]("job")
    def bisLink: Rep[Option[String]] = column[Option[String]]("bis_link")
    def priority: Rep[Int] = column[Int]("priority", O.Default(1))

    def * =
      (partyId, playerId.?, created, nick, job, bisLink, priority) <> ((PlayerRep.apply _).tupled, PlayerRep.unapply)

    def playersNickJobIdx: Index =
      index("players_nick_job_idx", (partyId, nick, job), unique = true)
  }

  def deletePlayer(playerId: PlayerId): Future[Int] = db.run(player(playerId).delete)
  def getParty(partyId: String): Future[Map[Long, Player]] =
    db.run(players(partyId).result).map(_.foldLeft(Map.empty[Long, Player]) {
      case (acc, p @ PlayerRep(_, Some(id), _, _, _, _, _)) => acc + (id -> p.toPlayer)
      case (acc, _) => acc
    })
  def getPlayer(playerId: PlayerId): Future[Option[Long]] =
    db.run(player(playerId).map(_.playerId).result.headOption)
  def getPlayers(partyId: String): Future[Seq[Long]] =
    db.run(players(partyId).map(_.playerId).result)
  def insertPlayer(player: Player): Future[Int] =
    db.run(playersTable.insertOrUpdate(PlayerRep.fromPlayer(player)))

  private def player(playerId: PlayerId) =
    playersTable
      .filter(_.partyId === playerId.partyId)
      .filter(_.job === playerId.job.toString)
      .filter(_.nick === playerId.nick)
  private def players(partyId: String) =
    playersTable.filter(_.partyId === partyId)
}
