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

from ffxivbis.models.piece import Piece
from ffxivbis.models.player import Player, PlayerId

from ffxivbis.api.utils import wrap_exception, wrap_invalid_param
from ffxivbis.api.views.common.bis_base import BiSBaseView
from ffxivbis.api.views.common.player_base import PlayerBaseView


class BiSHtmlView(BiSBaseView, PlayerBaseView):

    @template('bis.jinja2')
    async def get(self) -> Dict[str, Any]:
        error = None
        items: List[Dict[str, str]] = []
        players: List[Player] = []

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
            'items': items,
            'pieces': Piece.available(),
            'players': [player.player_id.pretty_name for player in players],
            'request_error': error
        }

    async def post(self) -> Response:
        data = await self.request.post()

        required = ['method', 'player']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            method = data.getone('method')
            player_id = PlayerId.from_pretty_name(data.getone('player'))  # type: ignore

            if method == 'post':
                required = ['action', 'piece']
                if any(param not in data for param in required):
                    return wrap_invalid_param(required, data)
                is_tome = (data.getone('is_tome', None) == 'on')
                await self.bis_post(data.getone('action'), player_id,  # type: ignore
                                    Piece.get({'piece': data.getone('piece'), 'is_tome': is_tome}))  # type: ignore

            elif method == 'put':
                required = ['bis']
                if any(param not in data for param in required):
                    return wrap_invalid_param(required, data)

                await self.bis_put(player_id, data.getone('bis'))  # type: ignore

        except Exception as e:
            self.request.app.logger.exception('could not manage bis')
            return wrap_exception(e, data)

        return HTTPFound(self.request.url)