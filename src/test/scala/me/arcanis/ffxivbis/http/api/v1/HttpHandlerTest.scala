package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Allow
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.arcanis.ffxivbis.http.api.v1.json._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HttpHandlerTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest with JsonSupport with HttpHandler {

  "http handler" must {

    "convert IllegalArgumentException into 400 response" in {
      Get("/400") ~> withExceptionHandler("400", failWith(new IllegalArgumentException(""))) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ErrorModel] shouldEqual ErrorModel("")
      }
    }

    "convert IllegalArgumentException into 400 response with details" in {
      Get("/400") ~> withExceptionHandler("400", failWith(new IllegalArgumentException("message"))) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ErrorModel] shouldEqual ErrorModel("message")
      }
    }

    "convert exception message to error response" in {
      Get("/500") ~> withExceptionHandler("500", failWith(new ArithmeticException)) ~> check {
        status shouldEqual StatusCodes.InternalServerError
        responseAs[ErrorModel] shouldEqual ErrorModel("unknown server error")
      }
    }

    "process OPTIONS request" in {
      Options("/200") ~> withRejectionHandler() ~> check {
        status shouldEqual StatusCodes.OK
        headers.collectFirst { case header: Allow => header } should not be empty
        responseAs[String] shouldBe empty
      }
    }

    "reject unknown request" in {
      Post("/200") ~> withRejectionHandler() ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        headers.collectFirst { case header: Allow => header } should not be empty
        responseAs[ErrorModel].message should not be empty
      }
    }

    "handle 404 response" in {
      Get("/404") ~> withRejectionHandler() ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[ErrorModel] shouldEqual ErrorModel("The requested resource could not be found.")
      }
    }

  }

  private def single(uri: String, completeWith: Route) =
    path(uri)(get(completeWith))

  private def withExceptionHandler(uri: String = "200", completeWith: Route = complete(StatusCodes.OK)) =
    Route.seal {
      handleExceptions(exceptionHandler) {
        single(uri, completeWith)
      }
    }

  private def withRejectionHandler(uri: String = "200", completeWith: Route = complete(StatusCodes.OK)) =
    Route.seal {
      handleRejections(rejectionHandler) {
        single(uri, completeWith)
      }
    }
}
