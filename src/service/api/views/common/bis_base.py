#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import View
from typing import List, Optional

from service.core.ariyala_parser import AriyalaParser
from service.models.bis import BiS
from service.models.piece import Piece
from service.models.player import PlayerId


class BiSBaseView(View):

    async def bis_add(self, player_id: PlayerId, piece: Piece) -> Piece:
        await self.request.app['party'].set_item_bis(player_id, piece)
        return piece

    def bis_get(self, nick: Optional[str]) -> List[Piece]:
        party = [
            player
            for player in self.request.app['party'].party
            if nick is None or player.nick == nick
        ]
        return list(sum([player.bis.pieces for player in party], []))

    async def bis_post(self, action: str, player_id: PlayerId, piece: Piece) -> Optional[Piece]:
        if action == 'add':
            return await self.bis_add(player_id, piece)
        elif action == 'remove':
            return await self.bis_remove(player_id, piece)
        return None

    async def bis_put(self, player_id: PlayerId, link: str) -> BiS:
        parser = AriyalaParser(self.request.app['config'])
        items = parser.get(link, player_id.job.name)
        for piece in items:
            await self.request.app['party'].set_item_bis(player_id, piece)
        await self.request.app['party'].set_bis_link(player_id, link)
        return self.request.app['party'].players[player_id].bis

    async def bis_remove(self, player_id: PlayerId, piece: Piece) -> Piece:
        await self.request.app['party'].remove_item_bis(player_id, piece)
        return piece
