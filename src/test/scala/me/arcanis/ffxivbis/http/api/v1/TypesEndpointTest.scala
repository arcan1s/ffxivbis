package me.arcanis.ffxivbis.http.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import com.typesafe.config.Config
import me.arcanis.ffxivbis.Settings
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.models.{Job, Party, Permission, Piece, PieceType}
import org.scalatest.{Matchers, WordSpec}

import scala.language.postfixOps

class TypesEndpointTest extends WordSpec
  with Matchers with ScalatestRouteTest with JsonSupport {

  private val route: Route = new TypesEndpoint(testConfig).route

  override def testConfig: Config = Settings.withRandomDatabase

  "api v1 types endpoint" must {

    "return all available jobs" in {
      Get("/types/jobs") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[String]] shouldEqual Job.availableWithAnyJob.map(_.toString)
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

    "return all available piece types" in {
      Get("/types/pieces/types") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[String]] shouldEqual PieceType.available.map(_.toString)
      }
    }

    "return current priority" in {
      Get("/types/priority") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[String]] shouldEqual Party.getRules(testConfig)
      }
    }

  }
}
