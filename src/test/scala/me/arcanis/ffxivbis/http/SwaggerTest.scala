package me.arcanis.ffxivbis.http

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.arcanis.ffxivbis.Settings
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SwaggerTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest {

  private val swagger = new Swagger(Settings.withRandomDatabase)

  "swagger guard" must {

    "generate json" in {
      Get(Uri("/api-docs/swagger.json")) ~> swagger.routes ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "generate yml" in {
      Get(Uri("/api-docs/swagger.yaml")) ~> swagger.routes ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }
}
