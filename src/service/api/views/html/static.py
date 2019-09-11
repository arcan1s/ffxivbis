# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import os

from aiohttp.web import HTTPNotFound, Response, View


class StaticHtmlView(View):

    def __get_content_type(self, filename: str) -> str:
        _, ext = os.path.splitext(filename)
        print(ext)
        if ext == '.css':
            return 'text/css'
        elif ext == '.js':
            return 'text/javascript'
        return 'text/plain'

    async def get(self) -> Response:
        resource_name = self.request.match_info['resource_id']
        resource_path = os.path.join(self.request.app['templates_root'], 'static', resource_name)
        if not os.path.exists(resource_path) or os.path.isdir(resource_path):
            return HTTPNotFound()
        content_type = self.__get_content_type(resource_name)

        with open(resource_path) as resource_file:
            return Response(text=resource_file.read(), content_type=content_type)