package me.arcanis.ffxivbis.http

import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.arcanis.ffxivbis.Fixtures
import me.arcanis.ffxivbis.http.view.RootView
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AuthorizationTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest {

  private val auth =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))

  "authorization directive" must {

    "accept credentials" in {
      val route = new RootView(Fixtures.authProvider).routes

      Get(Uri(s"/party/${Fixtures.partyId}")).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "reject credentials" in {
      val route = new RootView(Fixtures.rejectingAuthProvider).routes

      Get(Uri(s"/party/${Fixtures.partyId}")).withHeaders(auth) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
      }
    }

    "reject with empty credentials" in {
      val route = new RootView(Fixtures.authProvider).routes

      Get(Uri(s"/party/${Fixtures.partyId}")) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
      }
    }

  }
}
