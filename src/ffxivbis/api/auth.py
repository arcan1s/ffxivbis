#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import middleware, Request, Response
from aiohttp_security import AbstractAuthorizationPolicy, check_permission
from typing import Callable, Optional, Tuple

from ffxivbis.core.database import Database


class AuthorizationPolicy(AbstractAuthorizationPolicy):

    def __init__(self, database: Database) -> None:
        self.database = database

    def split_identity(self, identity: str) -> Tuple[str, str]:
        # identity is party_id + username
        party_id, username = identity.split('+')
        return party_id, username

    async def authorized_userid(self, identity: str) -> Optional[str]:
        party_id, username = self.split_identity(identity)
        user = await self.database.get_user(party_id, username)
        return username if user is not None else None

    async def permits(self, identity: str, permission: str, context: str = None) -> bool:
        party_id, username = self.split_identity(identity)
        user = await self.database.get_user(party_id, username)
        if user is None:
            return False
        if user.username != identity:
            return False
        if user.permission == 'admin':
            return True
        return permission == 'get' or user.permission == permission


def authorize_factory() -> Callable:
    allowed_paths = {'/', '/favicon.ico', '/api/v1/login', '/api/v1/logout'}
    allowed_paths_groups = {'/api-docs', '/static'}

    @middleware
    async def authorize(request: Request, handler: Callable) -> Response:
        if request.path.startswith('/admin'):
            permission = 'admin'
        else:
            permission = 'get' if request.method in ('GET', 'HEAD') else 'post'
        if request.path not in allowed_paths \
                and not any(request.path.startswith(path) for path in allowed_paths_groups):
            await check_permission(request, permission)

        return await handler(request)

    return authorize

