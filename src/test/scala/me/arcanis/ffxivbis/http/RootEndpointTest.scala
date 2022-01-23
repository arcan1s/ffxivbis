package me.arcanis.ffxivbis.http

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.Config
import me.arcanis.ffxivbis.Settings
import me.arcanis.ffxivbis.service.PartyService
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.database.Database
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class RootEndpointTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest {

  private val testKit = ActorTestKit(Settings.withRandomDatabase)
  override val testConfig: Config = testKit.system.settings.config

  private val storage = testKit.spawn(Database())
  private val provider = testKit.spawn(BisProvider())
  private val party = testKit.spawn(PartyService(storage))
  private val route = new RootEndpoint(testKit.system, party, provider).routes

  "root route" must {

    "return swagger ui" in {
      Get(Uri("/api-docs")) ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "return static routes" in {
      Get(Uri("/static/favicon.ico")) ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }
}
