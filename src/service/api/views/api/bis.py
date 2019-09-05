#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Response

from service.models.job import Job
from service.models.piece import Piece
from service.models.player import PlayerId

from service.api.utils import wrap_exception, wrap_invalid_param, wrap_json
from service.api.views.common.bis_base import BiSBaseView


class BiSView(BiSBaseView):

    async def get(self) -> Response:
        try:
            loot = self.bis_get(self.request.query.getone('nick', None))

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

        action = data.get('action')
        if action not in ('add', 'remove'):
            return wrap_invalid_param(['action'], data)

        try:
            player_id = PlayerId(Job[data['job']], data['nick'])
            piece: Piece = Piece.get(data)  # type: ignore
            self.bis_post(action, player_id, piece)

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

        try:
            player_id = PlayerId(Job[data['job']], data['nick'])
            link = self.bis_put(player_id, data['link'])

        except Exception as e:
            self.request.app.logger.exception('could not parse bis')
            return wrap_exception(e, data)

        return wrap_json({'link': link}, data)