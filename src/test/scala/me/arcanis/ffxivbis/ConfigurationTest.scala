package me.arcanis.ffxivbis

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ConfigurationTest extends AnyWordSpecLike with Matchers {

  private val requiredPaths =
    Seq("akka.http.server.transparent-head-requests")

  "configuration helper" must {

    requiredPaths.foreach { path =>
      s"has $path propery" in {
        Configuration.load().hasPath(path) shouldBe true
      }
    }

  }
}
