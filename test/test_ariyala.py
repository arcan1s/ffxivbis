from typing import List

from service.core.ariyala_parser import AriyalaParser
from service.models.piece import Piece
from service.models.player import Player


def test_get(parser: AriyalaParser, player: Player, bis_link: str, bis_set: List[Piece]) -> None:
    items = parser.get(bis_link, player.job.name)

    assert items == bis_set
