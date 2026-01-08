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
import me.arcanis.ffxivbis.models.PartyDescription

import scala.concurrent.Future

trait PartyProfile extends DatabaseConnection {

  private val description: RowParser[PartyDescription] =
    (str("party_name") ~ str("party_alias").?)
      .map { case partyName ~ partyAlias =>
        PartyDescription(
          partyId = partyName,
          partyAlias = partyAlias,
        )
      }

  def getPartyDescription(partyId: String): Future[PartyDescription] =
    withConnection { implicit conn =>
      SQL("""select * from parties where party_name = {party_name}""")
        .on("party_name" -> partyId)
        .executeQuery()
        .as(description.singleOpt)
        .getOrElse(PartyDescription.empty(partyId))
    }

  def insertPartyDescription(partyDescription: PartyDescription): Future[Int] =
    withConnection { implicit conn =>
      SQL("""insert into parties
          |  (party_name, party_alias)
          | values
          |  ({party_name}, {party_alias})
          | on conflict (party_name) do update set
          |  party_alias = {party_alias}""".stripMargin)
        .on("party_name" -> partyDescription.partyId, "party_alias" -> partyDescription.partyAlias)
        .executeUpdate()
    }
}
