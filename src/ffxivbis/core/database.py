#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from __future__ import annotations

import datetime
import logging

from yoyo import get_backend, read_migrations
from typing import List, Mapping, Optional, Type, Union

from ffxivbis.models.loot import Loot
from ffxivbis.models.piece import Piece
from ffxivbis.models.player import Player, PlayerId
from ffxivbis.models.upgrade import Upgrade
from ffxivbis.models.user import User

from .config import Configuration
from .exceptions import InvalidDatabase


class Database:

    def __init__(self, migrations_path: str) -> None:
        self.migrations_path = migrations_path
        self.logger = logging.getLogger('database')

    @staticmethod
    def now() -> int:
        return int(datetime.datetime.now().timestamp())

    @classmethod
    async def get(cls: Type[Database], config: Configuration) -> Database:
        database_type = config.get('settings', 'database')
        database_settings = config.get_section(database_type)

        if database_type == 'sqlite':
            from .sqlite import SQLiteDatabase
            obj: Type[Database] = SQLiteDatabase
        elif database_type == 'postgres':
            from .postgres import PostgresDatabase
            obj = PostgresDatabase
        else:
            raise InvalidDatabase(database_type)

        database = obj(**database_settings)
        await database.init()
        return database

    @property
    def connection(self) -> str:
        raise NotImplementedError

    async def init(self) -> None:
        pass

    async def delete_piece(self, party_id: str, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        raise NotImplementedError

    async def delete_piece_bis(self, party_id: str, player_id: PlayerId, piece: Piece) -> None:
        raise NotImplementedError

    async def delete_player(self, party_id: str, player_id: PlayerId) -> None:
        raise NotImplementedError

    async def delete_user(self, party_id: str, username: str) -> None:
        raise NotImplementedError

    async def get_party(self, party_id: str) -> List[Player]:
        raise NotImplementedError

    async def get_player(self, party_id: str, player_id: PlayerId) -> Optional[int]:
        raise NotImplementedError

    async def get_players(self, party_id: str) -> List[int]:
        raise NotImplementedError

    async def get_user(self, party_id: str, username: str) -> Optional[User]:
        raise NotImplementedError

    async def get_users(self, party_id: str) -> List[User]:
        raise NotImplementedError

    async def insert_piece(self, party_id: str, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        raise NotImplementedError

    async def insert_piece_bis(self, party_id: str, player_id: PlayerId, piece: Piece) -> None:
        raise NotImplementedError

    async def insert_player(self, party_id: str, player: Player) -> None:
        raise NotImplementedError

    async def insert_user(self, party_id: str, user: User, hashed_password: bool) -> None:
        raise NotImplementedError

    def migration(self) -> None:
        self.logger.info('perform migrations')
        backend = get_backend(self.connection)
        migrations = read_migrations(self.migrations_path)
        with backend.lock():
            backend.apply_migrations(backend.to_apply(migrations))

    def set_loot(self, party: Mapping[int, Player], bis: List[Loot], loot: List[Loot]) -> List[Player]:
        for piece in bis:
            party[piece.player_id].bis.set_item(piece.piece)
        for piece in loot:
            party[piece.player_id].loot.append(piece.piece)
        return list(party.values())
