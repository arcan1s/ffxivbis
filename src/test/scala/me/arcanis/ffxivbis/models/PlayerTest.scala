package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.Fixtures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerTest extends AnyWordSpecLike with Matchers {

  "player model" must {

    "add best in slot set" in {
      Fixtures.playerEmpty.withBiS(Some(Fixtures.bis)).bis shouldEqual Fixtures.bis
    }

    "add loot" in {
      import me.arcanis.ffxivbis.utils.Converters._

      Fixtures.playerEmpty.withLoot(Fixtures.loot.map(pieceToLoot)).loot.map(_.piece) shouldEqual Fixtures.loot
    }

  }
}
