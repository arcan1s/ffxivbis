package me.arcanis.ffxivbis.utils

import java.time.Instant

import me.arcanis.ffxivbis.models.{Loot, Piece}

import scala.language.implicitConversions

object Converters {
  implicit def pieceToLoot(piece: Piece): Loot = Loot(-1, piece, Instant.ofEpochMilli(0), isFreeLoot = false)
}
