package me.arcanis.ffxivbis.models

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PieceTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  "piece model" must {

    "convert `isTome` property to string" in {
      Fixtures.lootBody.isTomeToString shouldEqual "no"
      Fixtures.lootHands.isTomeToString shouldEqual "yes"
    }

    "return upgrade" in {
      Fixtures.lootWeapon.upgrade shouldEqual Some(WeaponUpgrade)
      Fixtures.lootBody.upgrade shouldEqual None
      Fixtures.lootHands.upgrade shouldEqual Some(BodyUpgrade)
      Fixtures.lootWaist.upgrade shouldEqual Some(AccessoryUpgrade)
      Fixtures.lootLegs.upgrade shouldEqual None
      Fixtures.lootEars.upgrade shouldEqual None
      Fixtures.lootLeftRing.upgrade shouldEqual Some(AccessoryUpgrade)
      Fixtures.lootRightRing.upgrade shouldEqual Some(AccessoryUpgrade)
    }

    "build piece from string" in {
      Fixtures.bis.pieces.foreach { piece =>
        Piece(piece.piece, piece.isTome, piece.job) shouldEqual piece
      }
    }

  }
}
