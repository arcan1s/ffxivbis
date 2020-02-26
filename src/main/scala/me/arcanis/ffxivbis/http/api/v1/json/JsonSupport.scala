/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import me.arcanis.ffxivbis.models.Permission
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  private def enumFormat[E <: Enumeration](enum: E): RootJsonFormat[E#Value] =
    new RootJsonFormat[E#Value] {
      override def write(obj: E#Value): JsValue = obj.toString.toJson
      override def read(json: JsValue): E#Value = json match {
        case JsNumber(value) => enum(value.toInt)
        case JsString(name) => enum.withName(name)
        case other => deserializationError(s"String or number expected, got $other")
      }
    }

  implicit val instantFormat: RootJsonFormat[Instant] = new RootJsonFormat[Instant] {
    override def write(obj: Instant): JsValue = obj.toString.toJson
    override def read(json: JsValue): Instant = json match {
      case JsNumber(value) => Instant.ofEpochMilli(value.toLongExact)
      case JsString(value) => Instant.parse(value)
      case other => deserializationError(s"String or number expected, got $other")
    }
  }

  implicit val actionFormat: RootJsonFormat[ApiAction.Value] = enumFormat(ApiAction)
  implicit val permissionFormat: RootJsonFormat[Permission.Value] = enumFormat(Permission)

  implicit val errorFormat: RootJsonFormat[ErrorResponse] = jsonFormat1(ErrorResponse.apply)
  implicit val partyIdFormat: RootJsonFormat[PartyIdResponse] = jsonFormat1(PartyIdResponse.apply)
  implicit val pieceFormat: RootJsonFormat[PieceResponse] = jsonFormat3(PieceResponse.apply)
  implicit val lootFormat: RootJsonFormat[LootResponse] = jsonFormat2(LootResponse.apply)
  implicit val playerFormat: RootJsonFormat[PlayerResponse] = jsonFormat7(PlayerResponse.apply)
  implicit val playerActionFormat: RootJsonFormat[PlayerActionResponse] = jsonFormat2(PlayerActionResponse.apply)
  implicit val playerIdFormat: RootJsonFormat[PlayerIdResponse] = jsonFormat3(PlayerIdResponse.apply)
  implicit val pieceActionFormat: RootJsonFormat[PieceActionResponse] = jsonFormat3(PieceActionResponse.apply)
  implicit val playerBiSLinkFormat: RootJsonFormat[PlayerBiSLinkResponse] = jsonFormat2(PlayerBiSLinkResponse.apply)
  implicit val playerIdWithCountersFormat: RootJsonFormat[PlayerIdWithCountersResponse] =
    jsonFormat9(PlayerIdWithCountersResponse.apply)
  implicit val userFormat: RootJsonFormat[UserResponse] = jsonFormat4(UserResponse.apply)
}
