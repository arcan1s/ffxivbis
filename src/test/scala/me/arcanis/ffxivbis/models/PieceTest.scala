package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.Fixtures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PieceTest extends AnyWordSpecLike with Matchers {

  "piece model" must {

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
        Piece(piece.piece, piece.pieceType, piece.job) shouldEqual piece
      }
    }

  }
}
