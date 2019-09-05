import logging

from service.api.web import run_server, setup_service
from service.core.config import Configuration
from service.core.database import Database
from service.core.loot_selector import LootSelector
from service.core.party import Party


class Application:

    def __init__(self, config: Configuration) -> None:
        self.config = config
        self.logger = logging.getLogger('application')

    def run(self) -> None:
        database = Database.get(self.config)
        database.migration()
        party = Party.get(database)

        priority = self.config.get('settings', 'priority').split()
        loot_selector = LootSelector(party, priority)

        web = setup_service(self.config, database, loot_selector, party)
        run_server(web)