package me.arcanis.ffxivbis.models

trait Piece {
  def isTome: Boolean
  def job: Job.Job
  def piece: String

  def upgrade: Option[PieceUpgrade] = this match {
    case _ if !isTome => None
    case _: Waist => Some(AccessoryUpgrade)
    case _: PieceAccessory => Some(AccessoryUpgrade)
    case _: PieceBody => Some(BodyUpgrade)
    case _: PieceWeapon => Some(WeaponUpgrade)
    case _ => None
  }
}

trait PieceAccessory extends Piece
trait PieceBody extends Piece
trait PieceUpgrade extends Piece {
  val isTome: Boolean = true
  val job: Job.Job = Job.AnyJob
}
trait PieceWeapon extends Piece

case class Weapon(override val isTome: Boolean, override val job: Job.Job) extends PieceWeapon {
  val piece: String = "weapon"
}

case class Head(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "head"
}
case class Body(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "body"
}
case class Hands(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "hands"
}
case class Waist(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "waist"
}
case class Legs(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "legs"
}
case class Feet(override val isTome: Boolean, override val job: Job.Job) extends PieceBody {
  val piece: String = "feet"
}

case class Ears(override val isTome: Boolean, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "ears"
}
case class Neck(override val isTome: Boolean, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "neck"
}
case class Wrist(override val isTome: Boolean, override val job: Job.Job) extends PieceAccessory {
  val piece: String = "wrist"
}
case class Ring(override val isTome: Boolean, override val job: Job.Job, override val piece: String = "ring")
  extends PieceAccessory {
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
      case "ring" => Ring(isTome, job)
      case "leftring" => Ring(isTome, job).copy(piece = "leftRing")
      case "rightring" => Ring(isTome, job).copy(piece = "rightRing")
      case "accessory upgrade" => AccessoryUpgrade
      case "body upgrade" => BodyUpgrade
      case "weapon upgrade" => WeaponUpgrade
      case other => throw new Error(s"Unknown item type $other")
    }
}
