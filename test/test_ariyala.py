from typing import List

from service.core.ariyala_parser import AriyalaParser
from service.models.piece import Piece


def test_get(parser: AriyalaParser, bis_link: str, bis_set: List[Piece]) -> None:
    items = parser.get(bis_link)

    assert items == bis_set
