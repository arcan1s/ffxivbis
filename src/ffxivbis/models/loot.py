#
# Copyright (c) 2019 Evgeniy Alekseev.
#
# This file is part of ffxivbis
# (see https://github.com/arcan1s/ffxivbis).
#
# License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
#
from dataclasses import dataclass
from typing import Any, Dict, List, Type, Union

from .piece import Piece
from .serializable import Serializable
from .upgrade import Upgrade


@dataclass
class Loot(Serializable):
    player_id: int
    piece: Union[Piece, Upgrade]

    @classmethod
    def model_properties(cls: Type[Serializable]) -> Dict[str, Any]:
        return {
            'piece': {
                'description': 'player piece',
                '$ref': cls.model_ref('Piece')
            },
            'player_id': {
                'description': 'player identifier',
                '$ref': cls.model_ref('PlayerId')
            }
        }

    @classmethod
    def model_required(cls: Type[Serializable]) -> List[str]:
        return ['piece', 'player_id']