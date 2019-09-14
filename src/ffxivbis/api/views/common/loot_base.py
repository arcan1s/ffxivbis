#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import View
from typing import List, Optional, Union

from ffxivbis.models.piece import Piece
from ffxivbis.models.player import PlayerId, PlayerIdWithCounters
from ffxivbis.models.upgrade import Upgrade


class LootBaseView(View):

    async def loot_add(self, player_id: PlayerId, piece: Piece) -> Piece:
        await self.request.app['party'].set_item(player_id, piece)
        return piece

    def loot_get(self, nick: Optional[str]) -> List[Piece]:
        party = [
            player
            for player in self.request.app['party'].party
            if nick is None or player.nick == nick
        ]
        return list(sum([player.loot for player in party], []))

    async def loot_post(self, action: str, player_id: PlayerId, piece: Piece) -> Optional[Piece]:
        if action == 'add':
            return await self.loot_add(player_id, piece)
        elif action == 'remove':
            return await self.loot_remove(player_id, piece)
        return None

    def loot_put(self, piece: Union[Piece, Upgrade]) -> List[PlayerIdWithCounters]:
        return self.request.app['loot'].suggest(piece)

    async def loot_remove(self, player_id: PlayerId, piece: Piece) -> Piece:
        await self.request.app['party'].remove_item(player_id, piece)
        return piece