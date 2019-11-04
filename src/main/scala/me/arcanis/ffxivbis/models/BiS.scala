/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

case class BiS(weapon: Option[Piece],
               head: Option[Piece],
               body: Option[Piece],
               hands: Option[Piece],
               waist: Option[Piece],
               legs: Option[Piece],
               feet: Option[Piece],
               ears: Option[Piece],
               neck: Option[Piece],
               wrist: Option[Piece],
               leftRing: Option[Piece],
               rightRing: Option[Piece]) {

  val pieces: Seq[Piece] =
    Seq(weapon, head, body, hands, waist, legs, feet, ears, neck, wrist, leftRing, rightRing).flatten

  def hasPiece(piece: Piece): Boolean = piece match {
    case upgrade: PieceUpgrade => upgrades.contains(upgrade)
    case _ => pieces.contains(piece)
  }

  def upgrades: Map[PieceUpgrade, Int] =
    pieces.groupBy(_.upgrade).foldLeft(Map.empty[PieceUpgrade, Int]) {
      case (acc, (Some(k), v)) => acc + (k -> v.length)
      case (acc, _) => acc
    } withDefaultValue 0

  def withPiece(piece: Piece): BiS = copyWithPiece(piece.piece, Some(piece))
  def withoutPiece(piece: Piece): BiS = copyWithPiece(piece.piece, None)

  private def copyWithPiece(name: String, piece: Option[Piece]): BiS = {
    val params = Map(
      "weapon" -> weapon,
      "head" -> head,
      "body" -> body,
      "hands" -> hands,
      "waist" -> waist,
      "legs" -> legs,
      "feet" -> feet,
      "ears" -> ears,
      "neck" -> neck,
      "wrist" -> wrist,
      "left ring" -> leftRing,
      "right ring" -> rightRing
    ) + (name -> piece)
    BiS(params)
  }
}

object BiS {
  def apply(data: Map[String, Option[Piece]]): BiS =
    BiS(
      data.get("weapon").flatten,
      data.get("head").flatten,
      data.get("body").flatten,
      data.get("hands").flatten,
      data.get("waist").flatten,
      data.get("legs").flatten,
      data.get("feet").flatten,
      data.get("ears").flatten,
      data.get("neck").flatten,
      data.get("wrist").flatten,
      data.get("left ring").flatten,
      data.get("right ring").flatten)

  def apply(): BiS = BiS(Seq.empty)

  def apply(pieces: Seq[Piece]): BiS =
    BiS(pieces.map(piece => piece.piece -> Some(piece)).toMap)
}
