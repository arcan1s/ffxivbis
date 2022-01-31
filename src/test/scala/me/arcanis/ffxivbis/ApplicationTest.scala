package me.arcanis.ffxivbis

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class ApplicationTest extends ScalaTestWithActorTestKit(Settings.withRandomDatabase)
  with AnyWordSpecLike {

  "application" must {

    "load" in {
      testKit.spawn[Nothing](Application())
    }

  }
}
