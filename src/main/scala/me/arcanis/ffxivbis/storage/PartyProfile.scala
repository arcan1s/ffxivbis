/*
 * Copyright (c) 2020 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import me.arcanis.ffxivbis.models.PartyDescription

import scala.concurrent.Future

trait PartyProfile { this: DatabaseProfile =>
  import dbConfig.profile.api._

  case class PartyRep(partyId: Option[Long], partyName: String,
                      partyAlias: Option[String]) {
    def toDescription: PartyDescription = PartyDescription(partyName, partyAlias)
  }
  object PartyRep {
    def fromDescription(party: PartyDescription, id: Option[Long]): PartyRep =
      PartyRep(id, party.partyId, party.partyAlias)
  }

  class Parties(tag: Tag) extends Table[PartyRep](tag, "parties") {
    def partyId: Rep[Long] = column[Long]("party_id", O.AutoInc, O.PrimaryKey)
    def partyName: Rep[String] = column[String]("party_name")
    def partyAlias: Rep[Option[String]] = column[Option[String]]("party_alias")

    def * =
      (partyId.?, partyName, partyAlias) <> ((PartyRep.apply _).tupled, PartyRep.unapply)
  }

  def getPartyDescription(partyId: String): Future[PartyDescription] =
    db.run(partyDescription(partyId).result.headOption.map(_.map(_.toDescription).getOrElse(PartyDescription.empty(partyId))))
  def getUniquePartyId(partyId: String): Future[Option[Long]] =
    db.run(partyDescription(partyId).map(_.partyId).result.headOption)
  def insertPartyDescription(partyDescription: PartyDescription): Future[Int] =
    getUniquePartyId(partyDescription.partyId).flatMap {
      case Some(id) => db.run(partiesTable.update(PartyRep.fromDescription(partyDescription, Some(id))))
      case _ => db.run(partiesTable.insertOrUpdate(PartyRep.fromDescription(partyDescription, None)))
    }


  private def partyDescription(partyId: String) =
    partiesTable.filter(_.partyName === partyId)
}