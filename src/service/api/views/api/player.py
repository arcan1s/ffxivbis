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

from service.api.utils import wrap_exception, wrap_invalid_param, wrap_json
from service.api.views.common.player_base import PlayerBaseView


class PlayerView(PlayerBaseView):

    async def get(self) -> Response:
        try:
            party = self.player_get(self.request.query.getone('nick', None))

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
            player_id = self.player_post(action, Job[data['job']], data['nick'], link, priority)

        except Exception as e:
            self.request.app.logger.exception('could not add loot')
            return wrap_exception(e, data)

        return wrap_json(player_id, data)