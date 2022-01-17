/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import anorm.SqlParser._
import anorm._
import me.arcanis.ffxivbis.models.{Permission, User}

import scala.concurrent.Future

trait UsersProfile extends DatabaseConnection {

  private val user: RowParser[User] =
    (str("party_id") ~ str("username") ~ str("password") ~ str("permission"))
      .map { case partyId ~ username ~ password ~ permission =>
        User(
          partyId = partyId,
          username = username,
          password = password,
          permission = Permission.withName(permission),
        )
      }

  def deleteUser(partyId: String, username: String): Future[Int] =
    withConnection { implicit conn =>
      SQL("""delete from users
          | where party_id = {party_id}
          |   and username = {username}
          |   and permission <> {admin}""".stripMargin)
        .on("party_id" -> partyId, "username" -> username, "admin" -> Permission.admin.toString)
        .executeUpdate()
    }

  def exists(partyId: String): Future[Boolean] = getUsers(partyId).map(_.nonEmpty)(executionContext)

  def getUser(partyId: String, username: String): Future[Option[User]] =
    withConnection { implicit conn =>
      SQL("""select * from users where party_id = {party_id} and username = {username}""")
        .on("party_id" -> partyId, "username" -> username)
        .executeQuery()
        .as(user.singleOpt)
    }

  def getUsers(partyId: String): Future[Seq[User]] =
    withConnection { implicit conn =>
      SQL("""select * from users where party_id = {party_id}""")
        .on("party_id" -> partyId)
        .executeQuery()
        .as(user.*)
    }

  def insertUser(user: User): Future[Int] =
    withConnection { implicit conn =>
      SQL("""insert into users
          |  (party_id, username, password, permission)
          | values
          |  ({party_id}, {username}, {password}, {permission})
          | on conflict (party_id, username) do update set
          |  password = {password}, permission = {permission}""".stripMargin)
        .on(
          "party_id" -> user.partyId,
          "username" -> user.username,
          "password" -> user.password,
          "permission" -> user.permission.toString
        )
        .executeUpdate()
    }
}
