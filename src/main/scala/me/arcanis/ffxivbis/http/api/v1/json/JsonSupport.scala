/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import me.arcanis.ffxivbis.models.Permission
import spray.json._

import java.time.Instant

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

  implicit val errorFormat: RootJsonFormat[ErrorModel] = jsonFormat1(ErrorModel.apply)
  implicit val partyIdFormat: RootJsonFormat[PartyIdModel] = jsonFormat1(PartyIdModel.apply)
  implicit val pieceFormat: RootJsonFormat[PieceModel] = jsonFormat3(PieceModel.apply)
  implicit val lootFormat: RootJsonFormat[LootModel] = jsonFormat3(LootModel.apply)
  implicit val partyDescriptionFormat: RootJsonFormat[PartyDescriptionModel] = jsonFormat2(
    PartyDescriptionModel.apply
  )
  implicit val playerFormat: RootJsonFormat[PlayerModel] = jsonFormat9(PlayerModel.apply)
  implicit val playerActionFormat: RootJsonFormat[PlayerActionModel] = jsonFormat2(PlayerActionModel.apply)
  implicit val playerIdFormat: RootJsonFormat[PlayerIdModel] = jsonFormat3(PlayerIdModel.apply)
  implicit val pieceActionFormat: RootJsonFormat[PieceActionModel] = jsonFormat4(PieceActionModel.apply)
  implicit val playerBiSLinkFormat: RootJsonFormat[PlayerBiSLinkModel] = jsonFormat2(PlayerBiSLinkModel.apply)
  implicit val playerIdWithCountersFormat: RootJsonFormat[PlayerIdWithCountersModel] =
    jsonFormat9(PlayerIdWithCountersModel.apply)
  implicit val statusFormat: RootJsonFormat[StatusModel] = jsonFormat1(StatusModel.apply)
  implicit val userFormat: RootJsonFormat[UserModel] = jsonFormat4(UserModel.apply)
}
