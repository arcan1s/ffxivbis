/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema
import me.arcanis.ffxivbis.models.{Permission, User}

case class UserModel(
  @Schema(description = "unique party ID", required = true, example = "o3KicHQPW5b0JcOm5yI3") partyId: String,
  @Schema(description = "username to login to party", required = true, example = "siuan") username: String,
  @Schema(description = "password to login to party, required for user editing", example = "pa55w0rd") password: Option[
    String
  ],
  @Schema(
    description = "user permission",
    defaultValue = "get",
    `type` = "string",
    allowableValues = Array("get", "post", "admin")
  ) permission: Option[Permission.Value] = None
) extends Validator {

  require(isValidString(username), stringMatchError("Username"))
  require(password.forall(_.nonEmpty), "Password must not be empty")

  def toUser: User =
    password.fold(throw new IllegalArgumentException("Password must noot be empty"))(
      User(partyId, username, _, permission.getOrElse(Permission.get))
    )
}

object UserModel {

  def fromUser(user: User): UserModel =
    UserModel(user.partyId, user.username, None, Some(user.permission))
}
