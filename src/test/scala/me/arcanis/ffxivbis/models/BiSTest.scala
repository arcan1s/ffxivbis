package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.Fixtures
import me.arcanis.ffxivbis.utils.Compare
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BiSTest extends AnyWordSpecLike with Matchers {

  "bis model" must {

    "build set from list" in {
      BiS(Fixtures.bis.pieces) shouldEqual Fixtures.bis
    }

    "has piece" in {
      Fixtures.bis.hasPiece(Fixtures.lootBody) shouldEqual true
      Fixtures.bis.hasPiece(Fixtures.lootHands) shouldEqual true
    }

    "has upgrade" in {
      Fixtures.bis.hasPiece(Fixtures.lootUpgrade) shouldEqual true
    }

    "does not have piece" in {
      Fixtures.bis.hasPiece(Fixtures.lootLegs) shouldEqual false
    }

    "create copy with another piece" in {
      val bis = BiS(Seq(Fixtures.lootLegs))
      val newBis = bis.withPiece(Fixtures.lootHands)

      newBis shouldEqual BiS(Seq(Fixtures.lootLegs, Fixtures.lootHands))
    }

    "create copy without piece" in {
      val bis = BiS(Seq(Fixtures.lootHands, Fixtures.lootLegs))
      val newBis = bis.withoutPiece(Fixtures.lootHands)

      newBis shouldEqual BiS(Seq(Fixtures.lootLegs))
    }

    "ignore upgrade on modification" in {
      Fixtures.bis.withPiece(Fixtures.lootUpgrade) shouldEqual Fixtures.bis
      Fixtures.bis.withoutPiece(Fixtures.lootUpgrade) shouldEqual Fixtures.bis
    }

    "return upgrade list" in {
      Compare.mapEquals(Fixtures.bis.upgrades, Map[Piece.PieceUpgrade, Int](Piece.BodyUpgrade -> 2, Piece.AccessoryUpgrade -> 3)) shouldEqual true
    }

  }
}
