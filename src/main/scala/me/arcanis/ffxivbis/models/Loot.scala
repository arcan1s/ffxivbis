/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

import java.time.Instant

case class Loot(playerId: Long, piece: Piece, timestamp: Instant, isFreeLoot: Boolean) {

  def isFreeLootToString: String = if (isFreeLoot) "yes" else "no"
}
