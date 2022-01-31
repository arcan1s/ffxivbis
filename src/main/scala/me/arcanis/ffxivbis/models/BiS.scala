/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

case class BiS(pieces: Seq[Piece]) {

  def hasPiece(piece: Piece): Boolean = piece match {
    case upgrade: Piece.PieceUpgrade => upgrades.contains(upgrade)
    case _ => pieces.contains(piece)
  }

  def upgrades: Map[Piece.PieceUpgrade, Int] =
    pieces
      .groupBy(_.upgrade)
      .foldLeft(Map.empty[Piece.PieceUpgrade, Int]) {
        case (acc, (Some(k), v)) => acc + (k -> v.size)
        case (acc, _) => acc
      }
      .withDefaultValue(0)

  def withPiece(piece: Piece): BiS = copy(pieces :+ piece)

  def withoutPiece(piece: Piece): BiS = copy(pieces.filterNot(_.strictEqual(piece)))

  override def equals(obj: Any): Boolean = {
    def comparePieces(left: Seq[Piece], right: Seq[Piece]): Boolean =
      left.groupBy(identity).view.mapValues(_.size).forall { case (key, count) =>
        right.count(_.strictEqual(key)) == count
      }

    obj match {
      case left: BiS => comparePieces(left.pieces, pieces)
      case _ => false
    }
  }
}

object BiS {

  val empty: BiS = BiS(Seq.empty)
}
