package me.arcanis.ffxivbis.utils

import akka.util.Timeout
import me.arcanis.ffxivbis.Settings
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class ImplicitsTest extends AnyWordSpecLike with Matchers {

  import me.arcanis.ffxivbis.utils.Implicits._

  "configuration extension" must {

    "return finite duration" in {
      val config = Settings.config(Map("value" -> "1m"))
      config.getFiniteDuration("value") shouldBe FiniteDuration(1, TimeUnit.MINUTES)
    }

    "return optional string" in {
      val config = Settings.config(Map("value" -> "string"))
      config.getOptString("value") shouldBe Some("string")
    }

    "return None for missing optional string" in {
      val config = Settings.config(Map.empty)
      config.getOptString("value") shouldBe None
    }

    "return None for empty optional string" in {
      val config = Settings.config(Map("value" -> ""))
      config.getOptString("value") shouldBe None
    }

    "return timeout" in {
      val config = Settings.config(Map("value" -> "1m"))
      config.getTimeout("value") shouldBe Timeout(1, TimeUnit.MINUTES)
    }

  }
}
