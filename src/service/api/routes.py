#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Application

from service.api.views.api.bis import BiSView
from service.api.views.api.loot import LootView
from service.api.views.api.player import PlayerView


def setup_routes(app: Application) -> None:
    app.router.add_get('/api/v1/party', PlayerView)
    app.router.add_post('/api/v1/party', PlayerView)

    app.router.add_get('/api/v1/party/bis', BiSView)
    app.router.add_post('/api/v1/party/bis', BiSView)
    app.router.add_put('/api/v1/party/bis', BiSView)

    app.router.add_get('/api/v1/party/loot', LootView)
    app.router.add_post('/api/v1/party/loot', LootView)
    app.router.add_put('/api/v1/party/loot', LootView)