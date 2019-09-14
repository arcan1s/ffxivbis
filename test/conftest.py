import os
import pytest
import tempfile

from typing import Any, List

from ffxivbis.api.web import setup_service
from ffxivbis.core.ariyala_parser import AriyalaParser
from ffxivbis.core.config import Configuration
from ffxivbis.core.database import Database
from ffxivbis.core.loot_selector import LootSelector
from ffxivbis.core.party import Party
from ffxivbis.core.sqlite import SQLiteDatabase
from ffxivbis.models.bis import BiS
from ffxivbis.models.job import Job
from ffxivbis.models.piece import Head, Piece, Weapon
from ffxivbis.models.player import Player


@pytest.fixture
def parser(config: Configuration) -> AriyalaParser:
    return AriyalaParser(config)


@pytest.fixture
def bis() -> BiS:
    return BiS()


@pytest.fixture
def bis2() -> BiS:
    return BiS()


@pytest.fixture
def bis_link() -> str:
    return 'https://ffxiv.ariyala.com/19V5R'


@pytest.fixture
def bis_set() -> List[Piece]:
    items: List[Piece] = []
    items.append(Piece.get({'piece': 'weapon', 'is_tome': False}))
    items.append(Piece.get({'piece': 'head', 'is_tome': False}))
    items.append(Piece.get({'piece': 'body', 'is_tome': False}))
    items.append(Piece.get({'piece': 'hands', 'is_tome': True}))
    items.append(Piece.get({'piece': 'waist', 'is_tome': True}))
    items.append(Piece.get({'piece': 'legs', 'is_tome': True}))
    items.append(Piece.get({'piece': 'feet', 'is_tome': False}))
    items.append(Piece.get({'piece': 'ears', 'is_tome': False}))
    items.append(Piece.get({'piece': 'neck', 'is_tome': True}))
    items.append(Piece.get({'piece': 'wrist', 'is_tome': False}))
    items.append(Piece.get({'piece': 'left_ring', 'is_tome': True}))
    items.append(Piece.get({'piece': 'right_ring', 'is_tome': True}))

    return items


@pytest.fixture
def config() -> Configuration:
    config = Configuration()
    config.load('/dev/null', {
        'ariyala': {
            'ariyala_url': 'https://ffxiv.ariyala.com',
            'request_timeout': 1,
            'xivapi_url': 'https://xivapi.com'
        },
        'auth': {
            'enabled': 'no'
        },
        'settings': {
            'include': '/dev/null'
        }
    })
    return config


@pytest.fixture
def database() -> SQLiteDatabase:
    db = SQLiteDatabase(
        tempfile.mktemp('-ffxivbis.db'),
        os.path.join(os.path.dirname(os.path.dirname(__file__)), 'migrations'))
    db.migration()
    return db


@pytest.fixture
def head_with_upgrade() -> Piece:
    return Head(is_tome=True)


@pytest.fixture
def party(database: Database) -> Party:
    return Party(database)


@pytest.fixture
def player(bis: BiS) -> Player:
    return Player(Job.WHM, 'A nick', bis, [])


@pytest.fixture
def player2(bis2: BiS) -> Player:
    return Player(Job.AST, 'Another nick', bis2, [], priority=0)


@pytest.fixture
async def selector(party: Party, player: Player, player2: Player,
                   head_with_upgrade: Piece, weapon: Piece) -> LootSelector:
    obj = LootSelector(party)

    await obj.party.set_player(player)
    player.bis.set_item(weapon)

    await obj.party.set_player(player2)
    player2.bis.set_item(head_with_upgrade)
    player2.bis.set_item(weapon)

    return LootSelector(party)


@pytest.fixture
def server(loop: Any, aiohttp_client: Any,
           config: Configuration, database: Database, selector: LootSelector, party: Party) -> Any:
    app = setup_service(config, database, selector, party)
    return loop.run_until_complete(aiohttp_client(app))


@pytest.fixture
def weapon() -> Piece:
    return Weapon(is_tome=False)