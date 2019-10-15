package me.arcanis.ffxivbis.http.api.v1.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import me.arcanis.ffxivbis.models.Permission
import spray.json._

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  private def enumFormat[E <: Enumeration](enum: E): RootJsonFormat[E#Value] =
    new RootJsonFormat[E#Value] {
      override def write(obj: E#Value): JsValue = obj.toString.toJson
      override def read(json: JsValue): E#Value = json match {
        case JsString(name) => enum.withName(name)
        case other => deserializationError(s"String or number expected, got $other")
      }
    }

  implicit val actionFormat: RootJsonFormat[ApiAction.Value] = enumFormat(ApiAction)
  implicit val permissionFormat: RootJsonFormat[Permission.Value] = enumFormat(Permission)

  implicit val pieceFormat: RootJsonFormat[PieceResponse] = jsonFormat3(PieceResponse.apply)
  implicit val pieceActionFormat: RootJsonFormat[PieceActionResponse] = jsonFormat3(PieceActionResponse.apply)
  implicit val playerFormat: RootJsonFormat[PlayerResponse] = jsonFormat7(PlayerResponse.apply)
  implicit val playerActionFormat: RootJsonFormat[PlayerActionResponse] = jsonFormat2(PlayerActionResponse.apply)
  implicit val playerBiSLinkFormat: RootJsonFormat[PlayerBiSLinkResponse] = jsonFormat2(PlayerBiSLinkResponse.apply)
  implicit val playerIdFormat: RootJsonFormat[PlayerIdResponse] = jsonFormat3(PlayerIdResponse.apply)
  implicit val playerIdWithCountersFormat: RootJsonFormat[PlayerIdWithCountersResponse] =
    jsonFormat9(PlayerIdWithCountersResponse.apply)
  implicit val userFormat: RootJsonFormat[UserResponse] = jsonFormat4(UserResponse.apply)
}
