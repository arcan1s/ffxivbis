#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from dataclasses import dataclass
from typing import Union

from .piece import Piece
from .upgrade import Upgrade


@dataclass
class Loot:
    player_id: int
    piece: Union[Piece, Upgrade]