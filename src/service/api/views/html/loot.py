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

from service.models.piece import Piece
from service.models.player import Player, PlayerId

from service.api.utils import wrap_exception, wrap_invalid_param
from service.api.views.common.loot_base import LootBaseView
from service.api.views.common.player_base import PlayerBaseView


class LootHtmlView(LootBaseView, PlayerBaseView):

    @template('loot.jinja2')
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
                    'is_tome': 'yes' if piece.is_tome else 'no'  # type: ignore
                }
                for player in players
                for piece in player.loot
            ]

        except Exception as e:
            self.request.app.logger.exception('could not get loot')
            error = repr(e)

        return {
            'items': items,
            'pieces': Piece.available(),
            'players': [player.player_id.pretty_name for player in players],
            'request_error': error
        }

    async def post(self) -> Response:
        data = await self.request.post()

        required = ['action', 'piece', 'player']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            player_id = PlayerId.from_pretty_name(data.get('player'))  # type: ignore
            self.loot_post(data.get('action'), player_id,  # type: ignore
                           Piece.get({'piece': data.get('piece'), 'is_tome': data.get('is_tome', False)}))  # type: ignore

        except Exception as e:
            self.request.app.logger.exception('could not manage loot')
            return wrap_exception(e, data)

        return HTTPFound(self.request.url)
