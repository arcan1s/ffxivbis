from aiohttp.web import Application

from .views.bis import BiSView
from .views.loot import LootView
from .views.player import PlayerView


def setup_routes(app: Application) -> None:
    app.router.add_get('/api/v1/party', PlayerView)
    app.router.add_post('/api/v1/party', PlayerView)

    app.router.add_get('/api/v1/party/bis', BiSView)
    app.router.add_post('/api/v1/party/bis', BiSView)
    app.router.add_put('/api/v1/party/bis', BiSView)

    app.router.add_get('/api/v1/party/loot', LootView)
    app.router.add_post('/api/v1/party/loot', LootView)
    app.router.add_put('/api/v1/party/loot', LootView)