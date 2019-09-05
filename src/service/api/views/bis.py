#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Response, View
from typing import Iterable, Optional

from service.core.ariyala_parser import AriyalaParser
from service.models.job import Job
from service.models.piece import Piece
from service.models.player import Player, PlayerId

from ..utils import wrap_exception, wrap_invalid_param, wrap_json


class BiSView(View):

    async def get(self) -> Response:
        try:
            nick = self.request.query.getone('nick', None)
            party: Iterable[Player] = [
                player
                for player in self.request.app['party'].party
                if nick is None or player.nick == nick
            ]
            loot = list(sum([player.bis.pieces for player in party], []))

        except Exception as e:
            self.request.app.logger.exception('could not get bis')
            return wrap_exception(e, self.request.query)

        return wrap_json(loot, self.request.query)

    async def post(self) -> Response:
        try:
            data = await self.request.json()
        except Exception:
            data = dict(await self.request.post())

        required = ['action', 'is_tome', 'job', 'nick', 'piece']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)
        player_id = PlayerId(Job[data['job']], data['nick'])

        action = data.get('action')
        if action not in ('add', 'remove'):
            return wrap_invalid_param(['action'], data)

        try:
            piece = Piece.get(data)  # type: ignore
            if action == 'add':
                self.request.app['party'].set_item_bis(player_id, piece)
            elif action == 'remove':
                self.request.app['party'].remove_item_bis(player_id, piece)

        except Exception as e:
            self.request.app.logger.exception('could not add bis')
            return wrap_exception(e, data)

        return wrap_json({'piece': piece, 'player_id': player_id}, data)

    async def put(self) -> Response:
        try:
            data = await self.request.json()
        except Exception:
            data = dict(await self.request.post())

        required = ['job', 'link', 'nick']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)
        player_id = PlayerId(Job[data['job']], data['nick'])

        try:
            parser = AriyalaParser(self.request.app['config'])
            items = parser.get(data['link'])
            for piece in items:
                self.request.app['party'].set_item_bis(player_id, piece)
            self.request.app['party'].set_bis_link(player_id, data['link'])

        except Exception as e:
            self.request.app.logger.exception('could not parse bis')
            return wrap_exception(e, data)

        return wrap_json({'link': data['link']}, data)