#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from .config import Configuration
from .database import Database
from .loot_selector import LootSelector
from .party import Party


class PartyAggregator:

    def __init__(self, config: Configuration, database: Database) -> None:
        self.config = config
        self.database = database

    async def get_party(self, party_id: str) -> Party:
        return await Party.get(party_id, self.database)

    async def get_loot_selector(self, party: Party) -> LootSelector:
        priority = self.config.get('settings', 'priority').split()
        return LootSelector(party, priority)