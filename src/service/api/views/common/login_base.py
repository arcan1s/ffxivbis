#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import HTTPFound, HTTPUnauthorized, View
from aiohttp_security import check_authorized, forget, remember
from passlib.hash import md5_crypt

from service.models.user import User


class LoginBaseView(View):

    async def check_credentials(self, username: str, password: str) -> bool:
        user = await self.request.app['database'].get_user(username)
        if user is None:
            return False
        return md5_crypt.verify(password, user.password)

    async def create_user(self, username: str, password: str, permission: str) -> None:
        await self.request.app['database'].insert_user(User(username, password, permission), False)

    async def login(self, username: str, password: str) -> None:
        if await self.check_credentials(username, password):
            response = HTTPFound('/')
            await remember(self.request, response, username)
            raise response

        raise HTTPUnauthorized()

    async def logout(self) -> None:
        await check_authorized(self.request)
        response = HTTPFound('/')
        await forget(self.request, response)

        raise response

    async def remove_user(self, username: str) -> None:
        await self.request.app['database'].delete_user(username)