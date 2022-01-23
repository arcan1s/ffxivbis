package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.arcanis.ffxivbis.http.api.v1.json._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps

class StatusEndpointTest extends AnyWordSpecLike
  with Matchers with ScalatestRouteTest with JsonSupport {

  private val route = new StatusEndpoint().routes

  "api v1 status endpoint" must {

    "return server status" in {
      Get("/status") ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }
}
