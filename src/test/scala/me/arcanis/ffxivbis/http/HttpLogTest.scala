package me.arcanis.ffxivbis.http

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HttpLogTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest {

  private val log = new HttpLog {}

  "log directive" must {

    "work with empty request" in {
      Get(Uri("/")) ~> log.withHttpLog(complete(StatusCodes.OK)) ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }
}
