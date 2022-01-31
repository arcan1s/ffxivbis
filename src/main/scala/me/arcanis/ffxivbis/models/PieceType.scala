/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

sealed trait PieceType

object PieceType {

  case object Savage extends PieceType
  case object Tome extends PieceType
  case object Crafted extends PieceType
  case object Artifact extends PieceType

  val available: Seq[PieceType] = Seq(Savage, Tome, Crafted, Artifact)

  def withName(pieceType: String): PieceType =
    available.find(_.toString.equalsIgnoreCase(pieceType)) match {
      case Some(value) => value
      case _ => throw new IllegalArgumentException(s"Invalid or unknown piece type $pieceType")
    }
}
