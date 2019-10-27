package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.Fixtures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class UserTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  "user model" must {

    "verify password" in {
      Fixtures.userAdmin.verify(Fixtures.userPassword) shouldEqual true
      Fixtures.userAdmin.verify(Fixtures.userPassword2) shouldEqual false
    }

    "verify scope" in {
      Permission.values.foreach { permission =>
        Fixtures.userAdmin.verityScope(permission) shouldEqual true
      }
      Permission.values.foreach { permission =>
        Fixtures.userGet.verityScope(permission) shouldEqual (permission == Permission.get)
      }
    }

  }
}
