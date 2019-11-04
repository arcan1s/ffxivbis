/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

sealed trait Piece {
  def isTome: Boolean
  def job: Job.Job
  def piece: String

  def withJob(other: Job.Job): Piece

  def isTomeToString: String = if (isTome) "yes" else "no"
  def upgrade: Option[PieceUpgrade] = this match {
    case _ if !isTome => None
    case _: Waist => Some(AccessoryUpgrade)
    case _: PieceAccessory => Some(AccessoryUpgrade)
    case _: PieceBody => Some(BodyUpgrade)
    case _: PieceWeapon => Some(WeaponUpgrade)
  }
}

trait PieceAccessory extends Piece
trait PieceBody extends Piece
trait PieceUpgrade extends Piece {
  val isTome: Boolean = true
  val job: Job.Job = Job.AnyJob
  def withJob(other: Job.Job): Piece = this
}
trait PieceWeapon extends Piece

case class Weapon(override val isTome: Boolean, override val job: Job.Job) extends PieceWeapon {
  val piece: String = "weapon"
  def withJob(other: Job.Job): Piece = copy(job = other)
}

case class Head(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "head"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Body(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "body"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Hands(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "hands"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Waist(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "waist"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Legs(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "legs"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Feet(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "feet"
  def withJob(other: Job.Job): Piece = copy(job = other)
}

case class Ears(override val isTome: Boolean, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "ears"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Neck(override val isTome: Boolean, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "neck"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Wrist(override val isTome: Boolean, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "wrist"
  def withJob(other: Job.Job): Piece = copy(job = other)
}
case class Ring(override val isTome: Boolean, override val job: Job.Job, override val piece: String = "ring")
  extends PieceAccessory {
  def withJob(other: Job.Job): Piece = copy(job = other)
  override def equals(obj: Any): Boolean = obj match {
    case Ring(thatIsTome, thatJob, _) => (thatIsTome == isTome) && (thatJob == job)
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
  def apply(piece: String, isTome: Boolean, job: Job.Job = Job.AnyJob): Piece =
    piece.toLowerCase match {
      case "weapon" => Weapon(isTome, job)
      case "head" => Head(isTome, job)
      case "body" => Body(isTome, job)
      case "hands" => Hands(isTome, job)
      case "waist" => Waist(isTome, job)
      case "legs" => Legs(isTome, job)
      case "feet" => Feet(isTome, job)
      case "ears" => Ears(isTome, job)
      case "neck" => Neck(isTome, job)
      case "wrist" => Wrist(isTome, job)
      case ring @ ("ring" | "left ring" | "right ring") => Ring(isTome, job, ring)
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
