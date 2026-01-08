package me.arcanis.ffxivbis.service.bis

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import me.arcanis.ffxivbis.messages.BiSProviderMessage.DownloadBiS
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.{Fixtures, Settings}
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

    "get best in slot set (ariyala 2)" in {
      val probe = testKit.createTestProbe[BiS]()
      provider ! DownloadBiS(Fixtures.link5, Job.SGE, probe.ref)
      probe.expectMessage(askTimeout, Fixtures.bis3)
    }

    "get best in slot set (etro)" in {
      val probe = testKit.createTestProbe[BiS]()
      provider ! DownloadBiS(Fixtures.link3, Job.DNC, probe.ref)
      probe.expectMessage(askTimeout, Fixtures.bis)
    }

    "get best in slot set (etro 2)" ignore {
      val probe = testKit.createTestProbe[BiS]()
      provider ! DownloadBiS(Fixtures.link4, Job.DNC, probe.ref)
      probe.expectMessage(askTimeout, Fixtures.bis2)
    }

    "get best in slot set (xivgear)" in {
      val probe = testKit.createTestProbe[BiS]()
      provider ! DownloadBiS(Fixtures.link6, Job.VPR, probe.ref)
      probe.expectMessage(askTimeout, Fixtures.bis4)
    }

  }
}
