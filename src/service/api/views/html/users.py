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

from service.models.user import User

from service.api.utils import wrap_exception, wrap_invalid_param
from service.api.views.common.login_base import LoginBaseView


class UsersHtmlView(LoginBaseView):

    @template('users.jinja2')
    async def get(self) -> Dict[str, Any]:
        error = None
        users: List[User] = []

        try:
            users = await self.request.app['database'].get_users()
        except Exception as e:
            self.request.app.logger.exception('could not get users')
            error = repr(e)

        return {
            'request_error': error,
            'users': users
        }

    async def post(self) -> Response:
        data = await self.request.post()

        required = ['action', 'username']
        if any(param not in data for param in required):
            return wrap_invalid_param(required, data)

        try:
            action = data.getone('action')
            username = str(data.getone('username'))

            if action == 'add':
                required = ['password', 'permission']
                if any(param not in data for param in required):
                    return wrap_invalid_param(required, data)
                await self.create_user(username, data.getone('password'), data.getone('permission'))  # type: ignore
            elif action == 'remove':
                await self.remove_user(username)
            else:
                return wrap_invalid_param(['action'], data)

        except Exception as e:
            self.request.app.logger.exception('could not manage users')
            return wrap_exception(e, data)

        return HTTPFound(self.request.url)