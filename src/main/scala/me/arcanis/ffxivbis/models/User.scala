/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

import org.mindrot.jbcrypt.BCrypt

object Permission extends Enumeration {

  val get, post, admin = Value
}

case class User(partyId: String, username: String, password: String, permission: Permission.Value) {

  def hash: String = BCrypt.hashpw(password, BCrypt.gensalt)

  def verify(plain: String): Boolean = BCrypt.checkpw(plain, password)

  def verityScope(scope: Permission.Value): Boolean = permission >= scope

  def withHashedPassword: User = copy(password = hash)
}
