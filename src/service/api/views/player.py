#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Response, View
from typing import Iterable

from service.core.ariyala_parser import AriyalaParser
from service.models.bis import BiS
from service.models.job import Job
from service.models.player import Player, PlayerId

from ..utils import wrap_exception, wrap_invalid_param, wrap_json


class PlayerView(View):

    async def get(self) -> Response:
        try:
            nick = self.request.query.getone('nick', None)
            party: Iterable[Player] = [
                player
                for player in self.request.app['party'].party
                if nick is None or player.nick == nick
            ]

        except Exception as e:
            self.request.app.logger.exception('could not get loot')
            return wrap_exception(e, self.request.query)

        return wrap_json(party, self.request.query)

    async def post(self) -> Response:
        try:
            data = await self.request.json()
        except Exception:
            data = dict(await self.request.post())

        required = ['action', 'job', 'nick']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)
        priority = data.get('priority', 0)
        link = data.get('link', None)

        action = data.get('action')
        if action not in ('add', 'remove'):
            return wrap_invalid_param(['action'], data)

        try:
            if action == 'add':
                player = Player(Job[data['job']], data['nick'], BiS(), [], link, priority)
                player_id = player.player_id
                self.request.app['party'].set_player(player)

                if link is not None:
                    parser = AriyalaParser(self.request.app['config'])
                    items = parser.get(link)
                    for piece in items:
                        self.request.app['party'].set_item_bis(player_id, piece)

            elif action == 'remove':
                player_id = PlayerId(Job[data['job']], data['nick'])
                self.request.app['party'].remove_player(player_id)

        except Exception as e:
            self.request.app.logger.exception('could not add loot')
            return wrap_exception(e, data)

        return wrap_json(player_id, data)