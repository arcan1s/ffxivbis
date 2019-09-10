#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Response

from service.api.utils import wrap_exception, wrap_invalid_param, wrap_json
from service.api.views.common.login_base import LoginBaseView


class LoginView(LoginBaseView):

    async def delete(self) -> Response:
        username = self.request.match_info['username']

        try:
            await self.remove_user(username)
        except Exception as e:
            self.request.app.logger.exception('cannot remove user')
            return wrap_exception(e, {'username': username})

        return wrap_json({}, {'username': username})

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

        return wrap_json({}, data)

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
            self.request.app.logger.exception('cannot login user')
            return wrap_exception(e, data)

        return wrap_json({}, data)