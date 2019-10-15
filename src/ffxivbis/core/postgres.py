#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import asyncpg

from passlib.hash import md5_crypt
from psycopg2.extras import DictCursor
from typing import List, Optional, Union

from ffxivbis.models.bis import BiS
from ffxivbis.models.job import Job
from ffxivbis.models.loot import Loot
from ffxivbis.models.piece import Piece
from ffxivbis.models.player import Player, PlayerId
from ffxivbis.models.upgrade import Upgrade
from ffxivbis.models.user import User

from .database import Database


class PostgresDatabase(Database):

    def __init__(self, host: str, port: int, username: str, password: str, database: str, migrations_path: str) -> None:
        Database.__init__(self, migrations_path)
        self.host = host
        self.port = int(port)
        self.username = username
        self.password = password
        self.database = database
        self.pool: asyncpg.pool.Pool = None  # type: ignore

    @property
    def connection(self) -> str:
        return f'postgresql://{self.username}:{self.password}@{self.host}:{self.port}/{self.database}'

    async def init(self) -> None:
        self.pool = await asyncpg.create_pool(host=self.host, port=self.port, username=self.username,
                                              password=self.password, database=self.database)

    async def delete_piece(self, party_id: str, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        player = await self.get_player(party_id, player_id)
        if player is None:
            return

        async with self.pool.acquire() as conn:
            await conn.execute(
                '''delete from loot
                where loot_id in (
                    select loot_id from loot 
                    where player_id = $1 and piece = $2 and is_tome = $3 order by created desc limit 1
                )''',
                player, piece.name, getattr(piece, 'is_tome', True)
            )

    async def delete_piece_bis(self, party_id: str, player_id: PlayerId, piece: Piece) -> None:
        player = await self.get_player(party_id, player_id)
        if player is None:
            return

        async with self.pool.acquire() as conn:
            await conn.execute(
                '''delete from bis where player_id = $1 and piece = $2''',
                player, piece.name)

    async def delete_player(self, party_id: str, player_id: PlayerId) -> None:
        async with self.pool.acquire() as conn:
            await conn.execute('''delete from players where nick = $1 and job = $2 and party_id = $3''',
                               player_id.nick, player_id.job.name, party_id)

    async def delete_user(self, party_id: str, username: str) -> None:
        async with self.pool.acquire() as conn:
            await conn.execute('''delete from users where username = $1 and party_id = $2''',
                               (username, party_id))

    async def get_party(self, party_id: str) -> List[Player]:
        players = await self.get_players(party_id)
        if not players:
            return []

        async with self.pool.acquire() as conn:
            rows = await conn.fetch('''select * from bis where player_id in $1''', players)
            bis_pieces = [Loot(row['player_id'], Piece.get(row)) for row in rows]

            rows = await conn.fetch('''select * from loot where player_id in $1''', players)
            loot_pieces = [Loot(row['player_id'], Piece.get(row)) for row in rows]

            rows = await conn.fetch('''select * from players where party_id = $1''', party_id)
            party = {
                row['player_id']: Player(Job[row['job']], row['nick'], BiS(), [], row['bis_link'], row['priority'])
                for row in rows
            }

        return self.set_loot(party, bis_pieces, loot_pieces)

    async def get_player(self, party_id: str, player_id: PlayerId) -> Optional[int]:
        async with self.pool.acquire() as conn:
            player = await conn.fetchrow('''select player_id from players where nick = $1 and job = $2 and party_id = $3''',
                                         player_id.nick, player_id.job.name, party_id)
            return player['player_id'] if player is not None else None

    async def get_players(self, party_id: str) -> List[int]:
        async with self.pool.acquire() as conn:
            players = await conn.fetch('''select player_id from players where party_id = $1''', (party_id,))
            return [player['player_id'] for player in players]

    async def get_user(self, party_id: str, username: str) -> Optional[User]:
        async with self.pool.acquire() as conn:
            user = await conn.fetchrow('''select * from users where username = $1 and party_id = $2''',
                                       username, party_id)
            return User(user['username'], user['password'], user['permission']) if user is not None else None

    async def get_users(self, party_id: str) -> List[User]:
        async with self.pool.acquire() as conn:
            users = await conn.fetch('''select * from users where party_id = $1''', party_id)
            return [User(user['username'], user['password'], user['permission']) for user in users]

    async def insert_piece(self, party_id: str, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        player = await self.get_player(party_id, player_id)
        if player is None:
            return

        async with self.pool.acquire() as conn:
            await conn.execute(
                '''insert into loot
                (created, piece, is_tome, player_id)
                values
                ($1, $2, $3, $4)''',
                Database.now(), piece.name, getattr(piece, 'is_tome', True), player
            )

    async def insert_piece_bis(self, party_id: str, player_id: PlayerId, piece: Piece) -> None:
        player = await self.get_player(party_id, player_id)
        if player is None:
            return

        async with self.pool.acquire() as conn:
            await conn.execute(
                '''insert into bis
                (created, piece, is_tome, player_id)
                values
                ($1, $2, $3, $4)
                on conflict on constraint bis_piece_player_id_idx do update set
                created = $1, is_tome = $3''',
                Database.now(), piece.name, piece.is_tome, player
            )

    async def insert_player(self, party_id: str, player: Player) -> None:
        async with self.pool.acquire() as conn:
            await conn.execute(
                '''insert into players
                (party_id, created, nick, job, bis_link, priority)
                values
                ($1, $2, $3, $4, $5, $6)
                on conflict on constraint players_nick_job_idx do update set
                created = $1, bis_link = $4, priority = $5''',
                Database.now(), player.nick, player.job.name, player.link, player.priority, party_id
            )

    async def insert_user(self, party_id: str, user: User, hashed_password: bool) -> None:
        password = user.password if hashed_password else md5_crypt.hash(user.password)
        async with self.pool.acquire() as conn:
            await conn.execute(
                '''insert into users
                (party_id, username, password, permission)
                values
                ($1, $2, $3, $4)
                on conflict on constraint users_username_idx do update set
                password = $2, permission = $3''',
                party_id, user.username, password, user.permission
            )