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
from service.models.piece import Piece
from service.models.player import PlayerId

from service.api.utils import wrap_exception, wrap_invalid_param, wrap_json
from service.api.views.common.bis_base import BiSBaseView

from .openapi import OpenApi


class BiSView(BiSBaseView, OpenApi):

    @classmethod
    def endpoint_get_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Get party players BiS items'

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
            '200': {'content': {'application/json': { 'schema': {
                'type': 'array',
                'items': {
                    'allOf': [{'$ref': cls.model_ref('Piece')}]
                }}}}}
        }

    @classmethod
    def endpoint_get_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'get party BiS items'

    @classmethod
    def endpoint_get_tags(cls: Type[OpenApi]) -> List[str]:
        return ['BiS']

    @classmethod
    def endpoint_post_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Add new item to player BiS or remove existing'

    @classmethod
    def endpoint_post_request_body(cls: Type[OpenApi], content_type: str) -> List[str]:
        return ['Piece', 'PlayerEdit']

    @classmethod
    def endpoint_post_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {
            '200': {'content': {'application/json': {'schema':  {'$ref': cls.model_ref('Loot')}}}}
        }

    @classmethod
    def endpoint_post_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'edit BiS'

    @classmethod
    def endpoint_post_tags(cls: Type[OpenApi]) -> List[str]:
        return ['BiS']

    @classmethod
    def endpoint_put_consumes(cls: Type[OpenApi]) -> List[str]:
        return ['application/json']

    @classmethod
    def endpoint_put_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Generate new BiS set'

    @classmethod
    def endpoint_put_request_body(cls: Type[OpenApi], content_type: str) -> List[str]:
        return ['BiSLink']

    @classmethod
    def endpoint_put_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {
            '200': {'content': {'application/json': {'schema': {'$ref': cls.model_ref('BiS')}}}}
        }

    @classmethod
    def endpoint_put_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'update BiS'

    @classmethod
    def endpoint_put_tags(cls: Type[OpenApi]) -> List[str]:
        return ['BiS']

    async def get(self) -> Response:
        try:
            loot = self.bis_get(self.request.query.getone('nick', None))

        except Exception as e:
            self.request.app.logger.exception('could not get bis')
            return wrap_exception(e, self.request.query)

        return wrap_json(loot)

    async def post(self) -> Response:
        try:
            data = await self.request.json()
        except Exception:
            data = dict(await self.request.post())

        required = ['action', 'is_tome', 'job', 'name', 'nick']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        action = data.get('action')
        if action not in ('add', 'remove'):
            return wrap_invalid_param(['action'], data)

        try:
            player_id = PlayerId(Job[data['job']], data['nick'])
            piece: Piece = Piece.get(data)  # type: ignore
            await self.bis_post(action, player_id, piece)

        except Exception as e:
            self.request.app.logger.exception('could not add bis')
            return wrap_exception(e, data)

        return wrap_json({'piece': piece, 'player_id': player_id})

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
            bis = await self.bis_put(player_id, data['link'])

        except Exception as e:
            self.request.app.logger.exception('could not parse bis')
            return wrap_exception(e, data)

        return wrap_json(bis)