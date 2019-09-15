from typing import List

from ffxivbis.core.ariyala_parser import AriyalaParser
from ffxivbis.models.piece import Piece
from ffxivbis.models.player import Player


async def test_get(parser: AriyalaParser, player: Player, bis_link: str, bis_set: List[Piece]) -> None:
    items = await parser.get(bis_link, player.job.name)

    assert items == bis_set
