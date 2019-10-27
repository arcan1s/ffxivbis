package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.Fixtures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PlayerIdTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  "player id model" must {

    "be parsed from string" in {
      PlayerId(Fixtures.partyId, Fixtures.playerEmpty.playerId.toString) shouldEqual Some(Fixtures.playerEmpty.playerId)
    }

  }
}
