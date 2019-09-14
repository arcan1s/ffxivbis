# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import json

from aiohttp.web import Response, View
from aiohttp_jinja2 import template
from typing import Any, Dict


class ApiDocVIew(View):

    async def get(self) -> Response:
        return Response(
            text=json.dumps(self.request.app['spec'].to_dict()),
            status=200,
            content_type='application/json'
        )


class ApiHtmlView(View):

    @template('api.jinja2')
    async def get(self) -> Dict[str, Any]:
        return {}