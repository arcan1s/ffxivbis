#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from __future__ import annotations

from threading import Lock
from typing import Dict, List, Optional, Type, Union

from service.models.piece import Piece
from service.models.player import Player, PlayerId
from service.models.upgrade import Upgrade

from .database import Database


class Party:

    def __init__(self, database: Database) -> None:
        self.lock = Lock()
        self.players: Dict[PlayerId, Player] = {}
        self.database = database

    @property
    def party(self) -> List[Player]:
        with self.lock:
            return list(self.players.values())

    @classmethod
    async def get(cls: Type[Party], database: Database) -> Party:
        obj = Party(database)
        players = await database.get_party()
        for player in players:
            obj.players[player.player_id] = player
        return obj

    async def set_bis_link(self, player_id: PlayerId, link: str) -> None:
        with self.lock:
            player = self.players[player_id]
        player.link = link
        await self.database.insert_player(player)

    async def remove_player(self, player_id: PlayerId) -> Optional[Player]:
        await self.database.delete_player(player_id)
        with self.lock:
            player = self.players.pop(player_id, None)
        return player

    async def set_player(self, player: Player) -> PlayerId:
        player_id = player.player_id
        await self.database.insert_player(player)
        with self.lock:
            self.players[player_id] = player
        return player_id

    async def set_item(self, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        await self.database.insert_piece(player_id, piece)
        with self.lock:
            self.players[player_id].loot.append(piece)

    async def remove_item(self, player_id: PlayerId, piece: Union[Piece, Upgrade]) -> None:
        await self.database.delete_piece(player_id, piece)
        with self.lock:
            try:
                self.players[player_id].loot.remove(piece)
            except ValueError:
                pass

    async def set_item_bis(self, player_id: PlayerId, piece: Piece) -> None:
        await self.database.insert_piece_bis(player_id, piece)
        with self.lock:
            self.players[player_id].bis.set_item(piece)

    async def remove_item_bis(self, player_id: PlayerId, piece: Piece) -> None:
        await self.database.delete_piece_bis(player_id, piece)
        with self.lock:
            self.players[player_id].bis.remove_item(piece)
