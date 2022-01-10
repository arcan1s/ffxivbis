/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

case class PartyDescription(partyId: String, partyAlias: Option[String]) {

  def alias: String = partyAlias.getOrElse(partyId)
}

object PartyDescription {

  def empty(partyId: String): PartyDescription = PartyDescription(partyId, None)
}
