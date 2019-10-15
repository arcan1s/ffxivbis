package me.arcanis.ffxivbis.models

import org.mindrot.jbcrypt.BCrypt

object Permission extends Enumeration {
  val get, post, admin = Value
}

case class User(partyId: String,
                username: String,
                password: String,
                permission: Permission.Value) {

  def hash: String = BCrypt.hashpw(password, BCrypt.gensalt)
  def verify(plain: String): Boolean = BCrypt.checkpw(plain, password)
  def verityScope(scope: Permission.Value): Boolean = permission >= scope
}
