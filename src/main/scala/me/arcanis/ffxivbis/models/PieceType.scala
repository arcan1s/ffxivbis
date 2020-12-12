package me.arcanis.ffxivbis.models

object PieceType {

  sealed trait PieceType

  case object Crafted extends PieceType
  case object Tome extends PieceType
  case object Savage extends PieceType
  case object Artifact extends PieceType

  lazy val available: Seq[PieceType] =
    Seq(Crafted, Tome, Savage, Artifact)

  def withName(pieceType: String): PieceType =
    available.find(_.toString.equalsIgnoreCase(pieceType)) match {
      case Some(value) => value
      case _ => throw new IllegalArgumentException(s"Invalid or unknown piece type $pieceType")
    }
}
