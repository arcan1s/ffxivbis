#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from aiohttp.web import Application

from .views.api.bis import BiSView
from .views.api.login import LoginView
from .views.api.logout import LogoutView
from .views.api.loot import LootView
from .views.api.player import PlayerView
from .views.html.api import ApiDocVIew, ApiHtmlView
from .views.html.bis import BiSHtmlView
from .views.html.index import IndexHtmlView
from .views.html.loot import LootHtmlView
from .views.html.loot_suggest import LootSuggestHtmlView
from .views.html.player import PlayerHtmlView
from .views.html.static import StaticHtmlView
from .views.html.users import UsersHtmlView


def setup_routes(app: Application) -> None:
    # api routes
    app.router.add_delete('/admin/api/v1/login/{username}', LoginView)
    app.router.add_post('/api/v1/login', LoginView)
    app.router.add_post('/api/v1/logout', LogoutView)
    app.router.add_put('/admin/api/v1/login', LoginView)

    app.router.add_get('/api/v1/party', PlayerView)
    app.router.add_post('/api/v1/party', PlayerView)

    app.router.add_get('/api/v1/party/bis', BiSView)
    app.router.add_post('/api/v1/party/bis', BiSView)
    app.router.add_put('/api/v1/party/bis', BiSView)

    app.router.add_get('/api/v1/party/loot', LootView)
    app.router.add_post('/api/v1/party/loot', LootView)
    app.router.add_put('/api/v1/party/loot', LootView)

    # html routes
    app.router.add_get('/', IndexHtmlView)
    app.router.add_get('/static/{resource_id}', StaticHtmlView)

    app.router.add_get('/api-docs', ApiHtmlView)
    app.router.add_get('/api-docs/swagger.json', ApiDocVIew)

    app.router.add_get('/party', PlayerHtmlView)
    app.router.add_post('/party', PlayerHtmlView)

    app.router.add_get('/bis', BiSHtmlView)
    app.router.add_post('/bis', BiSHtmlView)

    app.router.add_get('/loot', LootHtmlView)
    app.router.add_post('/loot', LootHtmlView)

    app.router.add_get('/suggest', LootSuggestHtmlView)
    app.router.add_post('/suggest', LootSuggestHtmlView)

    app.router.add_get('/admin/users', UsersHtmlView)
    app.router.add_post('/admin/users', UsersHtmlView)


