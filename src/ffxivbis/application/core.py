#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import asyncio
import logging

from ffxivbis.api.web import run_server, setup_service
from ffxivbis.core.config import Configuration
from ffxivbis.core.database import Database
from ffxivbis.core.loot_selector import LootSelector
from ffxivbis.core.party import Party
from ffxivbis.models.user import User


class Application:

    def __init__(self, config: Configuration) -> None:
        self.config = config
        self.logger = logging.getLogger('application')

    def run(self) -> None:
        loop = asyncio.get_event_loop()

        database = loop.run_until_complete(Database.get(self.config))
        database.migration()

        party = loop.run_until_complete(Party.get(database))

        admin = User(self.config.get('auth', 'root_username'), self.config.get('auth', 'root_password'), 'admin')
        loop.run_until_complete(database.insert_user(admin, True))

        priority = self.config.get('settings', 'priority').split()
        loot_selector = LootSelector(party, priority)

        web = setup_service(self.config, database, loot_selector, party)
        run_server(web)