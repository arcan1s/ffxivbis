package me.arcanis.ffxivbis

import me.arcanis.ffxivbis.models._

object Fixtures {
  lazy val bis: BiS = BiS(
    Seq(
      Weapon(isTome = false ,Job.DNC),
      Head(isTome = false, Job.DNC),
      Body(isTome = false, Job.DNC),
      Hands(isTome = true, Job.DNC),
      Waist(isTome = true, Job.DNC),
      Legs(isTome = true, Job.DNC),
      Feet(isTome = false, Job.DNC),
      Ears(isTome = false, Job.DNC),
      Neck(isTome = true, Job.DNC),
      Wrist(isTome = false, Job.DNC),
      Ring(isTome = true, Job.DNC, "left ring"),
      Ring(isTome = true, Job.DNC, "right ring")
    )
  )

  lazy val link: String = "https://ffxiv.ariyala.com/19V5R"
  lazy val link2: String = "https://ffxiv.ariyala.com/1A0WM"

  lazy val lootWeapon: Piece = Weapon(isTome = true, Job.AnyJob)
  lazy val lootBody: Piece = Body(isTome = false, Job.AnyJob)
  lazy val lootHands: Piece = Hands(isTome = true, Job.AnyJob)
  lazy val lootWaist: Piece = Waist(isTome = true, Job.AnyJob)
  lazy val lootLegs: Piece = Legs(isTome = false, Job.AnyJob)
  lazy val lootEars: Piece = Ears(isTome = false, Job.AnyJob)
  lazy val lootRing: Piece = Ring(isTome = true, Job.AnyJob)
  lazy val lootLeftRing: Piece = Ring(isTome = true, Job.AnyJob, "left ring")
  lazy val lootRightRing: Piece = Ring(isTome = true, Job.AnyJob, "right ring")
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
