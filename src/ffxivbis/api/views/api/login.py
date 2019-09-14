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

from ffxivbis.api.utils import wrap_exception, wrap_invalid_param, wrap_json
from ffxivbis.api.views.common.login_base import LoginBaseView

from .openapi import OpenApi


class LoginView(LoginBaseView, OpenApi):

    @classmethod
    def endpoint_delete_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Delete registered user'

    @classmethod
    def endpoint_delete_parameters(cls: Type[OpenApi]) -> List[Dict[str, Any]]:
        return [
            {
                'name': 'username',
                'in': 'path',
                'description': 'username to remove',
                'required': True,
                'type': 'string'
            }
        ]

    @classmethod
    def endpoint_delete_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {
            '200': {'content': {'application/json': {'type': 'object'}}}
        }

    @classmethod
    def endpoint_delete_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'delete user'

    @classmethod
    def endpoint_delete_tags(cls: Type[OpenApi]) -> List[str]:
        return ['users']

    @classmethod
    def endpoint_post_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Login as user'

    @classmethod
    def endpoint_post_request_body(cls: Type[OpenApi], content_type: str) -> List[str]:
        return ['User']

    @classmethod
    def endpoint_post_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {
            '200': {'content': {'application/json': {'type': 'object'}}}
        }

    @classmethod
    def endpoint_post_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'login'

    @classmethod
    def endpoint_post_tags(cls: Type[OpenApi]) -> List[str]:
        return ['users']

    @classmethod
    def endpoint_put_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Create new user'

    @classmethod
    def endpoint_put_request_body(cls: Type[OpenApi], content_type: str) -> List[str]:
        return ['User']

    @classmethod
    def endpoint_put_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {
            '200': {'content': {'application/json': {'type': 'object'}}}
        }

    @classmethod
    def endpoint_put_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'create user'

    @classmethod
    def endpoint_put_tags(cls: Type[OpenApi]) -> List[str]:
        return ['users']

    async def delete(self) -> Response:
        username = self.request.match_info['username']

        try:
            await self.remove_user(username)
        except Exception as e:
            self.request.app.logger.exception('cannot remove user')
            return wrap_exception(e, {'username': username})

        return wrap_json({})

    async def post(self) -> Response:
        try:
            data = await self.request.json()
        except Exception:
            data = dict(await self.request.post())

        required = ['username', 'password']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            await self.login(data['username'], data['password'])
        except Exception as e:
            self.request.app.logger.exception('cannot login user')
            return wrap_exception(e, data)

        return wrap_json({})

    async def put(self) -> Response:
        try:
            data = await self.request.json()
        except Exception:
            data = dict(await self.request.post())

        required = ['username', 'password']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            await self.create_user(data['username'], data['password'], data.get('permission', 'get'))
        except Exception as e:
            self.request.app.logger.exception('cannot create user')
            return wrap_exception(e, data)

        return wrap_json({})