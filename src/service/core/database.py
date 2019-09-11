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

from service.models.loot import Loot
from service.models.piece import Piece
from service.models.player import Player, PlayerId
from service.models.upgrade import Upgrade
from service.models.user import User

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
    def get(cls: Type[Database], config: Configuration) -> Database:
        database_type = config.get('settings', 'database')
        database_settings = config.get_section(database_type)

        if database_type == 'sqlite':
            from .sqlite import SQLiteDatabase
            obj: Type[Database] = SQLiteDatabase
        else:
            raise InvalidDatabase(database_type)

        return obj(**database_settings)

    @property
    def connection(self) -> str:
        raise NotImplementedError

    async def delete_piece(self, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        raise NotImplementedError

    async def delete_piece_bis(self, player_id: PlayerId, piece: Piece) -> None:
        raise NotImplementedError

    async def delete_player(self, player_id: PlayerId) -> None:
        raise NotImplementedError

    async def delete_user(self, username: str) -> None:
        raise NotImplementedError

    async def get_party(self) -> List[Player]:
        raise NotImplementedError

    async def get_player(self, player_id: PlayerId) -> Optional[int]:
        raise NotImplementedError

    async def get_user(self, username: str) -> Optional[User]:
        raise NotImplementedError

    async def get_users(self) -> List[User]:
        raise NotImplementedError

    async def insert_piece(self, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        raise NotImplementedError

    async def insert_piece_bis(self, player_id: PlayerId, piece: Piece) -> None:
        raise NotImplementedError

    async def insert_player(self, player: Player) -> None:
        raise NotImplementedError

    async def insert_user(self, user: User, hashed_password: bool) -> None:
        raise NotImplementedError

    def migration(self) -> None:
        self.logger.info('perform migrations at {}'.format(self.connection))
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
