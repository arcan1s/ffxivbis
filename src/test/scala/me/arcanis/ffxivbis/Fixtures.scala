package me.arcanis.ffxivbis

import me.arcanis.ffxivbis.models._

object Fixtures {
  lazy val bis: BiS = BiS(
    Seq(
      Weapon(pieceType = PieceType.Savage ,Job.DNC),
      Head(pieceType = PieceType.Savage, Job.DNC),
      Body(pieceType = PieceType.Savage, Job.DNC),
      Hands(pieceType = PieceType.Tome, Job.DNC),
      Waist(pieceType = PieceType.Tome, Job.DNC),
      Legs(pieceType = PieceType.Tome, Job.DNC),
      Feet(pieceType = PieceType.Savage, Job.DNC),
      Ears(pieceType = PieceType.Savage, Job.DNC),
      Neck(pieceType = PieceType.Tome, Job.DNC),
      Wrist(pieceType = PieceType.Savage, Job.DNC),
      Ring(pieceType = PieceType.Tome, Job.DNC, "left ring"),
      Ring(pieceType = PieceType.Tome, Job.DNC, "right ring")
    )
  )

  lazy val link: String = "https://ffxiv.ariyala.com/19V5R"
  lazy val link2: String = "https://ffxiv.ariyala.com/1A0WM"

  lazy val lootWeapon: Piece = Weapon(pieceType = PieceType.Tome, Job.AnyJob)
  lazy val lootBody: Piece = Body(pieceType = PieceType.Savage, Job.AnyJob)
  lazy val lootHands: Piece = Hands(pieceType = PieceType.Tome, Job.AnyJob)
  lazy val lootWaist: Piece = Waist(pieceType = PieceType.Tome, Job.AnyJob)
  lazy val lootLegs: Piece = Legs(pieceType = PieceType.Savage, Job.AnyJob)
  lazy val lootEars: Piece = Ears(pieceType = PieceType.Savage, Job.AnyJob)
  lazy val lootRing: Piece = Ring(pieceType = PieceType.Tome, Job.AnyJob)
  lazy val lootLeftRing: Piece = Ring(pieceType = PieceType.Tome, Job.AnyJob, "left ring")
  lazy val lootRightRing: Piece = Ring(pieceType = PieceType.Tome, Job.AnyJob, "right ring")
  lazy val lootUpgrade: Piece = BodyUpgrade
  lazy val loot: Seq[Piece] = Seq(lootBody, lootHands, lootLegs, lootUpgrade)

  lazy val partyId: String = Party.randomPartyId
  lazy val partyId2: String = Party.randomPartyId

  lazy val playerEmpty: Player =
    Player(1, partyId, Job.DNC, "Siuan Sanche", BiS(), Seq.empty, Some(link))
  lazy val playerWithBiS: Player = playerEmpty.copy(bis = bis)

  lazy val userPassword: String = "password"
  lazy val userPassword2: String = "pa55w0rd"
  lazy val userAdmin: User = User(partyId, "admin", userPassword, Permission.admin).withHashedPassword
  lazy val userGet: User = User(partyId, "get", userPassword, Permission.get).withHashedPassword
  lazy val users: Seq[User] = Seq(userAdmin, userGet)
}
