from dataclasses import dataclass
from typing import Union

from .piece import Piece
from .upgrade import Upgrade


@dataclass
class Loot:
    player_id: int
    piece: Union[Piece, Upgrade]