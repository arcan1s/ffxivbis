#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import View
from aiohttp_jinja2 import template
from typing import Any, Dict


class IndexHtmlView(View):

    @template('index.jinja2')
    async def get(self) -> Dict[str, Any]:
        return {}
