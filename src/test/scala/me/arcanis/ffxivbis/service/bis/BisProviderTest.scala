package me.arcanis.ffxivbis.service.bis

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import me.arcanis.ffxivbis.Fixtures
import me.arcanis.ffxivbis.models._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

class BisProviderTest extends TestKit(ActorSystem("bis-provider"))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  private val timeout: FiniteDuration = 60 seconds

  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  "ariyala actor" must {

    "get best in slot set (ariyala)" in {
      val provider = system.actorOf(BisProvider.props)
      provider ! BisProvider.GetBiS(Fixtures.link, Job.DNC)
      expectMsg(timeout, Fixtures.bis)
    }

    "get best in slot set (etro)" in {
      val provider = system.actorOf(BisProvider.props)
      provider ! BisProvider.GetBiS(Fixtures.link3, Job.DNC)
      expectMsg(timeout, Fixtures.bis)
    }

  }
}
