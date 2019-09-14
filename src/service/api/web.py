#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import aiohttp_jinja2
import jinja2
import logging

from aiohttp import web
from aiohttp_security import setup as setup_security
from aiohttp_security import CookiesIdentityPolicy

from service.core.config import Configuration
from service.core.database import Database
from service.core.loot_selector import LootSelector
from service.core.party import Party

from .auth import AuthorizationPolicy, authorize_factory
from .routes import setup_routes
from .spec import get_spec


async def on_shutdown(app: web.Application) -> None:
    app.logger.warning('server terminated')


def run_server(app: web.Application) -> None:
    app.logger.info('start server')
    web.run_app(app,
                host=app['config'].get('web', 'host'),
                port=app['config'].getint('web', 'port'),
                handle_signals=False)

def setup_service(config: Configuration, database: Database, loot: LootSelector, party: Party) -> web.Application:
    app = web.Application(logger=logging.getLogger('http'))
    app.on_shutdown.append(on_shutdown)

    app.middlewares.append(web.normalize_path_middleware(append_slash=False, remove_slash=True))

    # auth related
    auth_required = config.getboolean('auth', 'enabled')
    if auth_required:
        setup_security(app, CookiesIdentityPolicy(), AuthorizationPolicy(database))
        app.middlewares.append(authorize_factory())

    # routes
    app.logger.info('setup routes')
    setup_routes(app)
    if config.has_option('web', 'templates'):
        templates_root = app['templates_root'] = config.get('web', 'templates')
        app['static_root_url'] = '/static'
        aiohttp_jinja2.setup(app, loader=jinja2.FileSystemLoader(templates_root))
    app['spec'] = get_spec(app)

    app.logger.info('setup configuration')
    app['config'] = config

    app.logger.info('setup database')
    app['database'] = database

    app.logger.info('setup loot selector')
    app['loot'] = loot

    app.logger.info('setup party worker')
    app['party'] = party

    return app
