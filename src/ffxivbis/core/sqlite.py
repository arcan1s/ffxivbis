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

from ffxivbis.models.bis import BiS
from ffxivbis.models.job import Job
from ffxivbis.models.loot import Loot
from ffxivbis.models.piece import Piece
from ffxivbis.models.player import Player, PlayerId
from ffxivbis.models.upgrade import Upgrade
from ffxivbis.models.user import User

from .database import Database
from .sqlite_helper import SQLiteHelper


class SQLiteDatabase(Database):

    def __init__(self, database_path: str, migrations_path: str) -> None:
        Database.__init__(self, migrations_path)
        self.database_path = database_path

    @property
    def connection(self) -> str:
        return f'sqlite:///{self.database_path}'

    async def delete_piece(self, party_id: str, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        player = await self.get_player(party_id, player_id)
        if player is None:
            return

        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute(
                '''delete from loot
                where loot_id in (
                    select loot_id from loot 
                    where player_id = ? and piece = ? and is_tome = ? order by created desc limit 1
                )''',
                (player, piece.name, getattr(piece, 'is_tome', True)))

    async def delete_piece_bis(self, party_id: str, player_id: PlayerId, piece: Piece) -> None:
        player = await self.get_player(party_id, player_id)
        if player is None:
            return

        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute(
                '''delete from bis where player_id = ? and piece = ?''',
                (player, piece.name))

    async def delete_player(self, party_id: str, player_id: PlayerId) -> None:
        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute('''delete from players where nick = ? and job = ? and party_id = ?''',
                                 (player_id.nick, player_id.job.name, party_id))

    async def delete_user(self, party_id: str, username: str) -> None:
        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute('''delete from users where username = ? and party_id = ?''',
                                 (username, party_id))

    async def get_party(self, party_id: str) -> List[Player]:
        players = await self.get_players(party_id)
        if not players:
            return []
        placeholder = ', '.join(['?'] * len(players))

        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute('''select * from bis where player_id in ({})'''.format(placeholder), players)
            rows = await cursor.fetchall()
            bis_pieces = [Loot(row['player_id'], Piece.get(row)) for row in rows]

            await cursor.execute('''select * from loot where player_id in ({})'''.format(placeholder), players)
            rows = await cursor.fetchall()
            loot_pieces = [Loot(row['player_id'], Piece.get(row)) for row in rows]

            await cursor.execute('''select * from players where party_id = ?''', (party_id,))
            rows = await cursor.fetchall()
            party = {
                row['player_id']: Player(Job[row['job']], row['nick'], BiS(), [], row['bis_link'], row['priority'])
                for row in rows
            }

        return self.set_loot(party, bis_pieces, loot_pieces)

    async def get_player(self, party_id: str, player_id: PlayerId) -> Optional[int]:
        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute('''select player_id from players where nick = ? and job = ? and party_id = ?''',
                                 (player_id.nick, player_id.job.name, party_id))
            player = await cursor.fetchone()
            return player['player_id'] if player is not None else None

    async def get_players(self, party_id: str) -> List[int]:
        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute('''select player_id from players where party_id = ?''', (party_id,))
            players = await cursor.fetchall()
            return [player['player_id'] for player in players]

    async def get_user(self, party_id: str, username: str) -> Optional[User]:
        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute('''select * from users where username = ? and party_id = ?''',
                                 (username, party_id))
            user = await cursor.fetchone()
            return User(user['username'], user['password'], user['permission']) if user is not None else None

    async def get_users(self, party_id: str) -> List[User]:
        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute('''select * from users where party_id = ?''', (party_id,))
            users = await cursor.fetchall()
            return [User(user['username'], user['password'], user['permission']) for user in users]

    async def insert_piece(self, party_id: str, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        player = await self.get_player(party_id, player_id)
        if player is None:
            return

        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute(
                '''insert into loot
                (created, piece, is_tome, player_id)
                values
                (?, ?, ?, ?)''',
                (Database.now(), piece.name, getattr(piece, 'is_tome', True), player)
            )

    async def insert_piece_bis(self, party_id: str, player_id: PlayerId, piece: Piece) -> None:
        player = await self.get_player(party_id, player_id)
        if player is None:
            return

        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute(
                '''replace into bis
                (created, piece, is_tome, player_id)
                values
                (?, ?, ?, ?)''',
                (Database.now(), piece.name, piece.is_tome, player)
            )

    async def insert_player(self, party_id: str, player: Player) -> None:
        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute(
                '''replace into players
                (party_id, created, nick, job, bis_link, priority)
                values
                (?, ?, ?, ?, ?, ?)''',
                (party_id, Database.now(), player.nick, player.job.name, player.link, player.priority)
            )

    async def insert_user(self, party_id: str, user: User, hashed_password: bool) -> None:
        password = user.password if hashed_password else md5_crypt.hash(user.password)
        async with SQLiteHelper(self.database_path) as cursor:
            await cursor.execute(
                '''replace into users
                (party_id, username, password, permission)
                values
                (?, ?, ?, ?)''',
                (party_id, user.username, password, user.permission)
            )