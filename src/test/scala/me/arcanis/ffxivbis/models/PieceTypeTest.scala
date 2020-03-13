package me.arcanis.ffxivbis.models

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PieceTypeTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

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
