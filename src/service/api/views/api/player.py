#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Response
from typing import Any, Dict, List, Optional, Type

from service.models.job import Job

from service.api.utils import wrap_exception, wrap_invalid_param, wrap_json
from service.api.views.common.player_base import PlayerBaseView

from .openapi import OpenApi


class PlayerView(PlayerBaseView, OpenApi):

    @classmethod
    def endpoint_get_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Get party players with optional nick filter'

    @classmethod
    def endpoint_get_parameters(cls: Type[OpenApi]) -> List[Dict[str, Any]]:
        return [
            {
                'name': 'nick',
                'in': 'query',
                'description': 'player nick name to filter',
                'required': False,
                'type': 'string'
            }
        ]

    @classmethod
    def endpoint_get_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {
            '200': {'content': {'application/json': {'schema': {'$ref': cls.model_ref('Player')}}}}
        }

    @classmethod
    def endpoint_get_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'get party players'

    @classmethod
    def endpoint_get_tags(cls: Type[OpenApi]) -> List[str]:
        return ['party']

    @classmethod
    def endpoint_post_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Create new party player or remove existing'

    @classmethod
    def endpoint_post_request_body(cls: Type[OpenApi], content_type: str) -> List[str]:
        return ['PlayerEdit']

    @classmethod
    def endpoint_post_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {
            '200': {'content': {'application/json': {'schema': {'$ref': cls.model_ref('PlayerId')}}}}
        }

    @classmethod
    def endpoint_post_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'add or remove player'

    @classmethod
    def endpoint_post_tags(cls: Type[OpenApi]) -> List[str]:
        return ['party']

    async def get(self) -> Response:
        try:
            party = self.player_get(self.request.query.getone('nick', None))

        except Exception as e:
            self.request.app.logger.exception('could not get party')
            return wrap_exception(e, self.request.query)

        return wrap_json(party)

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
            player_id = await self.player_post(action, Job[data['job']], data['nick'], link, priority)

        except Exception as e:
            self.request.app.logger.exception('could not add loot')
            return wrap_exception(e, data)

        return wrap_json(player_id)