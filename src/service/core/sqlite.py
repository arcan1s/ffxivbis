#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from passlib.hash import md5_crypt
from typing import List, Optional, Union

from service.models.bis import BiS
from service.models.job import Job
from service.models.loot import Loot
from service.models.piece import Piece
from service.models.player import Player, PlayerId
from service.models.upgrade import Upgrade
from service.models.user import User

from .database import Database
from .sqlite_helper import SQLiteHelper


class SQLiteDatabase(Database):

    def __init__(self, database_path: str, migrations_path: str) -> None:
        Database.__init__(self, migrations_path)
        self.database_path = database_path

    @property
    def connection(self) -> str:
        return 'sqlite:///{path}'.format(path=self.database_path)

    def delete_piece(self, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        player = self.get_player(player_id)
        if player is None:
            return

        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute(
                '''delete from loot
                where loot_id in (
                    select loot_id from loot 
                    where player_id = ? and piece = ? and is_tome = ? order by created desc limit 1
                )''',
                (player, piece.name, getattr(piece, 'is_tome', True)))

    def delete_piece_bis(self, player_id: PlayerId, piece: Piece) -> None:
        player = self.get_player(player_id)
        if player is None:
            return

        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute(
                '''delete from bis where player_id = ? and piece = ?''',
                (player, piece.name))

    def delete_player(self, player_id: PlayerId) -> None:
        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute('''delete from players where nick = ? and job = ?''',
                           (player_id.nick, player_id.job.name))

    def delete_user(self, username: str) -> None:
        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute('''delete from users where username = ?''', (username,))

    def get_party(self) -> List[Player]:
        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute('''select * from bis''')
            bis_pieces = [Loot(row['player_id'], Piece.get(row)) for row in cursor.fetchall()]
            cursor.execute('''select * from loot''')
            loot_pieces = [Loot(row['player_id'], Piece.get(row)) for row in cursor.fetchall()]
            cursor.execute('''select * from players''')
            party = {
                row['player_id']: Player(Job[row['job']], row['nick'], BiS(), [], row['bis_link'], row['priority'])
                for row in cursor.fetchall()
            }
        return self.set_loot(party, bis_pieces, loot_pieces)

    def get_player(self, player_id: PlayerId) -> Optional[int]:
        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute('''select player_id from players where nick = ? and job = ?''',
                           (player_id.nick, player_id.job.name))
            player = cursor.fetchone()
            return player['player_id'] if player is not None else None

    def get_user(self, username: str) -> Optional[User]:
        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute('''select * from users where username = ?''', (username,))
            user = cursor.fetchone()
            return User(user['username'], user['password'], user['permission']) if user is not None else None

    def get_users(self) -> List[User]:
        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute('''select * from users''')
            return [User(user['username'], user['password'], user['permission']) for user in cursor.fetchall()]

    def insert_piece(self, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        player = self.get_player(player_id)
        if player is None:
            return

        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute(
                '''insert into loot
                (created, piece, is_tome, player_id)
                values
                (?, ?, ?, ?)''',
                (Database.now(), piece.name, getattr(piece, 'is_tome', True), player)
            )

    def insert_piece_bis(self, player_id: PlayerId, piece: Piece) -> None:
        player = self.get_player(player_id)
        if player is None:
            return

        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute(
                '''replace into bis
                (created, piece, is_tome, player_id)
                values
                (?, ?, ?, ?)''',
                (Database.now(), piece.name, piece.is_tome, player)
            )

    def insert_player(self, player: Player) -> None:
        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute(
                '''replace into players
                (created, nick, job, bis_link, priority)
                values
                (?, ?, ?, ?, ?)''',
                (Database.now(), player.nick, player.job.name, player.link, player.priority)
            )

    def insert_user(self, user: User, hashed_password: bool) -> None:
        password = user.password if hashed_password else md5_crypt.hash(user.password)
        with SQLiteHelper(self.database_path) as cursor:
            cursor.execute(
                '''replace into users
                (username, password, permission)
                values
                (?, ?, ?)''',
                (user.username, password, user.permission)
            )