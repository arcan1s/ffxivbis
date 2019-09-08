#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Response
from aiohttp_jinja2 import template
from typing import Any, Dict, List, Union

from service.models.piece import Piece
from service.models.player import Player, PlayerIdWithCounters

from service.api.utils import wrap_invalid_param
from service.api.views.common.loot_base import LootBaseView
from service.api.views.common.player_base import PlayerBaseView


class LootSuggestHtmlView(LootBaseView, PlayerBaseView):

    @template('loot_suggest.jinja2')
    async def get(self) -> Dict[str, Any]:
        return {
            'pieces': Piece.available(),
            'players': [player.player_id.pretty_name for player in self.player_get(None)],
        }

    @template('loot_suggest.jinja2')
    async def post(self) -> Union[Dict[str, Any], Response]:
        data = await self.request.post()
        error = None
        players: List[PlayerIdWithCounters] = []

        required = ['piece']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            piece = Piece.get({'piece': data.get('piece'), 'is_tome': data.get('is_tome', False)})
            players = self.loot_put(piece)

        except Exception as e:
            self.request.app.logger.exception('could not manage loot')
            error = repr(e)

        return {
            'pieces': Piece.available(),
            'players': [player.player_id.pretty_name for player in self.player_get(None)],
            'suggest': [
                {
                    'player': player.pretty_name,
                    'loot_count_bis': player.loot_count_bis,
                    'loot_count': player.loot_count,
                }
                for player in players
            ],
            'request_error': error
        }