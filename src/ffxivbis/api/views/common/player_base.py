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

from ffxivbis.core.ariyala_parser import AriyalaParser
from ffxivbis.models.bis import BiS
from ffxivbis.models.job import Job
from ffxivbis.models.player import Player, PlayerId


class PlayerBaseView(View):

    async def player_add(self, job: Job, nick: str, link: Optional[str], priority: int) -> PlayerId:
        player = Player(job, nick, BiS(), [], link, int(priority))
        player_id = player.player_id
        await self.request.app['party'].set_player(player)

        if link:
            parser = AriyalaParser(self.request.app['config'])
            items = await parser.get(link, job.name)
            for piece in items:
                await self.request.app['party'].set_item_bis(player_id, piece)

        return player_id

    def player_get(self, nick: Optional[str]) -> List[Player]:
        return [
            player
            for player in self.request.app['party'].party
            if nick is None or player.nick == nick
        ]

    async def player_post(self, action: str, job: Job, nick: str, link: Optional[str], priority: int) -> Optional[PlayerId]:
        if action == 'add':
            return await self.player_add(job, nick, link, priority)
        elif action == 'remove':
            return await self.player_remove(job, nick)
        return None

    async def player_remove(self, job: Job, nick: str) -> PlayerId:
        player_id = PlayerId(job, nick)
        await self.request.app['party'].remove_player(player_id)
        return player_id