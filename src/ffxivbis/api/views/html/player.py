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

from ffxivbis.models.job import Job
from ffxivbis.models.player import PlayerIdWithCounters

from ffxivbis.api.utils import wrap_exception, wrap_invalid_param
from ffxivbis.api.views.common.player_base import PlayerBaseView


class PlayerHtmlView(PlayerBaseView):

    @template('party.jinja2')
    async def get(self) -> Dict[str, Any]:
        counters: List[PlayerIdWithCounters] = []
        error = None

        try:
            party = self.player_get(None)
            counters = [player.player_id_with_counters(None) for player in party]

        except Exception as e:
            self.request.app.logger.exception('could not get party')
            error = repr(e)

        return {
            'jobs': [job.name for job in Job],
            'players': [
                {
                    'job': player.job.name,
                    'nick': player.nick,
                    'loot_count_bis': player.loot_count_bis,
                    'loot_count_total': player.loot_count_total,
                    'priority': player.priority
                }
                for player in counters
            ],
            'request_error': error
        }

    async def post(self) -> Response:
        data = await self.request.post()

        required = ['action', 'job', 'nick']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            action = data.getone('action')
            priority = data.getone('priority', 0)
            link = data.getone('bis', None)
            await self.player_post(action, Job[data['job'].upper()], data['nick'], link, priority)  # type: ignore

        except Exception as e:
            self.request.app.logger.exception('could not manage players')
            return wrap_exception(e, data)

        return HTTPFound(self.request.url)