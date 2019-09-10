# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Response

from service.api.utils import wrap_exception, wrap_json
from service.api.views.common.login_base import LoginBaseView


class LogoutView(LoginBaseView):

    async def post(self) -> Response:
        try:
            await self.logout()
        except Exception as e:
            self.request.app.logger.exception('cannot logout user')
            return wrap_exception(e, {})

        return wrap_json({}, {})