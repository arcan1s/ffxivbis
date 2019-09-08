#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import HTTPFound, Response
from aiohttp_jinja2 import template
from typing import Any, Dict, List

from service.models.job import Job
from service.models.piece import Piece
from service.models.player import PlayerId

from service.api.utils import wrap_exception, wrap_invalid_param, wrap_json
from service.api.views.common.bis_base import BiSBaseView
from service.api.views.common.player_base import PlayerBaseView


class BiSHtmlView(BiSBaseView, PlayerBaseView):

    @template('bis.jinja2')
    async def get(self) -> Dict[str, Any]:
        items: List[Dict[str, str]] = []
        error = None

        try:
            players = self.player_get(None)
            items = [
                {
                    'player': player.player_id.pretty_name,
                    'piece': piece.name,
                    'is_tome': 'yes' if piece.is_tome else 'no'
                }
                for player in players
                for piece in player.bis.pieces
            ]

        except Exception as e:
            self.request.app.logger.exception('could not get bis')
            error = repr(e)

        return {
            'pieces': Piece.available(),
            'players': items,
            'request_error': error
        }

    async def post(self) -> Response:
        data = await self.request.post()

        required = ['method', 'player']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            method = data.get('method')
            player_id = PlayerId.from_pretty_name(data.get('player'))

            if method == 'post':
                required = ['action', 'piece']
                if any(param not in data for param in required):
                    return wrap_invalid_param(required, data)
                self.bis_post(data.get('action'), player_id,
                              Piece.get({'piece': data.get('piece'), 'is_tome': data.get('is_tome', False)}))

            elif method == 'put':
                required = ['bis']
                if any(param not in data for param in required):
                    return wrap_invalid_param(required, data)

                self.bis_put(player_id, data.get('bis'))

        except Exception as e:
            self.request.app.logger.exception('could not manage bis')
            return wrap_exception(e, data)

        return HTTPFound(self.request.url)