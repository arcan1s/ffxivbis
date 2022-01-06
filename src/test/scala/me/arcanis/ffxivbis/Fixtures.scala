package me.arcanis.ffxivbis

import me.arcanis.ffxivbis.models._

object Fixtures {
  lazy val bis: BiS = BiS(
    Seq(
      Weapon(pieceType = PieceType.Savage ,Job.DNC),
      Head(pieceType = PieceType.Savage, Job.DNC),
      Body(pieceType = PieceType.Savage, Job.DNC),
      Hands(pieceType = PieceType.Tome, Job.DNC),
      Legs(pieceType = PieceType.Tome, Job.DNC),
      Feet(pieceType = PieceType.Savage, Job.DNC),
      Ears(pieceType = PieceType.Savage, Job.DNC),
      Neck(pieceType = PieceType.Tome, Job.DNC),
      Wrist(pieceType = PieceType.Savage, Job.DNC),
      Ring(pieceType = PieceType.Tome, Job.DNC, "left ring"),
      Ring(pieceType = PieceType.Tome, Job.DNC, "right ring")
    )
  )
  lazy val bis2: BiS = BiS(
    Seq(
      Weapon(pieceType = PieceType.Savage ,Job.DNC),
      Head(pieceType = PieceType.Tome, Job.DNC),
      Body(pieceType = PieceType.Savage, Job.DNC),
      Hands(pieceType = PieceType.Tome, Job.DNC),
      Legs(pieceType = PieceType.Savage, Job.DNC),
      Feet(pieceType = PieceType.Tome, Job.DNC),
      Ears(pieceType = PieceType.Savage, Job.DNC),
      Neck(pieceType = PieceType.Savage, Job.DNC),
      Wrist(pieceType = PieceType.Savage, Job.DNC),
      Ring(pieceType = PieceType.Tome, Job.DNC, "left ring"),
      Ring(pieceType = PieceType.Savage, Job.DNC, "right ring")
    )
  )
  lazy val bis3: BiS = BiS(
    Seq(
      Weapon(pieceType = PieceType.Savage ,Job.SGE),
      Head(pieceType = PieceType.Tome, Job.SGE),
      Body(pieceType = PieceType.Savage, Job.SGE),
      Hands(pieceType = PieceType.Tome, Job.SGE),
      Legs(pieceType = PieceType.Tome, Job.SGE),
      Feet(pieceType = PieceType.Savage, Job.SGE),
      Ears(pieceType = PieceType.Savage, Job.SGE),
      Neck(pieceType = PieceType.Tome, Job.SGE),
      Wrist(pieceType = PieceType.Savage, Job.SGE),
      Ring(pieceType = PieceType.Savage, Job.SGE, "left ring"),
      Ring(pieceType = PieceType.Tome, Job.SGE, "right ring")
    )
  )

  lazy val link: String = "https://ffxiv.ariyala.com/19V5R"
  lazy val link2: String = "https://ffxiv.ariyala.com/1A0WM"
  lazy val link3: String = "https://etro.gg/gearset/26a67536-b4ce-4adc-a46a-f70e348bb138"
  lazy val link4: String = "https://etro.gg/gearset/865fc886-994f-4c28-8fc1-4379f160a916"
  lazy val link5: String = "https://ffxiv.ariyala.com/1FGU0"

  lazy val lootWeapon: Piece = Weapon(pieceType = PieceType.Tome, Job.AnyJob)
  lazy val lootBody: Piece = Body(pieceType = PieceType.Savage, Job.AnyJob)
  lazy val lootBodyCrafted: Piece = Body(pieceType = PieceType.Crafted, Job.AnyJob)
  lazy val lootHands: Piece = Hands(pieceType = PieceType.Tome, Job.AnyJob)
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
    Player(1, partyId, Job.DNC, "Siuan Sanche", BiS.empty, Seq.empty, Some(link))
  lazy val playerWithBiS: Player = playerEmpty.copy(bis = bis)

  lazy val userPassword: String = "password"
  lazy val userPassword2: String = "pa55w0rd"
  lazy val userAdmin: User = User(partyId, "admin", userPassword, Permission.admin).withHashedPassword
  lazy val userGet: User = User(partyId, "get", userPassword, Permission.get).withHashedPassword
  lazy val users: Seq[User] = Seq(userAdmin, userGet)
}
