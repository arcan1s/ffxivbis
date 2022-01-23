package me.arcanis.ffxivbis.http.view

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.arcanis.ffxivbis.Fixtures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class RootViewTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest {

  private val auth =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))

  private val route = new RootView(Fixtures.authProvider).routes

  "html view endpoint" must {

    "return root view" in {
      Get(Uri("/")) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] should not be empty
      }
    }

    "return root party view" in {
      Get(Uri(s"/party/${Fixtures.partyId}")).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] should not be empty
      }
    }

    "return bis view" in {
      Get(Uri(s"/party/${Fixtures.partyId}/bis")).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] should not be empty
      }
    }

    "return loot view" in {
      Get(Uri(s"/party/${Fixtures.partyId}/loot")).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] should not be empty
      }
    }

    "return users view" in {
      Get(Uri(s"/party/${Fixtures.partyId}/users")).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] should not be empty
      }
    }

  }
}
