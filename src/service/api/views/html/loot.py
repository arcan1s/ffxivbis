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
from service.models.upgrade import Upgrade

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
                    'is_tome': 'yes' if getattr(piece, 'is_tome', True) else 'no'
                }
                for player in players
                for piece in player.loot
            ]

        except Exception as e:
            self.request.app.logger.exception('could not get loot')
            error = repr(e)

        return {
            'items': items,
            'pieces': Piece.available() + [upgrade.name for upgrade in Upgrade],
            'players': [player.player_id.pretty_name for player in players],
            'request_error': error
        }

    async def post(self) -> Response:
        data = await self.request.post()

        required = ['action', 'piece', 'player']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            player_id = PlayerId.from_pretty_name(data.getone('player'))  # type: ignore
            is_tome = (data.getone('is_tome', None) == 'on')
            self.loot_post(data.getone('action'), player_id,  # type: ignore
                           Piece.get({'piece': data.getone('piece'), 'is_tome': is_tome}))  # type: ignore

        except Exception as e:
            self.request.app.logger.exception('could not manage loot')
            return wrap_exception(e, data)

        return HTTPFound(self.request.url)
