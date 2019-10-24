package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.service.Party

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
      Ring(isTome = true, Job.DNC, "leftRing"),
      Ring(isTome = true, Job.DNC, "rightRing")
    )
  )

  lazy val link: String = "https://ffxiv.ariyala.com/19V5R"

  lazy val lootBody: Piece = Body(isTome = false, Job.DNC)
  lazy val lootHands: Piece = Hands(isTome = true, Job.DNC)
  lazy val lootLegs: Piece = Legs(isTome = false, Job.DNC)
  lazy val lootUpgrade: Piece = BodyUpgrade
  lazy val loot: Seq[Piece] = Seq(lootBody, lootHands, lootLegs, lootUpgrade)

  lazy val partyId: String = Party.randomPartyId
  lazy val partyId2: String = Party.randomPartyId

  lazy val playerEmpty: Player =
    Player(partyId, Job.DNC, "Siuan Sanche", BiS(), Seq.empty, Some(link))
  lazy val playerWithBiS: Player = playerEmpty.copy(bis = bis)

  lazy val userPassword: String = "password"
  lazy val userPassword2: String = "pa55w0rd"
  lazy val userAdmin: User = User(partyId, "admin", userPassword, Permission.admin).withHashedPassword
  lazy val userGet: User = User(partyId, "get", userPassword, Permission.get).withHashedPassword
  lazy val users: Seq[User] = Seq(userAdmin, userGet)
}
