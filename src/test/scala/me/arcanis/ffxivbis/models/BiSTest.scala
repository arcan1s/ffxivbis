package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.utils.Compare
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class BiSTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

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

      newBis.legs shouldEqual Some(Fixtures.lootLegs)
      newBis.hands shouldEqual Some(Fixtures.lootHands)
      newBis.pieces.length shouldEqual 2
    }

    "create copy without piece" in {
      val bis = BiS(Seq(Fixtures.lootHands, Fixtures.lootLegs))
      val newBis = bis.withoutPiece(Fixtures.lootHands)

      newBis.legs shouldEqual Some(Fixtures.lootLegs)
      newBis.hands shouldEqual None
      newBis.pieces.length shouldEqual 1
    }

    "ignore upgrade on modification" in {
      Fixtures.bis.withPiece(Fixtures.lootUpgrade) shouldEqual Fixtures.bis
      Fixtures.bis.withoutPiece(Fixtures.lootUpgrade) shouldEqual Fixtures.bis
    }

    "return upgrade list" in {
      Compare.mapEquals(Fixtures.bis.upgrades, Map[PieceUpgrade, Int](BodyUpgrade -> 2, AccessoryUpgrade -> 4)) shouldEqual true
    }

  }
}
