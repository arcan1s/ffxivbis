package me.arcanis.ffxivbis.service.bis

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import me.arcanis.ffxivbis.messages.DownloadBiS
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.models._
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps

class BisProviderTest extends ScalaTestWithActorTestKit(Settings.withRandomDatabase)
  with AnyWordSpecLike {

  private val provider = testKit.spawn(BisProvider())
  private val askTimeout = 60 seconds

  "ariyala actor" must {

    "get best in slot set (ariyala)" in {
      val probe = testKit.createTestProbe[BiS]()
      provider ! DownloadBiS(Fixtures.link, Job.DNC, probe.ref)
      probe.expectMessage(askTimeout, Fixtures.bis)
    }

    "get best in slot set (etro)" in {
      val probe = testKit.createTestProbe[BiS]()
      provider ! DownloadBiS(Fixtures.link3, Job.DNC, probe.ref)
      probe.expectMessage(askTimeout, Fixtures.bis)
    }

    "get best in slot set (etro 2)" in {
      val probe = testKit.createTestProbe[BiS]()
      provider ! DownloadBiS(Fixtures.link4, Job.DNC, probe.ref)
      probe.expectMessage(askTimeout, Fixtures.bis2)
    }

  }
}
