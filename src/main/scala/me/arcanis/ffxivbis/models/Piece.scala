/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

sealed trait Piece extends Equals {

  def pieceType: PieceType.PieceType
  def job: Job.Job
  def piece: String

  def withJob(other: Job.Job): Piece

  def upgrade: Option[PieceUpgrade] = this match {
    case _ if pieceType != PieceType.Tome => None
    case _: Waist => Some(AccessoryUpgrade)
    case _: PieceAccessory => Some(AccessoryUpgrade)
    case _: PieceBody => Some(BodyUpgrade)
    case _: PieceWeapon => Some(WeaponUpgrade)
  }

  // used for ring comparison
  def strictEqual(obj: Any): Boolean = equals(obj)
}

trait PieceAccessory extends Piece
trait PieceBody extends Piece
trait PieceUpgrade extends Piece {
  val pieceType: PieceType.PieceType = PieceType.Tome
  val job: Job.Job = Job.AnyJob
  def withJob(other: Job.Job): Piece = this
}
trait PieceWeapon extends Piece

case class Weapon(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceWeapon {
  val piece: String = "weapon"
  def withJob(other: Job.Job): Piece = copy(job = other)
}

case class Head(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceBody {
  val piece: String = "head"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Body(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceBody {
  val piece: String = "body"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Hands(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceBody {
  val piece: String = "hands"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Waist(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceBody {
  val piece: String = "waist"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Legs(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceBody {
  val piece: String = "legs"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Feet(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceBody {
  val piece: String = "feet"
  def withJob(other: Job.Job): Piece = copy(job = other)
}

case class Ears(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "ears"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Neck(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "neck"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Wrist(override val pieceType: PieceType.PieceType, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "wrist"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Ring(override val pieceType: PieceType.PieceType, override val job: Job.Job, override val piece: String = "ring")
  extends PieceAccessory {
  def withJob(other: Job.Job): Piece = copy(job = other)

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
  val piece: String = "accessory upgrade"
}
case object BodyUpgrade extends PieceUpgrade {
  val piece: String = "body upgrade"
}
case object WeaponUpgrade extends PieceUpgrade {
  val piece: String = "weapon upgrade"
}

object Piece {
  def apply(piece: String, pieceType: PieceType.PieceType, job: Job.Job = Job.AnyJob): Piece =
    piece.toLowerCase match {
      case "weapon" => Weapon(pieceType, job)
      case "head" => Head(pieceType, job)
      case "body" => Body(pieceType, job)
      case "hands" => Hands(pieceType, job)
      case "waist" => Waist(pieceType, job)
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

  lazy val available: Seq[String] = Seq("weapon",
    "head", "body", "hands", "waist", "legs", "feet",
    "ears", "neck", "wrist", "left ring", "right ring",
    "accessory upgrade", "body upgrade", "weapon upgrade")
}
