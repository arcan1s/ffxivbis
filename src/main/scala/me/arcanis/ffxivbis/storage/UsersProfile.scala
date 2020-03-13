/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import me.arcanis.ffxivbis.models.{Permission, User}
import slick.lifted.{Index, PrimaryKey}

import scala.concurrent.Future

trait UsersProfile { this: DatabaseProfile =>
  import dbConfig.profile.api._

  case class UserRep(partyId: String, userId: Option[Long], username: String,
                     password: String, permission: String) {
    def toUser: User = User(partyId, username, password, Permission.withName(permission))
  }
  object UserRep {
    def fromUser(user: User, id: Option[Long]): UserRep =
      UserRep(user.partyId, id, user.username, user.password, user.permission.toString)
  }

  class Users(tag: Tag) extends Table[UserRep](tag, "users") {
    def partyId: Rep[String] = column[String]("party_id")
    def userId: Rep[Long] = column[Long]("user_id", O.AutoInc, O.PrimaryKey)
    def username: Rep[String] = column[String]("username")
    def password: Rep[String] = column[String]("password")
    def permission: Rep[String] = column[String]("permission")

    def * =
      (partyId, userId.?, username, password, permission) <> ((UserRep.apply _).tupled, UserRep.unapply)

    def pk: PrimaryKey = primaryKey("users_username_idx", (partyId, username))
    def usersUsernameIdx: Index =
      index("users_username_idx", (partyId, username), unique = true)
  }

  def deleteUser(partyId: String, username: String): Future[Int] =
    db.run(user(partyId, Some(username)).delete)
  def exists(partyId: String): Future[Boolean] =
    db.run(user(partyId, None).exists.result)
  def getUser(partyId: String, username: String): Future[Option[User]] =
    db.run(user(partyId, Some(username)).result.headOption).map(_.map(_.toUser))
  def getUsers(partyId: String): Future[Seq[User]] =
    db.run(user(partyId, None).result).map(_.map(_.toUser))
  def insertUser(userObj: User): Future[Int] =
    db.run(user(userObj.partyId, Some(userObj.username)).map(_.userId).result.headOption).flatMap {
      case Some(id) => db.run(usersTable.insertOrUpdate(UserRep.fromUser(userObj, Some(id))))
      case _ => db.run(usersTable.insertOrUpdate(UserRep.fromUser(userObj, None)))
    }

  private def user(partyId: String, username: Option[String]) =
    usersTable
      .filter(_.partyId === partyId)
      .filterIf(username.isDefined)(_.username === username.orNull)
}
