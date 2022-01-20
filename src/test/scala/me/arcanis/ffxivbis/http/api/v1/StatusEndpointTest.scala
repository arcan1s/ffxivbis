package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.Config
import me.arcanis.ffxivbis.Settings
import me.arcanis.ffxivbis.http.api.v1.json._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps

class StatusEndpointTest extends AnyWordSpecLike
  with Matchers with ScalatestRouteTest with JsonSupport {

  override val testConfig: Config = Settings.withRandomDatabase

  private val route = new StatusEndpoint().routes

  "api v1 status endpoint" must {

    "return server status" in {
      Get("/status") ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }
}
