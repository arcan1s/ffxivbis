from aiohttp.web import Response, View
from typing import Iterable

from service.models.job import Job
from service.models.piece import Piece
from service.models.player import Player, PlayerId

from ..utils import wrap_exception, wrap_invalid_param, wrap_json


class LootView(View):

    async def get(self) -> Response:
        try:
            nick = self.request.query.getone('nick', None)
            party: Iterable[Player] = [
                player
                for player in self.request.app['party'].party
                if nick is None or player.nick == nick
            ]
            loot = list(sum([player.loot for player in party], []))

        except Exception as e:
            self.request.app.logger.exception('could not get loot')
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

        action = data.get('action')
        if action not in ('add', 'remove'):
            return wrap_invalid_param(['action'], data)

        try:
            player_id = PlayerId(Job[data['job']], data['nick'])
            piece = Piece.get(data)
            if action == 'add':
                self.request.app['party'].set_item(player_id, piece)

            elif action == 'remove':
                self.request.app['party'].remove_item(player_id, piece)

        except Exception as e:
            self.request.app.logger.exception('could not add loot')
            return wrap_exception(e, data)

        return wrap_json({'piece': piece, 'player_id': player_id}, data)

    async def put(self) -> Response:
        try:
            data = await self.request.json()
        except Exception:
            data = dict(await self.request.post())

        required = ['is_tome', 'piece']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            piece = Piece.get(data)
            players = self.request.app['loot'].suggest(piece)

        except Exception as e:
            self.request.app.logger.exception('could not suggest loot')
            return wrap_exception(e, data)

        return wrap_json(players, data)