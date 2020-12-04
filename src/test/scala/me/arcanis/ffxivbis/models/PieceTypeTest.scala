package me.arcanis.ffxivbis.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PieceTypeTest extends AnyWordSpecLike with Matchers {

  "piece type model" must {

    "create piece type from string" in {
      PieceType.available.foreach { pieceType =>
        PieceType.withName(pieceType.toString) shouldEqual pieceType
      }
    }

    "fail on unknown piece type" in {
      an [IllegalArgumentException] should be thrownBy PieceType.withName("random string")
    }

  }
}
