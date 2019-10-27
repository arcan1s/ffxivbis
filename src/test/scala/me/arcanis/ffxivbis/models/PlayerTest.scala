package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.Fixtures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PlayerTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  "player model" must {

    "add best in slot set" in {
      Fixtures.playerEmpty.withBiS(Some(Fixtures.bis)).bis shouldEqual Fixtures.bis
    }

    "add loot" in {
      Fixtures.playerEmpty.withLoot(Fixtures.loot).loot shouldEqual Fixtures.loot
    }

  }
}
