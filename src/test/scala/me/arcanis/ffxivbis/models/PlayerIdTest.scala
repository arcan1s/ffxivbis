package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.Fixtures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerIdTest extends AnyWordSpecLike with Matchers {

  "player id model" must {

    "be parsed from string" in {
      PlayerId(Fixtures.partyId, Fixtures.playerEmpty.playerId.toString) shouldEqual Some(Fixtures.playerEmpty.playerId)
    }

  }
}
