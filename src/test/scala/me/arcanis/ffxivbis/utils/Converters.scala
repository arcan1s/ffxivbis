package me.arcanis.ffxivbis.utils

import me.arcanis.ffxivbis.models.{Loot, Piece}

import java.time.Instant
import scala.language.implicitConversions

object Converters {

  implicit def pieceToLoot(piece: Piece): Loot = Loot(-1, piece, Instant.ofEpochMilli(0), isFreeLoot = false)
}
