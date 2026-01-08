/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema

case class PlayerBiSLinkModel(
  @Schema(
    description = "link to player best in slot",
    required = true,
    example = "https://ffxiv.ariyala.com/19V5R"
  ) link: String,
  @Schema(description = "player description", required = true) playerId: PlayerIdModel
) extends Validator {

  require(isValidString(link), stringMatchError("BiS link"))
}
