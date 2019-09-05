#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
import itertools

from dataclasses import dataclass
from typing import Dict, List, Optional, Union

from .piece import Piece
from .upgrade import Upgrade


@dataclass
class BiS:
    weapon: Optional[Piece] = None
    head: Optional[Piece] = None
    body: Optional[Piece] = None
    hands: Optional[Piece] = None
    waist: Optional[Piece] = None
    legs: Optional[Piece] = None
    feet: Optional[Piece] = None
    ears: Optional[Piece] = None
    neck: Optional[Piece] = None
    wrist: Optional[Piece] = None
    left_ring: Optional[Piece] = None
    right_ring: Optional[Piece] = None

    def has_piece(self, piece: Union[Piece, Upgrade]) -> bool:
        if isinstance(piece, Piece):
            return piece in self.pieces
        elif isinstance(piece, Upgrade):
            return self.upgrades_required.get(piece) is not None
        return False

    @property
    def pieces(self) -> List[Piece]:
        return [piece for piece in self.__dict__.values() if isinstance(piece, Piece)]

    @property
    def upgrades_required(self) -> Dict[Upgrade, int]:
        return {
            upgrade: len(list(pieces))
            for upgrade, pieces in itertools.groupby(self.pieces, lambda piece: piece.upgrade)
        }

    def set_item(self, piece: Union[Piece, Upgrade]) -> None:
        setattr(self, piece.name, piece)

    def remove_item(self, piece: Union[Piece, Upgrade]) -> None:
        setattr(self, piece.name, None)