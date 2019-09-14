# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Response
from typing import Any, Dict, List, Optional, Type

from ffxivbis.api.utils import wrap_exception, wrap_json
from ffxivbis.api.views.common.login_base import LoginBaseView

from .openapi import OpenApi


class LogoutView(LoginBaseView, OpenApi):

    @classmethod
    def endpoint_post_description(cls: Type[OpenApi]) -> Optional[str]:
        return 'Logout'

    @classmethod
    def endpoint_post_request_body(cls: Type[OpenApi], content_type: str) -> List[str]:
        return []

    @classmethod
    def endpoint_post_responses(cls: Type[OpenApi]) -> Dict[str, Any]:
        return {
            '200': {'content': {'application/json': {'type': 'object'}}}
        }

    @classmethod
    def endpoint_post_summary(cls: Type[OpenApi]) -> Optional[str]:
        return 'logout'

    @classmethod
    def endpoint_post_tags(cls: Type[OpenApi]) -> List[str]:
        return ['users']

    async def post(self) -> Response:
        try:
            await self.logout()
        except Exception as e:
            self.request.app.logger.exception('cannot logout user')
            return wrap_exception(e, {})

        return wrap_json({})