/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema
import me.arcanis.ffxivbis.models.PartyDescription

case class PartyDescriptionModel(
  @Schema(description = "party id", required = true, example = "abcdefgh") partyId: String,
  @Schema(description = "party name") partyAlias: Option[String]
) {

  def toDescription: PartyDescription = PartyDescription(partyId, partyAlias)
}

object PartyDescriptionModel {

  def fromDescription(description: PartyDescription): PartyDescriptionModel =
    PartyDescriptionModel(description.partyId, description.partyAlias)
}
