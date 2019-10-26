package me.arcanis.ffxivbis.models

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PlayerTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  "player model" must {

    "add best in slot set" in {
      Fixtures.playerEmpty.withBiS(Some(Fixtures.bis)).bis shouldEqual Fixtures.bis
    }

    "add loot" in {
      Fixtures.playerEmpty.withLoot(Some(Fixtures.loot)).loot shouldEqual Fixtures.loot
    }

  }
}
