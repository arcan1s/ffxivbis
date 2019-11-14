package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.models.{Job, Permission, Piece}
import org.scalatest.{Matchers, WordSpec}

import scala.language.postfixOps

class TypesEndpointTest extends WordSpec
  with Matchers with ScalatestRouteTest with JsonSupport {

  private val route: Route = new TypesEndpoint().route

  "api v1 types endpoint" must {

    "return all available jobs" in {
      Get("/types/jobs") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[String]] shouldEqual Job.available.map(_.toString)
      }
    }

    "return all available permissions" in {
      Get("/types/permissions") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[String]] shouldEqual Permission.values.toSeq.sorted.map(_.toString)
      }
    }

    "return all available pieces" in {
      Get("/types/pieces") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[String]] shouldEqual Piece.available
      }
    }

  }
}
