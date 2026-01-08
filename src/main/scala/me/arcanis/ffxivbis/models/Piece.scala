/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

sealed trait Piece extends Equals {

  def pieceType: PieceType

  def job: Job

  def piece: String

  def withJob(other: Job): Piece

  def upgrade: Option[Piece.PieceUpgrade] = {
    val isTome = pieceType == PieceType.Tome
    Some(this).collect {
      case _: Piece.PieceAccessory if isTome => Piece.AccessoryUpgrade
      case _: Piece.PieceBody if isTome => Piece.BodyUpgrade
      case _: Piece.PieceWeapon if isTome => Piece.WeaponUpgrade
    }
  }

  // used for ring comparison
  def strictEqual(obj: Any): Boolean = equals(obj)
}

object Piece {

  trait PieceAccessory extends Piece
  trait PieceBody extends Piece
  trait PieceUpgrade extends Piece {
    override val pieceType: PieceType = PieceType.Tome
    override val job: Job = Job.AnyJob
    override def withJob(other: Job): Piece = this
  }
  trait PieceWeapon extends Piece

  case class Weapon(override val pieceType: PieceType, override val job: Job) extends PieceWeapon {
    override val piece: String = "weapon"
    override def withJob(other: Job): Piece = copy(job = other)
  }

  case class Head(override val pieceType: PieceType, override val job: Job) extends PieceBody {
    override val piece: String = "head"
    override def withJob(other: Job): Piece = copy(job = other)
  }
  case class Body(override val pieceType: PieceType, override val job: Job) extends PieceBody {
    override val piece: String = "body"
    override def withJob(other: Job): Piece = copy(job = other)
  }
  case class Hands(override val pieceType: PieceType, override val job: Job) extends PieceBody {
    override val piece: String = "hands"
    override def withJob(other: Job): Piece = copy(job = other)
  }
  case class Legs(override val pieceType: PieceType, override val job: Job) extends PieceBody {
    override val piece: String = "legs"
    override def withJob(other: Job): Piece = copy(job = other)
  }
  case class Feet(override val pieceType: PieceType, override val job: Job) extends PieceBody {
    override val piece: String = "feet"
    override def withJob(other: Job): Piece = copy(job = other)
  }

  case class Ears(override val pieceType: PieceType, override val job: Job) extends PieceAccessory {
    override val piece: String = "ears"
    override def withJob(other: Job): Piece = copy(job = other)
  }
  case class Neck(override val pieceType: PieceType, override val job: Job) extends PieceAccessory {
    override val piece: String = "neck"
    override def withJob(other: Job): Piece = copy(job = other)
  }
  case class Wrist(override val pieceType: PieceType, override val job: Job) extends PieceAccessory {
    override val piece: String = "wrist"
    override def withJob(other: Job): Piece = copy(job = other)
  }
  case class Ring(
    override val pieceType: PieceType,
    override val job: Job,
    override val piece: String = "ring"
  ) extends PieceAccessory {
    override def withJob(other: Job): Piece = copy(job = other)

    override def equals(obj: Any): Boolean = obj match {
      case Ring(thatPieceType, thatJob, _) => (thatPieceType == pieceType) && (thatJob == job)
      case _ => false
    }

    override def strictEqual(obj: Any): Boolean = obj match {
      case ring: Ring => equals(obj) && (ring.piece == this.piece)
      case _ => false
    }
  }

  case object AccessoryUpgrade extends PieceUpgrade {
    override val piece: String = "accessory upgrade"
  }
  case object BodyUpgrade extends PieceUpgrade {
    override val piece: String = "body upgrade"
  }
  case object WeaponUpgrade extends PieceUpgrade {
    override val piece: String = "weapon upgrade"
  }

  def apply(piece: String, pieceType: PieceType, job: Job = Job.AnyJob): Piece =
    piece.toLowerCase match {
      case "weapon" => Weapon(pieceType, job)
      case "head" => Head(pieceType, job)
      case "body" => Body(pieceType, job)
      case "hand" | "hands" => Hands(pieceType, job)
      case "legs" => Legs(pieceType, job)
      case "feet" => Feet(pieceType, job)
      case "ears" => Ears(pieceType, job)
      case "neck" => Neck(pieceType, job)
      case "wrist" | "wrists" => Wrist(pieceType, job)
      case ring @ ("ring" | "left ring" | "right ring") => Ring(pieceType, job, ring)
      case "accessory upgrade" => AccessoryUpgrade
      case "body upgrade" => BodyUpgrade
      case "weapon upgrade" => WeaponUpgrade
      case other => throw new Error(s"Unknown item type $other")
    }

  val available: Seq[String] = Seq(
    "weapon",
    "head",
    "body",
    "hands",
    "legs",
    "feet",
    "ears",
    "neck",
    "wrist",
    "left ring",
    "right ring",
    "accessory upgrade",
    "body upgrade",
    "weapon upgrade"
  )
}
